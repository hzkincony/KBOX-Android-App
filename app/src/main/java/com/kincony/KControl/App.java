package com.kincony.KControl;

import android.app.Application;
import android.util.Log;

import com.kincony.KControl.utils.LogUtils;

public class App extends Application {
    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LogUtils.INSTANCE.e(t.toString());
            LogUtils.INSTANCE.e(Log.getStackTraceString(e));
            defaultHandler.uncaughtException(t, e);
        });
    }
}
