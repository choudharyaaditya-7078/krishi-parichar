package com.example.ui.home

import androidx.compose.runtime.Immutable
import com.example.data.mqtt.MqttConnectionState
import com.example.data.mqtt.PumpCommand
import com.example.data.mqtt.PumpState

enum class NavDestination {
    HOME,
    MONITORING,
    REPORTS,
    PROFILE
}

@Immutable
data class HomeUiState(
    val pumpState: PumpState = PumpState.UNKNOWN,
    val connectionState: MqttConnectionState = MqttConnectionState.DISCONNECTED,
    val pendingCommand: PumpCommand? = null,
    val lastConfirmedStatus: PumpState? = null,
    val lastStatusTimestamp: Long? = null,
    val isLastStatusRetained: Boolean = false,
    val lastError: String? = null,
    val temperatureDisplay: String = "28°C",
    val weatherCondition: String = "Partly Cloudy",
    val soilMoisturePercent: Int = 72,
    val soilNutrientsDisplay: String = "NPK: 8-6-10",
    val nutrientsLastReport: String = "Last Report: Today",
    val activeNav: NavDestination = NavDestination.HOME
) {
    val isCommandPending: Boolean
        get() = pendingCommand != null

    val soilMoistureProgress: Float
        get() = (soilMoisturePercent.coerceIn(0, 100)) / 100f
}
