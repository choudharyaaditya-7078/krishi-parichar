package com.example.ui.home

import androidx.compose.runtime.Immutable

enum class NavDestination {
    HOME,
    MONITORING,
    REPORTS,
    PROFILE
}

@Immutable
data class HomeUiState(
    val isPumpOn: Boolean = true,
    val isConnected: Boolean = true,
    val temperatureDisplay: String = "28°C",
    val weatherCondition: String = "Partly Cloudy",
    val soilMoisturePercent: Int = 72,
    val soilNutrientsDisplay: String = "NPK: 8-6-10",
    val nutrientsLastReport: String = "Last Report: Today",
    val activeNav: NavDestination = NavDestination.HOME
) {
    val soilMoistureProgress: Float
        get() = (soilMoisturePercent.coerceIn(0, 100)) / 100f
}
