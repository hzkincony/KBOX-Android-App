package com.kincony.KControl.ui

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.kincony.KControl.R
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.ui.adapter.HistoryAdapter
import com.kincony.KControl.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_address.*
import org.json.JSONObject

class AddHistoryActivity : BaseActivity() {
    var list = ArrayList<IPAddress>()
    var adapter = HistoryAdapter()

    override fun getLayoutId(): Int {
        return R.layout.activity_add_history
    }

    override fun initView() {
        back.setOnClickListener {
            onBackPressed()
        }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        val address = KBoxDatabase.getInstance(this).addressDao.allAddress
        list.addAll(address)
        adapter.setNewInstance(list)
        adapter.setOnItemClickListener { adapter, view, position ->
            val address = list[position]
            val jsonObject = JSONObject()
            jsonObject.put("ip", address.ip)
            jsonObject.put("port", address.port)
            jsonObject.put("deviceType", address.deviceType)
            jsonObject.put("protocolType", address.protocolType)
            jsonObject.put("userName", address.username)
            jsonObject.put("password", address.password)
            jsonObject.put("deviceId", address.deviceId)

            val result = Intent()
            result.putExtra("history_result", jsonObject.toString())
            setResult(RESULT_OK, result)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}