package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.LinearLayoutManager
import com.kincony.KControl.R
import com.kincony.KControl.net.data.*
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.ui.adapter.device.DeviceAdapter
import com.kincony.KControl.ui.adapter.device.DimmerDeviceConvert
import com.kincony.KControl.ui.adapter.device.RelayDeviceConvert
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.utils.ImageLoader
import kotlinx.android.synthetic.main.activity_scene_edit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList

/**
 * 情景模式编辑界面
 */
class SceneEditActivity : BaseActivity() {
    private var iIcon: Int = 0
    var scene: Scene? = null
    var adapter: DeviceAdapter? = null
    var list = ArrayList<Device>()

    companion object {
        fun start(context: Context?, scene: Scene?) {
            val intent = Intent(context, SceneEditActivity::class.java)
            intent.putExtra("scene", scene)
            context?.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_scene_edit
    }

    override fun initView() {
        EventBus.getDefault().register(this)
        back.setOnClickListener {
            finish()
        }
        scene = intent.getSerializableExtra("scene") as Scene?

        if (scene == null) {
            titleName.text = getString(R.string.scene_new)
            scene = Scene()
        } else {
            titleName.text = getString(R.string.scene_edit)
            name.setText(scene!!.name);
        }
        iIcon = scene?.icon ?: R.drawable.icon6
        ImageLoader.load(this, iIcon, icon)

        ok.setOnClickListener {
            if (scene!!.ids.isNullOrEmpty()) {
                showToast(getString(R.string.device_alert))
                return@setOnClickListener
            }
            scene!!.name = name.text.toString()
            scene!!.icon = iIcon
            KBoxDatabase.getInstance(this).sceneDao.insertScene(scene)
            EventBus.getDefault().post(RefreshSceneEvent())
            finish()
        }
        add.setOnClickListener {
            showAddDeviceDialog()
        }

        iconLay.setOnClickListener {
            IconSelectActivity.start(this, 0)
        }


        if (scene?.isTouch == true) {
            model.setSelection(1, true);
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
                        scene?.isTouch = false
                    }
                    1 -> {
                        scene?.isTouch = true
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        adapter = DeviceAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter?.setNewInstance(list)
        // 继电器点击事件回调
        adapter?.relayDeviceConvert?.callback = object : RelayDeviceConvert.Callback {
            override fun onSceneActionDown(position: Int, device: Device) {

            }

            override fun onSceneActionUp(position: Int, device: Device) {
            }

            override fun onSwitchClick(position: Int, device: Device) {
                val lengthArray = scene!!.length.split("_")
                var start = 0
                var result = ""
                for (i in lengthArray) {
                    val length = i.toInt()
                    val deviceChannelList = list.subList(start, start + length)
                    start += length
                    if (TextUtils.isEmpty(result)) {
                        result = actionCode(deviceChannelList, device.address.deviceType)
                    } else {
                        result =
                            "${result}_${actionCode(deviceChannelList, device.address.deviceType)}"
                    }
                }
                scene!!.action = result
            }

            override fun onEditClick(position: Int, device: Device) {
            }

            override fun onInPutEditClick(position: Int, device: Device) {
            }
        }

        // 调光器点击事件回调
        adapter?.dimmerDeviceConvert?.callback = object : DimmerDeviceConvert.Callback {
            override fun onAddClick(position: Int, device: Device) {
                changeAction(device)
            }

            override fun onSubClick(position: Int, device: Device) {
                changeAction(device)
            }

            override fun onProgressChange(position: Int, device: Device, progress: Int) {
                changeAction(device)
            }

            override fun onEditClick(position: Int, device: Device) {

            }

            override fun onInPutEditClick(position: Int, device: Device) {

            }

            fun changeAction(device: Device) {
                val lengthArray = scene!!.length.split("_")
                var start = 0
                var result = ""
                for (i in lengthArray) {
                    val length = i.toInt()
                    val deviceChannelList = list.subList(start, start + length)
                    start += length
                    if (TextUtils.isEmpty(result)) {
                        result = actionCode(deviceChannelList, device.address.deviceType)
                    } else {
                        result =
                            "${result}_${actionCode(deviceChannelList, device.address.deviceType)}"
                    }
                }
                scene!!.action = result
            }
        }

        loadDate()
    }


    @Subscribe
    public fun receiveIcon(event: IconEvent) {
        this.iIcon = event.icon
        ImageLoader.load(this, iIcon, icon)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun loadDate() {
        list.clear()
        adapter?.notifyDataSetChanged()
        if (scene!!.ids.isNullOrEmpty()) return
        val ids = scene!!.ids.split("_")
        val actions = scene!!.action.split("_")
        val lengths = scene!!.length.split("_")
        for ((index, i) in ids.withIndex()) {
            val devices = KBoxDatabase.getInstance(this).deviceDao.getDevice(i.toInt())
            val address = KBoxDatabase.getInstance(this).addressDao.getAddress(devices[0].addressId)
            for (temp in devices) {
                temp.address = address
            }
            var length = lengths[index].toInt()
            val action = actions[index]
            when (address.deviceType) {
                DeviceType.Relay_2.value,
                DeviceType.Relay_4.value,
                DeviceType.Relay_8.value -> {//2,4,8
                    val subArray = action.split(",")
                    val r0 = Integer.valueOf(subArray[0])
                    for (d in devices) {
                        d.open = readIfOpen(r0, d.number)
                        d.isTouch = false
                    }
                }
                DeviceType.Relay_16.value -> {//16
                    val subArray = action.split(",")
                    val r0 = Integer.valueOf(subArray[0])
                    val r1 = Integer.valueOf(subArray[1])
                    for (d in devices) {
                        if (d.number > 8) {
                            d.open = readIfOpen(r0, d.number)
                            d.isTouch = false
                        } else {
                            d.open = readIfOpen(r1, d.number)
                            d.isTouch = false
                        }
                    }
                }
                DeviceType.Relay_32.value -> {//32
                    val subArray = action.split(",")
                    val r0 = Integer.valueOf(subArray[0])
                    val r1 = Integer.valueOf(subArray[1])
                    val r2 = Integer.valueOf(subArray[2])
                    val r3 = Integer.valueOf(subArray[3])
                    for (d in devices) {
                        if (d.number > 24) {
                            d.open = readIfOpen(r0, d.number)
                            d.isTouch = false
                        } else if (d.number > 16) {
                            d.open = readIfOpen(r1, d.number)
                            d.isTouch = false
                        } else if (d.number > 8) {
                            d.open = readIfOpen(r2, d.number)
                            d.isTouch = false
                        } else {
                            d.open = readIfOpen(r3, d.number)
                            d.isTouch = false
                        }
                    }
                }
                DeviceType.Dimmer_8.value -> {
                    for ((index, d) in devices.withIndex()) {
                        d.isTouch = true
                        val subArray = action.split(",")
                        if (d.number >= 0 && d.number < subArray.size) {
                            d.state = subArray[index]
                        }
                    }
                }
            }
            list.addAll(devices)
        }
        adapter?.notifyDataSetChanged()
    }


    private fun readIfOpen(number: Int, index: Int): Boolean {
        var tempIndex = index
        when (index) {
            in 1..8 -> {
                tempIndex = index
            }
            in 9..16 -> {
                tempIndex = index - 8
            }
            in 17..24 -> {
                tempIndex = index - 16
            }
            in 25..32 -> {
                tempIndex = index - 24
            }
        }
        var result = false
        when (tempIndex) {
            1 -> {
                result = (number and 0b00000001) == 0b00000001
            }
            2 -> {
                result = (number and 0b00000010) == 0b00000010
            }
            3 -> {
                result = (number and 0b00000100) == 0b00000100
            }
            4 -> {
                result = (number and 0b00001000) == 0b00001000
            }
            5 -> {
                result = (number and 0b00010000) == 0b00010000
            }
            6 -> {
                result = (number and 0b00100000) == 0b00100000
            }
            7 -> {
                result = (number and 0b01000000) == 0b01000000
            }
            8 -> {
                result = (number and 0b10000000) == 0b10000000
            }
        }
        return result
    }

    private fun actionCode(deviceChannelList: List<Device>, deviceType: Int): String {
        var r = ""
        when (deviceType) {
            DeviceType.Relay_2.value,
            DeviceType.Relay_4.value,
            DeviceType.Relay_8.value -> {
                var result = 0
                for ((index, device) in deviceChannelList.withIndex()) {
                    val state = if (device.open) 1 else 0
                    result += state * pow(index)
                }
                r = "$result"
            }
            DeviceType.Relay_16.value -> {
                var index = 0
                var result1 = 0
                var result2 = 0
                for (device in deviceChannelList) {
                    val state = if (device.open) 1 else 0
                    if (index < 8) {
                        result1 += state * pow(index++)
                    } else {
                        result2 += state * pow(index++ - 8)
                    }
                }
                r = "${result2},${result1}"
            }
            DeviceType.Relay_32.value -> {
                var index = 0
                var result1 = 0
                var result2 = 0
                var result3 = 0
                var result4 = 0
                for (device in deviceChannelList) {
                    var state = if (device.open) 1 else 0
                    if (index < 8) {
                        result1 += state * pow(index++)
                    } else if (index < 16) {
                        result2 += state * pow(index++ - 8)
                    } else if (index < 24) {
                        result3 += state * pow(index++ - 16)
                    } else {
                        result4 += state * pow(index++ - 24)
                    }
                }
                r = "${result4},${result3},${result2},${result1}"
            }
            DeviceType.Dimmer_8.value -> {
                var result = ""
                val deviceChannelSortList = ArrayList<Device>()
                deviceChannelSortList.addAll(deviceChannelList)
                Collections.sort(deviceChannelSortList, object : Comparator<Device> {
                    override fun compare(o1: Device?, o2: Device?): Int {
                        if (o1 != null && o2 != null) {
                            return o1.number - o2.number
                        }
                        return 0
                    }
                })
                for ((index, device) in deviceChannelSortList.withIndex()) {
                    if (device.type == 1) continue
                    if (index != 0) {
                        result += ",${device.state}"
                    } else {
                        result += device.state
                    }
                }
                r = result
            }
        }
        return r
    }

    private fun pow(number: Int): Int {
        return Math.pow(2 * 1.0, number * 1.0).toInt()
    }

    /**
     * 添加设备时选中的设备
     */
    var addressSelected: IPAddress? = null

    /**
     * 显示添加设备Dialog
     */
    private fun showAddDeviceDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_scene_add, null)
        val spinner = view.findViewById<AppCompatSpinner>(R.id.device)
        val address = KBoxDatabase.getInstance(this).addressDao.allAddress;
        val it = address.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (next.deviceType == DeviceType.COLB.value) {
                it.remove()
            }
        }
        val items = Array<String>(address.size) { i -> address[i].toString() }
        val itemsAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.adapter = itemsAdapter
        if (address.size > 0) {
            addressSelected = address[0]
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                addressSelected = address[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        val dialog = AlertDialog.Builder(this)
            .setCancelable(true)
            .setView(view)
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                addressSelected = null
            }
            .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
                if (addressSelected != null) {
                    addDevice(addressSelected!!)
                }
            }
            .create()
        dialog.show()
    }

