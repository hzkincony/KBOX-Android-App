package com.kincony.KControl.camera;

import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

import com.ipcamera.demo.BridgeService;
import com.kincony.KControl.R;
import com.kincony.KControl.net.data.IPAddress;
import com.kincony.KControl.ui.base.BaseActivity;
import com.kincony.KControl.utils.ThreadUtils;
import com.kincony.KControl.utils.Tools;

import vstc2.nativecaller.NativeCaller;

public class VideoActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        return R.layout.activity_video;
    }

    @Override
    public void initView() {
        IPAddress ipAddress = Tools.INSTANCE.getGson().fromJson(getIntent().getStringExtra("ipAddress"), IPAddress.class);

        VideoFragment videoFragment = VideoFragment.newInstance(ipAddress, true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, videoFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent();
        intent.setClass(this, BridgeService.class);
        startService(intent);
        ThreadUtils.network().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NativeCaller.PPPPInitial("ADCBBFAOPPJAHGJGBBGLFLAGDBJJHNJGGMBFBKHIBBNKOKLDHOBHCBOEHOKJJJKJBPMFLGCPPJMJAPDOIPNL");
                    NativeCaller.PPPPNetworkDetect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.setClass(this, BridgeService.class);
        stopService(intent);
    }

}
