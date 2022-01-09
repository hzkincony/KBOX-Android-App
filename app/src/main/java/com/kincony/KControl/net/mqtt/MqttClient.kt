package com.kincony.KControl.net.mqtt

import com.kincony.KControl.App
import com.kincony.KControl.R
import com.kincony.KControl.net.mqtt.callback.MqttConnectCallback
import com.kincony.KControl.net.mqtt.callback.MqttSubscribeCallback
import com.kincony.KControl.net.mqtt.event.MqttPublishEvent
import com.kincony.KControl.net.mqtt.event.MqttSubscribeEvent
import com.kincony.KControl.utils.LogUtils
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.mqtt.MqttDecoder
import io.netty.handler.codec.mqtt.MqttEncoder


class MqttClient(
    val ip: String,
    val port: String,
    val userName: String = "",
    val password: String = "",
    val clientId: String = "${ip}:${port}:${System.currentTimeMillis()}:${(Math.random() * 1000).toInt()}"
) {

    companion object {
        const val INIT = 0
        const val CONNECTED = 1
        const val CONNECTING = 3
        const val CONNECTED_FAIL = 4
        const val DISCONNECTED = 5
        const val DISCONNECTING = 6
        const val ERROR = 7
        const val CLOSE = 8
    }

    private var channel: Channel? = null
    private var messageId: Int = 0
    private fun getMessageId(): Int {
        if (messageId < 1 || messageId >= 65535) {
            messageId = 0
        }
        return ++messageId
    }

    private val workerGroup: EventLoopGroup = NioEventLoopGroup()
    private val bootstrap = Bootstrap()
    var state = INIT
    val subscribeEventList: ArrayList<MqttSubscribeEvent> = ArrayList()
    val publishEventList: ArrayList<MqttPublishEvent> = ArrayList()
    var reconnectCallback: MqttConnectCallback? = null
    var isSubACK: Boolean = false
    var ignoreCount: Int = 0
    var needReconnect: Boolean = false

    fun connect(connectCallback: MqttConnectCallback) {
        if (state == INIT) {
            bootstrap.group(workerGroup)
            bootstrap.channel(NioSocketChannel::class.java)
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
            bootstrap.handler(object : ChannelInitializer<SocketChannel>() {
                @Throws(Exception::class)
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(MqttEncoder.INSTANCE)
                    ch.pipeline().addLast(MqttDecoder())
//                    ch.pipeline().addLast(IdleStateHandler(60, 60, 0))
//                    ch.pipeline().addLast(MqttPingHandler(this@MqttClient, 30))
                    ch.pipeline()
                        .addLast(
                            MqttChannelHandler(
                                this@MqttClient,
                                object : MqttConnectCallback {

                                    override fun onSuccess(client: MqttClient) {
                                        state = CONNECTED
                                        reconnectCallback?.onSuccess(this@MqttClient)
                                    }

                                    override fun onFail(msg: String) {
                                        state = CONNECTED_FAIL
                                        reconnectCallback?.onFail(msg)
                                    }
                                })
                        )
                }
            })
        }
        if (state != CONNECTING && state != CONNECTED) {
            state = CONNECTING
            reconnectCallback = connectCallback
            bootstrap.connect(ip, port.toInt()).addListener(object : ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    if (future.isSuccess) {
                        channel = future.channel()
                    } else {
                        state = CONNECTED_FAIL
                        reconnectCallback?.onFail(App.application.getString(R.string.connect_fail))
                    }
                }
            })
        }
    }

    fun publishFlush() {
        val iterator = publishEventList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val publishMessage =
                MqttMessageHelper.publishMessage(next.topic, next.message, getMessageId())
            LogUtils.d("MQTT[${clientId}] SEND_PUB ${next.topic} ${next.message}")
            channel?.writeAndFlush(publishMessage)
            iterator.remove()
        }
    }

    fun publish(topic: String, message: String, ignore: Boolean = false) {
        if (state == CONNECTED && isSubACK) {
            val publishMessage = MqttMessageHelper.publishMessage(topic, message, getMessageId())
            LogUtils.d("MQTT[${clientId}] SEND_PUB ${topic} ${message}")
            channel?.writeAndFlush(publishMessage)
        } else {
            publishEventList.add(MqttPublishEvent(topic, message))
        }
        if (ignore) {
            ignoreCount++
        }
    }

    fun subscribe(topic: String, callback: MqttSubscribeCallback?) {
        for (mqttSubscribeEvent in subscribeEventList) {
            if (mqttSubscribeEvent.topic == topic) {
                mqttSubscribeEvent.callback = callback
                mqttSubscribeEvent.callback?.onSubscribe(mqttSubscribeEvent.lastMessage)
                return
            }
        }
        subscribeEventList.add(MqttSubscribeEvent(topic, callback))
        if (state == CONNECTED) {
            val subscribeMessage = MqttMessageHelper.subscribeMessage(topic, getMessageId())
            LogUtils.d("MQTT[${clientId}] SEND_SUB ${topic} ")
            channel?.writeAndFlush(subscribeMessage)
        }
    }

    fun unsubscribe(topic: String) {
        if (state == CONNECTED) {
            val iterator = subscribeEventList.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.topic == topic) {
                    iterator.remove()
                    break
                }
            }
            val unSubscribeMessage = MqttMessageHelper.unSubscribeMessage(topic, getMessageId())
            channel?.writeAndFlush(unSubscribeMessage)
        }
    }


    fun disconnect() {
        if (state == CONNECTED) {
            state = DISCONNECTING
            channel?.close()?.addListener {
                state = DISCONNECTED
            }
        }
    }

    fun close() {
        try {
            state = CLOSE
            workerGroup.shutdownGracefully()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}