package com.kincony.KControl.net.mqtt.callback

interface MqttSubscribeCallback {
    fun onSubscribe(msg: String)
    fun onFail(msg: String)
}