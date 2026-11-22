package com.example.data.mqtt

import java.util.Locale

/**
 * Hardware-confirmed water pump state reported by the ESP8266 controller.
 */
enum class PumpState {
    UNKNOWN,
    ON,
    OFF
}

/**
 * Explicit pump commands published from the Android app to the ESP8266.
 */
enum class PumpCommand(val payload: String) {
    ON("ON"),
    OFF("OFF");

    fun expectedPumpState(): PumpState = when (this) {
        ON -> PumpState.ON
        OFF -> PumpState.OFF
    }
}

/**
 * Connection status between the Android application and the EMQX Cloud MQTT broker.
 * Note: This reflects broker connectivity, NOT whether the ESP8266 hardware is online.
 */
enum class MqttConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Parsed status response arriving on krishi-parichar/pump/1/status.
 */
data class PumpStatusEvent(
    val state: PumpState,
    val isRetained: Boolean,
    val receivedAtMillis: Long
)

/**
 * Exact EMQX / ESP8266 protocol constants and payload parser.
 */
object PumpProtocol {
    const val COMMAND_TOPIC = "krishi-parichar/pump/1/set"
    const val STATUS_TOPIC = "krishi-parichar/pump/1/status"

    const val COMMAND_QOS = 0
    const val STATUS_QOS = 0
    const val COMMAND_RETAINED = false

    const val PAYLOAD_STATUS_ON = "p_1"
    const val PAYLOAD_STATUS_OFF = "p_0"

    /**
     * Parses only recognized ESP8266 status payloads ("p_1" or "p_0") after trimming
     * and case-insensitive normalization. Unknown or malformed payloads return null.
     */
    fun parseStatusPayload(rawPayload: String?): PumpState? {
        if (rawPayload == null) return null
        return when (rawPayload.trim().lowercase(Locale.ROOT)) {
            PAYLOAD_STATUS_ON -> PumpState.ON
            PAYLOAD_STATUS_OFF -> PumpState.OFF
            else -> null
        }
    }
}
