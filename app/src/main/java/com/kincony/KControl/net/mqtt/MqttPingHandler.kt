package com.kincony.KControl.net.mqtt

import com.kincony.KControl.utils.LogUtils
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.mqtt.MqttFixedHeader
import io.netty.handler.codec.mqtt.MqttMessage
import io.netty.handler.codec.mqtt.MqttMessageType
import io.netty.handler.codec.mqtt.MqttQoS
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.util.ReferenceCountUtil
import io.netty.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class MqttPingHandler(
    private val client: MqttClient,
    private val keepaliveSeconds: Int
) :
    ChannelInboundHandlerAdapter() {
    private var pingRespTimeout: ScheduledFuture<*>? = null

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is MqttMessage) {
            ctx.fireChannelRead(msg)
            return
        }
        val message = msg
        if (message.fixedHeader().messageType() == MqttMessageType.PINGREQ) {
            LogUtils.d("MQTT[${client.clientId}] PINGREQ ${msg}")
            handlePingReq(ctx.channel())
        } else if (message.fixedHeader().messageType() == MqttMessageType.PINGRESP) {
            LogUtils.d("MQTT[${client.clientId}] PINGRESP ${msg}")
            handlePingResp()
        } else {
            ctx.fireChannelRead(ReferenceCountUtil.retain<Any>(msg))
        }
    }

    @Throws(Exception::class)
    override fun userEventTriggered(
        ctx: ChannelHandlerContext,
        evt: Any
    ) {
        super.userEventTriggered(ctx, evt)
        if (evt is IdleStateEvent) {
            when (evt.state()) {
                IdleState.READER_IDLE -> {
                }
                IdleState.WRITER_IDLE -> sendPingReq(ctx.channel())
            }
        }
    }

    private fun sendPingReq(channel: Channel) {
        val fixedHeader =
            MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0)
        channel.writeAndFlush(MqttMessage(fixedHeader))
        if (pingRespTimeout != null) {
            pingRespTimeout = channel.eventLoop().schedule({
                val fixedHeader2 = MqttFixedHeader(
                    MqttMessageType.DISCONNECT,
                    false,
                    MqttQoS.AT_MOST_ONCE,
                    false,
                    0
                )
                channel.writeAndFlush(MqttMessage(fixedHeader2))
                    .addListener(ChannelFutureListener.CLOSE)
            }, keepaliveSeconds.toLong(), TimeUnit.SECONDS)
        }
    }

    private fun handlePingReq(channel: Channel) {
        val fixedHeader =
            MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0)
        channel.writeAndFlush(MqttMessage(fixedHeader))
    }

    private fun handlePingResp() {
        if (pingRespTimeout != null && !pingRespTimeout!!.isCancelled && !pingRespTimeout!!.isDone) {
            pingRespTimeout!!.cancel(true)
            pingRespTimeout = null
        }
    }

}