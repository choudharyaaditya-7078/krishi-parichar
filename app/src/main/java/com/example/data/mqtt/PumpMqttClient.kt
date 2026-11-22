package com.example.data.mqtt

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttSecurityException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.SSLSocketFactory
import kotlin.coroutines.resume

/**
 * Asynchronous MQTT 3.1.1 over TLS client for EMQX Cloud Serverless using Eclipse Paho.
 * Runs all network operations off the main thread and preserves strict Android TLS certificate
 * and hostname verification.
 */
class PumpMqttClient(
    private val appContext: Context,
    private val config: PumpMqttConfig
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val clientLock = Any()

    @Volatile
    private var asyncClient: MqttAsyncClient? = null
    private val isConnecting = AtomicBoolean(false)
    private val isSubscribed = AtomicBoolean(false)
    private val isReleased = AtomicBoolean(false)
    private val networkCallbackRegistered = AtomicBoolean(false)

    private val _connectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
    val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private val _statusEvents = MutableSharedFlow<PumpStatusEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val statusEvents: SharedFlow<PumpStatusEvent> = _statusEvents.asSharedFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (!isReleased.get()) {
                val state = _connectionState.value
                if (state == MqttConnectionState.DISCONNECTED || state == MqttConnectionState.ERROR) {
                    connect()
                }
            }
        }

        override fun onLost(network: Network) {
            if (!isReleased.get() && !hasActiveInternetNetwork()) {
                isSubscribed.set(false)
                _connectionState.value = MqttConnectionState.DISCONNECTED
                _connectionError.value = "Internet connection unavailable."
            }
        }
    }

    private val mqttCallback = object : MqttCallbackExtended {
        override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            if (isReleased.get()) return
            isConnecting.set(false)
            isSubscribed.set(false)
            _connectionState.value = MqttConnectionState.CONNECTED
            _connectionError.value = null
            Log.i(TAG, if (reconnect) "Reconnected to EMQX broker" else "Connected to EMQX broker")
            subscribeToStatusTopic()
        }

        override fun connectionLost(cause: Throwable?) {
            if (isReleased.get()) return
            isConnecting.set(false)
            isSubscribed.set(false)
            _connectionState.value = MqttConnectionState.DISCONNECTED
            _connectionError.value = "Cloud connection lost. Reconnecting..."
            Log.w(TAG, "EMQX MQTT connection lost: ${cause?.message ?: "unknown cause"}")
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            if (isReleased.get() || topic != PumpProtocol.STATUS_TOPIC || message == null) return
            val rawPayload = String(message.payload, StandardCharsets.UTF_8)
            val parsedState = PumpProtocol.parseStatusPayload(rawPayload)
            if (parsedState == null) {
                Log.w(TAG, "Ignored unrecognized pump status payload on $topic")
                return
            }
            _statusEvents.tryEmit(
                PumpStatusEvent(
                    state = parsedState,
                    isRetained = message.isRetained,
                    receivedAtMillis = System.currentTimeMillis()
                )
            )
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            // Publish acknowledgement does NOT confirm ESP8266 hardware execution.
        }
    }

    fun connect() {
        if (isReleased.get()) return

        if (!config.hasValidCredentials()) {
            _connectionState.value = MqttConnectionState.ERROR
            _connectionError.value =
                "EMQX MQTT credentials not configured. Set EMQX_MQTT_USERNAME and EMQX_MQTT_PASSWORD in AI Studio Secrets."
            return
        }

        registerNetworkMonitoringOnce()

        if (!isConnecting.compareAndSet(false, true)) {
            return
        }

        scope.launch {
            try {
                val client = synchronized(clientLock) {
                    if (isReleased.get()) return@launch
                    asyncClient ?: MqttAsyncClient(
                        config.brokerUri,
                        config.clientId,
                        MemoryPersistence()
                    ).also { created ->
                        created.setCallback(mqttCallback)
                        asyncClient = created
                    }
                }

                if (client.isConnected) {
                    isConnecting.set(false)
                    _connectionState.value = MqttConnectionState.CONNECTED
                    _connectionError.value = null
                    subscribeToStatusTopic()
                    return@launch
                }

                _connectionState.value = MqttConnectionState.CONNECTING
                _connectionError.value = null

                val options = buildConnectOptions()
                client.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        isConnecting.set(false)
                        if (isReleased.get()) return
                        _connectionState.value = MqttConnectionState.CONNECTED
                        _connectionError.value = null
                        // connectComplete also triggers subscription; atomic guard avoids duplicates.
                        subscribeToStatusTopic()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        isConnecting.set(false)
                        if (isReleased.get()) return
                        val friendlyMessage = mapConnectionError(exception)
                        _connectionState.value = MqttConnectionState.ERROR
                        _connectionError.value = friendlyMessage
                        Log.e(TAG, "EMQX connection failed: ${exception?.javaClass?.simpleName}")
                    }
                })
            } catch (ex: Exception) {
                isConnecting.set(false)
                if (!isReleased.get()) {
                    _connectionState.value = MqttConnectionState.ERROR
                    _connectionError.value = mapConnectionError(ex)
                    Log.e(TAG, "EMQX client initialization error: ${ex.javaClass.simpleName}")
                }
            }
        }
    }

    suspend fun publishCommand(command: PumpCommand): Result<Unit> = withContext(Dispatchers.IO) {
        if (isReleased.get()) {
            return@withContext Result.failure(IllegalStateException("MQTT client has been released."))
        }
        val client = synchronized(clientLock) { asyncClient }
        if (client == null || !client.isConnected) {
            return@withContext Result.failure(IllegalStateException("Not connected to EMQX cloud broker."))
        }

        suspendCancellableCoroutine { continuation ->
            try {
                val payloadBytes = command.payload.toByteArray(StandardCharsets.UTF_8)
                client.publish(
                    PumpProtocol.COMMAND_TOPIC,
                    payloadBytes,
                    PumpProtocol.COMMAND_QOS,
                    PumpProtocol.COMMAND_RETAINED,
                    null,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            if (continuation.isActive) {
                                continuation.resume(Result.success(Unit))
                            }
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    Result.failure(
                                        exception ?: RuntimeException("Failed to publish pump command.")
                                    )
                                )
                            }
                        }
                    }
                )
            } catch (ex: Exception) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(ex))
                }
            }
        }
    }

    fun release() {
        if (!isReleased.compareAndSet(false, true)) return
        unregisterNetworkMonitoring()

        val clientToClose = synchronized(clientLock) {
            val current = asyncClient
            asyncClient = null
            current
        }

        isConnecting.set(false)
        isSubscribed.set(false)
        _connectionState.value = MqttConnectionState.DISCONNECTED

        if (clientToClose != null) {
            scope.launch {
                try {
                    clientToClose.setCallback(null)
                    if (clientToClose.isConnected) {
                        clientToClose.disconnectForcibly(500L, 500L)
                    }
                } catch (_: Exception) {
                    // Best-effort disconnect on shutdown.
                } finally {
                    try {
                        clientToClose.close(true)
                    } catch (_: Exception) {
                        // Best-effort resource cleanup.
                    }
                    scope.cancel()
                }
            }
        } else {
            scope.cancel()
        }
    }

    private fun buildConnectOptions(): MqttConnectOptions {
        return MqttConnectOptions().apply {
            mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
            isCleanSession = true
            isAutomaticReconnect = true
            connectionTimeout = config.connectionTimeoutSeconds
            keepAliveInterval = config.keepAliveSeconds
            userName = config.username.trim()
            password = config.password.toCharArray()
            // Strict Android default TLS truststore and HTTPS hostname verification.
            socketFactory = SSLSocketFactory.getDefault()
            isHttpsHostnameVerificationEnabled = true
        }
    }

    private fun subscribeToStatusTopic() {
        if (isReleased.get()) return
        val client = synchronized(clientLock) { asyncClient } ?: return
        if (!client.isConnected) return
        if (!isSubscribed.compareAndSet(false, true)) return

        try {
            client.subscribe(
                PumpProtocol.STATUS_TOPIC,
                PumpProtocol.STATUS_QOS,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.i(TAG, "Subscribed to ${PumpProtocol.STATUS_TOPIC}")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        isSubscribed.set(false)
                        if (!isReleased.get()) {
                            _connectionError.value = "Failed to subscribe to pump status topic."
                        }
                        Log.e(TAG, "Subscription failed: ${exception?.javaClass?.simpleName}")
                    }
                }
            )
        } catch (ex: Exception) {
            isSubscribed.set(false)
            Log.e(TAG, "Error subscribing to status topic: ${ex.javaClass.simpleName}")
        }
    }

    private fun registerNetworkMonitoringOnce() {
        if (!networkCallbackRegistered.compareAndSet(false, true)) return
        try {
            val connectivityManager =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {
            networkCallbackRegistered.set(false)
        }
    }

    private fun unregisterNetworkMonitoring() {
        if (!networkCallbackRegistered.compareAndSet(true, false)) return
        try {
            val connectivityManager =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // Safe ignore if callback was not active.
        }
    }

    private fun hasActiveInternetNetwork(): Boolean {
        return try {
            val connectivityManager =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return false
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    private fun mapConnectionError(exception: Throwable?): String {
        return when (exception) {
            is MqttSecurityException -> {
                "MQTT authentication failed. Check EMQX username, password, and ACL permissions."
            }
            is MqttException -> {
                when (exception.reasonCode.toShort()) {
                    MqttException.REASON_CODE_FAILED_AUTHENTICATION,
                    MqttException.REASON_CODE_NOT_AUTHORIZED ->
                        "MQTT authentication failed. Verify EMQX credentials."
                    else ->
                        "Unable to reach EMQX cloud broker. Check internet connection."
                }
            }
            else -> "Unable to connect to EMQX cloud broker."
        }
    }

    companion object {
        private const val TAG = "PumpMqttClient"
    }
}
