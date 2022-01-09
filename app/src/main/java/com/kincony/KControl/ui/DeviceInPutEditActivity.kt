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

    companion object {
        fun start(
            context: Context?,
            id: Int,
            name: String,
            icon: Int,
            itemName: String,
            max: String?,
            min: String?,
            unit: String?
        ) {
            val intent = Intent(context, DeviceInPutEditActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("name", name)
            intent.putExtra("icon", icon)
            intent.putExtra("itemName", itemName)
            intent.putExtra("max", max ?: "")
            intent.putExtra("min", min ?: "")
            intent.putExtra("unit", unit ?: "")
            context?.startActivity(intent)
        }
    }

    private var iIcon: Int = 0
    private var max: String = ""
    private var min: String = ""
    private var unit: String = ""

    override fun getLayoutId(): Int {
        return R.layout.activity_device_input_edit
    }

    override fun initView() {
        val iId = intent.getIntExtra("id", 0)
        val sName = intent.getStringExtra("name")
        iIcon = intent.getIntExtra("icon", R.drawable.icon6)
        max = intent.getStringExtra("max") ?: ""
        min = intent.getStringExtra("min") ?: ""
        unit = intent.getStringExtra("unit") ?: ""
        val sItemName = intent.getStringExtra("itemName") ?: ""

        ImageLoader.load(this, iIcon, icon)
        name.setText(sName)

        val itemNameList: List<String> = sItemName.split(";")
        val maxList: List<String> = max.split(";")
        val minList: List<String> = min.split(";")
        val unitList: List<String> = unit.split(";")


        for ((index, item) in itemNameList.withIndex()) {
            when (index + 1) {
                1 -> {
                    llInput1.visibility = View.VISIBLE
                    text1.visibility = View.VISIBLE
                    name1.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit1.visibility = View.VISIBLE
                        max1.setText(maxList[index])
                        min1.setText(minList[index])
                        unit1.setText(unitList[index])
                    }
                    name1.setText(item)
                }
                2 -> {
                    llInput2.visibility = View.VISIBLE
                    text2.visibility = View.VISIBLE
                    name2.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit2.visibility = View.VISIBLE
                        max2.setText(maxList[index])
                        min2.setText(minList[index])
                        unit2.setText(unitList[index])
                    }
                    name2.setText(item)
                }
                3 -> {
                    llInput3.visibility = View.VISIBLE
                    text3.visibility = View.VISIBLE
                    name3.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit3.visibility = View.VISIBLE
                        max3.setText(maxList[index])
                        min3.setText(minList[index])
                        unit3.setText(unitList[index])
                    }
                    name3.setText(item)
                }
                4 -> {
                    llInput4.visibility = View.VISIBLE
                    text4.visibility = View.VISIBLE
                    name4.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit4.visibility = View.VISIBLE
                        max4.setText(maxList[index])
                        min4.setText(minList[index])
                        unit4.setText(unitList[index])
                    }
                    name4.setText(item)
                }
                5 -> {
                    llInput5.visibility = View.VISIBLE
                    text5.visibility = View.VISIBLE
                    name5.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit5.visibility = View.VISIBLE
                        max5.setText(maxList[index])
                        min5.setText(minList[index])
                        unit5.setText(unitList[index])
                    }
                    name5.setText(item)
                }
                6 -> {
                    llInput6.visibility = View.VISIBLE
                    text6.visibility = View.VISIBLE
                    name6.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit6.visibility = View.VISIBLE
                        max6.setText(maxList[index])
                        min6.setText(minList[index])
                        unit6.setText(unitList[index])
                    }
                    name6.setText(item)
                }
                7 -> {
                    llInput7.visibility = View.VISIBLE
                    text7.visibility = View.VISIBLE
                    name7.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit7.visibility = View.VISIBLE
                        max7.setText(maxList[index])
                        min7.setText(minList[index])
                        unit7.setText(unitList[index])
                    }
                    name7.setText(item)
                }
                8 -> {
                    llInput8.visibility = View.VISIBLE
                    text8.visibility = View.VISIBLE
                    name8.visibility = View.VISIBLE
                    if (!TextUtils.isEmpty(max) && !TextUtils.isEmpty(max) && !TextUtils.isEmpty(max)) {
                        llUnit8.visibility = View.VISIBLE
                        max8.setText(maxList[index])
                        min8.setText(minList[index])
                        unit8.setText(unitList[index])
                    }
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
            if (llUnit1.visibility == View.VISIBLE) {
                if (max1.text.isEmpty()) {
                    showToast(resources.getString(R.string.max_not_empty))
                    return@setOnClickListener
                }
                if (max1.text.toString().toFloat() == 0F) {
                    showToast(resources.getString(R.string.max_not_zero))
                    return@setOnClickListener
                }
                if (min1.text.isEmpty()) {
                    showToast(resources.getString(R.string.min_not_empty))
                    return@setOnClickListener
                }
            }
            if (llUnit2.visibility == View.VISIBLE) {
                if (max2.text.isEmpty()) {
                    showToast(resources.getString(R.string.max_not_empty))
                    return@setOnClickListener
                }
                if (max2.text.toString().toFloat() == 0F) {
                    showToast(resources.getString(R.string.max_not_zero))
                    return@setOnClickListener
                }
                if (min2.text.isEmpty()) {
                    showToast(resources.getString(R.string.min_not_empty))
                    return@setOnClickListener
                }
            }
            if (llUnit3.visibility == View.VISIBLE) {
                if (max3.text.isEmpty()) {
                    showToast(resources.getString(R.string.max_not_empty))
                    return@setOnClickListener
                }
                if (max3.text.toString().toFloat() == 0F) {
                    showToast(resources.getString(R.string.max_not_zero))
                    return@setOnClickListener
                }
                if (min3.text.isEmpty()) {
                    showToast(resources.getString(R.string.min_not_empty))
                    return@setOnClickListener
                }
            }
            if (llUnit4.visibility == View.VISIBLE) {
                if (max4.text.isEmpty()) {
                    showToast(resources.getString(R.string.max_not_empty))
                    return@setOnClickListener
                }
                if (max4.text.toString().toFloat() == 0F) {
                    showToast(resources.getString(R.string.max_not_zero))
                    return@setOnClickListener
                }
                if (min4.text.isEmpty()) {
                    showToast(resources.getString(R.string.min_not_empty))
                    return@setOnClickListener
                }
            }

            EventBus.getDefault()
                .post(
                    DeviceInPutChange(
                        iId,
                        name.text.toString(),
                        getItemName(itemNameList.size),
                        iIcon,
                        getMax(maxList.size),
                        getMin(minList.size),
                        getUnit(unitList.size)
                    )
                )
            finish()
        }
        EventBus.getDefault().register(this)
    }

    private fun getItemName(num: Int): String {
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

    private fun getMax(num: Int): String {
        var max = ""
        for (i in 1..num) {
            when (i) {
                1 -> max += max1.text.toString()
                2 -> max += ";" + max2.text.toString()
                3 -> max += ";" + max3.text.toString()
                4 -> max += ";" + max4.text.toString()
                5 -> max += ";" + max5.text.toString()
                6 -> max += ";" + max6.text.toString()
                7 -> max += ";" + max7.text.toString()
                8 -> max += ";" + max8.text.toString()
            }
        }
        return max
    }

    private fun getMin(num: Int): String {
        var min = ""
        for (i in 1..num) {
            when (i) {
                1 -> min += min1.text.toString()
                2 -> min += ";" + min2.text.toString()
                3 -> min += ";" + min3.text.toString()
                4 -> min += ";" + min4.text.toString()
                5 -> min += ";" + min5.text.toString()
                6 -> min += ";" + min6.text.toString()
                7 -> min += ";" + min7.text.toString()
                8 -> min += ";" + min8.text.toString()
            }
        }
        return min
    }

    private fun getUnit(num: Int): String {
        var unit = ""
        for (i in 1..num) {
            when (i) {
                1 -> unit += if (!TextUtils.isEmpty(unit1.text)) unit1.text.toString()
                    .trim() else " "
                2 -> unit += ";" + if (!TextUtils.isEmpty(unit2.text)) unit2.text.toString()
                    .trim() else " "
                3 -> unit += ";" + if (!TextUtils.isEmpty(unit3.text)) unit3.text.toString()
                    .trim() else " "
                4 -> unit += ";" + if (!TextUtils.isEmpty(unit4.text)) unit4.text.toString()
                    .trim() else " "
                5 -> unit += ";" + if (!TextUtils.isEmpty(unit5.text)) unit5.text.toString()
                    .trim() else " "
                6 -> unit += ";" + if (!TextUtils.isEmpty(unit6.text)) unit6.text.toString()
                    .trim() else " "
                7 -> unit += ";" + if (!TextUtils.isEmpty(unit7.text)) unit7.text.toString()
                    .trim() else " "
                8 -> unit += ";" + if (!TextUtils.isEmpty(unit8.text)) unit8.text.toString()
                    .trim() else " "
            }
        }
        return unit
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
