package com.example.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Local UI-only demonstration toggle for the Water Pump control card.
     * Does not initiate hardware, network, or ESP8266 commands.
     */
    fun toggleWaterPump() {
        _uiState.update { current ->
            current.copy(isPumpOn = !current.isPumpOn)
        }
    }
}
