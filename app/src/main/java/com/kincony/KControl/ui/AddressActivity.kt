package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.kincony.KControl.R
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.RefreshAddressEvent
import com.kincony.KControl.net.data.RefreshSceneEvent
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.ui.adapter.AddressAdapter
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_address.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class AddressActivity : BaseActivity() {
    var list = ArrayList<IPAddress>()
    var adapter = AddressAdapter()

    companion object {
        fun start(context: Context?) {
            val intent = Intent(context, AddressActivity::class.java)
            context?.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_address
    }

    override fun initView() {
        back.setOnClickListener {
            finish()
        }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        var address = KBoxDatabase.getInstance(this).addressDao.allAddress
        list.addAll(address)
        adapter.setNewInstance(list)
        adapter.qrCodeClickCallback = object : AddressAdapter.QrCodeClickCallback {
            override fun onQrCodeClick(item: IPAddress) {
                val jsonObject = JSONObject()
                jsonObject.put("ip", item.ip)
                jsonObject.put("port", item.port)
                jsonObject.put("deviceType", item.deviceType)
                jsonObject.put("protocolType", item.protocolType)
                jsonObject.put("userName", item.username)
                jsonObject.put("password", item.password)
                jsonObject.put("deviceId", item.deviceId)
                val bitmap =
                    createQRCodeBitmap(
                        jsonObject.toString(),
                        240,
                        240,
                        "UTF-8",
                        "H",
                        "1",
                        Color.BLACK,
                        Color.WHITE
                    )
                if (bitmap != null) {
                    val view =
                        LayoutInflater.from(this@AddressActivity)
                            .inflate(R.layout.dialog_qrcode, null)
                    view.findViewById<ImageView>(R.id.qr_code).setImageBitmap(bitmap)
                    AlertDialog.Builder(this@AddressActivity)
                        .setCancelable(true)
                        .setView(view)
                        .setPositiveButton(resources.getString(R.string.confirm), null)
                        .create()
                        .show()
                } else {
                    ToastUtils.showToastLong(getString(R.string.scan_qr_code_create_wrong))
                }
            }
        }
        adapter.setOnItemLongClickListener { adapter, view, position ->
            var address = list[position]
            getLoadingDialog(this@AddressActivity, address)?.show()
            false
        }
    }

    private fun deleteAddress(address: IPAddress) {
        KBoxDatabase.getInstance(this).addressDao.delete(address)
        var scenes = KBoxDatabase.getInstance(this).sceneDao.allScene
        for (s in scenes) {
            if (s.address.contains(address.toString())) {
                KBoxDatabase.getInstance(this).sceneDao.deleteScene(s)
            }
        }

        list.remove(address)
        adapter.notifyDataSetChanged()
        EventBus.getDefault().post(RefreshAddressEvent())
        EventBus.getDefault().post(RefreshSceneEvent())
    }

    private fun getLoadingDialog(context: Context?, address: IPAddress): AlertDialog? {
        var dialog: AlertDialog? = null
        if (context != null) {
            dialog = AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(resources.getString(R.string.Message))
                .setMessage(resources.getString(R.string.message_delete))
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->

                }
                .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
                    deleteAddress(address)
                }
                .create()
        }
        return dialog
    }

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param characterSet          编码方式（一般使用UTF-8）
     * @param errorCorrectionLevel  容错率 L：7% M：15% Q：25% H：35%
     * @param margin                空白边距（二维码与边框的空白区域）
     * @param colorBlack            黑色色块
     * @param colorWhite            白色色块
     * @return BitMap
     */
    private fun createQRCodeBitmap(
        content: String?,
        width: Int, height: Int,
        characterSet: String?,
        errorCorrectionLevel: String?,
        margin: String?,
        colorBlack: Int,
        colorWhite: Int
    ): Bitmap? {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null
        }
        try {
            /** 1.设置二维码相关配置  */
            val hints =
                Hashtable<EncodeHintType, String?>()
            // 字符转码格式设置
            if (!TextUtils.isEmpty(characterSet)) {
                hints[EncodeHintType.CHARACTER_SET] = characterSet
            }
            // 容错率设置
            if (!TextUtils.isEmpty(errorCorrectionLevel)) {
                hints[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints[EncodeHintType.MARGIN] = margin
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象  */
            val bitMatrix =
                QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix[x, y]) {
                        pixels[y * width + x] =
                            colorBlack //黑色色块像素设置，可以通过传入不同的颜色实现彩色二维码，例如Color.argb(1,55,206,141)等设置不同的颜色。
                    } else {
                        pixels[y * width + x] = colorWhite // 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象  */
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


}

