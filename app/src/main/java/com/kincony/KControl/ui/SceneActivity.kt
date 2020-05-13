package com.kincony.KControl.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.kincony.KControl.R
import com.kincony.KControl.net.data.*
import com.kincony.KControl.net.data.database.KBoxDatabase
import com.kincony.KControl.ui.adapter.SceneAdapter
import com.kincony.KControl.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.activity_address.recycler
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus

class SceneActivity : BaseActivity() {
    var list = ArrayList<Scene>()
    var adapter = SceneAdapter()

    companion object {
        fun start(context: Context?) {
            val intent = Intent(context, SceneActivity::class.java)
            context?.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_scene
    }

    override fun initView() {
        back.setOnClickListener {
            finish()
        }

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        add.setOnClickListener {
            SceneEditActivity.start(this, null)
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            var scene = list[position]
            SceneEditActivity.start(this, scene)
        }
        adapter.setOnItemLongClickListener { adapter, view, position ->
            var scene = list[position]
            getDeleteDialog(this@SceneActivity, scene)?.show()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    private fun initData() {
        var scenes = KBoxDatabase.getInstance(this).sceneDao.allScene
        list.clear()
        list.addAll(scenes)
        adapter.setNewInstance(list)
        adapter?.notifyDataSetChanged()
    }

    private fun deleteAddress(scene: Scene) {
        KBoxDatabase.getInstance(this).sceneDao.deleteScene(scene)
        list.remove(scene)
        adapter.notifyDataSetChanged()
        EventBus.getDefault().post(RefreshSceneEvent())
    }


    private fun getDeleteDialog(context: Context?, scene: Scene): AlertDialog? {
        var dialog: AlertDialog? = null
        if (context != null) {
            dialog = AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(resources.getString(R.string.Message))
                .setMessage(resources.getString(R.string.message_delete))
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->

                }
                .setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
                    deleteAddress(scene)
                }
                .create()
        }
        return dialog
    }


}

