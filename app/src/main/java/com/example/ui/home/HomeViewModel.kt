package com.example.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.mqtt.MqttConnectionState
import com.example.data.mqtt.PumpCommand
import com.example.data.mqtt.PumpControlRepository
import com.example.data.mqtt.PumpMqttConfig
import com.example.data.mqtt.PumpMqttRepository
import com.example.data.mqtt.PumpState
import com.example.data.mqtt.PumpStatusEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PumpControlRepository,
    private val commandTimeoutMs: Long = PumpMqttConfig.COMMAND_ACK_TIMEOUT_MS
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var commandTimeoutJob: Job? = null

    init {
        observeConnectionState()
        observeConnectionErrors()
        observePumpStatusResponses()
        repository.connect()
    }

    /**
     * Publishes an explicit ON/OFF MQTT command to krishi-parichar/pump/1/set.
     * Never changes confirmed pumpState optimistically — confirmed state updates ONLY
     * when a valid status payload (p_1 / p_0) arrives from the ESP8266.
     */
    fun onPumpActionRequested() {
        val current = _uiState.value

        // Prevent duplicate/conflicting commands while awaiting ESP8266 confirmation.
        if (current.isCommandPending) {
            return
        }

        // Do not publish or alter pump state when EMQX cloud connection is unavailable.
        if (current.connectionState != MqttConnectionState.CONNECTED) {
            val brokerError = repository.connectionError.value
                ?: "Cloud Offline. Unable to send pump command."
            _uiState.update { it.copy(lastError = brokerError) }
            if (current.connectionState == MqttConnectionState.DISCONNECTED ||
                current.connectionState == MqttConnectionState.ERROR
            ) {
                repository.connect()
            }
            return
        }

        // When state is UNKNOWN, explicitly request ON to synchronize output.
        val commandToSend = when (current.pumpState) {
            PumpState.ON -> PumpCommand.OFF
            PumpState.OFF, PumpState.UNKNOWN -> PumpCommand.ON
        }

        _uiState.update {
            it.copy(
                pendingCommand = commandToSend,
                lastError = null
            )
        }

        viewModelScope.launch {
            val publishResult = repository.sendCommand(commandToSend)
            if (publishResult.isFailure) {
                cancelPendingTimeout()
                _uiState.update {
                    it.copy(
                        pendingCommand = null,
                        lastError = "Failed to send command. Check cloud connection and retry."
                    )
                }
                return@launch
            }

            // Publish succeeded to broker, but ESP8266 has NOT confirmed yet.
            // Keep confirmed pumpState unchanged and start acknowledgement timeout.
            startCommandTimeout(commandToSend)
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            repository.connectionState.collect { newConnectionState ->
                _uiState.update { current ->
                    val lostWhilePending = current.isCommandPending &&
                        newConnectionState != MqttConnectionState.CONNECTED
                    if (lostWhilePending) {
                        cancelPendingTimeout()
                    }
                    current.copy(
                        connectionState = newConnectionState,
                        pendingCommand = if (lostWhilePending) null else current.pendingCommand,
                        lastError = when {
                            lostWhilePending -> "Cloud connection lost while waiting for pump response."
                            newConnectionState == MqttConnectionState.CONNECTED &&
                                repository.connectionError.value == null &&
                                !current.isCommandPending -> null
                            else -> current.lastError
                        }
                    )
                }
            }
        }
    }

    private fun observeConnectionErrors() {
        viewModelScope.launch {
            repository.connectionError.collect { errorMessage ->
                if (!errorMessage.isNullOrBlank()) {
                    _uiState.update { current ->
                        current.copy(lastError = errorMessage)
                    }
                }
            }
        }
    }

    private fun observePumpStatusResponses() {
        viewModelScope.launch {
            repository.statusEvents.collect { event ->
                handleIncomingStatusEvent(event)
            }
        }
    }

    private fun handleIncomingStatusEvent(event: PumpStatusEvent) {
        if (event.state == PumpState.UNKNOWN) return

        _uiState.update { current ->
            if (event.isRetained) {
                // Retained broker message represents last reported state, NOT live confirmation
                // of an active command. Never clear pendingCommand on a retained message.
                if (current.isCommandPending) {
                    current
                } else {
                    current.copy(
                        pumpState = event.state,
                        lastConfirmedStatus = event.state,
                        lastStatusTimestamp = event.receivedAtMillis,
                        isLastStatusRetained = true
                    )
                }
            } else {
                // Fresh non-retained status message from ESP8266.
                val pending = current.pendingCommand
                if (pending != null) {
                    cancelPendingTimeout()
                }

                val mismatchError = if (pending != null && event.state != pending.expectedPumpState()) {
                    "Controller reported ${event.state.name} instead of ${pending.name}."
                } else {
                    null
                }

                current.copy(
                    pumpState = event.state,
                    lastConfirmedStatus = event.state,
                    lastStatusTimestamp = event.receivedAtMillis,
                    isLastStatusRetained = false,
                    pendingCommand = null,
                    lastError = mismatchError
                )
            }
        }
    }

    private fun startCommandTimeout(expectedCommand: PumpCommand) {
        cancelPendingTimeout()
        commandTimeoutJob = viewModelScope.launch {
            delay(commandTimeoutMs)
            _uiState.update { current ->
                if (current.pendingCommand == expectedCommand) {
                    current.copy(
                        pendingCommand = null,
                        lastError = NO_RESPONSE_ERROR
                    )
                } else {
                    current
                }
            }
        }
    }

    private fun cancelPendingTimeout() {
        commandTimeoutJob?.cancel()
        commandTimeoutJob = null
    }

    override fun onCleared() {
        cancelPendingTimeout()
        repository.close()
        super.onCleared()
    }

    companion object {
        const val NO_RESPONSE_ERROR = "No response from pump controller"

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    return HomeViewModel(
                        repository = PumpMqttRepository.create(appContext)
                    ) as T
                }
            }
        }
    }
}
