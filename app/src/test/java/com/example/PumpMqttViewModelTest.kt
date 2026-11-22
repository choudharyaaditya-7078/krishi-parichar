package com.example

import com.example.data.mqtt.MqttConnectionState
import com.example.data.mqtt.PumpCommand
import com.example.data.mqtt.PumpControlRepository
import com.example.data.mqtt.PumpMqttConfig
import com.example.data.mqtt.PumpProtocol
import com.example.data.mqtt.PumpState
import com.example.data.mqtt.PumpStatusEvent
import com.example.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PumpMqttViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakePumpRepository : PumpControlRepository {
        val mutableConnectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
        val mutableConnectionError = MutableStateFlow<String?>(null)
        val mutableStatusEvents = MutableSharedFlow<PumpStatusEvent>(extraBufferCapacity = 16)

        override val connectionState: StateFlow<MqttConnectionState> = mutableConnectionState.asStateFlow()
        override val connectionError: StateFlow<String?> = mutableConnectionError.asStateFlow()
        override val statusEvents: SharedFlow<PumpStatusEvent> = mutableStatusEvents.asSharedFlow()

        val sentCommands = mutableListOf<PumpCommand>()
        var connectCalls = 0
        var closeCalls = 0
        var publishResult: Result<Unit> = Result.success(Unit)

        override fun connect() {
            connectCalls++
        }

        override suspend fun sendCommand(command: PumpCommand): Result<Unit> {
            sentCommands.add(command)
            return publishResult
        }

        override fun close() {
            closeCalls++
        }
    }

    @Test
    fun `test A - pump turns ON only after receiving fresh p_1 from ESP8266`() = runTest {
        val repo = FakePumpRepository()
        val viewModel = HomeViewModel(repo, commandTimeoutMs = 10_000L)
        repo.mutableConnectionState.value = MqttConnectionState.CONNECTED
        runCurrent()

        // Establish initial confirmed OFF state from ESP8266
        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.OFF, isRetained = false, receivedAtMillis = 1000L))
        runCurrent()
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)

        // User taps power button -> publishes ON and enters pending WITHOUT changing confirmed state
        viewModel.onPumpActionRequested()
        runCurrent()
        assertEquals(listOf(PumpCommand.ON), repo.sentCommands)
        assertTrue(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpCommand.ON, viewModel.uiState.value.pendingCommand)
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)

        // Duplicate tap while pending is ignored
        viewModel.onPumpActionRequested()
        runCurrent()
        assertEquals(1, repo.sentCommands.size)

        // ESP8266 confirms with fresh p_1
        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.ON, isRetained = false, receivedAtMillis = 2000L))
        runCurrent()
        assertEquals(PumpState.ON, viewModel.uiState.value.pumpState)
        assertFalse(viewModel.uiState.value.isCommandPending)
        assertNull(viewModel.uiState.value.lastError)
        assertFalse(viewModel.uiState.value.isLastStatusRetained)
    }

    @Test
    fun `test B - pump turns OFF only after receiving fresh p_0 from ESP8266`() = runTest {
        val repo = FakePumpRepository()
        val viewModel = HomeViewModel(repo, commandTimeoutMs = 10_000L)
        repo.mutableConnectionState.value = MqttConnectionState.CONNECTED
        runCurrent()

        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.ON, isRetained = false, receivedAtMillis = 1000L))
        runCurrent()
        assertEquals(PumpState.ON, viewModel.uiState.value.pumpState)

        // Tap to turn OFF -> still visually ON while pending
        viewModel.onPumpActionRequested()
        runCurrent()
        assertEquals(listOf(PumpCommand.OFF), repo.sentCommands)
        assertTrue(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.ON, viewModel.uiState.value.pumpState)

        // ESP8266 confirms with fresh p_0
        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.OFF, isRetained = false, receivedAtMillis = 2000L))
        runCurrent()
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)
        assertFalse(viewModel.uiState.value.isCommandPending)
        assertNull(viewModel.uiState.value.lastError)
    }

    @Test
    fun `test C - ESP8266 offline triggers timeout error and preserves confirmed state`() = runTest {
        val repo = FakePumpRepository()
        val viewModel = HomeViewModel(repo, commandTimeoutMs = 10_000L)
        repo.mutableConnectionState.value = MqttConnectionState.CONNECTED
        runCurrent()

        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.OFF, isRetained = false, receivedAtMillis = 1000L))
        runCurrent()

        viewModel.onPumpActionRequested()
        runCurrent()
        assertTrue(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)

        // Advance past 10-second acknowledgement timeout without ESP8266 response
        advanceTimeBy(10_001L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)
        assertEquals(HomeViewModel.NO_RESPONSE_ERROR, viewModel.uiState.value.lastError)

        // User can retry cleanly after timeout
        viewModel.onPumpActionRequested()
        runCurrent()
        assertEquals(2, repo.sentCommands.size)
        assertTrue(viewModel.uiState.value.isCommandPending)
    }

    @Test
    fun `test D - broker offline blocks publish and keeps pump state unchanged`() = runTest {
        val repo = FakePumpRepository()
        val viewModel = HomeViewModel(repo, commandTimeoutMs = 10_000L)
        runCurrent()
        assertEquals(MqttConnectionState.DISCONNECTED, viewModel.uiState.value.connectionState)
        val initialConnectCalls = repo.connectCalls

        viewModel.onPumpActionRequested()
        runCurrent()

        assertTrue(repo.sentCommands.isEmpty())
        assertFalse(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.UNKNOWN, viewModel.uiState.value.pumpState)
        assertNotNull(viewModel.uiState.value.lastError)
        assertEquals(initialConnectCalls + 1, repo.connectCalls)
    }

    @Test
    fun `test E - protocol parser handles valid p_1 and p_0 and ignores invalid payloads`() {
        assertEquals(PumpState.ON, PumpProtocol.parseStatusPayload("p_1"))
        assertEquals(PumpState.ON, PumpProtocol.parseStatusPayload("  P_1 \r\n"))
        assertEquals(PumpState.OFF, PumpProtocol.parseStatusPayload("p_0"))
        assertEquals(PumpState.OFF, PumpProtocol.parseStatusPayload(" P_0 "))

        assertNull(PumpProtocol.parseStatusPayload("ON"))
        assertNull(PumpProtocol.parseStatusPayload("OFF"))
        assertNull(PumpProtocol.parseStatusPayload("1"))
        assertNull(PumpProtocol.parseStatusPayload("0"))
        assertNull(PumpProtocol.parseStatusPayload("unknown"))
        assertNull(PumpProtocol.parseStatusPayload(""))
        assertNull(PumpProtocol.parseStatusPayload(null))
    }

    @Test
    fun `test F - startup state is UNKNOWN and retained message never clears pending command`() = runTest {
        val repo = FakePumpRepository()
        val viewModel = HomeViewModel(repo, commandTimeoutMs = 10_000L)
        runCurrent()

        // Initial state must be UNKNOWN, never fake ON
        assertEquals(PumpState.UNKNOWN, viewModel.uiState.value.pumpState)

        // Connect and receive retained status p_0 from broker
        repo.mutableConnectionState.value = MqttConnectionState.CONNECTED
        runCurrent()
        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.OFF, isRetained = true, receivedAtMillis = 1000L))
        runCurrent()
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)
        assertTrue(viewModel.uiState.value.isLastStatusRetained)

        // User requests ON -> enters pending
        viewModel.onPumpActionRequested()
        runCurrent()
        assertTrue(viewModel.uiState.value.isCommandPending)

        // Stale retained message arriving during pending MUST NOT clear pendingCommand
        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.ON, isRetained = true, receivedAtMillis = 1500L))
        runCurrent()
        assertTrue(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)

        // Fresh non-retained message confirms the command
        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.ON, isRetained = false, receivedAtMillis = 2000L))
        runCurrent()
        assertFalse(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.ON, viewModel.uiState.value.pumpState)
        assertFalse(viewModel.uiState.value.isLastStatusRetained)
    }

    @Test
    fun `tapping power when state is UNKNOWN sends explicit ON command`() = runTest {
        val repo = FakePumpRepository()
        val viewModel = HomeViewModel(repo, commandTimeoutMs = 10_000L)
        repo.mutableConnectionState.value = MqttConnectionState.CONNECTED
        runCurrent()

        assertEquals(PumpState.UNKNOWN, viewModel.uiState.value.pumpState)
        viewModel.onPumpActionRequested()
        runCurrent()

        assertEquals(listOf(PumpCommand.ON), repo.sentCommands)
        assertTrue(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.UNKNOWN, viewModel.uiState.value.pumpState)
    }

    @Test
    fun `unexpected valid status updates confirmed state and surfaces mismatch message`() = runTest {
        val repo = FakePumpRepository()
        val viewModel = HomeViewModel(repo, commandTimeoutMs = 10_000L)
        repo.mutableConnectionState.value = MqttConnectionState.CONNECTED
        runCurrent()

        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.OFF, isRetained = false, receivedAtMillis = 1000L))
        runCurrent()

        // Request ON, but controller replies p_0 (OFF)
        viewModel.onPumpActionRequested()
        runCurrent()
        repo.mutableStatusEvents.emit(PumpStatusEvent(PumpState.OFF, isRetained = false, receivedAtMillis = 2000L))
        runCurrent()

        assertFalse(viewModel.uiState.value.isCommandPending)
        assertEquals(PumpState.OFF, viewModel.uiState.value.pumpState)
        assertNotNull(viewModel.uiState.value.lastError)
    }

    @Test
    fun `config rejects placeholder credentials and accepts real credentials`() {
        val placeholderConfig = PumpMqttConfig(
            username = PumpMqttConfig.PLACEHOLDER_USERNAME,
            password = PumpMqttConfig.PLACEHOLDER_PASSWORD,
            clientId = "krishi-parichar-android-test123"
        )
        assertFalse(placeholderConfig.hasValidCredentials())

        val blankConfig = PumpMqttConfig(
            username = "",
            password = "secret",
            clientId = "krishi-parichar-android-test123"
        )
        assertFalse(blankConfig.hasValidCredentials())

        val validConfig = PumpMqttConfig(
            username = "android_app_user",
            password = "strong_password_value",
            clientId = "krishi-parichar-android-test123"
        )
        assertTrue(validConfig.hasValidCredentials())
        assertEquals("ssl://v911782f.ala.asia-southeast1.emqxsl.com:8883", validConfig.brokerUri)
        assertEquals("krishi-parichar/pump/1/set", PumpProtocol.COMMAND_TOPIC)
        assertEquals("krishi-parichar/pump/1/status", PumpProtocol.STATUS_TOPIC)
        assertFalse(PumpProtocol.COMMAND_RETAINED)
    }

    @Test
    fun `BuildConfig exposes EMQX MQTT credential fields`() {
        // Verifies the Secrets Gradle Plugin generated BuildConfig fields at compile time.
        assertNotNull(BuildConfig.EMQX_MQTT_USERNAME)
        assertNotNull(BuildConfig.EMQX_MQTT_PASSWORD)
    }

    @Test
    fun `generated Android MQTT client IDs are unique and never match ESP8266 client ID`() {
        val id1 = PumpMqttConfig.generateUniqueAndroidClientId()
        val id2 = PumpMqttConfig.generateUniqueAndroidClientId()
        assertTrue(id1.startsWith("krishi-parichar-android-"))
        assertTrue(id2.startsWith("krishi-parichar-android-"))
        assertTrue(id1 != id2)
        assertTrue(id1 != PumpMqttConfig.ESP8266_RESERVED_CLIENT_ID)
        assertTrue(id2 != PumpMqttConfig.ESP8266_RESERVED_CLIENT_ID)
    }
}
