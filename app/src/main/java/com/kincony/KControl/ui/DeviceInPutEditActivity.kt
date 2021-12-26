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
        val iId = intent.getIntExtra("id", 0)
        val sName = intent.getStringExtra("name")
        iIcon = intent.getIntExtra("icon", R.drawable.icon6)
        val sItemName = intent.getStringExtra("itemName")

        ImageLoader.load(this, iIcon, icon)
        name.setText(sName)

        val list: List<String> = sItemName.split(";")

        for ((index, item) in list.withIndex()) {
            when (index + 1) {
                1 -> {
                    text1.visibility = View.VISIBLE
                    name1.visibility = View.VISIBLE
                    name1.setText(item)
                }
                2 -> {
                    text2.visibility = View.VISIBLE
                    name2.visibility = View.VISIBLE
                    name2.setText(item)
                }
                3 -> {
                    text3.visibility = View.VISIBLE
                    name3.visibility = View.VISIBLE
                    name3.setText(item)
                }
                4 -> {
                    text4.visibility = View.VISIBLE
                    name4.visibility = View.VISIBLE
                    name4.setText(item)
                }
                5 -> {
                    text5.visibility = View.VISIBLE
                    name5.visibility = View.VISIBLE
                    name5.setText(item)
                }
                6 -> {
                    text6.visibility = View.VISIBLE
                    name6.visibility = View.VISIBLE
                    name6.setText(item)
                }
                7 -> {
                    text7.visibility = View.VISIBLE
                    name7.visibility = View.VISIBLE
                    name7.setText(item)
                }
                8 -> {
                    text8.visibility = View.VISIBLE
                    name8.visibility = View.VISIBLE
                    name8.setText(item)
                }
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
        for (i in 1..num) {
            when (i) {
                1 -> itemName += name1.text.toString()
                2 -> itemName += ";" + name2.text.toString()
                3 -> itemName += ";" + name3.text.toString()
                4 -> itemName += ";" + name4.text.toString()
                5 -> itemName += ";" + name5.text.toString()
                6 -> itemName += ";" + name6.text.toString()
                7 -> itemName += ";" + name7.text.toString()
                8 -> itemName += ";" + name8.text.toString()
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
