package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.mqtt.MqttConnectionState
import com.example.data.mqtt.PumpState
import com.example.ui.home.components.AiCropDoctorBanner
import com.example.ui.home.components.FarmReportsCard
import com.example.ui.home.components.KrishiBottomNavBar
import com.example.ui.home.components.KrishiNavigationRail
import com.example.ui.home.components.MonitoringCardsRow
import com.example.ui.home.components.TopHeaderSection
import com.example.ui.home.components.WaterPumpCard
import com.example.ui.home.components.WelcomeHeroSection
import com.example.ui.theme.BackgroundWhite
import com.example.ui.theme.KrishiParicharTheme
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftLavender

@Composable
fun HomeRoute() {
    val appContext = LocalContext.current.applicationContext
    val factory = remember(appContext) { HomeViewModel.provideFactory(appContext) }
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onPumpAction = viewModel::onPumpActionRequested
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onPumpAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to SkyBlue,
                        0.32f to Color(0xFFEAF4FF),
                        0.68f to BackgroundWhite,
                        0.88f to SoftLavender.copy(alpha = 0.45f),
                        1.00f to BackgroundWhite
                    )
                )
            )
            .testTag("home_screen_root")
    ) {
        val isExpanded = maxWidth >= 840.dp
        val isTablet = maxWidth >= 600.dp

        // Full-bleed sunlit agricultural sunrise landscape organically blended into the header & hero
        SunriseAtmosphereBackdrop(
            heightFraction = if (isTablet) 310.dp else 285.dp
        )

        if (isExpanded) {
            // Expanded Tablet / Desktop Layout with Side Navigation Rail & Balanced Dashboard Container
            Row(modifier = Modifier.fillMaxSize()) {
                KrishiNavigationRail(
                    activeDestination = uiState.activeNav
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    HomeDashboardContent(
                        uiState = uiState,
                        onPumpAction = onPumpAction,
                        isTabletOrDesktop = true,
                        bottomPadding = 32.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 880.dp)
                    )
                }
            }
        } else {
            // Mobile-First & Medium Tablet Layout with Fixed Bottom Glass Navigation Bar
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                HomeDashboardContent(
                    uiState = uiState,
                    onPumpAction = onPumpAction,
                    isTabletOrDesktop = isTablet,
                    bottomPadding = 108.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                )

                KrishiBottomNavBar(
                    activeDestination = uiState.activeNav,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun SunriseAtmosphereBackdrop(
    heightFraction: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightFraction)
    ) {
        // Sunrise farmland landscape with hills, tree, and tractor
        Image(
            painter = painterResource(id = R.drawable.img_farm_sunrise_1795265834327),
            contentDescription = stringResource(R.string.cd_farm_sunrise),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.92f)
        )

        // Top soft sky feather for crisp header readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SkyBlue.copy(alpha = 0.90f),
                            SkyBlue.copy(alpha = 0.50f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom seamless mist dissolve so the landscape blends organically into the dashboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xB8ECF5FF),
                            Color(0xF0F1F7FF),
                            BackgroundWhite
                        )
                    )
                )
        )
    }
}

@Composable
private fun HomeDashboardContent(
    uiState: HomeUiState,
    onPumpAction: () -> Unit,
    isTabletOrDesktop: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val horizontalPad = if (isTabletOrDesktop) 24.dp else 16.dp
    val sectionGap = if (isTabletOrDesktop) 16.dp else 14.dp

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("home_content_list"),
        contentPadding = PaddingValues(
            start = horizontalPad,
            end = horizontalPad,
            top = 10.dp,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        // SECTION A: TOP HEADER
        item(key = "section_a_header") {
            TopHeaderSection(connectionState = uiState.connectionState)
        }

        // SECTION B: WELCOME / GREETING AREA (Over Sunlit Agricultural Landscape)
        item(key = "section_b_greeting") {
            Column {
                WelcomeHeroSection()
                // Generous breathing room letting the sunlit farmland hills, tree & tractor shine
                Spacer(modifier = Modifier.height(if (isTabletOrDesktop) 78.dp else 68.dp))
            }
        }

        // SECTION C: WATER PUMP CONTROL CARD
        item(key = "section_c_water_pump") {
            WaterPumpCard(
                pumpState = uiState.pumpState,
                isCommandPending = uiState.isCommandPending,
                isLastStatusRetained = uiState.isLastStatusRetained,
                lastError = uiState.lastError,
                onPumpAction = onPumpAction
            )
        }

        // SECTION D: THREE AGRICULTURAL MONITORING CARDS
        item(key = "section_d_monitoring") {
            MonitoringCardsRow(
                temperature = uiState.temperatureDisplay,
                weatherCondition = uiState.weatherCondition,
                soilMoisturePercent = uiState.soilMoisturePercent,
                soilMoistureProgress = uiState.soilMoistureProgress,
                soilNutrients = uiState.soilNutrientsDisplay,
                nutrientsLastReport = uiState.nutrientsLastReport
            )
        }

        // SECTION E: FARM REPORTS CARD
        item(key = "section_e_reports") {
            FarmReportsCard()
        }

        // SECTION F: AI CROP DOCTOR FEATURE BANNER
        item(key = "section_f_ai_crop_doctor") {
            AiCropDoctorBanner()
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPhonePreview() {
    KrishiParicharTheme {
        HomeScreen(
            uiState = HomeUiState(
                pumpState = PumpState.ON,
                connectionState = MqttConnectionState.CONNECTED
            ),
            onPumpAction = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 720)
@Composable
private fun HomeScreenTabletPreview() {
    KrishiParicharTheme {
        HomeScreen(
            uiState = HomeUiState(
                pumpState = PumpState.OFF,
                connectionState = MqttConnectionState.CONNECTED
            ),
            onPumpAction = {}
        )
    }
}
