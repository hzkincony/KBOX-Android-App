package com.kincony.KControl.net.mqtt.event

import com.kincony.KControl.net.mqtt.callback.MqttSubscribeCallback

class MqttSubscribeEvent(
    val topic: String,
    var callback: MqttSubscribeCallback? = null
) {

    companion object {
        const val STATUS_INIT = 0
        const val STATUS_SUCCESS = 1
        const val STATUS_FAIL = 2
    }

    var status: Int = STATUS_INIT

    var lastMessage: String = "{}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MqttSubscribeEvent

        if (topic != other.topic) return false

        return true
    }

    override fun hashCode(): Int {
        return topic.hashCode()
    }

}