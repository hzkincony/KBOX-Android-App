package com.kincony.KControl.ui.base

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kincony.KControl.utils.ToastUtils
import kotlinx.android.synthetic.main.fragment_setting.*

open abstract class BaseActivity : AppCompatActivity() {
    var mHandler = Handler()

    var canLoad = true

    private var progressDialog: ProgressDialog? = null

    public fun showProgressDialog(message: String) {
        mHandler.post {
            if (progressDialog == null) {
                progressDialog = ProgressDialog(this)
                progressDialog!!.setCanceledOnTouchOutside(false)
                progressDialog!!.setCancelable(false)
                progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            }
            progressDialog!!.setMessage(message)
            if (!progressDialog!!.isShowing) {
                progressDialog!!.show()
            }
        }
    }

    private val dismissProgressRunnable = Runnable {
        mHandler.post {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }
    }

    public fun dismissProgressDialog(delay: Long) {
        mHandler.removeCallbacks(dismissProgressRunnable)
        if (delay <= 0) {
            dismissProgressRunnable.run()
        } else {
            mHandler.postDelayed(dismissProgressRunnable, delay)
        }
    }

    fun showToast(msg: String?) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()
        } else {
            mHandler.post {
                Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (canLoad) {
            if (getLayoutId() != 0) {
                setContentView(getLayoutId())
            }
            initView()
        }
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()

    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog(0)
        ToastUtils.destroy(this)
    }
}