package com.kincony.KControl.net.internal.connection

import android.util.Log
import com.kincony.KControl.net.internal.Call

object RealConnectionPool {
    private var list = arrayListOf<RealConnection>()

    @Synchronized
    fun getRealConnection(call: Call): RealConnection {
        var temp: RealConnection? = null

        for (connection in list) {
            if (call.host == connection.lastHost && !connection.inUse) {
                temp = connection
                temp.setNextCall(call)
                break
            }
        }
        if (temp == null) {
            temp = RealConnection().setNextCall(call)
            list.add(temp)
        }
        return temp!!
    }
}