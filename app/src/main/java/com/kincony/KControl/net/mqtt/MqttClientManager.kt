package com.kincony.KControl.net.mqtt

import android.os.Handler
import android.os.Looper
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.ProtocolType
import com.kincony.KControl.net.mqtt.callback.MqttConnectCallback
import com.kincony.KControl.net.mqtt.callback.MqttPublishCallback
import com.kincony.KControl.net.mqtt.callback.MqttSubscribeCallback
import com.kincony.KControl.utils.LogUtils

object MqttClientManager {
    val mainThread = Handler(Looper.getMainLooper())
    val mqttClientMap: HashMap<IPAddress, MqttClient> = HashMap()

    fun disconnect(ipAddress: IPAddress) {
        val client = mqttClientMap.get(ipAddress)
        if (client != null) {
            client.disconnect()
            mqttClientMap.remove(ipAddress)?.close()
        }
    }

    fun connect(ipAddress: IPAddress, callback: MqttConnectCallback?) {
        val client = mqttClientMap.get(ipAddress)
        if (client != null && (client.state == MqttClient.CONNECTED || client.state == MqttClient.CONNECTING)) {
            if (callback != null) {
                mainThread.post {
                    callback.onSuccess(client)
                }
            }
        } else if (ipAddress.protocolType == ProtocolType.MQTT.value) {
            val mqttClient = MqttClient(
                ipAddress.ip,
                ipAddress.port.toString(),
                ipAddress.username.toString(),
                ipAddress.password.toString(),
                "${ipAddress.ip}:${ipAddress.port}:${System.currentTimeMillis()}:${(Math.random() * 1000).toInt()}"
            );
            mqttClientMap.put(ipAddress, mqttClient)
            mqttClient.connect(object : MqttConnectCallback {
                override fun onSuccess(client: MqttClient) {
                    if (callback != null) {
                        mainThread.post {
                            callback.onSuccess(client)
                        }
                    }
                }

                override fun onFail(msg: String) {
                    mqttClientMap.remove(ipAddress)?.close()
                    if (callback != null) {
                        mainThread.post {
                            callback.onFail(msg)
                        }
                    }
                }
            })
        } else {
            if (callback != null) {
                mainThread.post {
                    callback.onFail("The device must use the MQTT protocol!")
                }
            }
        }
    }

    fun subscribe(ipAddress: IPAddress, topic: String, callback: MqttSubscribeCallback?) {
        connect(ipAddress, object : MqttConnectCallback {
            override fun onSuccess(client: MqttClient) {
                val subTimeout = Runnable {
                    if (callback != null) {
                        mainThread.post {
                            callback.onFail("${ipAddress} timeout")
                        }
                    }
                }
                mainThread.postDelayed(subTimeout, 10000)
                client.subscribe(topic, object : MqttSubscribeCallback {
                    override fun onSubscribe(msg: String) {
                        mainThread.removeCallbacks(subTimeout)
                        if (callback != null) {
                            mainThread.post {
                                callback.onSubscribe(msg)
                            }
                        }
                    }

                    override fun onFail(msg: String) {
                        if (callback != null) {
                            mainThread.post {
                                callback.onFail(msg)
                            }
                        }
                    }
                })
            }

            override fun onFail(msg: String) {
                if (callback != null) {
                    mainThread.post {
                        callback.onFail(msg)
                    }
                }
            }
        })
    }

    var lastPublishCount = 1

    fun publish(
        ipAddress: IPAddress,
        topic: String,
        message: String,
        isIgnore: Boolean = false,
        callback: MqttPublishCallback? = null
    ) {
        connect(ipAddress, object : MqttConnectCallback {
            override fun onSuccess(client: MqttClient) {
                mainThread.postDelayed({
                    lastPublishCount--
                    LogUtils.d("MQTT[${client.clientId}] PUBLISH ${topic} ${message}")
                    client.publish(topic, message, isIgnore)
                }, 300L * lastPublishCount++)

                if (callback != null) {
                    mainThread.post {
                        callback.onSuccess()
                    }
                }
            }

            override fun onFail(msg: String) {
                if (callback != null) {
                    mainThread.post {
                        callback.onFail(msg)
                    }
                }
            }
        })
    }


}