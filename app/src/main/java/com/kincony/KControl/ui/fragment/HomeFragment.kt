package com.kincony.KControl.ui.fragment

import android.content.Context
import android.os.Handler
import android.view.*
import android.widget.AdapterView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kincony.KControl.R
import com.kincony.KControl.net.KBoxCommand
import com.kincony.KControl.net.data.*
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.net.internal.Client
import com.kincony.KControl.net.internal.Request
import com.kincony.KControl.net.internal.interfaces.Callback
import com.kincony.KControl.net.internal.interfaces.ResponseBody
import com.kincony.KControl.ui.DeviceEditActivity
import com.kincony.KControl.ui.DeviceInPutEditActivity
import com.kincony.KControl.ui.adapter.DeviceAdapter
import com.kincony.KControl.ui.adapter.HomeSceneAdapter
import com.kincony.KControl.ui.base.BaseFragment
import com.kincony.KControl.utils.KBoxStateRead
import kotlinx.android.synthetic.main.activity_device_edit.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class HomeFragment : BaseFragment() {
    private var optionsInt = 2
    private var deviceList = ArrayList<Device>()
    private var adapter: DeviceAdapter? = null

    private var sceneList = ArrayList<Scene>()
    private var sceneAdapter: HomeSceneAdapter? = null
    private var isSortMode = false

    override fun getLayoutId() = R.layout.fragment_home

    /**
     * 循环获取设备输入状态
     */
//    var mUpdateHandler: Handler = Handler()
//    var mUpdateRunnable: Runnable = object: Runnable {
//
//        override fun run() {
//
//            getStateList()
//            mUpdateHandler.postDelayed(this,3000)
//
//        }
//
//    }

    override fun initView() {
        refresh.setOnRefreshListener {
            readState(true)
        }

        adapter = DeviceAdapter()
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        adapter?.setNewInstance(deviceList)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.mSwitchClick -> {
                    var device = deviceList[position]
                    changeState(device, device.open, position)
                }
            }
        }

        adapter?.setOnItemClickListener { adapter, view, position ->
            var device = deviceList[position]

            if (device.type == 1) {
                DeviceInPutEditActivity.start(
                    context,
                    device.deviceId,
                    device.name,
                    device.icon,
                    device.itemName
                )
            } else {
                DeviceEditActivity.start(
                    context,
                    device.deviceId,
                    device.name,
                    device.icon,
                    device.isTouch,
                    device.iconTouch
                )
            }

            false
        }
        adapter?.callback = { device, isOpen ->
            onLongClick(device, isOpen)
        }

        sceneAdapter = HomeSceneAdapter()
        scene.layoutManager = GridLayoutManager(context, 4)
        scene.adapter = sceneAdapter
        sceneAdapter?.setNewInstance(sceneList)
        sceneAdapter?.setOnItemClickListener { adapter, view, position ->
            var scene = sceneList[position]
            if (!scene.isTouch) {
                onScene(scene)
            }
        }
        sceneAdapter?.callback = { scene, b ->
            if (scene.isTouch) {
                if (b) {
                    onScene(scene)
                } else {
                    onReScene(scene)
                }
            }
        }

        var itemTouchHelper = ItemTouchHelper(HelperCallback(adapter, deviceList))
        sortButton.setOnClickListener {
            isSortMode = !isSortMode
            adapter?.isSort = isSortMode
            adapter?.notifyDataSetChanged()
            if (isSortMode) {
                itemTouchHelper.attachToRecyclerView(recycler)
                sortButton.isActivated = true
                refresh.isEnabled = false
            } else {
                itemTouchHelper.attachToRecyclerView(null)
                sortButton.isActivated = false
                refresh.isEnabled = true
            }
        }
        add.setOnClickListener {
            var dialog = getLoadingDialog(context)
            dialog?.show()
        }

        EventBus.getDefault().register(this)
        loadDevice()
        loadScene()


        /**
         * 循环获取设备输入状态
         * 启动
         */
