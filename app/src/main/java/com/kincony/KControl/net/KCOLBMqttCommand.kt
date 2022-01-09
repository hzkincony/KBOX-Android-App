package com.kincony.KControl.net

import com.kincony.KControl.net.data.IPAddress

object KCOLBMqttCommand {

    fun subscribeTopic(ipAddress: IPAddress): String {
        return "COLB/${ipAddress.deviceId}${ipAddress.devicePassword}/state"
    }

    fun publishTopic(ipAddress: IPAddress): String {
        return "COLB/${ipAddress.deviceId}${ipAddress.devicePassword}/set"
    }

    fun readAllDigitalInputState(): String {
        return "{State=D_FF}"
    }

    fun readAllAnalogInputState(): String {
        return "{State=A_FF}"
    }

    fun readAllDS18B20TemperatureState(): String {
        return "{State=T_FF}"
    }

    fun enableAutoReport(enable: Boolean): String {
        return "{Auto_upload=${if (enable) 1 else 0}}"
    }

}