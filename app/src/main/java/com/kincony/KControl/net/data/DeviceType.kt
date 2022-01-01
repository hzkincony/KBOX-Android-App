package com.kincony.KControl.net.data

enum class DeviceType(
    val value: Int,
    val numberCount: Int,
    val typeName: String,
    val typeNameCN: String
) {
    Unknown(0, 0, "Unknown", "未知"),           // 未知

    //继电器类型1-9999
    Relay_2(2, 2, "2 Channel Relay", "2路继电器"),           // 2路继电器
    Relay_4(4, 4, "4 Channel Relay", "4路继电器"),           // 4路继电器
    Relay_8(8, 8, "8 Channel Relay", "8路继电器"),           // 8路继电器
    Relay_16(16, 16, "16 Channel Relay", "16路继电器"),       // 16路继电器
    Relay_32(32, 32, "32 Channel Relay", "32路继电器"),       // 32路继电器

    //调光器类型10000-19999
    Dimmer_8(10008, 8, "8 Channel Dimmer", "8调光器"),     // 8路调光器

    //其他设备60000-int max
    COLB(60000, 9, "COLB", "COLB");             // COLB设备
}