//        mUpdateHandler.postDelayed(mUpdateRunnable,1000)

    }

    @Subscribe
    public fun refreshDevice(event: RefreshAddressEvent) {
        loadDevice()
    }


    @Subscribe
    public fun refreshScene(event: RefreshSceneEvent?) {
        loadScene()
    }


    @Subscribe
    public fun setDeviceName(event: DeviceChange) {
        var d: Device? = null
        for (i in deviceList) {
            if (i.deviceId == event.id) {
                d = i;
            }
        }
        d?.name = event.name
        d?.icon = event.icon
        d?.isTouch = event.mode
        d?.iconTouch = event.iconTouch
        if (d != null) {
            KBoxDatabase.getInstance(context).deviceDao.updateDevice(d)
            adapter?.notifyDataSetChanged()
        }
    }

    @Subscribe
    public fun setDeviceInPutName(event: DeviceInPutChange) {
        var d: Device? = null
        for (i in deviceList) {
            if (i.deviceId == event.id) {
                d = i;
            }
        }
        d?.name = event.name
        d?.itemName = event.itemName
        d?.icon = event.icon

        if (d != null) {
            KBoxDatabase.getInstance(context).deviceDao.updateDevice(d)
            adapter?.notifyDataSetChanged()
        }
    }

    @Subscribe
    public fun updateDeviceUI(event: UpdateDeviceUI) {
        if (event.flag) {
            adapter?.notifyDataSetChanged()
            refresh.isRefreshing = false
        }
    }

    @Subscribe
    public fun getALARM(alarmEvent: AlarmEvent) {
        getStateList()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        /**
         * 循环获取设备输入状态
         * 停止
         */
//        mUpdateHandler.removeCallbacks(mUpdateRunnable)
    }

    private fun loadScene() {
        sceneList.clear()
        sceneList.addAll(KBoxDatabase.getInstance(context).sceneDao.allScene)
        sceneAdapter?.notifyDataSetChanged()
    }

    /**
     * 从数据库读取设备
     */
    private fun loadDevice() {
        deviceList.clear()
        adapter?.notifyDataSetChanged()

        var devices = KBoxDatabase.getInstance(context).deviceDao.allDevice
        var address = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (d in devices) {
            for (a in address) {
                if (a.id == d.addressId) {
                    d.address = a
                }
            }
        }

        deviceList.addAll(devices)
        adapter?.notifyDataSetChanged()
        refresh.isRefreshing = true
        readState(true)
    }

    /**
     * 插入设备
     * model:2,4,8,16,32
     */
    private fun addDevice(address: IPAddress, model: Int) {
        var allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
        var t: IPAddress? = null
        for (a in allAddress) {
            if (a == address) {
                t = a
            }
        }
        if (t != null) {
            showToast(resources.getString(R.string.add_already))
            return
        }

        KBoxDatabase.getInstance(context).addressDao.insertAddress(address)
        var temp = KBoxDatabase.getInstance(context).addressDao.getAddress(address.ip, address.port)

        var size = deviceList.size
        var index = 0
        var list = ArrayList<Device>()
        for (i in 1..model) {
            list.add(Device(temp, i, size + index++))
        }
        list.add(Device(temp, model + 1, size + index++, 1))
        KBoxDatabase.getInstance(context).deviceDao.insertDevice(list)

        var readList = KBoxDatabase.getInstance(context).deviceDao.allDevice
        var address = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (d in readList) {
            for (a in address) {
                if (a.id == d.addressId) {
                    d.address = a
                }
            }
        }

        deviceList.clear()
        deviceList.addAll(readList)
        adapter?.notifyDataSetChanged()
    }

    override fun onPause() {
        changeDate()
        super.onPause()
    }

    private fun changeDate() {
        var index = 0
        for (i in deviceList) {
            i.index = index++
        }
        KBoxDatabase.getInstance(context).deviceDao.updateDevice(deviceList)
    }

    private fun onScene(scene: Scene) {

        if (scene.address.isNullOrEmpty()) return
        var address = scene.address.split("_")
        var actions = scene.action.split("_")
        var reAction = ""
        var dList = ArrayList<ArrayList<Device>>()
        for (item in address) {
            var dsTemp = ArrayList<Device>()
            for (ds in deviceList) {
                if (item == ds.address.toString()) {
                    dsTemp.add(ds)
                }
            }
            dList.add(dsTemp)
        }
        for ((index, item) in address.withIndex()) {
            var dsTemp: ArrayList<Device>? = null
            for (ds in dList) {
                if (!ds.isNullOrEmpty()) {
                    if (ds[0].address.toString() == item) {
                        dsTemp = ds
                        break
                    }
                }
            }
            if (reAction.isNullOrEmpty()) {
                if (!dsTemp.isNullOrEmpty()) {
                    reAction = actionCode(dsTemp!!, dsTemp[0].address.type)
                }
            } else {
                if (!dsTemp.isNullOrEmpty()) {
                    reAction = reAction + "_" + actionCode(dsTemp!!, dsTemp[0].address.type)
                }
            }
            var a = IPAddress(item.split(":")[0], item.split(":")[1].toInt())
            Client.newCall(Request.obtain(a.ip, a.port, KBoxCommand.setAllState(actions[index])))
                .enqueue(object : Callback {
                    override fun onStart() {
                        showLoading()
                    }

                    override fun onFailure(e: Exception) {
                        showToast(e?.message)
                        closeLoading()
                    }

                    override fun onResponse(response: ResponseBody?) {
                        if (response is KBoxState) {
                            KBoxStateRead.readAllState(response, a, deviceList)
                        }
                        adapter?.notifyDataSetChanged()
                        closeLoading()
                    }

                })
        }
        scene.reAction = reAction
    }

    private fun onReScene(scene: Scene) {
        if (scene.address.isNullOrEmpty() || scene.reAction.isNullOrEmpty()) return
        var address = scene.address.split("_")
        var reActions = scene.reAction.split("_")
        for ((index, item) in address.withIndex()) {
            var a = IPAddress(item.split(":")[0], item.split(":")[1].toInt())
            Client.newCall(Request.obtain(a.ip, a.port, KBoxCommand.setAllState(reActions[index])))
                .enqueue(object : Callback {
                    override fun onStart() {
                        showLoading()
                    }

                    override fun onFailure(e: Exception) {
                        showToast(e?.message)
                        closeLoading()
                    }

                    override fun onResponse(response: ResponseBody?) {
                        if (response is KBoxState) {
                            KBoxStateRead.readAllState(response, a, deviceList)
                        }
                        adapter?.notifyDataSetChanged()
                        closeLoading()
                    }

                })
        }
        scene.reAction = ""
    }


    private fun actionCode(array: ArrayList<Device>, mode: Int): String {
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

    private fun readState(flag: Boolean) {
        var allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (address in allAddress) {
            Client.newCall(Request.obtain(address.ip, address.port, KBoxCommand.readAllState()))
                .enqueue(object : Callback {
                    override fun onStart() {
                        if (flag) refresh.isRefreshing = true
                    }

                    override fun onFailure(e: Exception) {
                        showToast(e.message)
                        refresh.isRefreshing = false
                    }

                    override fun onResponse(response: ResponseBody?) {
                        if (response is KBoxState) {
                            KBoxStateRead.readAllState(response, address, deviceList)
                        }

                        EventBus.getDefault().post(UpdateDeviceUI(true))

//                        thread(start = true) {
                            if(flag) {
                                getStateList()
                            }
//                        }

                    }
                })

        }
        if (allAddress.size == 0) {
            refresh.isRefreshing = false
        }


    }

    /**
     * 获取设备输入状态
     */
    private fun getStateList() {
        var allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (address in allAddress) {
            Client.newCall(Request.obtain(address.ip, address.port, KBoxCommand.getState()))
                .enqueue(object : Callback {
                    override fun onStart() {

                    }

                    override fun onFailure(e: Exception) {

                    }

                    override fun onResponse(response: ResponseBody?) {
                        if (response is KBoxState) {
                            KBoxStateRead.readAllState(response, address, deviceList)
                        }

                        EventBus.getDefault().post(UpdateDeviceUI(true))

                    }
                })

        }
    }

    private fun onLongClick(device: Device, open: Boolean) {
        var address = device.address
        Client.newCall(
            Request.obtain(
                address.ip,
                address.port,
                KBoxCommand.setState(device.number, if (open) 1 else 0)
            )
        )
            .enqueue(object : Callback {
                override fun onStart() {
                }

                override fun onFailure(e: Exception) {
                    showToast(e.message)
                }

                override fun onResponse(response: ResponseBody?) {
                    if (response is KBoxState) {
                        KBoxStateRead.readOneState(response, device)
                    }
                }
            })
    }

    private fun changeState(device: Device, open: Boolean, position: Int) {
        var command = KBoxCommand.setState(device.number, if (open) 0 else 1)
        var address = device.address
        Client.newCall(
            Request.obtain(
                address.ip,
                address.port,
                command
            )
        ).enqueue(object : Callback {
            override fun onStart() {
//                showLoading()
            }

            override fun onFailure(e: Exception) {
                showToast(e.message)
//                closeLoading()
            }

            override fun onResponse(response: ResponseBody?) {
                if (response is KBoxState) {
                    KBoxStateRead.readOneState(response, device)
                }
                adapter?.notifyItemChanged(position)
                closeLoading()

                //读取按钮状态，循环三次每次间隔500ms
                thread(start = true) {
                    for (i in 1..2) {
                        Thread.sleep(100)
                        readState(false)
                    }
                }
            }

        })
    }

    private fun getLoadingDialog(context: Context?): AlertDialog? {
        var dialog: AlertDialog? = null
        if (context != null) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_add, null)
            var ip = view.findViewById<EditText>(R.id.ip)
            var port = view.findViewById<EditText>(R.id.port)
            var model = view.findViewById<AppCompatSpinner>(R.id.model)
            model.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> {
                            optionsInt = 2
                        }
                        1 -> {
                            optionsInt = 4
                        }
                        2 -> {
                            optionsInt = 8
                        }
                        3 -> {
                            optionsInt = 16
                        }
                        4 -> {
                            optionsInt = 32
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
            dialog = AlertDialog.Builder(context)
                .setCancelable(true)
                .setView(view)
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->

                }
                .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
//                    if (!IPUtils.isIp(ip.text.toString())) {
//                        showToast(resources.getString(R.string.ip_alert))
//                        return@setPositiveButton
//                    }
                    if (port.text.toString().isEmpty()) {
                        showToast(resources.getString(R.string.port_alert))
                        return@setPositiveButton
                    }
                    addDevice(
                        IPAddress(
                            ip.text.toString(),
                            Integer.decode(port.text.toString()),
                            optionsInt
                        ), optionsInt
                    )
                }
                .create()
        }
        return dialog
    }

    inner class HelperCallback(var adapter: DeviceAdapter?, var list: ArrayList<Device>) :
        ItemTouchHelper.Callback() {
        override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            var swipFlag = 0;
            var dragflag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragflag, swipFlag)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition);
            Collections.swap(list, viewHolder.adapterPosition, target.adapterPosition);
            return true;
        }


        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

    }

}
