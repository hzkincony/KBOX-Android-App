package com.kincony.KControl.net.mqtt

import com.kincony.KControl.net.mqtt.callback.MqttConnectCallback
import com.kincony.KControl.utils.LogUtils
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.mqtt.*


class MqttChannelHandler(
    private val client: MqttClient,
    private val connectCallback: MqttConnectCallback
) : SimpleChannelInboundHandler<MqttMessage>() {

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        // send connect message
        val connectMessage =
            MqttMessageHelper.connectMessage(client.clientId, client.userName, client.password)
        ctx.channel().writeAndFlush(connectMessage)
        LogUtils.d("MQTT[${client.clientId}] CONNECT")
    }

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: MqttMessage) {
        when (msg.fixedHeader().messageType()) {
            MqttMessageType.CONNACK -> {
                handleConnAck(ctx.channel(), msg as MqttConnAckMessage)
            }
            MqttMessageType.SUBACK -> {
                LogUtils.d("MQTT[${client.clientId}] SUBACK ${msg}")
            }
            MqttMessageType.UNSUBACK -> {
                LogUtils.d("MQTT[${client.clientId}] UNSUBACK ${msg}")
            }
            MqttMessageType.SUBSCRIBE -> {
                LogUtils.d("MQTT[${client.clientId}] SUBSCRIBE ${msg}")
            }
            MqttMessageType.PUBLISH -> {
                handlePublish(ctx.channel(), msg as MqttPublishMessage)
            }
            MqttMessageType.PUBACK -> {
                LogUtils.d("MQTT[${client.clientId}] PUBACK ${msg}")
            }
            MqttMessageType.PUBREC -> {
                LogUtils.d("MQTT[${client.clientId}] PUBREC ${msg}")
            }
            MqttMessageType.PUBREL -> {
                LogUtils.d("MQTT[${client.clientId}] PUBREL ${msg}")
            }
            MqttMessageType.PUBCOMP -> {
                LogUtils.d("MQTT[${client.clientId}] PUBCOMP ${msg}")
            }
            else -> {
                LogUtils.d(
                    "MQTT[${client.clientId}] type=${msg.fixedHeader().messageType()} msg=${msg}"
                )
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        LogUtils.d("MQTT[${client.clientId}] channelInactive")
        super.channelInactive(ctx)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        LogUtils.d("MQTT[${client.clientId}] userEventTriggered ${evt}")
        super.userEventTriggered(ctx, evt)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
        LogUtils.d("MQTT[${client.clientId}] channelWritabilityChanged")
        super.channelWritabilityChanged(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        LogUtils.d("MQTT[${client.clientId}] channelUnregistered")
        super.channelUnregistered(ctx)
        client.disconnect()
        client.connect(client.reconnectCallback!!)
    }

    override fun channelRegistered(ctx: ChannelHandlerContext?) {
        LogUtils.d("MQTT[${client.clientId}] channelRegistered")
        super.channelRegistered(ctx)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        LogUtils.d("MQTT[${client.clientId}] channelReadComplete")
        super.channelReadComplete(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        LogUtils.d("MQTT[${client.clientId}] exceptionCaught ${cause}")
        super.exceptionCaught(ctx, cause)
        client.disconnect()
        client.connect(client.reconnectCallback!!)
    }

    private fun handlePublish(channel: Channel, message: MqttPublishMessage) {
        val topic = message.variableHeader().topicName()
        val utf8Message = message.payload().toString(Charsets.UTF_8)
        LogUtils.d("MQTT[${client.clientId}] PUBLISH ${topic} ${utf8Message}")
        client.subscribeEventList.forEach {
            if (it.topic == topic && utf8Message.startsWith("{") && utf8Message.endsWith("}")) {
                it.lastMessage = utf8Message
                it.callback?.onSubscribe(utf8Message)
            }
        }
    }

    private fun handleConnAck(channel: Channel, message: MqttConnAckMessage) {
        val connectReturnCode = message.variableHeader().connectReturnCode()
        when (connectReturnCode) {
            MqttConnectReturnCode.CONNECTION_ACCEPTED -> {
                LogUtils.d("MQTT[${client.clientId}] CONNACK CONNECTION_ACCEPTED")
                connectCallback.onSuccess(client)
            }
            MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD,
            MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED,
            MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED,
            MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE,
            MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION -> {
                LogUtils.d(
                    "MQTT[${client.clientId}] CONNACK Fail Code=${connectReturnCode}"
                )
                connectCallback.onFail(
                    "Connect Fail! Code=${connectReturnCode}"
                )
                channel.close()
            }
        }
    }


}