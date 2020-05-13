package com.kincony.KControl.net.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "address")
class IPAddress {
    @PrimaryKey(autoGenerate = true)
    public var id: Int = 0

    public var ip: String = ""

    public var port: Int = 0

    public var type: Int = 2

    constructor() {

    }

    constructor(address: String, port: Int, type: Int = 2) {
        this.ip = address
        this.port = port
        this.type = type
    }

    fun isAvail(): Boolean {
        return ip != null && port != null
    }

    override fun equals(other: Any?): Boolean {
        return if (other is IPAddress) {
            ip != null && port != null && other.ip == ip && other.port == port
        } else {
            false
        }
    }

    override fun toString(): String {
        return "${ip}:${port}"
    }
}