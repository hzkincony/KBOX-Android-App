package com.kincony.KControl;

import android.app.Application;

//import com.tencent.bugly.Bugly;

public class App extends Application {
    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        this.application = this;
//        Bugly.init(getApplicationContext(), "a75454c189", BuildConfig.DEBUG);
//        Bugly.init(getApplicationContext(), "a75454c189", false);
    }
}
