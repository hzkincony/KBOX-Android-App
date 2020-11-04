package com.kincony.KControl.net.internal.connection

import com.kincony.KControl.net.internal.Call

object RealConnectionPool {
    private var realConnectionMap = HashMap<String, RealConnection>()

    @Synchronized
    fun getRealConnection(call: Call): RealConnection {
        var realConnection: RealConnection? = realConnectionMap[call.host]
        if (realConnection != null && !realConnection.inUse) {
            realConnection.setNextCall(call)
        } else {
            realConnection = RealConnection().setNextCall(call)
            realConnectionMap[call.host] = realConnection
        }
        return realConnection
    }
}