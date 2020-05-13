package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.kincony.KControl.R
import com.kincony.KControl.net.data.DeviceInPutChange
import com.kincony.KControl.net.data.IconEvent
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.utils.ImageLoader
import kotlinx.android.synthetic.main.activity_device_input_edit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DeviceInPutEditActivity : BaseActivity() {

    private var iIcon: Int = 0

    companion object {
        fun start(
            context: Context?,
            id: Int,
            name: String,
            icon: Int,
            itemName: String
        ) {
            val intent = Intent(context, DeviceInPutEditActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("name", name)
            intent.putExtra("icon", icon)
            intent.putExtra("itemName", itemName)
            context?.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_device_input_edit
    }

    override fun initView() {
        var iId = intent.getIntExtra("id", 0)
        var sName = intent.getStringExtra("name")
        iIcon = intent.getIntExtra("icon", R.drawable.icon6)
        var sItemName = intent.getStringExtra("itemName")

        ImageLoader.load(this, iIcon, icon)
        name.setText(sName)

        var list: List<String> = sItemName.split(";")

        name1.setText(list.get(0))
        name2.setText(list.get(1))
        name3.setText(list.get(2))
        name4.setText(list.get(3))
        name5.setText(list.get(4))
        name6.setText(list.get(5))

        when(list.size) {
            6 -> {
                text7.visibility = View.GONE
                name7.visibility = View.GONE
                text8.visibility = View.GONE
                name8.visibility = View.GONE
            }
            8 -> {
                text7.visibility = View.VISIBLE
                name7.visibility = View.VISIBLE
                text8.visibility = View.VISIBLE
                name8.visibility = View.VISIBLE
                name7.setText(list.get(6))
                name8.setText(list.get(7))
            }
        }

        iconLay.setOnClickListener {
            IconSelectActivity.start(this, 0, 0)
        }

        back.setOnClickListener {
            finish()
        }

        ok.setOnClickListener {
            if (TextUtils.isEmpty(name.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }

            if (name1.visibility == View.VISIBLE && TextUtils.isEmpty(name1.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            if (name2.visibility == View.VISIBLE && TextUtils.isEmpty(name2.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            if (name3.visibility == View.VISIBLE && TextUtils.isEmpty(name3.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            if (name4.visibility == View.VISIBLE && TextUtils.isEmpty(name4.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            if (name5.visibility == View.VISIBLE && TextUtils.isEmpty(name5.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            if (name6.visibility == View.VISIBLE && TextUtils.isEmpty(name6.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            if (name7.visibility == View.VISIBLE && TextUtils.isEmpty(name7.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            if (name8.visibility == View.VISIBLE && TextUtils.isEmpty(name8.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }

            EventBus.getDefault()
                .post(DeviceInPutChange(iId, name.text.toString(), getItemName(list.size), iIcon))
            finish()
        }
        EventBus.getDefault().register(this)
    }

    fun getItemName(num: Int): String {
        var itemName = ""
        when(num) {
            6 -> {
                itemName += name1.text.toString() + ";"
                itemName += name2.text.toString() + ";"
                itemName += name3.text.toString() + ";"
                itemName += name4.text.toString() + ";"
                itemName += name5.text.toString() + ";"
                itemName += name6.text.toString()
            }
            8 -> {
                itemName += name1.text.toString() + ";"
                itemName += name2.text.toString() + ";"
                itemName += name3.text.toString() + ";"
                itemName += name4.text.toString() + ";"
                itemName += name5.text.toString() + ";"
                itemName += name6.text.toString() + ";"
                itemName += name7.text.toString() + ";"
                itemName += name8.text.toString()
            }
        }

        return itemName
    }

    @Subscribe
    fun receiveIcon(event: IconEvent) {
        if (event.code == 0) {
            this.iIcon = event.icon
            ImageLoader.load(this, iIcon, icon)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
