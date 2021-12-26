package com.kincony.KControl.net

import com.kincony.KControl.net.data.IPAddress
import org.json.JSONObject

object KDimmerMqttCommand {

    fun subscribeTopic(ipAddress: IPAddress): String {
        return "dimmer/${ipAddress.deviceId}/state"
    }

    fun publishTopic(ipAddress: IPAddress): String {
        return "dimmer/${ipAddress.deviceId}/set"
    }

    fun readAll(): String {
        return " {\"dimmer\":{\"read\":all}}"
    }

    fun setDimmer(number: Int, brightness: Int): String {
        return "{\"dimmer${number}\":{\"value\":${brightness}}}"
    }

    fun setAllDimmer(brightnessList: List<String>): String {
        val json = JSONObject()
        for ((index, item) in brightnessList.withIndex()) {
            val value = JSONObject()
            value.put("value", item.toInt())
            json.put("dimmer${index + 1}", value)
        }
        return json.toString()
    }
}