package com.kincony.KControl.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.kincony.KControl.R
import com.kincony.KControl.net.data.*
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.ui.AddressActivity
import com.kincony.KControl.ui.SceneActivity
import com.kincony.KControl.ui.base.BaseFragment
import com.kincony.KControl.ui.scan.ScanActivity
import com.kincony.KControl.utils.BatteryUtils
import com.kincony.KControl.utils.SPUtils
import com.kincony.KControl.utils.ToastUtils
import com.kincony.KControl.utils.Tools
import kotlinx.android.synthetic.main.fragment_setting.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList


class SettingFragment : BaseFragment() {
    override fun getLayoutId() = R.layout.fragment_setting

    override fun initView() {
        device.setOnClickListener {
            AddressActivity.start(context)
        }
        ignoring_battery_optimizations.setOnClickListener {
            if (!BatteryUtils.isIgnoringBatteryOptimizations(activity)) {
                BatteryUtils.requestIgnoreBatteryOptimizations(this@SettingFragment, 1000)
            }
        }
//        boot_start_management.setOnClickListener {
//            BatteryUtils.requestBootStart(activity)
//        }
        load_address.setOnClickListener {
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
        temperature_unit.setOnClickListener {
            val message = if (SPUtils.getTemperatureUnit() == "℉") {
                getString(R.string.change_temperature_unit_to) + "℃"
            } else {
                getString(R.string.change_temperature_unit_to) + "℉"
            }
            AlertDialog.Builder(activity!!)
                .setCancelable(true)
                .setMessage(message)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    if (SPUtils.getTemperatureUnit() == "℉") {
                        SPUtils.setTemperatureUnit("℃")
                        tv_temperature_unit.setText("℃")
                    } else {
                        SPUtils.setTemperatureUnit("℉")
                        tv_temperature_unit.setText("℉")
                    }
                    EventBus.getDefault().post(RefreshAddressEvent())
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
        }
        mode.setOnClickListener {
            SceneActivity.start(context)
        }
        versionName.text = Tools.getAppVersionName(context)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1000 && !BatteryUtils.isIgnoringBatteryOptimizations(activity)) {
            ToastUtils.showToastLong(getString(R.string.ignoring_battery_optimizations_fail))
            AlertDialog.Builder(activity!!)
                .setCancelable(true)
                .setMessage(R.string.ignoring_battery_optimizations_fail)
                .setPositiveButton(resources.getString(R.string.boot_start_management)) { _, _ ->
                    BatteryUtils.requestBootStart(activity!!)
                }
                .create()
                .show()
        } else if (requestCode == 2000 && resultCode == Activity.RESULT_OK) {
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

            EventBus.getDefault().post(RefreshAddressEvent())
            EventBus.getDefault().post(RefreshSceneEvent())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}