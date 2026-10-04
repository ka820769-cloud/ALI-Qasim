package com.example.data.model

enum class DeviceType(val label: String, val iconEmoji: String) {
    SMARTWATCH("Smartwatch", "⌚"),
    FITNESS_BAND("Fitness Band", "📿"),
    CHEST_STRAP("Chest Strap Monitor", "🫀"),
    SMART_RING("Smart Ring", "💍"),
    UNKNOWN("Wearable Sensor", "📡")
}

data class WearableDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val batteryLevel: Int,
    val isConnected: Boolean,
    val isVirtual: Boolean = false,
    val rssiDbm: Int = -58,
    val macAddress: String = "E4:5F:01:A2:3B:90",
    val firmwareVersion: String = "v3.4.1-ble"
)
