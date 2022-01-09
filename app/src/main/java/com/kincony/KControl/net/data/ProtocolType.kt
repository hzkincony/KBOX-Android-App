package com.kincony.KControl.net.data

enum class ProtocolType(val value: Int, val protocolTypeName: String) {
    TCP(0, "TCP"),        // TCP 默认
    MQTT(1, "MQTT");       // MQTT
}