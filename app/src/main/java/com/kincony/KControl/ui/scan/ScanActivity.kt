package com.kincony.KControl.ui.scan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.Camera
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.Vibrator
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceHolder
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.kincony.KControl.R
import com.kincony.KControl.ui.base.BaseActivity
import com.kincony.KControl.utils.LogUtils
import com.kincony.KControl.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_scan.*
import java.io.File
import java.net.URI
import java.util.*

class ScanActivity : BaseActivity(), SurfaceHolder.Callback {

    private var vibrator: Vibrator? = null
    private var isAlbum = false

    private var cameraManager: CameraManager? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null
    private var surfaceHolder: SurfaceHolder? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_scan
    }

    override fun initView() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        cameraManager = CameraManager()

        torch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (cameraHandler == null) {
                return@setOnCheckedChangeListener
            }
            cameraHandler!!.post {
                cameraManager!!.setTorch(isChecked)
            }
        }

        back.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        album.setOnClickListener {
            isAlbum = true
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        cameraThread = HandlerThread("cameraThread", Process.THREAD_PRIORITY_BACKGROUND)
        cameraThread!!.start()
        cameraHandler = Handler(cameraThread!!.looper)

        surfaceHolder = surfaceView.holder
        surfaceHolder?.addCallback(this)
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun onPause() {
        cameraHandler?.post(closeRunnable)
        surfaceHolder!!.removeCallback(this)
        super.onPause()
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                isAlbum = true
                object : Thread() {
                    override fun run() {
                        val uri = data?.data
                        if (uri != null) {
                            val fromFile: File? = convertUriToFile(this@ScanActivity, uri)
                            if (fromFile != null && fromFile.exists()) {
                                val bmp: Bitmap? = getBitmapNearestSize(fromFile, 612)
                                if (bmp != null) {
                                    val text = decodeQrCodeFromBitmap(bmp)
                                    runOnUiThread {
                                        if (text == null) {
                                            isAlbum = false
                                            ToastUtils.showToastShort(getString(R.string.scan_qr_code_from_album_wrong))
                                        } else {
                                            com.kincony.KControl.utils.LogUtils.d("ScanActivity album result --> $text")
                                            val result = Intent()
                                            result.putExtra("scan_result", text)
                                            setResult(RESULT_OK, result)
                                            finish()
                                        }
                                    }
                                }
                            }
                        }

                    }
                }.start()
            } else {
                isAlbum = false
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        cameraHandler?.post(openRunnable)
    }

    private fun decodeQrCodeFromBitmap(bmp: Bitmap): String? {
        val width = bmp.width
        val height = bmp.height
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)
        bmp.recycle()
        val reader = QRCodeReader()
        val hints: MutableMap<DecodeHintType, Any?> =
            EnumMap<DecodeHintType, Any>(
                DecodeHintType::class.java
            )
        hints[DecodeHintType.TRY_HARDER] = true
        try {
            val result = reader.decode(
                BinaryBitmap(
                    HybridBinarizer(
                        RGBLuminanceSource(
                            width,
                            height,
                            pixels
                        )
                    )
                ), hints
            )
            return result.text
        } catch (e: Exception) {
            Log.i("ScanActivity", e.toString())
        }
        return null
    }

    private fun getBitmapNearestSize(file: File?, size: Int): Bitmap? {
        return try {
            if (file == null || !file.exists()) {
                return null
            } else if (file.length() == 0L) {
                file.delete()
                return null
            }
            val opts = BitmapFactory.Options()
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, opts)
            val sampleSize: Int = getSampleSize(Math.min(opts.outHeight, opts.outWidth), size)
            opts.inSampleSize = sampleSize
            opts.inJustDecodeBounds = false
            opts.inPurgeable = true
            opts.inInputShareable = false
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } catch (e: Exception) {
            Log.i("ScanActivity", e.toString())
            null
        }
    }

    private fun getSampleSize(fileSize: Int, targetSize: Int): Int {
        var sampleSize = 1
        if (fileSize > targetSize * 2) {
            var sampleLessThanSize = 0
            do {
                sampleLessThanSize++
            } while (fileSize / sampleLessThanSize > targetSize)
            for (i in 1..sampleLessThanSize) {
                if (Math.abs(fileSize / i - targetSize) <= Math.abs(fileSize / sampleSize - targetSize)) {
                    sampleSize = i
                }
            }
        } else {
            sampleSize = if (fileSize <= targetSize) 1 else 2
        }
        return sampleSize
    }

    private fun convertUriToFile(activity: Activity, uri: Uri): File? {
        var file: File? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val actualimagecursor = activity.managedQuery(uri, proj, null, null, null)
            if (actualimagecursor != null) {
                val actual_image_column_index =
                    actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                actualimagecursor.moveToFirst()
                val img_path = actualimagecursor.getString(actual_image_column_index)
                if (!TextUtils.isEmpty(img_path)) {
                    file = File(img_path)
                }
            } else {
                file = File(URI(uri.toString()))
                if (file.exists()) {
                    return file
                }
            }
        } catch (e: Exception) {
            Log.i("ScanActivity", e.toString())
        }
        return file
    }

    private val openRunnable = Runnable {
        try {
            val camera = cameraManager!!.open(surfaceHolder, true)
            val framingRect = cameraManager!!.frame
            val framingRectInPreview = cameraManager!!.framePreview
            runOnUiThread {
                scannerView.setFraming(
                    framingRect,
                    framingRectInPreview
                )
            }
            val focusMode = camera.parameters.focusMode
            val nonContinuousAutoFocus =
                (Camera.Parameters.FOCUS_MODE_AUTO == focusMode) || Camera.Parameters.FOCUS_MODE_MACRO == focusMode
            if (nonContinuousAutoFocus) {
                cameraHandler?.post(
                    AutoFocusRunnable(
                        camera,
                        cameraHandler!!
                    )
                )
            }
            cameraHandler?.post(fetchAndDecodeRunnable)
        } catch (e: Exception) {
            Log.i("ScanActivity", e.toString())
            finish()
        }
    }

    private val fetchAndDecodeRunnable: Runnable = object : Runnable {
        private val reader = QRCodeReader()
        private val hints: MutableMap<DecodeHintType, Any?> =
            EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)

        override fun run() {
            if (isAlbum) {
                cameraHandler?.postDelayed(this, 500)
                return
            }
            cameraManager?.requestPreviewFrame { data, camera -> decode(data) }
        }

        private fun decode(data: ByteArray) {
            val source = cameraManager!!.buildLuminanceSource(data)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] =
                    ResultPointCallback { dot -> runOnUiThread { scannerView.addDot(dot) } }
                val scanResult = reader.decode(bitmap, hints)
                if (!resultValid(scanResult.text)) {
                    cameraHandler!!.post(this)
                    return
                }
                val thumbnailWidth = source.thumbnailWidth
                val thumbnailHeight = source.thumbnailHeight
                val thumbnailScaleFactor = (thumbnailWidth.toFloat() / source.width)
                val thumbnailImage = Bitmap.createBitmap(
                    thumbnailWidth, thumbnailHeight,
                    Bitmap.Config.ARGB_8888
                )
                thumbnailImage.setPixels(
                    source.renderThumbnail(), 0,
                    thumbnailWidth, 0, 0, thumbnailWidth, thumbnailHeight
                )
                runOnUiThread {
                    handleResult(
                        scanResult,
                        thumbnailImage,
                        thumbnailScaleFactor
                    )
                }
            } catch (e: Exception) {
                Log.i("ScanActivity", e.toString())
                cameraHandler!!.post(this)
            } finally {
                reader.reset()
            }
        }
    }

    private fun handleResult(scanResult: Result, bitmap: Bitmap, thumbnailScaleFactor: Float) {
        var thumbnailImage = bitmap
        vibrator?.vibrate(50L)
        // superimpose dots to highlight the key features of the qr code
        val points = scanResult.resultPoints
        if (points != null && points.size > 0) {
            val paint = Paint()
            paint.color = resources.getColor(R.color.scan_result_dots)
            paint.strokeWidth = 10.0f
            val canvas = Canvas(thumbnailImage)
            canvas.scale(thumbnailScaleFactor, thumbnailScaleFactor)
            for (point in points) canvas.drawPoint(point.x, point.y, paint)
        }
        val matrix = Matrix()
        matrix.postRotate(90f)
        thumbnailImage = Bitmap.createBitmap(
            thumbnailImage, 0, 0,
            thumbnailImage.width, thumbnailImage.height, matrix,
            false
        )
        scannerView.drawResultBitmap(thumbnailImage)
        com.kincony.KControl.utils.LogUtils.d("ScanActivity scan result --> $scanResult.text")
        val result = Intent()
        result.putExtra("scan_result", scanResult.text)
        setResult(RESULT_OK, result)
        finish()
    }


    private fun resultValid(result: String?): Boolean {
        return true
    }

    private class AutoFocusRunnable(
        private val camera: Camera,
        private val cameraHandler: Handler
    ) : Runnable {
        override fun run() {
            camera.autoFocus { success, camera ->
                cameraHandler.postDelayed(
                    this@AutoFocusRunnable,
                    2500L
                )
            }
        }
    }

    private val closeRunnable = Runnable {
        cameraManager?.close()
        cameraHandler?.removeCallbacksAndMessages(null)
        cameraThread?.quit()
    }


}