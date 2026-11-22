package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.mqtt.PumpMqttConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Krishi Parichar", appName)
    }

    @Test
    fun `persistent Android MQTT client ID is unique and distinct from ESP8266`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstId = PumpMqttConfig.getOrCreatePersistentClientId(context)
        val secondId = PumpMqttConfig.getOrCreatePersistentClientId(context)

        assertTrue(firstId.startsWith("krishi-parichar-android-"))
        assertNotEquals(PumpMqttConfig.ESP8266_RESERVED_CLIENT_ID, firstId)
        assertEquals(firstId, secondId)
    }
}
