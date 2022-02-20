package com.kincony.KControl.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadUtils {
    private Executor io;
    private Executor network;
    private Executor mainThread;
    private Handler mainHandler;

    private static ThreadUtils instance;

    private static ThreadUtils getInstance() {
        if (instance == null) {
            instance = new ThreadUtils();
        }
        return instance;
    }

    private ThreadUtils() {
        io = Executors.newSingleThreadExecutor();
        network = Executors.newFixedThreadPool(5);
        mainHandler = new Handler(Looper.getMainLooper());
        mainThread = new Executor() {
            @Override
            public void execute(Runnable command) {
                mainHandler.post(command);
            }
        };
    }

    public static Executor io() {
        return getInstance().io;
    }

    public static Executor network() {
        return getInstance().network;
    }

    public static Executor mainThread() {
        return getInstance().mainThread;
    }

}
