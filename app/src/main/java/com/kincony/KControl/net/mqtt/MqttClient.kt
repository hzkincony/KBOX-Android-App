package com.kincony.KControl.net.mqtt

import com.kincony.KControl.net.mqtt.callback.MqttConnectCallback
import com.kincony.KControl.net.mqtt.callback.MqttSubscribeCallback
import com.kincony.KControl.net.mqtt.event.MqttPublishEvent
import com.kincony.KControl.net.mqtt.event.MqttSubscribeEvent
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
    }

    private var channel: Channel? = null
    private var messageId: Int = 0
    private fun getMessageId(): Int {
        if (messageId < 1 || messageId >= 65535) {
            messageId = 0
        }
        return ++messageId
    }

    var state = INIT
    val subscribeEventList: ArrayList<MqttSubscribeEvent> = ArrayList()
    val publishEventList: ArrayList<MqttPublishEvent> = ArrayList()
    var reconnectCallback: MqttConnectCallback? = null

    fun connect(connectCallback: MqttConnectCallback) {
        if (state != CONNECTING && state != CONNECTED) {
            state = CONNECTING
            reconnectCallback = connectCallback
            val workerGroup: EventLoopGroup = NioEventLoopGroup()
            val bootstrap = Bootstrap()
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
                        .addLast(MqttChannelHandler(this@MqttClient, object : MqttConnectCallback {

                            override fun onSuccess(client: MqttClient) {
                                state = CONNECTED
                                reconnectCallback?.onSuccess(this@MqttClient)
                            }

                            override fun onFail(msg: String) {
                                state = CONNECTED_FAIL
                                reconnectCallback?.onFail(msg)
                            }
                        }))
                }
            })
            bootstrap.connect(ip, port.toInt()).addListener(object : ChannelFutureListener {
                override fun operationComplete(future: ChannelFuture) {
                    if (future.isSuccess) {
                        channel = future.channel()
                    } else {
                        state = CONNECTED_FAIL
                        reconnectCallback?.onFail("Connect Fail!")
                    }
                }
            })
        }
    }

    fun publish(topic: String, message: String) {
        if (state == CONNECTED) {
            val publishMessage = MqttMessageHelper.publishMessage(topic, message, getMessageId())
            channel?.writeAndFlush(publishMessage)
        } else {
            publishEventList.add(MqttPublishEvent(topic, message))
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

}