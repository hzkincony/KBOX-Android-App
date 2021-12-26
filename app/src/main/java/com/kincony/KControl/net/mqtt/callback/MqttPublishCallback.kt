package com.kincony.KControl.net.mqtt.callback

interface MqttPublishCallback {
    fun onSuccess()
    fun onFail(msg: String)
}