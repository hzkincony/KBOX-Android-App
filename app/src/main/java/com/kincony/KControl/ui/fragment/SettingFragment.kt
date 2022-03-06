package com.kincony.KControl.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.JsonParser
import com.kincony.KControl.R
import com.kincony.KControl.net.data.*
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.net.socket.NettyClient
import com.kincony.KControl.ui.AddressActivity
import com.kincony.KControl.ui.SceneActivity
import com.kincony.KControl.ui.base.BaseFragment
import com.kincony.KControl.ui.scan.ScanActivity
import com.kincony.KControl.utils.*
import kotlinx.android.synthetic.main.fragmemt_video.*
import kotlinx.android.synthetic.main.fragment_setting.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import java.lang.Runnable as Runnable1


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

            showReBKDialog()
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
                    EventBus.getDefault().post(RefreshAddressEvent(null))
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

    private fun showReBKDialog() {
//        val dialogView = LayoutInflater.from(activity)
//            .inflate(R.layout.dialog_re_bk, null, false)
//        val alertDialog = AlertDialog.Builder(activity!!)
//            .setView(dialogView)
//            .create()
//        dialogView.findViewById<View>(R.id.tvQRCode).setOnClickListener {
//            alertDialog.dismiss()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1008)
            return
        }
        val intent = Intent(activity!!, ScanActivity::class.java)
        startActivityForResult(intent, 2000)
