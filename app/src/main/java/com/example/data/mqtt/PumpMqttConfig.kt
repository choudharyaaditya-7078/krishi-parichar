package com.example.data.mqtt

import android.content.Context
import com.example.BuildConfig
import java.util.UUID

/**
 * Configuration for connecting Krishi Parichar to the EMQX Cloud Serverless MQTT broker.
 * Credentials are injected at build time via the Secrets Gradle Plugin (.env / .env.example)
 * into BuildConfig and are never hardcoded or logged.
 */
data class PumpMqttConfig(
    val brokerUri: String = BROKER_URI,
    val username: String,
    val password: String,
    val clientId: String,
    val connectionTimeoutSeconds: Int = CONNECTION_TIMEOUT_SECONDS,
    val keepAliveSeconds: Int = KEEP_ALIVE_SECONDS
) {
    fun hasValidCredentials(): Boolean {
        val cleanUser = username.trim()
        val cleanPass = password.trim()
        return cleanUser.isNotEmpty() &&
            cleanPass.isNotEmpty() &&
            cleanUser != PLACEHOLDER_USERNAME &&
            cleanPass != PLACEHOLDER_PASSWORD
    }

    companion object {
        const val BROKER_HOST = "v911782f.ala.asia-southeast1.emqxsl.com"
        const val BROKER_TLS_PORT = 8883
        const val BROKER_URI = "ssl://$BROKER_HOST:$BROKER_TLS_PORT"

        const val CONNECTION_TIMEOUT_SECONDS = 10
        const val KEEP_ALIVE_SECONDS = 30
        const val COMMAND_ACK_TIMEOUT_MS = 10_000L

        const val ESP8266_RESERVED_CLIENT_ID = "krishi-parichar-esp8266-001"
        const val PLACEHOLDER_USERNAME = "YOUR_EMQX_MQTT_USERNAME"
        const val PLACEHOLDER_PASSWORD = "YOUR_EMQX_MQTT_PASSWORD"

        private const val PREFS_NAME = "krishi_parichar_mqtt_prefs"
        private const val KEY_CLIENT_ID = "android_mqtt_client_id"

        fun fromContext(context: Context): PumpMqttConfig {
            val appContext = context.applicationContext
            val clientId = getOrCreatePersistentClientId(appContext)
            return PumpMqttConfig(
                brokerUri = BROKER_URI,
                username = BuildConfig.EMQX_MQTT_USERNAME,
                password = BuildConfig.EMQX_MQTT_PASSWORD,
                clientId = clientId
            )
        }

        internal fun generateUniqueAndroidClientId(): String {
            var generated: String
            do {
                val suffix = UUID.randomUUID().toString().replace("-", "").take(16)
                generated = "krishi-parichar-android-$suffix"
            } while (generated == ESP8266_RESERVED_CLIENT_ID)
            return generated
        }

        internal fun getOrCreatePersistentClientId(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existing = prefs.getString(KEY_CLIENT_ID, null)
            if (!existing.isNullOrBlank() && existing != ESP8266_RESERVED_CLIENT_ID) {
                return existing
            }
            val generated = generateUniqueAndroidClientId()
            prefs.edit().putString(KEY_CLIENT_ID, generated).apply()
            return generated
        }
    }
}
