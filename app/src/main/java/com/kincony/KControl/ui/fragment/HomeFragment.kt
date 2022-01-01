package com.kincony.KControl.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kincony.KControl.R
import com.kincony.KControl.net.KBoxCommand
import com.kincony.KControl.net.KBoxMqttCommand
import com.kincony.KControl.net.KCOLBMqttCommand
import com.kincony.KControl.net.KDimmerMqttCommand
import com.kincony.KControl.net.data.*
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.net.internal.Client
import com.kincony.KControl.net.internal.Request
import com.kincony.KControl.net.internal.interfaces.Callback
import com.kincony.KControl.net.internal.interfaces.ResponseBody
import com.kincony.KControl.net.mqtt.MqttClientManager
import com.kincony.KControl.net.mqtt.callback.MqttSubscribeCallback
import com.kincony.KControl.net.usecase.KDimmerReadAllDimmerUseCase
import com.kincony.KControl.net.usecase.KDimmerSetAllDimmerUseCase
import com.kincony.KControl.net.usecase.KDimmerSetOneDimmerUseCase
import com.kincony.KControl.ui.AddDeviceActivity
import com.kincony.KControl.ui.DeviceEditActivity
import com.kincony.KControl.ui.DeviceInPutEditActivity
import com.kincony.KControl.ui.adapter.HomeSceneAdapter
import com.kincony.KControl.ui.adapter.device.CLOBDeviceConvert
import com.kincony.KControl.ui.adapter.device.DeviceAdapter
import com.kincony.KControl.ui.adapter.device.DimmerDeviceConvert
import com.kincony.KControl.ui.adapter.device.RelayDeviceConvert
import com.kincony.KControl.ui.base.BaseFragment
import com.kincony.KControl.ui.scan.ScanActivity
import com.kincony.KControl.utils.KBoxStateRead
import com.kincony.KControl.utils.ToastUtils
import com.kincony.KControl.utils.Tools
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment() {
    private var deviceList: ArrayList<Device>? = null
    private var adapter: DeviceAdapter? = null

    private var sceneList: ArrayList<Scene>? = null
    private var sceneAdapter: HomeSceneAdapter? = null
    private var isSortMode = false

    private var lastSceneMillis: Long = 0

    override fun getLayoutId() = R.layout.fragment_home

    override fun initView() {
        deviceList = ArrayList()
        sceneList = ArrayList()
        // 下拉刷新
        refresh.setOnRefreshListener {
            getAllDeviceState(true)
        }

        // 设备列表
        adapter = DeviceAdapter()
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        adapter?.setNewInstance(deviceList!!)
        // 继电器点击事件
        adapter?.relayDeviceConvert?.callback = object : RelayDeviceConvert.Callback {
            override fun onSceneActionDown(position: Int, device: Device) {
                changeKBoxState(device, true, position)
            }

            override fun onSceneActionUp(position: Int, device: Device) {
                changeKBoxState(device, false, position)
            }

            override fun onSwitchClick(position: Int, device: Device) {
                changeKBoxState(device, device.open, position)
            }

            override fun onEditClick(position: Int, device: Device) {
                DeviceEditActivity.start(
                    context,
                    device.deviceId,
                    device.name,
                    device.icon,
                    device.isTouch,
                    device.iconTouch
                )
            }

            override fun onInPutEditClick(position: Int, device: Device) {
                DeviceInPutEditActivity.start(
                    context,
                    device.deviceId,
                    device.name,
                    device.icon,
                    device.itemName
                )
            }

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

            override fun onEditClick(position: Int, device: Device) {
                DeviceEditActivity.start(
                    context,
                    device.deviceId,
                    device.name,
                    device.icon,
                    device.isTouch,
                    device.iconTouch
                )
            }

            override fun onInPutEditClick(position: Int, device: Device) {
                DeviceInPutEditActivity.start(
                    context,
                    device.deviceId,
                    device.name,
                    device.icon,
                    device.itemName
                )
            }
        }

        adapter?.clobDeviceConvert?.callback = object : CLOBDeviceConvert.Callback {
            override fun onInPutEditClick(position: Int, device: Device) {
                DeviceInPutEditActivity.start(
                    context,
                    device.deviceId,
                    device.name,
                    device.icon,
                    device.itemName
                )
            }

        }

        // 情景模式列表
        sceneAdapter = HomeSceneAdapter()
        scene.layoutManager = GridLayoutManager(context, 4)
        scene.adapter = sceneAdapter
        sceneAdapter?.setNewInstance(sceneList!!)
        sceneAdapter?.setOnItemClickListener { adapter, view, position ->
            val scene = sceneList!![position]
            if (!scene.isTouch) {
                onScene(scene)
            }
        }
        sceneAdapter?.callback = { scene, b ->
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
        val itemTouchHelper = ItemTouchHelper(HelperCallback(adapter, deviceList!!))
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

        // 扫描
        camera.setOnClickListener {
            if (activity == null) return@setOnClickListener
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        activity!!,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(activity!!, ScanActivity::class.java)
                    startActivityForResult(intent, 2000)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), 1000)
                }
            } else {
                val intent = Intent(activity!!, ScanActivity::class.java)
                startActivityForResult(intent, 2000)
            }
        }

        // 注册event事件
        EventBus.getDefault().register(this)

        // 加载设备数据
        loadDevice()
        // 加载
        loadScene()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2000 && resultCode == Activity.RESULT_OK) {
            val scanResult = data?.getStringExtra("scan_result")
            if (TextUtils.isEmpty(scanResult)) return
            val unzip = Tools.unzip(scanResult!!)
            if (!unzip.startsWith("{") || !unzip.endsWith("}")) {
                ToastUtils.showToastLong(getString(R.string.scan_qr_code_from_wrong))
                return
            }
            val shareQRCode = Tools.gson.fromJson<ShareQRCode>(unzip, ShareQRCode::class.java)
            if (shareQRCode.allAddress == null || shareQRCode.allAddress.size == 0) {
                ToastUtils.showToastLong(getString(R.string.scan_qr_code_from_wrong))
                return
            }

            val newAddressIDMap = HashMap<Int, Int>()
            val existsAddressList = ArrayList<IPAddress>()

            shareQRCode.allAddress!!.forEach {
                val temp = if (it.protocolType == ProtocolType.MQTT.value) {
                    KBoxDatabase.getInstance(context).addressDao.getMQTTAddress(
                        it.ip,
                        it.port,
                        it.deviceType,
                        it.username,
                        it.password,
                        it.deviceId
                    )
                } else {
                    KBoxDatabase.getInstance(context).addressDao.getTCPAddress(
                        it.ip,
                        it.port,
                        it.deviceType
                    )
                }
                if (temp == null) {
                    val oldId = it.id;
                    it.id = 0
                    val newId = KBoxDatabase.getInstance(activity).addressDao.insertAddress(it)
                    it.id = newId.toInt()
                    newAddressIDMap.put(oldId, it.id)
                } else {
                    existsAddressList.add(temp)
                }
            }

            val lastDevice = KBoxDatabase.getInstance(context).deviceDao.lastDevice
            var deviceCount =
                if (lastDevice != null && lastDevice.size == 1) lastDevice[0].index + 1 else 1
            shareQRCode.allDevice?.forEach {
                if (newAddressIDMap.get(it.addressId) != null) {
                    it.index = deviceCount
                    it.addressId = newAddressIDMap.get(it.addressId)!!.toInt()
                    KBoxDatabase.getInstance(activity).deviceDao.insertDevice(it)
                    deviceCount++
                }
            }

            shareQRCode.allScene?.forEach {
                val ids = it.ids.split("_")
                var isAdd = true
                for ((index, item) in ids.withIndex()) {
                    if (newAddressIDMap.get(item.toInt()) == null) {
                        isAdd = false
                        break
                    }
                    if (index == 0) {
                        it.ids = "${newAddressIDMap.get(item.toInt())!!.toInt()}"
                    } else {
                        it.ids += "_${newAddressIDMap.get(item.toInt())!!.toInt()}"
                    }
                }
                if (isAdd) {
                    it.id = 0
                    KBoxDatabase.getInstance(activity).sceneDao.insertScene(it)
                }
            }

            // 加载设备数据
            loadDevice()
            // 加载
            loadScene()

            if (existsAddressList.size > 0) {
                var message = ""
                for (ipAddress in existsAddressList) {
                    if (ProtocolType.MQTT.value == ipAddress.protocolType) {
                        message += "${ipAddress.getDeviceTypeName(activity!!)}:\n${ipAddress.deviceId}\n"
                    } else {
                        message += "${ipAddress.getDeviceTypeName(activity!!)}:\n${ipAddress.ip}:${ipAddress.port}\n"
                    }
                }
                AlertDialog.Builder(activity!!)
                    .setTitle(resources.getString(R.string.add_already))
                    .setMessage(message)
                    .setPositiveButton(resources.getString(R.string.confirm), null)
                    .create()
                    .show()
            }
        } else
            if (requestCode == 2001 && resultCode == Activity.RESULT_OK) {
                val deviceResult = data?.getStringExtra("device_result")
                if (TextUtils.isEmpty(deviceResult)) return
                val json = JSONObject(deviceResult!!)
                val ipAddress = IPAddress(
                    json.optString("ip"),
                    Integer.decode(json.optString("port")),
                    json.optInt("deviceType"),
                    json.optInt("protocolType"),
                    json.optString("userName"),
                    json.optString("password"),
                    json.optString("deviceId")
                )
                addDevice(ipAddress)
            }
        super.onActivityResult(requestCode, resultCode, data)
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
        for (i in deviceList!!) {
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
        for (i in deviceList!!) {
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
        if (getAllDeviceStateCount == 0) {
            refresh.isRefreshing = false
        }
        if (event.ipAddress != null) {
            for ((index, device) in deviceList!!.withIndex()) {
                if (device.address.ip == event.ipAddress!!.ip) {
                    adapter?.notifyItemChanged(index)
                }
            }
        } else {
            adapter?.notifyItemRangeChanged(0, deviceList!!.size)
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
        sceneList!!.clear()
        sceneList!!.addAll(KBoxDatabase.getInstance(context).sceneDao.allScene)
        sceneAdapter?.notifyDataSetChanged()
    }

    /**
     * 从数据库读取设备
     */
    private fun loadDevice() {
        deviceList!!.clear()
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

        deviceList!!.addAll(devices)
        adapter?.notifyDataSetChanged()
        getAllDeviceState(true)
    }

    /**
     * 插入设备
     *
     * @param address ip address
     * @param model DeviceType
     */
    private fun addDevice(address: IPAddress) {
        val allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (a in allAddress) {
            if (a == address) {
                showToast(resources.getString(R.string.add_already))
                return
            }
        }

        KBoxDatabase.getInstance(context).addressDao.insertAddress(address)
        val temp = if (address.protocolType == ProtocolType.MQTT.value) {
            KBoxDatabase.getInstance(context).addressDao.getMQTTAddress(
                address.ip,
                address.port,
                address.deviceType,
                address.username,
                address.password,
                address.deviceId
            )
        } else {
            KBoxDatabase.getInstance(context).addressDao.getTCPAddress(
                address.ip,
                address.port,
                address.deviceType
            )
        }
        val size = deviceList!!.size
        var index = 0
        val list = ArrayList<Device>()

        val deviceChannelCount = address.getDeviceTypeNumberCount()

        for (i in 1..deviceChannelCount) {
            val channelDevice = Device(temp, i, size + index++)
            if (DeviceType.Dimmer_8.value == channelDevice.address.deviceType) {
                channelDevice.state = "0"
                channelDevice.isTouch = true
            } else if (DeviceType.COLB.value == channelDevice.address.deviceType) {
                channelDevice.state = ""
                if (channelDevice.number in 1..4) {
                    channelDevice.itemName =
                        "D${(channelDevice.number - 1) * 4 + 1};D${(channelDevice.number - 1) * 4 + 2};D${(channelDevice.number - 1) * 4 + 3};D${(channelDevice.number - 1) * 4 + 4}"
                } else if (channelDevice.number in 5..8) {
                    channelDevice.itemName =
                        "A${(channelDevice.number - 5) * 4 + 1};A${(channelDevice.number - 5) * 4 + 2};A${(channelDevice.number - 5) * 4 + 3};A${(channelDevice.number - 5) * 4 + 4}"
                } else if (channelDevice.number == 9) {
                    channelDevice.itemName = "T1;T2;T3;T4;T5"
                }
            }
            list.add(channelDevice)
        }

        val itemName: String?
        when (address.deviceType) {
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
        when (address.deviceType) {
            DeviceType.Relay_2.value,
            DeviceType.Relay_4.value,
            DeviceType.Relay_8.value,
            DeviceType.Relay_16.value,
            DeviceType.Relay_32.value -> {
                // relay device add total item
                val allChannelDevice = Device(
                    temp,
                    deviceChannelCount + 1,
                    size + index,
                    1,
                    deviceChannelCount,
                    itemName
                )
                list.add(allChannelDevice)
            }
            DeviceType.Dimmer_8.value -> {
//            dimmer device dose not add total item
//            val allChannelDevice = Device(
//                temp,
//                deviceChannelCount + 1,
//                size + index,
//                1,
//                deviceChannelCount,
//                itemName
//            )
//            allChannelDevice.state = "0,0,0,0,0,0,0,0"
//            list.add(allChannelDevice)
            }
        }

        list.forEach {
            KBoxDatabase.getInstance(context).deviceDao.insertDevice(it)
        }

        val readList = KBoxDatabase.getInstance(context).deviceDao.allDevice
        val addressList = KBoxDatabase.getInstance(context).addressDao.allAddress
        for (d in readList) {
            for (a in addressList) {
                if (a.id == d.addressId) {
                    d.address = a
                }
            }
        }

        deviceList!!.clear()
        deviceList!!.addAll(readList)
        adapter?.notifyDataSetChanged()

        getAllDeviceState(true)
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
        for (i in deviceList!!) {
            i.index = index++
        }
        KBoxDatabase.getInstance(context).deviceDao.updateDevice(deviceList!!)
    }

    private fun onScene(scene: Scene) {

        if (scene.address.isNullOrEmpty()) return
        val sceneAddress = scene.address.split("_")
        val sceneActions = scene.action.split("_")
        var reAction = ""
        val dList = ArrayList<ArrayList<Device>>()
        for (item in sceneAddress) {
            val dsTemp = ArrayList<Device>()
            for (ds in deviceList!!) {
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
                    reAction = actionCode(dsTemp, dsTemp[0].address.deviceType)
                } else {
                    reAction = reAction + "_" + actionCode(dsTemp, dsTemp[0].address.deviceType)
                }

                if (dsTemp[0].address.deviceType == DeviceType.Dimmer_8.value) {
                    if (dsTemp[0].address.protocolType == ProtocolType.TCP.value) {
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
                    } else if (dsTemp[0].address.protocolType == ProtocolType.MQTT.value) {
                        MqttClientManager.publish(
                            dsTemp[0].address,
                            KDimmerMqttCommand.publishTopic(dsTemp[0].address),
                            KDimmerMqttCommand.setAllDimmer(sceneActions[index].split(","))
                        )
                    }
                } else {
                    if (dsTemp[0].address.protocolType == ProtocolType.TCP.value) {
                        val request = Request.obtain(
                            dsTemp[0].address.ip,
                            dsTemp[0].address.port,
                            KBoxCommand.setAllState(sceneActions[index])
                        )
                        Client.newCall(request).enqueue(object : Callback {
                            override fun onStart() {
                            }

                            override fun onFailure(e: Exception) {
                                showToast(e.message)
                            }

                            override fun onResponse(response: ResponseBody?) {
                                if (response is KBoxState) {
                                    getAllRelayState(dsTemp[0].address)
                                }
                            }
                        })
                    } else if (dsTemp[0].address.protocolType == ProtocolType.MQTT.value) {
                        MqttClientManager.publish(
                            dsTemp[0].address,
                            KBoxMqttCommand.publishTopic(dsTemp[0].address),
                            KBoxMqttCommand.setAllState(sceneActions[index])
                        )
                    }
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
            var address: IPAddress? = null
            for (d in deviceList!!) {
                if (item == d.address.toString()) {
                    address = d.address
                    break
                }
            }
            if (address != null) {
                if (address.deviceType == DeviceType.Dimmer_8.value) {
                    if (address.protocolType == ProtocolType.TCP.value) {
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
                    } else if (address.protocolType == ProtocolType.MQTT.value) {
                        MqttClientManager.publish(
                            address,
                            KDimmerMqttCommand.publishTopic(address),
                            KDimmerMqttCommand.setAllDimmer(sceneReActions[index].split(","))
                        )
                    }
                } else {
                    if (address.protocolType == ProtocolType.TCP.value) {
                        val request = Request.obtain(
                            address.ip,
                            address.port,
                            KBoxCommand.setAllState(sceneReActions[index])
                        )
                        Client.newCall(request).enqueue(object : Callback {
                            override fun onStart() {
                            }

                            override fun onFailure(e: Exception) {
                                showToast(e.message)
                            }

                            override fun onResponse(response: ResponseBody?) {
                                if (response is KBoxState) {
                                    getAllRelayState(address)
                                }
                            }

                        })
                    } else if (address.protocolType == ProtocolType.MQTT.value) {
                        MqttClientManager.publish(
                            address,
                            KBoxMqttCommand.publishTopic(address),
                            KBoxMqttCommand.setAllState(sceneReActions[index])
                        )
                    }
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

    var getAllDeviceStateCount: Int = 0

    private fun getAllDeviceState(isRefresh: Boolean) {
        if (getAllDeviceStateCount > 0) return

        val allAddress = KBoxDatabase.getInstance(context).addressDao.allAddress
        if (allAddress.size == 0) {
            refresh.isRefreshing = false
            return
        }

        getAllDeviceStateCount = allAddress.size

        refresh.isRefreshing = isRefresh

        for (address in allAddress) {
            when (address.deviceType) {
                DeviceType.Relay_2.value,
                DeviceType.Relay_4.value,
                DeviceType.Relay_8.value,
                DeviceType.Relay_16.value,
                DeviceType.Relay_32.value -> getAllRelayState(address) { --getAllDeviceStateCount }
                DeviceType.Dimmer_8.value -> getAllDimmerState(address) { --getAllDeviceStateCount }
                DeviceType.COLB.value -> getAllCOLBState(address) { --getAllDeviceStateCount }
                else -> {
                    --getAllDeviceStateCount
                    EventBus.getDefault().post(UpdateDeviceUI())
                }
            }
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
                        KBoxStateRead.readAllState(response, address, deviceList!!)
                    }
                    EventBus.getDefault().post(UpdateDeviceUI())
                }
            })

        }
    }

    /**
     * 改变继电器状态
     */
    private fun changeKBoxState(device: Device, open: Boolean, position: Int) {
        if (device.address.protocolType == ProtocolType.TCP.value) {
            val command = KBoxCommand.setState(device.number, if (open) 1 else 0)
            val address = device.address
            val request = Request.obtain(address.ip, address.port, command)
            Client.newCall(request).enqueue(object : Callback {
                override fun onStart() {
                }

                override fun onFailure(e: Exception) {
                    showToast(e.message)
                }

                override fun onResponse(response: ResponseBody?) {
                    if (response is KBoxState && !response.succeed) {
                        // 调光器数据异常，重新读取
                        showToast(getString(R.string.data_exception_tips))
                    }
                }
            })
        } else if (device.address.protocolType == ProtocolType.MQTT.value) {
            MqttClientManager.publish(
                device.address,
                KBoxMqttCommand.publishTopic(device.address),
                KBoxMqttCommand.setState(device.number, if (open) 1 else 0)
            )
        }
    }

    /**
     * 设置调光器亮度
     */
    fun setDimmerState(position: Int, device: Device) {
        if (device.itemType != DeviceType.Dimmer_8.value || device.type != 0) return
        if (device.address.protocolType == ProtocolType.TCP.value) {
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
        } else if (device.address.protocolType == ProtocolType.MQTT.value) {
            MqttClientManager.publish(
                device.address,
                KDimmerMqttCommand.publishTopic(device.address),
                KDimmerMqttCommand.setDimmer(device.number, device.state.toInt())
            )
        }
    }

    /**
     * 读取继电器全部通道状态
     */
    private fun getAllRelayState(
        address: IPAddress,
        onFinish: (() -> Unit)? = null
    ) {
        if (address.protocolType == ProtocolType.TCP.value) {
            val request = Request.obtain(address.ip, address.port, KBoxCommand.readAllState());
            Client.newCall(request).enqueue(object : Callback {
                override fun onStart() {

                }

                override fun onFailure(e: Exception) {
                    onFinish?.invoke()
                    showToast(e.message)
                    EventBus.getDefault().post(UpdateDeviceUI())
                }

                override fun onResponse(response: ResponseBody?) {
                    onFinish?.invoke()
                    if (response is KBoxState && response.succeed) {
                        KBoxStateRead.readAllState(response, address, deviceList!!)
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }
                }
            })
        } else if (address.protocolType == ProtocolType.MQTT.value) {
            MqttClientManager.subscribe(
                address,
                KBoxMqttCommand.subscribeTopic(address),
                object : MqttSubscribeCallback {
                    override fun onSubscribe(msg: String) {
                        onFinish?.invoke()
                        val json = JSONObject(msg)
                        val numberCount = address.getDeviceTypeNumberCount()
                        for (temp in deviceList!!) {
                            if (temp.address.id == address.id) {
                                if (temp.number >= 1 && temp.number <= numberCount) {
                                    temp.open =
                                        json.optJSONObject("relay${temp.number}")
                                            ?.optString("on") == "1"
                                } else if (temp.number == numberCount + 1) {
                                    var body = 0b00000000;
                                    json.keys().forEach {
                                        if (it.contains("input")) {
                                            if (json.optJSONObject(it)
                                                    ?.optString("on") == "1"
                                            ) {
                                                val number = it.replace("input", "").toInt()
                                                when (number) {
                                                    1 -> body = body or 0b00000001
                                                    2 -> body = body or 0b00000010
                                                    3 -> body = body or 0b00000100
                                                    4 -> body = body or 0b00001000
                                                    5 -> body = body or 0b00010000
                                                    6 -> body = body or 0b00100000
                                                    7 -> body = body or 0b01000000
                                                    8 -> body = body or 0b10000000
                                                }
                                            }
                                        }
                                    }
                                    temp.body = body
                                }
                            }
                        }
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }

                    override fun onFail(msg: String) {
                        onFinish?.invoke()
                        ToastUtils.showToastShort(msg)
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }
                }
            )
            MqttClientManager.publish(
                address,
                KBoxMqttCommand.publishTopic(address),
                KBoxMqttCommand.readAllState(address)
            )
        }
    }

    /**
     * 读取调光器全部通道状态
     */
    fun getAllDimmerState(
        address: IPAddress,
        onFinish: (() -> Unit)? = null
    ) {
        if (address.deviceType != DeviceType.Dimmer_8.value) return
        if (address.protocolType == ProtocolType.TCP.value) {
            KDimmerReadAllDimmerUseCase().execute(address, object : Callback {
                override fun onStart() {

                }

                override fun onFailure(e: Exception) {
                    onFinish?.invoke()
                    showToast(e.message)
                    EventBus.getDefault().post(UpdateDeviceUI())
                }

                override fun onResponse(response: ResponseBody?) {
                    onFinish?.invoke()
                    if (response is KBoxState && response.succeed) {
                        val stateArray = response.body().split(",")
                        for (temp in deviceList!!) {
                            if (temp.address.id == address.id) {
                                if (temp.number >= 1 && temp.number <= stateArray.size) {
                                    temp.state = stateArray[temp.number - 1]
                                } else if (temp.number == stateArray.size + 1) {
                                    temp.state = response.body()
                                }
                            }
                        }
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }
                }
            })
        } else if (address.protocolType == ProtocolType.MQTT.value) {
            MqttClientManager.subscribe(
                address,
                KDimmerMqttCommand.subscribeTopic(address),
                object : MqttSubscribeCallback {
                    override fun onSubscribe(msg: String) {
                        onFinish?.invoke()
                        val json = JSONObject(msg)
                        for (temp in deviceList!!) {
                            if (temp.address.id == address.id) {
                                if (temp.number >= 1 && temp.number <= temp.address.getDeviceTypeNumberCount()) {
                                    temp.state = json.optJSONObject("dimmer${temp.number}")
                                        ?.optString("value")
                                } else if (temp.number == temp.address.getDeviceTypeNumberCount() + 1) {
                                    var body = ""
                                    json.keys().forEach {
                                        if (it.contains("dimmer")) {
                                            if (it.equals("dimmer1")) {
                                                body += "${json.optJSONObject("dimmer${temp.number}")
                                                    ?.optString("value")}"
                                            } else {
                                                body += ",${json.optJSONObject("dimmer${temp.number}")
                                                    ?.optString("value")}"
                                            }
                                        }
                                    }
                                    temp.state = body
                                }
                            }
                        }
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }

                    override fun onFail(msg: String) {
                        onFinish?.invoke()
                        ToastUtils.showToastShort(msg)
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }
                }
            )
            MqttClientManager.publish(
                address,
                KDimmerMqttCommand.publishTopic(address),
                KDimmerMqttCommand.readAll()
            )
        }
    }

    fun getAllCOLBState(
        address: IPAddress,
        onFinish: (() -> Unit)? = null
    ) {
        if (address.protocolType == ProtocolType.MQTT.value) {
            MqttClientManager.subscribe(
                address,
                KCOLBMqttCommand.subscribeTopic(address),
                object : MqttSubscribeCallback {
                    override fun onSubscribe(msg: String) {
                        onFinish?.invoke()
                        val json = JSONObject(msg)
                        for (temp in deviceList!!) {
                            if (temp.address.id == address.id) {
                                if (msg.contains("D1") && temp.number >= 1 && temp.number <= 4) {
                                    temp.state = json.optJSONObject("D${(temp.number - 1) * 4 + 1}")
                                        ?.optString("on") + "," + json.optJSONObject("D${(temp.number - 1) * 4 + 2}")
                                        ?.optString("on") + "," + json.optJSONObject("D${(temp.number - 1) * 4 + 3}")
                                        ?.optString("on") + "," + json.optJSONObject("D${(temp.number - 1) * 4 + 4}")
                                        ?.optString("on")
                                } else if (msg.contains("A1") && temp.number >= 5 && temp.number <= 8) {
                                    temp.state = json.optJSONObject("A${(temp.number - 5) * 4 + 1}")
                                        ?.optString("value") + "," + json.optJSONObject("A${(temp.number - 5) * 4 + 2}")
                                        ?.optString("value") + "," + json.optJSONObject("A${(temp.number - 5) * 4 + 3}")
                                        ?.optString("value") + "," + json.optJSONObject("A${(temp.number - 5) * 4 + 4}")
                                        ?.optString("value")
                                } else if (msg.contains("T1") && temp.number == 9) {
                                    temp.state = json.optJSONObject("T1")?.optString("value") +
                                            "," + json.optJSONObject("T2")?.optString("value") +
                                            "," + json.optJSONObject("T3")?.optString("value") +
                                            "," + json.optJSONObject("T3")?.optString("value") +
                                            "," + json.optJSONObject("T5")?.optString("value")
                                }
                            }
                        }
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }

                    override fun onFail(msg: String) {
                        onFinish?.invoke()
                        ToastUtils.showToastShort(msg)
                        EventBus.getDefault().post(UpdateDeviceUI())
                    }
                }
            )
            MqttClientManager.publish(
                address,
                KCOLBMqttCommand.publishTopic(address),
                KCOLBMqttCommand.readAllDigitalInputState()
            )
            MqttClientManager.publish(
                address,
                KCOLBMqttCommand.publishTopic(address),
                KCOLBMqttCommand.readAllAnalogInputState()
            )
            MqttClientManager.publish(
                address,
                KCOLBMqttCommand.publishTopic(address),
                KCOLBMqttCommand.readAllDS18B20TemperatureState()
            )
        }
    }

    /**
     * 添加设备弹窗
     */
    private fun showAddDeviceDialog() {
        val intent = Intent(activity!!, AddDeviceActivity::class.java)
        startActivityForResult(intent, 2001)
    }

    /**
     * 情景模式拖拽ItemTouchHelper.Callback
     */
    inner class HelperCallback(var adapter: DeviceAdapter?, var list: ArrayList<Device>) :
        ItemTouchHelper.Callback() {
        override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val swipFlag = 0;
            val dragflag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
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
