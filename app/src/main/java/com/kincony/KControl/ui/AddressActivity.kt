package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.kincony.KControl.R
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.RefreshAddressEvent
import com.kincony.KControl.net.data.RefreshSceneEvent
import com.kincony.KControl.ui.adapter.AddressAdapter
import com.kincony.KControl.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_address.*
import org.greenrobot.eventbus.EventBus

class AddressActivity : BaseActivity() {
    var list = ArrayList<IPAddress>()
    var adapter = AddressAdapter()

    companion object {
        fun start(context: Context?) {
            val intent = Intent(context, AddressActivity::class.java)
            context?.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_address
    }

    override fun initView() {
        back.setOnClickListener {
            finish()
        }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        var address = KBoxDatabase.getInstance(this).addressDao.allAddress
        list.addAll(address)
        adapter.setNewInstance(list)

        adapter.setOnItemLongClickListener { adapter, view, position ->
            var address = list[position]
            getLoadingDialog(this@AddressActivity, address)?.show()
            false
        }
    }

    private fun deleteAddress(address: IPAddress) {
        KBoxDatabase.getInstance(this).addressDao.delete(address)
        var scenes = KBoxDatabase.getInstance(this).sceneDao.allScene
        for (s in scenes) {
            if (s.address.contains(address.toString())) {
                KBoxDatabase.getInstance(this).sceneDao.deleteScene(s)
            }
        }

        list.remove(address)
        adapter.notifyDataSetChanged()
        EventBus.getDefault().post(RefreshAddressEvent())
        EventBus.getDefault().post(RefreshSceneEvent())
    }

    private fun getLoadingDialog(context: Context?, address: IPAddress): AlertDialog? {
        var dialog: AlertDialog? = null
        if (context != null) {
            dialog = AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(resources.getString(R.string.Message))
                .setMessage(resources.getString(R.string.message_delete))
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->

                }
                .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
                    deleteAddress(address)
                }
                .create()
        }
        return dialog
    }


}

