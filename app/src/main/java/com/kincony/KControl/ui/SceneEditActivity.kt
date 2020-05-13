package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
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
import com.kincony.KControl.ui.adapter.DeviceAdapter
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.utils.ImageLoader
import kotlinx.android.synthetic.main.activity_scene_edit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SceneEditActivity : BaseActivity() {
    private var iIcon: Int = 0
    var scene: Scene? = null
    var adapter: DeviceAdapter? = null
    var list = ArrayList<Device>()
    var addressSelected: IPAddress? = null

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

        var deviceDialog: AlertDialog? = null
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
            if (deviceDialog == null) {
                deviceDialog = getLoadingDialog(this)
            }
            deviceDialog?.show()
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
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.mSwitchClick -> {
                    var device = list[position]
                    changeState(device, position)
                }
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
        var ids = scene!!.ids.split("_")
        var actions = scene!!.action.split("_")
        var lengths = scene!!.length.split("_")
        for ((index, i) in ids.withIndex()) {
            var devices = KBoxDatabase.getInstance(this).deviceDao.getDevice(i.toInt())
            var length = lengths[index].toInt()
            var action = actions[index]
            var subArray = action.split(",")
            when (length) {
                in 2..8 -> {//2,4,8
                    var r0 = Integer.valueOf(subArray[0])
                    for (d in devices) {
                        d.open = readIfOpen(r0, d.number)
                        d.isTouch = false
                    }
                }
                16 -> {//16
                    var r0 = Integer.valueOf(subArray[0])
                    var r1 = Integer.valueOf(subArray[1])
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
                32 -> {//32
                    var r0 = Integer.valueOf(subArray[0])
                    var r1 = Integer.valueOf(subArray[1])
                    var r2 = Integer.valueOf(subArray[2])
                    var r3 = Integer.valueOf(subArray[3])
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

    private fun changeState(device: Device, position: Int) {
        device.open = !device.open
        adapter?.notifyItemChanged(position)
        var lengthArray = scene!!.length.split("_")
        var start = 0
        var result = ""
        for (i in lengthArray) {
            var length = i.toInt()
            var temp = list.subList(start, start + length)
            start += length
            if (result.isNullOrEmpty()) {
                result = "${actionCode(temp, length)}"
            } else {
                result = "${result}_${actionCode(temp, length)}"
            }
        }
        scene!!.action = result
    }

    private fun actionCode(array: List<Device>, mode: Int): String {
        var r = ""
        when (mode) {
            in 2..8 -> {
                var result = 0
                for ((index, i) in array.withIndex()) {
                    var state = if (i.open) 1 else 0
                    result += state * pow(index)
                }
                r = "$result"
            }
            16 -> {
                var index = 0
                var result1 = 0
                var result2 = 0
                for (i in array) {
                    var state = if (i.open) 1 else 0
                    if (index < 8) {
                        result1 += state * pow(index++)
                    } else {
                        result2 += state * pow(index++ - 8)
                    }
                }
                r = "${result2},${result1}"
            }
            32 -> {
                var index = 0
                var result1 = 0
                var result2 = 0
                var result3 = 0
                var result4 = 0
                for (i in array) {
                    var state = if (i.open) 1 else 0
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
        }
        return r
    }

    private fun pow(number: Int): Int {
        return Math.pow(2 * 1.0, number * 1.0).toInt()
    }


    private fun getLoadingDialog(context: Context?): AlertDialog? {
        var dialog: AlertDialog? = null
        if (context != null) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_scene_add, null)
            var device = view.findViewById<AppCompatSpinner>(R.id.device)
            var address = KBoxDatabase.getInstance(this).addressDao.allAddress;
            var items = Array<String>(address.size) { i -> address[i].toString() }
            var itemsAdapter =
                ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
            device.adapter = itemsAdapter
            if (address.size > 0) {
                addressSelected = address[0]
            }

            device.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            dialog = AlertDialog.Builder(context)
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
        }
        return dialog
    }

    private fun addDevice(address: IPAddress) {
        if (scene?.address?.contains(address.toString()) == true) {
            showToast(getString(R.string.add_already))
            return
        }
        var devices = KBoxDatabase.getInstance(this).deviceDao.getDevice(address.id)
        for (i in devices) {
            i.open = false
            i.isTouch = false
        }
        list.addAll(devices)

        if (scene?.ids.isNullOrEmpty()) {
            scene?.ids = "${address.id}"
            scene?.address = address.toString()
            scene?.length = "${address.type}"
            scene?.action = "${getDefaultAction(address.type)}"
        } else {
            scene?.ids = "${scene?.ids}_${address.id}"
            scene?.address = "${scene?.address}_${address}"
            scene?.length = "${scene?.length}_${address.type}"
            scene?.action = "${scene?.action}_${getDefaultAction(address.type)}"
        }
        adapter?.notifyDataSetChanged()
    }

    private fun getDefaultAction(type: Int): String {
        var result = "0"
        when (type) {
            in 2..8 -> {
                result = "0"
            }
            16 -> {
                result = "0,0"
            }
            32 -> {
                result = "0,0,0,0"
            }
        }
        return result;
    }

}

