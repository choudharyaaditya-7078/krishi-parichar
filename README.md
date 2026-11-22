# 🌱 KRISHI PARICHAR
### Smart Farming for a Better Tomorrow

**An IoT-powered Smart Agriculture System connecting an Android application with real-world farming hardware through cloud-based MQTT communication.**

Krishi Parichar is a smart agriculture project originally envisioned as **Krishi Tech 5000 Pro**. Its goal is to bring intelligent farm monitoring and irrigation control into a connected digital ecosystem.

The project combines a native Android application, ESP8266-based hardware, and cloud MQTT communication to enable remote interaction with farm equipment.

The current development focuses on reliable app-to-hardware communication, real-time pump control feedback, and a scalable foundation for future smart farming capabilities.

---

## 🚀 Project Vision

Traditional farming operations can involve manual irrigation decisions, limited visibility into field conditions, and delayed responses to changing agricultural requirements.

Krishi Parichar aims to make farming more connected and accessible through:

- Remote irrigation control.
- Connected agricultural hardware.
- Cloud-based device communication.
- Farm monitoring interfaces.
- A foundation for future intelligent agricultural features.

The long-term vision is to develop a unified smart farming ecosystem where farmers can monitor and interact with their agricultural equipment through a mobile application.

---

## ✨ Application Features

### 1. Smart Agriculture Dashboard

A modern dashboard designed to provide an overview of farm-related information.

Current interface components include:

- Farm welcome and overview section.
- Water pump control card.
- Monitoring cards for agricultural parameters.
- Farm reports section.
- AI Crop Doctor interface.
- Bottom navigation.

### 2. Smart Irrigation Control

The application includes a pump control interface connected to an ESP8266 through MQTT.

Capabilities:

- Send pump ON commands.
- Send pump OFF commands.
- Receive hardware status responses.
- Reflect confirmed hardware state in the application.
- Support remote control through a cloud MQTT broker.

**Important:** The current hardware proof-of-concept uses the ESP8266 built-in LED to simulate pump output. Actual water pump integration is a future hardware step.

### 3. Cloud-Based Communication

The application and ESP8266 communicate through EMQX Cloud Serverless using MQTT over TLS.

This architecture allows the application and hardware to communicate even when connected to different Wi-Fi networks, provided both have internet connectivity.

### 4. AI Crop Doctor Interface

The existing application includes an AI Crop Doctor interface intended to support future AI-assisted agricultural guidance.

Its full AI/backend functionality is part of the future development scope.

### 5. Farm Monitoring Interface

The dashboard includes monitoring cards intended for agricultural information such as soil moisture, nutrients, and environmental conditions.

Actual sensor-based live monitoring will require the corresponding sensors, hardware integration, and data processing.

---

## 🏗️ System Architecture

The current communication architecture:

```text
        KRISHI PARICHAR
        ANDROID APPLICATION
                 |
                 |
          MQTT over TLS
                 |
                 v
          EMQX CLOUD
         SERVERLESS BROKER
                 |
                 |
          MQTT over TLS
                 |
                 v
          ESP8266 NODEMCU
              ESP-12E
                 |
                 |
          BUILT-IN LED
          PUMP SIMULATION
```

### Communication Flow

**Application → ESP8266**

The Android application publishes pump commands to the MQTT broker.

**ESP8266 → Application**

The ESP8266 processes the command, changes its output state, and publishes a status response.

The application updates its confirmed pump status after receiving the hardware response.

---

## 📡 MQTT Communication Protocol

The current prototype uses the following MQTT topics and payloads.

| Communication | MQTT Topic | Payload |
|---|---|---|
| App → ESP8266 | `krishi-parichar/pump/1/set` | `ON` |
| App → ESP8266 | `krishi-parichar/pump/1/set` | `OFF` |
| ESP8266 → App | `krishi-parichar/pump/1/status` | `p_1` |
| ESP8266 → App | `krishi-parichar/pump/1/status` | `p_0` |

### Status Definitions

| Payload | Meaning |
|---|---|
| `p_1` | Pump output confirmed ON |
| `p_0` | Pump output confirmed OFF |

The ESP8266 currently publishes retained status messages.

The application must distinguish a successful MQTT publish from a confirmed hardware response.

**A command being sent does not mean the pump has successfully changed state.**

The application uses the ESP8266 status response as the confirmation signal.

> Note: `p_1` and `p_0` confirm the controller's output state, not actual water flow. Actual water-flow verification will require a suitable flow sensor.

---

## 🔌 Hardware Components

### Current Hardware

| Component | Purpose |
|---|---|
| NodeMCU ESP8266 (ESP-12E) | Main IoT controller |
| Built-in LED | Initial pump-control simulation |
| Wi-Fi network | Internet connectivity |
| USB connection | Programming and Serial Monitor debugging |

### Planned Hardware Expansion

- Relay module or appropriately rated contactor.
- Water pump.
- Soil moisture sensor.
- Additional agricultural sensors.
- Water-flow sensor for actual flow verification.

The actual component selection and electrical ratings will be determined during hardware integration.

**Electrical safety:** A water pump must never be connected directly to an ESP8266 GPIO pin. Appropriate isolated switching hardware and safe electrical installation are required.

---

## 💻 Technology Stack

### Android Application

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Lifecycle
- Coroutines
- StateFlow
- Eclipse Paho MQTT Client

### Hardware and IoT

