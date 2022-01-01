package com.kincony.KControl.net

import com.kincony.KControl.net.data.IPAddress

object KBoxMqttCommand {

    fun subscribeTopic(ipAddress: IPAddress): String {
        return "relay${ipAddress.getDeviceTypeNumberCount()}/${ipAddress.deviceId}${ipAddress.devicePassword}/state"
    }

    fun publishTopic(ipAddress: IPAddress): String {
        return "relay${ipAddress.getDeviceTypeNumberCount()}/${ipAddress.deviceId}${ipAddress.devicePassword}/set"
    }

    fun readAllState(ipAddress: IPAddress): String {
        return "{\"relay${ipAddress.getDeviceTypeNumberCount()}\":{\"read\":1}}"
    }

    fun setState(number: Int, action: Int): String {
        return "{\"relay${number}\":{\"on\":${action}}}"
    }

    fun setAllState(action: String): String {
        var command = ""
        val actions = action.split(",")
        for ((index, item) in actions.withIndex()) {
            for (number in 1..8) {
                var result = false;
                when (number) {
                    1 -> {
                        result = (item.toInt() and 0b00000001) == 0b00000001
                    }
                    2 -> {
                        result = (item.toInt() and 0b00000010) == 0b00000010
                    }
                    3 -> {
                        result = (item.toInt() and 0b00000100) == 0b00000100
                    }
                    4 -> {
                        result = (item.toInt() and 0b00001000) == 0b00001000
                    }
                    5 -> {
                        result = (item.toInt() and 0b00010000) == 0b00010000
                    }
                    6 -> {
                        result = (item.toInt() and 0b00100000) == 0b00100000
                    }
                    7 -> {
                        result = (item.toInt() and 0b01000000) == 0b01000000
                    }
                    8 -> {
                        result = (item.toInt() and 0b10000000) == 0b10000000
                    }
                }
                command += "{\"relay${(actions.size - index - 1) * 8 + number}\":{\"on\":${if (result) 1 else 0}}}"
            }
        }
        return command
    }
}