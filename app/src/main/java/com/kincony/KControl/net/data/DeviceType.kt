package com.kincony.KControl.net.data

enum class DeviceType(val value: Int) {
    Unknown(0),         // 未知

    //继电器类型1-9999
    Relay_2(  2),       // 2路继电器
    Relay_4(  4),       // 4路继电器
    Relay_8(  8),       // 8路继电器
    Relay_16(16),       // 16路继电器
    Relay_32(32),       // 32路继电器

    //调光器类型10000-int max value
    Dimmer_8(10008);    // 8路调光器
}