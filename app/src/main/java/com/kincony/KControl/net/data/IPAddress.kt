package com.kincony.KControl.net.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "address")
class IPAddress {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var ip: String

    var port: Int

    var deviceType: Int = DeviceType.Relay_2.value

    var protocolType: Int = ProtocolType.TCP.value

    var username: String? = null

    var password: String? = null

    var deviceId: String? = null

    fun getDeviceTypeEnum(): DeviceType {
        return when (deviceType) {
            DeviceType.Relay_2.value -> DeviceType.Relay_2
            DeviceType.Relay_4.value -> DeviceType.Relay_4
            DeviceType.Relay_8.value -> DeviceType.Relay_8
            DeviceType.Relay_16.value -> DeviceType.Relay_16
            DeviceType.Relay_32.value -> DeviceType.Relay_32
            DeviceType.Dimmer_8.value -> DeviceType.Dimmer_8
            DeviceType.COLB.value -> DeviceType.COLB
            else -> DeviceType.Unknown
        }
    }

    fun getDeviceTypeNumberCount(): Int {
        return getDeviceTypeEnum().numberCount
    }

    constructor(
        ip: String,
        port: Int,
        deviceType: Int,
        protocolType: Int,
        username: String?,
        password: String?,
        deviceId: String?
    ) {
        this.ip = ip
        this.port = port
        this.deviceType = deviceType
        this.protocolType = protocolType
        this.username = username
        this.password = password
        this.deviceId = deviceId
    }

    override fun equals(other: Any?): Boolean {
        return if (other is IPAddress && protocolType == other.protocolType) {
            if (protocolType == ProtocolType.MQTT.value) {
                username != null && password != null && password != null && other.ip == ip && other.port == port && other.username == username && other.password == password && other.deviceId == deviceId
            } else {
                other.ip == ip && other.port == port
            }
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = id
        if (protocolType == ProtocolType.MQTT.value) {
            result = 31 * result + ip.hashCode()
            result = 31 * result + port
            result = 31 * result + deviceType
            result = 31 * result + protocolType
        } else {
            result = 31 * result + ip.hashCode()
            result = 31 * result + port
            result = 31 * result + deviceType
            result = 31 * result + protocolType
            result = 31 * result + (username?.hashCode() ?: 0)
            result = 31 * result + (password?.hashCode() ?: 0)
            result = 31 * result + (deviceId?.hashCode() ?: 0)
        }
        return result
    }

    override fun toString(): String {
        return if (protocolType == ProtocolType.MQTT.value) {
            "mqtt:${ip}:${port}:${deviceId}"
        } else {
            "tcp:${ip}:${port}"
        }
    }

}