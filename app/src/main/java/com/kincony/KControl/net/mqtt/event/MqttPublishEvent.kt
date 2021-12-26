package com.kincony.KControl.net.mqtt.event

class MqttPublishEvent(
    val topic: String,
    var message: String
) {

    companion object {
        const val STATUS_INIT = 0
        const val STATUS_SUCCESS = 1
        const val STATUS_FAIL = 2
    }

    var status: Int = STATUS_INIT
}