    private fun addDevice(address: IPAddress) {
        if (scene?.address?.contains(address.toString()) == true) {
            showToast(getString(R.string.add_already))
            return
        }
        val devices = KBoxDatabase.getInstance(this).deviceDao.getDevice(address.id)
        val allAddress = KBoxDatabase.getInstance(this).addressDao.allAddress
        for (d in devices) {
            for (a in allAddress) {
                if (a.id == d.addressId) {
                    d.address = a
                }
            }

            d.isTouch = d.address.deviceType == DeviceType.Dimmer_8.value
            d.open = false
        }
        list.addAll(devices)

        if (scene?.ids.isNullOrEmpty()) {
            scene?.ids = "${address.id}"
            scene?.address = address.toString()
            scene?.length = "${address.deviceType % 10000}"
            scene?.action = getDefaultAction(address)
        } else {
            scene?.ids = "${scene?.ids}_${address.id}"
            scene?.address = "${scene?.address}_${address}"
            scene?.length = "${scene?.length}_${address.deviceType % 10000}"
            scene?.action = "${scene?.action}_${getDefaultAction(address)}"
        }
        adapter?.notifyDataSetChanged()
    }

    private fun getDefaultAction(address: IPAddress): String {
        var result = "0"
        when (address.deviceType) {
            DeviceType.Relay_2.value,
            DeviceType.Relay_4.value,
            DeviceType.Relay_8.value -> result = "0"
            DeviceType.Relay_16.value -> result = "0,0"
            DeviceType.Relay_32.value -> result = "0,0,0,0"
            DeviceType.Dimmer_8.value -> {
                result = ""
                for ((index, device) in list.withIndex()) {
                    if (device.addressId == address.id && device.type == 0) {
                        if (index == 0) {
                            result += device.state
                        } else {
                            result += ",${device.state}"
                        }
                    }
                }
            }
        }
        return result;
    }

}

