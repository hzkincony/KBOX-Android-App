package com.kincony.KControl.net.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kincony.KControl.utils.Tools

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

    var deviceUserName: String? = null

    var devicePassword: String? = null

    fun getDeviceTypeNumberCount(): Int {
        return Tools.getDeviceTypeEnum(deviceType).numberCount
    }

    fun getDeviceTypeName(context: Context): String {
        if (context.resources.configuration.locale.country.contains("CN"))
            return Tools.getDeviceTypeEnum(deviceType).typeNameCN
        else
            return Tools.getDeviceTypeEnum(deviceType).typeName
    }

    fun getProtocolTypeName(): String {
        return Tools.getProtocolTypeEnum(protocolType).protocolTypeName
    }

    constructor(
        ip: String,
        port: Int,
        deviceType: Int,
        protocolType: Int,
        username: String?,
        password: String?,
        deviceId: String?,
        deviceUserName: String?,
        devicePassword: String?
    ) {
        this.ip = ip
        this.port = port
        this.deviceType = deviceType
        this.protocolType = protocolType
        this.username = username
        this.password = password
        this.deviceId = deviceId
        this.deviceUserName = deviceUserName
        this.devicePassword = devicePassword
    }

    override fun equals(other: Any?): Boolean {
        return if (other is IPAddress && protocolType == other.protocolType) {
            if (protocolType == ProtocolType.CAMERA.value) {
                other.deviceType == deviceType
                        && deviceUserName != null && devicePassword != null
                        && other.deviceUserName == deviceUserName && other.devicePassword == devicePassword
                        && other.deviceId == deviceId
            } else if (protocolType == ProtocolType.MQTT.value) {
                other.ip == ip && other.port == port
                        && other.deviceType == deviceType
                        && username != null && password != null
                        && other.username == username && other.password == password
                        && other.deviceId == deviceId && other.devicePassword == devicePassword
            } else {
                other.ip == ip && other.port == port
                        && other.deviceType == deviceType
            }
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = id
        if (protocolType == ProtocolType.CAMERA.value) {
            result = 31 * result + deviceType
            result = 31 * result + protocolType
            result = 31 * result + (deviceUserName?.hashCode() ?: 0)
            result = 31 * result + (devicePassword?.hashCode() ?: 0)
            result = 31 * result + (deviceId?.hashCode() ?: 0)
        } else if (protocolType == ProtocolType.MQTT.value) {
            result = 31 * result + ip.hashCode()
            result = 31 * result + port
            result = 31 * result + deviceType
            result = 31 * result + protocolType
            result = 31 * result + (username?.hashCode() ?: 0)
            result = 31 * result + (password?.hashCode() ?: 0)
            result = 31 * result + (deviceId?.hashCode() ?: 0)
            result = 31 * result + (devicePassword?.hashCode() ?: 0)
        } else {
            result = 31 * result + ip.hashCode()
            result = 31 * result + port
            result = 31 * result + deviceType
            result = 31 * result + protocolType
        }
        return result
    }

    override fun toString(): String {
        return if (protocolType == ProtocolType.CAMERA.value) {
            "camera:${deviceId}:${deviceType}"
        } else if (protocolType == ProtocolType.MQTT.value) {
            "mqtt:${ip}:${port}:${deviceId}:${deviceType}"
        } else {
            "tcp:${ip}:${port}:${deviceType}"
        }
    }

}