package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.kincony.KControl.R
import com.kincony.KControl.net.data.DeviceChange
import com.kincony.KControl.net.data.IconEvent
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.utils.ImageLoader
import kotlinx.android.synthetic.main.activity_device_edit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DeviceEditActivity : BaseActivity() {
    private var iIcon: Int = 0
    private var iIconTouch: Int = 0
    private var isTouch = false

    companion object {
        fun start(
            context: Context?,
            id: Int,
            name: String,
            icon: Int,
            isTouch: Boolean = false,
            iconTouch: Int = 0
        ) {
            val intent = Intent(context, DeviceEditActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("name", name)
            intent.putExtra("icon", icon)
            intent.putExtra("isTouch", isTouch)
            intent.putExtra("iconTouch", iconTouch)
            context?.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_device_edit
    }

    override fun initView() {
        var iId = intent.getIntExtra("id", 0)
        var sName = intent.getStringExtra("name")
        iIcon = intent.getIntExtra("icon", R.drawable.icon6)
        isTouch = intent.getBooleanExtra("isTouch", false)
        iIconTouch = intent.getIntExtra("iconTouch", 0)

        name.setText(sName)
        ImageLoader.load(this, iIcon, icon)
        ImageLoader.load(this, iIconTouch, iconTouch)

        back.setOnClickListener {
            finish()
        }
        iconLay.setOnClickListener {
            IconSelectActivity.start(this, 0, 0)
        }

        iconLayTouch.setOnClickListener {
            IconSelectActivity.start(this, 0, 1)
        }

        if (isTouch) {
            model.setSelection(if (isTouch) 1 else 0, true);
        }

        model.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        isTouch = false
                    }
                    1 -> {
                        isTouch = true
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        ok.setOnClickListener {
            if (TextUtils.isEmpty(name.text.toString())) {
                showToast(resources.getString(R.string.name_not_empty))
                return@setOnClickListener
            }
            EventBus.getDefault()
                .post(DeviceChange(iId, name.text.toString(), iIcon, isTouch, iIconTouch))
            finish()
        }
        EventBus.getDefault().register(this)
    }

    @Subscribe
    fun receiveIcon(event: IconEvent) {
        if (event.code == 0) {
            this.iIcon = event.icon
            ImageLoader.load(this, iIcon, icon)
        } else {
            this.iIconTouch = event.icon
            ImageLoader.load(this, iIconTouch, iconTouch)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
