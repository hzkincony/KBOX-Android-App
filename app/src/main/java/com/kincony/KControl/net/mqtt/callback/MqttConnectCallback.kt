package com.kincony.KControl.net.mqtt.callback

import com.kincony.KControl.net.mqtt.MqttClient

interface MqttConnectCallback {
    fun onSuccess(client: MqttClient)
    fun onFail(msg: String)
}