- NodeMCU ESP8266 (ESP-12E)
- Arduino IDE
- C++ / Arduino framework
- PubSubClient MQTT library
- Wi-Fi connectivity

### Cloud Communication

- EMQX Cloud Serverless
- MQTT 3.1.1
- MQTT over TLS
- Port 8883

---

## 📱 Application Architecture

The Android application follows a Kotlin and Jetpack Compose-based structure.

Main application components:

```text
app/
└── src/
    └── main/
        └── java/com/example/
            ├── MainActivity.kt
            │
            ├── data/
            │   └── mqtt/
            │       ├── PumpMqttModels.kt
            │       ├── PumpMqttConfig.kt
            │       ├── PumpMqttClient.kt
            │       └── PumpMqttRepository.kt
            │
            └── ui/
                ├── theme/
                │
                └── home/
                    ├── HomeScreen.kt
                    ├── HomeUiState.kt
                    ├── HomeViewModel.kt
                    │
                    └── components/
                        ├── GlassCard.kt
                        ├── TopHeaderSection.kt
                        ├── WelcomeHeroSection.kt
                        ├── WaterPumpCard.kt
                        ├── MonitoringCardsRow.kt
                        ├── FarmReportsCard.kt
                        ├── AiCropDoctorBanner.kt
                        └── KrishiBottomNavBar.kt
```

The MQTT communication layer separates network communication from the user interface.

The HomeViewModel manages application state and connects the MQTT repository with the existing dashboard components.

---

## ⚙️ Getting Started

### 1. Android Application

Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/krishi-parichar.git
```

Open the project in Android Studio or a compatible Gradle development environment.

Configure the required project secrets before building.

### 2. MQTT Configuration

Create an EMQX Cloud Serverless deployment and configure MQTT authentication.

Required configuration:

```properties
EMQX_MQTT_USERNAME=YOUR_MQTT_USERNAME
EMQX_MQTT_PASSWORD=YOUR_MQTT_PASSWORD
```

Broker configuration:

```text
Host: v911782f.ala.asia-southeast1.emqxsl.com
Port: 8883
Protocol: MQTT 3.1.1 over TLS
```

Use the project's supported local secrets configuration mechanism.

Never commit actual passwords, API keys, or private credentials to GitHub.

### 3. ESP8266 Firmware

Install the following Arduino libraries:

- ESP8266WiFi
- PubSubClient by Nick O'Leary

Configure your own Wi-Fi and MQTT credentials in the firmware.

Upload the firmware to the NodeMCU ESP8266.

Open Arduino Serial Monitor at:

```text
115200 baud
```

The built-in LED is currently used to simulate pump output.

---

## 🧪 Current Development Status

| Feature | Status |
|---|---|
| Android application | Implemented |
| Home dashboard UI | Implemented |
| Pump control interface | Implemented |
| ESP8266 Wi-Fi connection | Working |
| EMQX Cloud deployment | Configured |
| MQTT communication | Working |
| App-to-ESP8266 commands | Working |
| ESP8266-to-app status response | Working |
| Different Wi-Fi network communication | Tested successfully |
| Physical water pump integration | Tested successfully |
| AI Crop Doctor backend | under working |

---

## 🛣️ Development Roadmap

### Phase 1 — Application Foundation
- Develop the Krishi Parichar Android application.
- Establish the dashboard design.
- Build the pump control interface.
- Create the initial application architecture.

### Phase 2 — IoT Communication
- Configure the ESP8266 NodeMCU.
- Establish Wi-Fi connectivity.
- Deploy the EMQX Cloud MQTT broker.
- Implement bidirectional MQTT communication.
- Connect the Android application to the ESP8266.
- Test communication across different networks.

### Phase 3 — Physical Irrigation Control
- Integrate an appropriately rated relay or contactor.
- Replace the built-in LED simulation with actual pump control.
- Verify hardware switching.
- Add water-flow sensing for actual flow confirmation.

### Phase 4 — Agricultural Monitoring
- Integrate soil moisture sensors.
- Collect relevant environmental data.
- Connect monitoring hardware with the application.
- Display live agricultural measurements.

### Phase 5 — Intelligent Farming Features
- Expand AI Crop Doctor functionality.
- Develop intelligent agricultural insights.
- Improve farm reporting and monitoring.
- Explore automated irrigation based on agricultural conditions.

---

## 🔐 Security and Reliability

The prototype uses MQTT over TLS for communication with EMQX Cloud.

Security and reliability priorities include:

- Authenticated MQTT connections.
- TLS certificate verification.
- Separate application and hardware MQTT client identities.
- Restricted MQTT topic permissions.
- Secure local credential configuration.
- Reconnection handling.
- Hardware response-based state confirmation.

Production deployment will require stronger credential management, appropriate device authentication, and validated electrical safety.

---

## 🎯 Project Objective

Krishi Parichar aims to bridge the gap between traditional agricultural operations and connected smart farming technology.

By combining mobile application development, IoT hardware, cloud communication, and future AI-assisted capabilities, the project is intended to evolve into a practical smart agriculture ecosystem.

**From monitoring fields to controlling irrigation — one connected farming system.**

---

## 👨‍💻 Project

**Project Name:** Krishi Parichar  
**Original Project Identity:** Krishi Tech 5000 Pro  
**Category:** Smart Agriculture | Internet of Things | Android Development | Embedded Systems

**Development Status:** Active Development

