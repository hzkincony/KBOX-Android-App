package com.kincony.KControl.ui.fragment

import android.view.LayoutInflater
import android.view.View
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
import com.kincony.KControl.net.usecase.KDimmerReadAllDimmerUseCase
import com.kincony.KControl.net.usecase.KDimmerSetAllDimmerUseCase
import com.kincony.KControl.net.usecase.KDimmerSetOneDimmerUseCase
import com.kincony.KControl.ui.DeviceEditActivity
import com.kincony.KControl.ui.DeviceInPutEditActivity
import com.kincony.KControl.ui.adapter.HomeSceneAdapter
import com.kincony.KControl.ui.adapter.device.DimmerDeviceConvert
import com.kincony.KControl.ui.adapter.device.NewDeviceAdapter
import com.kincony.KControl.ui.base.BaseFragment
import com.kincony.KControl.utils.KBoxStateRead
import com.kincony.KControl.utils.LogUtils
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class HomeFragment : BaseFragment() {
    private var deviceList = ArrayList<Device>()
    private var adapter: NewDeviceAdapter? = null

    private var sceneList = ArrayList<Scene>()
    private var sceneAdapter: HomeSceneAdapter? = null
    private var isSortMode = false

    private var lastSceneMillis: Long = 0

    override fun getLayoutId() = R.layout.fragment_home

    override fun initView() {
        // 下拉刷新
        refresh.setOnRefreshListener {
            readState(true)
        }

        // 设备列表
        adapter = NewDeviceAdapter()
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        adapter?.setNewInstance(deviceList)
        // 继电器设备点击事件
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.mSwitchClick -> {
                    var device = deviceList[position]
                    changeKBoxState(device, device.open, position)
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
        adapter?.relayDeviceConvert?.callback = { device, isOpen ->
            var address = device.address
            Client.newCall(
                Request.obtain(
                    address.ip,
                    address.port,
                    KBoxCommand.setState(device.number, if (isOpen) 1 else 0)
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
        // 调光器点击事件
        adapter?.dimmerDeviceConvert?.callback = object : DimmerDeviceConvert.Callback {
            override fun onAddClick(position: Int, device: Device) {
                setDimmerState(position, device)
            }

            override fun onSubClick(position: Int, device: Device) {
                setDimmerState(position, device)
            }

            override fun onProgressChange(position: Int, device: Device, progress: Int) {
                setDimmerState(position, device)
            }
        }

        // 情景模式列表
        sceneAdapter = HomeSceneAdapter()
        scene.layoutManager = GridLayoutManager(context, 4)
        scene.adapter = sceneAdapter
        sceneAdapter?.setNewInstance(sceneList)
        sceneAdapter?.setOnItemClickListener { adapter, view, position ->
            LogUtils.d("setOnItemClickListener")
            val scene = sceneList[position]
            if (!scene.isTouch) {
                onScene(scene)
            }
        }
        sceneAdapter?.callback = { scene, b ->
            LogUtils.d("setOnItemClickListener callback")
            val interval = System.currentTimeMillis() - lastSceneMillis
            if (interval > 250) {
                lastSceneMillis = System.currentTimeMillis()
                if (scene.isTouch) {
                    if (b) {
                        onScene(scene)
                    } else {
                        onReScene(scene)
                    }
                }
            } else if (!b) {
                recycler.postDelayed({
                    lastSceneMillis = System.currentTimeMillis()
                    onReScene(scene)
                }, 250)
            }
        }
        // 情景模式拖拽事件
        val itemTouchHelper = ItemTouchHelper(HelperCallback(adapter, deviceList))
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

        // 添加设备点击事件
        add.setOnClickListener {
            showAddDeviceDialog()
        }

        // 注册event事件
        EventBus.getDefault().register(this)

        // 加载设备数据
        loadDevice()
        // 加载
        loadScene()
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
            adapter?.notifyItemRangeChanged(0, deviceList.size)
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
    }

    /**
     * 从数据库读取情景模式
     */
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

        val devices = KBoxDatabase.getInstance(context).deviceDao.allDevice
        val address = KBoxDatabase.getInstance(context).addressDao.allAddress
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
     *
     * @param address ip address
     * @param model DeviceType
     */
    private fun addDevice(address: IPAddress) {
        val allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
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
        val temp = KBoxDatabase.getInstance(context).addressDao.getAddress(address.ip, address.port)

        val size = deviceList.size
        var index = 0
        val list = ArrayList<Device>()

        val deviceChannelCount = address.type % 10000;

        for (i in 1..deviceChannelCount) {
            val channelDevice = Device(temp, i, size + index++)
            if (DeviceType.Dimmer_8.value == channelDevice.address.type) {
                channelDevice.state = "0"
            }
            list.add(channelDevice)
        }

        val itemName: String?
        when (address.type) {
            DeviceType.Relay_2.value -> {
                itemName = getInPutState(0)
            }
            DeviceType.Relay_4.value -> {
                itemName = getInPutState(8)
            }
            DeviceType.Relay_8.value -> {
                itemName = getInPutState(8)
            }
            DeviceType.Relay_16.value -> {
                itemName = getInPutState(8)
            }
            DeviceType.Relay_32.value -> {
                itemName = getInPutState(6)
            }
            DeviceType.Dimmer_8.value -> {
                itemName = getInPutState(8)
            }
            else -> {
                itemName = getInPutState(0)
            }
        }

        val allChannelDevice = Device(
            temp,
            deviceChannelCount + 1,
            size + index,
            1,
            deviceChannelCount,
            itemName
        )
        if (DeviceType.Dimmer_8.value == allChannelDevice.address.type) {
            allChannelDevice.state = "0,0,0,0,0,0,0,0"
        }
        list.add(allChannelDevice)
        KBoxDatabase.getInstance(context).deviceDao.insertDevice(list)

        val readList = KBoxDatabase.getInstance(context).deviceDao.allDevice
        val addressList = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (d in readList) {
            for (a in addressList) {
                if (a.id == d.addressId) {
                    d.address = a
                }
            }
        }

        deviceList.clear()
        deviceList.addAll(readList)
        adapter?.notifyDataSetChanged()
    }

    fun getInPutState(num: Int): String? {
        var itemName: String? = null

        if (num > 0) {
            for (i in 1..num) {
                if (itemName == null) itemName = "";
                itemName += resources.getString(R.string.InPutName) + i + ";";
            }

            if (itemName != null) {
                itemName = itemName.substring(0, itemName.length - 1);
            }
        }

        return itemName
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
        val sceneAddress = scene.address.split("_")
        val sceneActions = scene.action.split("_")
        var reAction = ""
        val dList = ArrayList<ArrayList<Device>>()
        for (item in sceneAddress) {
            val dsTemp = ArrayList<Device>()
            for (ds in deviceList) {
                if (item == ds.address.toString()) {
                    dsTemp.add(ds)
                }
            }
            dList.add(dsTemp)
        }
        for ((index, sceneAddressItem) in sceneAddress.withIndex()) {
            var dsTemp: ArrayList<Device>? = null
            for (ds in dList) {
                if (!ds.isNullOrEmpty()) {
                    if (ds[0].address.toString() == sceneAddressItem) {
                        dsTemp = ds
                        break
                    }
                }
            }

            if (!dsTemp.isNullOrEmpty()) {
                if (reAction.isEmpty()) {
                    reAction = actionCode(dsTemp, dsTemp[0].address.type)
                } else {
                    reAction = reAction + "_" + actionCode(dsTemp, dsTemp[0].address.type)
                }

                if (dsTemp[0].address.type == DeviceType.Dimmer_8.value) {
                    KDimmerSetAllDimmerUseCase().execute(
                        dsTemp[0].address,
                        sceneActions[index].split(","),
                        object : Callback {
                            override fun onStart() {
                            }

                            override fun onFailure(e: Exception) {
                                showToast(e.message)
                            }

                            override fun onResponse(response: ResponseBody?) {
                                if (response is KBoxState) {
                                    getAllDimmerState(dsTemp[0].address)
                                }
                            }
                        })
                } else {
                    val request = Request.obtain(
                        dsTemp[0].address.ip,
                        dsTemp[0].address.port,
                        KBoxCommand.setAllState(sceneActions[index])
                    )
                    Client.newCall(request).enqueue(object : Callback {
                        override fun onStart() {
                            showLoading()
                        }

                        override fun onFailure(e: Exception) {
                            showToast(e.message)
                            closeLoading()
                        }

                        override fun onResponse(response: ResponseBody?) {
                            if (response is KBoxState) {
                                KBoxStateRead.readAllState(response, dsTemp[0].address, deviceList)
                            }
                            adapter?.notifyDataSetChanged()
                            closeLoading()
                        }
                    })
                }
            }
        }
        scene.reAction = reAction
    }

    private fun onReScene(scene: Scene) {
        if (scene.address.isNullOrEmpty() || scene.reAction.isNullOrEmpty()) return
        val sceneAddress = scene.address.split("_")
        val sceneReActions = scene.reAction.split("_")

        for ((index, item) in sceneAddress.withIndex()) {
            val addressIp = item.split(":")[0]
            var address: IPAddress? = null
            for (d in deviceList) {
                if (d.address.ip == addressIp) {
                    address = d.address
                    break
                }
            }
            if (address != null) {
                if (address.type == DeviceType.Dimmer_8.value) {
                    KDimmerSetAllDimmerUseCase().execute(
                        address,
                        sceneReActions[index].split(","),
                        object : Callback {
                            override fun onStart() {

                            }

                            override fun onFailure(e: Exception) {
                                showToast(e.message)
                            }

                            override fun onResponse(response: ResponseBody?) {
                                if (response is KBoxState) {
                                    getAllDimmerState(address)
                                }
                            }

                        })
                } else {
                    val request = Request.obtain(
                        address.ip,
                        address.port,
                        KBoxCommand.setAllState(sceneReActions[index])
                    );
                    Client.newCall(request).enqueue(object : Callback {
                        override fun onStart() {
                            showLoading()
                        }

                        override fun onFailure(e: Exception) {
                            showToast(e.message)
                            closeLoading()
                        }

                        override fun onResponse(response: ResponseBody?) {
                            if (response is KBoxState) {
                                KBoxStateRead.readAllState(response, address, deviceList)
                            }
                            adapter?.notifyDataSetChanged()
                            closeLoading()
                        }

                    })
                }
            }
        }
        scene.reAction = ""
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

    private fun readState(flag: Boolean) {
        var allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (address in allAddress) {
            if (address.type == DeviceType.Dimmer_8.value) {
                getAllDimmerState(address)
                continue
            }
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

                        if (flag) {
                            getStateList()
                        }

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
        val allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (address in allAddress) {
            val request = Request.obtain(address.ip, address.port, KBoxCommand.getState())
            Client.newCall(request).enqueue(object : Callback {
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

    /**
     * 改变继电器状态
     */
    private fun changeKBoxState(device: Device, open: Boolean, position: Int) {
        var command = KBoxCommand.setState(device.number, if (open) 0 else 1)
        var address = device.address
        val request = Request.obtain(address.ip, address.port, command)
        Client.newCall(request).enqueue(object : Callback {
            override fun onStart() {
            }

            override fun onFailure(e: Exception) {
                showToast(e.message)
            }

            override fun onResponse(response: ResponseBody?) {
                if (response is KBoxState) {
                    KBoxStateRead.readOneState(response, device)
                }
                adapter?.notifyItemChanged(position)
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

    /**
     * 设置调光器亮度
     */
    fun setDimmerState(position: Int, device: Device) {
        if (device.itemType != DeviceType.Dimmer_8.value || device.type != 0) return
        KDimmerSetOneDimmerUseCase().execute(
            device.address,
            device.number,
            device.state.toInt(),
            object : Callback {
                override fun onStart() {

                }

                override fun onFailure(e: Exception) {
                    showToast("${e}")
                }

                override fun onResponse(response: ResponseBody?) {
                    if (response is KBoxState && !response.succeed) {
                        // 调光器数据异常，重新读取
                        showToast(getString(R.string.data_exception_tips))
                        getAllDimmerState(device.address)
                    }
                }
            })
    }

    fun getAllDimmerState(address: IPAddress) {
        if (address.type != DeviceType.Dimmer_8.value) return
        KDimmerReadAllDimmerUseCase().execute(address, object : Callback {
            override fun onStart() {

            }

            override fun onFailure(e: Exception) {

            }

            override fun onResponse(response: ResponseBody?) {
                if (response is KBoxState && response.succeed) {
                    val stateArray = response.body().split(",")
                    for (temp in deviceList) {
                        if (temp.address.id == address.id) {
                            if (temp.number >= 1 && temp.number <= stateArray.size) {
                                temp.state = stateArray[temp.number - 1]
                            } else if (temp.number == stateArray.size + 1) {
                                temp.state = response.body()
                            }
                        }
                    }
                    updateDeviceUI(UpdateDeviceUI())
                }
            }
        })
    }

    /**
     * 当前添加设备弹窗选中的设备类型，默认选中2路继电器
     */
    private var selectedDeviceType: Int = DeviceType.Relay_2.value

    /**
     * 添加设备弹窗
     */
    private fun showAddDeviceDialog() {
        if (context != null) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_add, null)
            val ip = view.findViewById<EditText>(R.id.ip)
            val port = view.findViewById<EditText>(R.id.port)
            val model = view.findViewById<AppCompatSpinner>(R.id.model)
            selectedDeviceType = DeviceType.Relay_2.value
            model.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> selectedDeviceType = DeviceType.Relay_2.value
                        1 -> selectedDeviceType = DeviceType.Relay_4.value
                        2 -> selectedDeviceType = DeviceType.Relay_8.value
                        3 -> selectedDeviceType = DeviceType.Relay_16.value
                        4 -> selectedDeviceType = DeviceType.Relay_32.value
                        5 -> selectedDeviceType = DeviceType.Dimmer_8.value
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
            val dialog = AlertDialog.Builder(context!!)
                .setCancelable(true)
                .setView(view)
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->

                }
                .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
                    if (port.text.toString().isEmpty()) {
                        showToast(resources.getString(R.string.port_alert))
                        return@setPositiveButton
                    }
                    addDevice(
                        IPAddress(
                            ip.text.toString(),
                            Integer.decode(port.text.toString()),
                            selectedDeviceType
                        )
                    )
                }
                .create()
            dialog.show()
        }
    }

    /**
     * 情景模式拖拽ItemTouchHelper.Callback
     */
    inner class HelperCallback(var adapter: NewDeviceAdapter?, var list: ArrayList<Device>) :
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
