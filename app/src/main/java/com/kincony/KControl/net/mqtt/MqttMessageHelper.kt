package com.kincony.KControl.net.mqtt

import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.*
import java.util.*

object MqttMessageHelper {

    fun connectMessage(clientId: String, userName: String, password: String): MqttConnectMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 10)
        val mqttConnectVariableHeader = MqttConnectVariableHeader(
            MqttVersion.MQTT_3_1_1.protocolName(),
            MqttVersion.MQTT_3_1_1.protocolLevel().toInt(),
            true,
            true,
            false,
            0,
            false,
            false,
            20,
            MqttProperties.NO_PROPERTIES
        )
        val mqttConnectPayload = MqttConnectPayload(
            clientId,
            MqttProperties.NO_PROPERTIES,
            null,
            null,
            userName,
            password.toByteArray(Charsets.UTF_8)
        )
        return MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload)
    }

    fun unsubAckMessage(messageId: Int): MqttUnsubAckMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(messageId)
        return MqttUnsubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader)
    }

    fun subAckMessage(
        messageId: Int,
        mqttQoSList: List<Int>
    ): MqttSubAckMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val mqttMessageIdVariableHeader =
            MqttMessageIdVariableHeader.from(messageId)
        val mqttSubAckPayload = MqttSubAckPayload(mqttQoSList)
        return MqttSubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubAckPayload)
    }

    fun pubCompMessage(messageId: Int): MqttMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val mqttMessageIdVariableHeader =
            MqttMessageIdVariableHeader.from(messageId)
        return MqttMessage(mqttFixedHeader, mqttMessageIdVariableHeader)
    }

    fun pubAckMessage(messageId: Int): MqttPubAckMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val mqttMessageIdVariableHeader =
            MqttMessageIdVariableHeader.from(messageId)
        return MqttPubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader)
    }

    fun pubRecMessage(messageId: Int): MqttMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val mqttMessageIdVariableHeader =
            MqttMessageIdVariableHeader.from(messageId)
        return MqttMessage(mqttFixedHeader, mqttMessageIdVariableHeader)
    }

    fun pubRelMessage(messageId: Int, isDup: Boolean = false): MqttMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.PUBREL, isDup, MqttQoS.AT_MOST_ONCE, false, 0)
        val messageIdVariableHeader =
            MqttMessageIdVariableHeader.from(messageId)
        return MqttMessage(mqttFixedHeader, messageIdVariableHeader)
    }

    fun pingRespMessage(): MqttMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0)
        return MqttMessage(mqttFixedHeader)
    }

    fun connAckMessage(
        sessionPresent: Boolean,
        code: MqttConnectReturnCode = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE
    ): MqttConnAckMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val mqttConnAckVariableHeader = MqttConnAckVariableHeader(code, sessionPresent)
        return MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader)
    }

    fun connectReturnCodeForException(cause: Throwable?): MqttConnectReturnCode {
        var code = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE
        if (cause is MqttUnacceptableProtocolVersionException) {
            // unacceptable protocol version
            code = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION
        } else if (cause is MqttIdentifierRejectedException) {
            // unacceptable client id
            code = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED
        }
        return code
    }

    fun publishMessage(
        topicName: String,
        message: String,
        messageId: Int,
        isDup: Boolean = false,
        mqttQos: MqttQoS = MqttQoS.AT_MOST_ONCE,
        isRetain: Boolean = false
    ): MqttPublishMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.PUBLISH, isDup, mqttQos, isRetain, 0)
        val mqttPublishVariableHeader =
            MqttPublishVariableHeader(topicName, messageId)
        val byteBuf = Unpooled.copiedBuffer(message, Charsets.UTF_8)
        return MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, byteBuf)
    }

    fun subscribeMessage(
        topic: String,
        messageId: Int
    ): MqttSubscribeMessage {
        val mqttSubscribePayload =
            MqttSubscribePayload(Arrays.asList(MqttTopicSubscription(topic, MqttQoS.AT_MOST_ONCE)))
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val mqttMessageIdVariableHeader =
            MqttMessageIdVariableHeader.from(messageId)
        return MqttSubscribeMessage(
            mqttFixedHeader,
            mqttMessageIdVariableHeader,
            mqttSubscribePayload
        )
    }

    fun unSubscribeMessage(
        topic: String,
        messageId: Int
    ): MqttUnsubscribeMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_MOST_ONCE, false, 0x02)
        val variableHeader =
            MqttMessageIdVariableHeader.from(messageId)
        val mqttUnsubscribeMessage = MqttUnsubscribePayload(Arrays.asList(topic))
        return MqttUnsubscribeMessage(mqttFixedHeader, variableHeader, mqttUnsubscribeMessage)
    }

    fun disConnectMessage(): MqttMessage {
        val mqttFixedHeader =
            MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0x02)
        return MqttMessage(mqttFixedHeader)
    }
}