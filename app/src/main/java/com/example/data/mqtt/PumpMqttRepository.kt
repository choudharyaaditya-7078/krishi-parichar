package com.example.data.mqtt

import android.content.Context
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean repository abstraction separating MQTT transport details from ViewModel & Compose UI.
 */
interface PumpControlRepository {
    val connectionState: StateFlow<MqttConnectionState>
    val connectionError: StateFlow<String?>
    val statusEvents: SharedFlow<PumpStatusEvent>

    fun connect()
    suspend fun sendCommand(command: PumpCommand): Result<Unit>
    fun close()
}

class PumpMqttRepository(
    private val mqttClient: PumpMqttClient
) : PumpControlRepository {

    override val connectionState: StateFlow<MqttConnectionState> = mqttClient.connectionState
    override val connectionError: StateFlow<String?> = mqttClient.connectionError
    override val statusEvents: SharedFlow<PumpStatusEvent> = mqttClient.statusEvents

    override fun connect() {
        mqttClient.connect()
    }

    override suspend fun sendCommand(command: PumpCommand): Result<Unit> {
        return mqttClient.publishCommand(command)
    }

    override fun close() {
        mqttClient.release()
    }

    companion object {
        fun create(context: Context): PumpMqttRepository {
            val appContext = context.applicationContext
            val config = PumpMqttConfig.fromContext(appContext)
            val client = PumpMqttClient(appContext = appContext, config = config)
            return PumpMqttRepository(client)
        }
    }
}
