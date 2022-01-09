package com.kincony.KControl.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.kincony.KControl.R
import com.kincony.KControl.net.data.IPAddress
import com.kincony.KControl.net.data.RefreshAddressEvent
import com.kincony.KControl.net.data.RefreshSceneEvent
import com.kincony.KControl.net.data.ShareQRCode
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.ui.adapter.AddressAdapter
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.utils.ToastUtils
import com.kincony.KControl.utils.Tools
import kotlinx.android.synthetic.main.activity_address.*
import org.greenrobot.eventbus.EventBus
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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

        iv_qr_code.setOnClickListener {
            showQRCodeView()
        }

        tv_qr_code.setOnClickListener {
            showQRCodeView()
        }

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        var address = KBoxDatabase.getInstance(this).addressDao.allAddress
        list.addAll(address)
        adapter.setNewInstance(list)
        adapter.setOnItemLongClickListener { adapter, view, position ->
            var address = list[position]
            getLoadingDialog(this@AddressActivity, address)?.show()
            false
        }
    }

    private fun showQRCodeView() {
        val allAddress = KBoxDatabase.getInstance(this).addressDao.allAddress
        val allDevice = KBoxDatabase.getInstance(this).deviceDao.allDevice
        val allScene = KBoxDatabase.getInstance(this).sceneDao.allScene
        val shareQRCode = ShareQRCode()
        shareQRCode.allAddress = allAddress
        shareQRCode.allScene = allScene
        shareQRCode.allDevice = allDevice
        val content = Tools.zip(Tools.gson.toJson(shareQRCode))
        val bitmap =
            createQRCodeBitmap(
                content,
                480,
                480,
                "UTF-8",
                "L",
                "1",
                Color.BLACK,
                Color.WHITE
            )
        if (bitmap != null) {
            val view =
                LayoutInflater.from(this@AddressActivity).inflate(R.layout.dialog_qrcode, null)
            view.findViewById<ImageView>(R.id.qr_code).setImageBitmap(bitmap)
            AlertDialog.Builder(this@AddressActivity)
                .setCancelable(true)
                .setView(view)
                .setPositiveButton(resources.getString(R.string.confirm), null)
                .setNegativeButton(resources.getString(R.string.save)) { dialog, _ ->
                    val saveBitmap =
                        createQRCodeBitmap(
                            content,
                            480,
                            480,
                            "UTF-8",
                            "L",
                            "10",
                            Color.BLACK,
                            Color.WHITE
                        )
                    if (saveBitmap == null) {
                        ToastUtils.showToastLong(getString(R.string.save_fail))
                        return@setNegativeButton
                    }
                    save2Album(saveBitmap, "KBOX_QRCode_${System.currentTimeMillis()}.png")
                }
                .create()
                .show()
        } else {
            ToastUtils.showToastLong(getString(R.string.scan_qr_code_create_wrong))
        }
    }

    private fun save2Album(saveBitmap: Bitmap, saveName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            val insertImage = MediaStore.Images.Media.insertImage(
//                contentResolver,
//                saveBitmap,
//                saveName,
//                saveName
//            )
//            if (TextUtils.isEmpty(insertImage)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        this@AddressActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val saveDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "KBOX"
                    )
                    if (!saveDir.exists()) {
                        saveDir.mkdir()
                    }
                    val saveFile = File(saveDir, saveName)
                    var os: OutputStream? = null
                    try {
                        os = BufferedOutputStream(FileOutputStream(saveFile))
                        val compressResult = saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                        if (compressResult) {
                            try {
                                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                                intent.data = Uri.fromFile(saveFile)
                                sendBroadcast(intent)
                                val intent2 = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                                intent2.data = Uri.parse("file://" + saveFile.getAbsolutePath())
                                sendBroadcast(intent2)
                            } catch (e: Exception) {
                                val intent3 = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                                intent3.data = Uri.parse("file://" + saveFile.getAbsolutePath())
                                sendBroadcast(intent3)
                            }
                            showSaveSuccess(saveFile.path)
                        } else {
                            ToastUtils.showToastLong(getString(R.string.save_fail))
                        }
                    } catch (e: Exception) {
                        ToastUtils.showToastLong(getString(R.string.save_fail))
                    } finally {
                        Tools.close(os)
                    }
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1000
                    )
                }
            } else {
                val saveDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "KBOX"
                )
                if (!saveDir.exists()) {
                    saveDir.mkdir()
                }
                val saveFile = File(saveDir, saveName)
                var os: OutputStream? = null
                try {
                    os = BufferedOutputStream(FileOutputStream(saveFile))
                    val compressResult = saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                    if (compressResult) {
                        try {
                            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            intent.data = Uri.fromFile(saveFile)
                            sendBroadcast(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            intent.data = Uri.parse("file://" + saveFile.getAbsolutePath())
                            sendBroadcast(intent)
                        }
                        showSaveSuccess(saveFile.path)
                    } else {
                        ToastUtils.showToastLong(getString(R.string.save_fail))
                    }
                } catch (e: Exception) {
                    ToastUtils.showToastLong(getString(R.string.save_fail))
                } finally {
                    Tools.close(os)
                }
            }
//            } else {
//                ToastUtils.showToastLong(getString(R.string.save_success))
//            }
        } else {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, saveName)
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            val contentUri: Uri
            contentUri = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            }
            contentValues.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + File.separator + "KBOX"
            )
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
            val insertUri = contentResolver.insert(contentUri, contentValues)
            if (insertUri == null) {
                ToastUtils.showToastLong(getString(R.string.save_fail))
                return
            }
            var os: OutputStream? = null
            try {
                os = contentResolver.openOutputStream(insertUri)
                val compressResult = saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                if (compressResult) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(insertUri, contentValues, null, null)
                    ToastUtils.showToastLong(getString(R.string.save_success))
                } else {
                    contentResolver.delete(insertUri, null, null)
                    ToastUtils.showToastLong(getString(R.string.save_fail))
                }
            } catch (e: Exception) {
                contentResolver.delete(insertUri, null, null)
                ToastUtils.showToastLong(getString(R.string.save_fail))
            } finally {
                Tools.close(os)
            }
        }
    }

    private fun showSaveSuccess(absolutePath: String) {
        AlertDialog.Builder(this@AddressActivity)
            .setTitle(R.string.save_success)
            .setMessage(absolutePath)
            .setCancelable(true)
            .setPositiveButton(resources.getString(R.string.confirm), null)
            .create()
            .show()
    }

    private fun deleteAddress(address: IPAddress) {
        KBoxDatabase.getInstance(this).addressDao.delete(address)
        val scenes = KBoxDatabase.getInstance(this).sceneDao.allScene
        for (s in scenes) {
            if (s.address.contains(address.toString())) {
                KBoxDatabase.getInstance(this).sceneDao.deleteScene(s)
            }
        }

        list.remove(address)
        adapter.notifyDataSetChanged()
        EventBus.getDefault().post(RefreshAddressEvent(address))
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