//        }
//        dialogView.findViewById<View>(R.id.tvFile).setOnClickListener {
//            alertDialog.dismiss()
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "*/*"
//            startActivityForResult(intent, 2001)
//        }
//        alertDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1008 && ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(activity!!, ScanActivity::class.java)
            startActivityForResult(intent, 2000)
        }
    }


    private var progressDialog: ProgressDialog? = null

    private fun showProgressDialog(message: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(activity)
            progressDialog!!.setCanceledOnTouchOutside(false)
            progressDialog!!.setCancelable(false)
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }
        progressDialog!!.setMessage(message)
        if (!progressDialog!!.isShowing) {
            progressDialog!!.show()
        }
    }

    private val dismissProgressRunnable = Runnable1 {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    private fun dismissProgressDialog(delay: Long) {
        topBar.removeCallbacks(dismissProgressRunnable)
        if (delay <= 0) {
            dismissProgressRunnable.run()
        } else {
            topBar.postDelayed(dismissProgressRunnable, delay)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1000 && !BatteryUtils.isIgnoringBatteryOptimizations(activity)) {
            ToastUtils.showToastLong(getString(R.string.ignoring_battery_optimizations_fail))
            AlertDialog.Builder(activity!!)
                .setCancelable(true)
                .setMessage(R.string.ignoring_battery_optimizations_fail)
                .setPositiveButton(getString(R.string.boot_start_management)) { _, _ ->
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
            val jsonObject = JsonParser().parse(unzip).asJsonObject
            if (jsonObject.get("kbox_qrcode") != null && "v2".equals(jsonObject.get("kbox_qrcode").asString)) {
                showProgressDialog(getString(R.string.connecting))
                val nettyClient = NettyClient()
                nettyClient.setCallback(object : NettyClient.Callback {
                    override fun onConnectSuccess() {
                        showProgressDialog(getString(R.string.connect_success))
                    }

                    override fun onConnectError(throwable: Throwable?) {
                        showProgressDialog(getString(R.string.connect_fail))
                        dismissProgressDialog(2000)
                    }

                    override fun onActive() {

                    }

                    override fun onInactive() {

                    }

                    override fun onError(throwable: Throwable?) {
                        showProgressDialog(getString(R.string.connect_fail))
                        dismissProgressDialog(2000)
                        nettyClient.close()
                    }

                    override fun onReadBefore(message: String?) {
                        if (TextUtils.isEmpty(message)) {
                            ThreadUtils.mainThread().execute(Runnable1 {
                                showProgressDialog(getString(R.string.connect_fail))
                                dismissProgressDialog(2000)
                            })
                            nettyClient.close()
                            return
                        }
                        val unzip2 = Tools.unzip(message!!)
                        if (!unzip2.startsWith("{") || !unzip2.endsWith("}")) {
                            ThreadUtils.mainThread().execute {
                                showProgressDialog(getString(R.string.data_error))
                                dismissProgressDialog(2000)
                            }
                            nettyClient.close()
                        } else {
                            reBKAddress(unzip2)
                        }
                    }

                    override fun onRead(message: String?) {

                    }

                    override fun onWrite(message: String?) {

                    }

                })
                nettyClient.connectServer(
                    jsonObject.get("ip").asString,
                    jsonObject.get("port").asInt
                )
            } else {
                reBKAddress(unzip)
            }
        } else if (requestCode == 2001 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                showProgressDialog(getString(R.string.data_loading))
                ThreadUtils.io().execute(Runnable1 {
                    val inputStream = activity!!.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        try {
                            val readText = inputStream.bufferedReader().readText()
                            if (readText.length > 100 * 1024 * 1024) {
                                ThreadUtils.mainThread().execute {
                                    showProgressDialog(getString(R.string.the_file_is_too_large))
                                    dismissProgressDialog(2000)
                                }
                                return@Runnable1
                            }
                            val unzip = Tools.unzip(readText)
                            if (!unzip.startsWith("{") || !unzip.endsWith("}")) {
                                ThreadUtils.mainThread().execute {
                                    showProgressDialog(getString(R.string.file_from_wrong))
                                    dismissProgressDialog(2000)
                                }
                                return@Runnable1
                            }
                            reBKAddress(unzip)
                        } catch (e: Exception) {
                            ThreadUtils.mainThread().execute {
                                showProgressDialog(getString(R.string.the_file_is_too_large))
                                dismissProgressDialog(2000)
                            }
                        } finally {
                            Tools.close(inputStream)
                        }
                    }
                })
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun reBKAddress(unzip: String) {
        ThreadUtils.mainThread().execute {
            showProgressDialog(getString(R.string.data_loading))
        }
        val addressBKBean =
            Tools.gson.fromJson<AddressBKBean>(unzip, AddressBKBean::class.java)
        if (addressBKBean.allAddress == null || addressBKBean.allAddress.size == 0) {
            ThreadUtils.mainThread().execute {
                showProgressDialog(getString(R.string.data_empty))
                dismissProgressDialog(2000)
            }
            return
        }

        val newAddressIDMap = HashMap<Int, Int>()
        val existsAddressList = ArrayList<IPAddress>()

        addressBKBean.allAddress!!.forEach {
            val temp =
                if (it.protocolType == ProtocolType.CAMERA.value) {
                    KBoxDatabase.getInstance(context).addressDao.getCAMERAAddress(
                        it.deviceType,
                        it.deviceUserName,
                        it.devicePassword,
                        it.deviceId
                    )
                } else if (it.protocolType == ProtocolType.MQTT.value) {
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
        addressBKBean.allDevice?.forEach {
            if (newAddressIDMap.get(it.addressId) != null) {
                it.index = deviceCount
                it.addressId = newAddressIDMap.get(it.addressId)!!.toInt()
                KBoxDatabase.getInstance(activity).deviceDao.insertDevice(it)
                deviceCount++
            }
        }

        addressBKBean.allScene?.forEach {
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
                if (ProtocolType.CAMERA.value == ipAddress.protocolType) {
                    message += "${ipAddress.getDeviceTypeName(activity!!)}:\n${ipAddress.deviceId}\n"
                } else if (ProtocolType.MQTT.value == ipAddress.protocolType) {
                    message += "${ipAddress.getDeviceTypeName(activity!!)}:\n${ipAddress.deviceId}\n"
                } else {
                    message += "${ipAddress.getDeviceTypeName(activity!!)}:\n${ipAddress.ip}:${ipAddress.port}\n"
                }
            }
            ThreadUtils.mainThread().execute {
                dismissProgressDialog(0)
                AlertDialog.Builder(activity!!)
                    .setTitle(getString(R.string.add_already))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                        EventBus.getDefault().post(RefreshAddressEvent(null))
                        EventBus.getDefault().post(RefreshSceneEvent())
                    }
                    .create()
                    .show()
            }
        } else {
            ThreadUtils.mainThread().execute {
                dismissProgressDialog(0)
                AlertDialog.Builder(activity!!)
                    .setMessage(getString(R.string.add_data_success))
                    .setPositiveButton(
                        getString(R.string.confirm)
                    ) { dialog, which ->
                        EventBus.getDefault().post(RefreshAddressEvent(null))
                        EventBus.getDefault().post(RefreshSceneEvent())
                    }
                    .create()
                    .show()
            }
        }
    }

}