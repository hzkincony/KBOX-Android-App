package com.kincony.KControl.wifi;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.kincony.KControl.BuildConfig;
import com.kincony.KControl.utils.LogUtils;
import com.kincony.KControl.utils.Md5Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: leavesC
 * @Date: 2018/4/3 17:32
 * @Desc:
 * @Github：https://github.com/leavesC
 */
public class StringSenderService extends IntentService {

    private Socket socket;

    private OutputStream outputStream;

    private ObjectOutputStream objectOutputStream;

    private InputStream inputStream;

    private OnSendProgressChangListener progressChangListener;

    private static final String ACTION_START_SEND = BuildConfig.APPLICATION_ID + ".service.action.startSend";

    private static final String EXTRA_PARAM_STRING_TRANSFER = BuildConfig.APPLICATION_ID + ".service.extra.StringContent";

    private static final String EXTRA_PARAM_IP_ADDRESS = BuildConfig.APPLICATION_ID + ".service.extra.IpAddress";

    private static final String TAG = "FileSenderService";

    public interface OnSendProgressChangListener {

        /**
         * 如果待发送的文件还没计算MD5码，则在开始计算MD5码时回调
         */
        void onStartComputeMD5();

        /**
         * 当传输进度发生变化时回调
         *
         * @param stringTransfer       待发送的文件模型
         * @param totalTime            传输到现在所用的时间
         * @param progress             文件传输进度
         * @param instantSpeed         瞬时-文件传输速率
         * @param instantRemainingTime 瞬时-预估的剩余完成时间
         * @param averageSpeed         平均-文件传输速率
         * @param averageRemainingTime 平均-预估的剩余完成时间
         */
        void onProgressChanged(StringTransfer stringTransfer, long totalTime, int progress, double instantSpeed, long instantRemainingTime, double averageSpeed, long averageRemainingTime);

        /**
         * 当文件传输成功时回调
         *
         * @param stringTransfer StringTransfer
         */
        void onTransferSucceed(StringTransfer stringTransfer);

        /**
         * 当文件传输失败时回调
         *
         * @param stringTransfer StringTransfer
         * @param e              Exception
         */
        void onTransferFailed(StringTransfer stringTransfer, Exception e);

    }

    public StringSenderService() {
        super("FileSenderService");
    }

    public class MyBinder extends Binder {
        public StringSenderService getService() {
            return StringSenderService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new StringSenderService.MyBinder();
    }

    private ScheduledExecutorService callbackService;

    private StringTransfer stringTransfer;

    //总的已传输字节数
    private long total;

    //在上一次更新进度时已传输的字符串总字节数
    private long tempTotal = 0;

    //计算瞬时传输速率的间隔时间
    private static final int PERIOD = 400;

    //传输操作开始时间
    private Date startTime;

    private void startCallback() {
        stopCallback();
        startTime = new Date();
        callbackService = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> {
            if (stringTransfer != null) {
                //过去 PERIOD 秒内字符串的瞬时传输速率（Kb/s）
                double instantSpeed = 0;
                //根据瞬时速率计算的-预估的剩余完成时间（秒）
                long instantRemainingTime = 0;
                //到现在所用的总的传输时间
                long totalTime = 0;
                //总的平均字符串传输速率（Kb/s）
                double averageSpeed = 0;
                //根据总的平均传输速率计算的预估的剩余完成时间（秒）
                long averageRemainingTime = 0;
                //字符串大小
                long size = stringTransfer.getSize();
                //当前的传输进度
                int progress = (int) (total * 100 / size);
                //距离上一次计算进度到现在之间新传输的字节数
                long temp = total - tempTotal;
                if (temp > 0) {
                    instantSpeed = (temp / 1024.0 / PERIOD);
                    instantRemainingTime = (long) ((size - total) / 1024.0 / instantSpeed);
                }
                if (startTime != null) {
                    totalTime = (new Date().getTime() - startTime.getTime()) / 1000;
                    averageSpeed = (total / 1024.0 / totalTime);
                    averageRemainingTime = (long) ((size - total) / 1024.0 / averageSpeed);
                }
                tempTotal = total;
                LogUtils.INSTANCE.e(TAG, "---------------------------");
                LogUtils.INSTANCE.e(TAG, "传输进度（%）: " + progress);
                LogUtils.INSTANCE.e(TAG, "所用时间：" + totalTime);
                LogUtils.INSTANCE.e(TAG, "瞬时-传输速率（Kb/s）: " + instantSpeed);
                LogUtils.INSTANCE.e(TAG, "瞬时-预估的剩余完成时间（秒）: " + instantRemainingTime);
                LogUtils.INSTANCE.e(TAG, "平均-传输速率（Kb/s）: " + averageSpeed);
                LogUtils.INSTANCE.e(TAG, "平均-预估的剩余完成时间（秒）: " + averageRemainingTime);
                LogUtils.INSTANCE.e(TAG, "字节变化：" + temp);
                if (progressChangListener != null) {
                    progressChangListener.onProgressChanged(stringTransfer, totalTime, progress, instantSpeed, instantRemainingTime, averageSpeed, averageRemainingTime);
                }
            }
        };
        //每隔 PERIOD 毫秒执行一次任务 runnable（定时任务内部要捕获可能发生的异常，否则如果异常抛出到上层的话，会导致定时任务停止）
        callbackService.scheduleAtFixedRate(runnable, 0, PERIOD, TimeUnit.MILLISECONDS);
    }

    private void stopCallback() {
        if (callbackService != null) {
            if (!callbackService.isShutdown()) {
                callbackService.shutdownNow();
            }
            callbackService = null;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_START_SEND.equals(intent.getAction())) {
            try {
                clean();

                String ipAddress = intent.getStringExtra(EXTRA_PARAM_IP_ADDRESS);
                Log.e(TAG, "IP地址：" + ipAddress);
                if (TextUtils.isEmpty(ipAddress)) {
                    return;
                }

                String content = intent.getStringExtra(EXTRA_PARAM_STRING_TRANSFER);


                stringTransfer = new StringTransfer();
                stringTransfer.setContent(content);
                stringTransfer.setSize(content.length());

                if (TextUtils.isEmpty(stringTransfer.getMd5())) {
                    LogUtils.INSTANCE.e(TAG, "MD5码为空，开始计算字符串的MD5码");
                    if (progressChangListener != null) {
                        progressChangListener.onStartComputeMD5();
                    }
                    stringTransfer.setMd5(Md5Util.getMd5(stringTransfer.getContent()));
                    Log.e(TAG, "计算结束，字符串的MD5码值是：" + stringTransfer.getMd5());
                } else {
                    LogUtils.INSTANCE.e(TAG, "MD5码不为空，无需再次计算，MD5码为：" + stringTransfer.getMd5());
                }
                int index = 0;
                while (ipAddress.equals("0.0.0.0") && index < 5) {
                    Log.e(TAG, "ip: " + ipAddress);
                    ipAddress = WifiLManager.getHotspotIpAddress(this);
                    index++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (ipAddress.equals("0.0.0.0")) {
                    return;
                }

                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(ipAddress, WifiTransferConfig.PORT)), 20000);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(stringTransfer);
                inputStream = new ByteArrayInputStream(stringTransfer.getContent().getBytes(StandardCharsets.UTF_8));
                startCallback();
                byte[] buf = new byte[512];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                    total += len;
                }
                Log.e(TAG, "字符串发送成功");
                stopCallback();
                if (progressChangListener != null) {
                    //因为上面在计算字符串传输进度时因为小数点问题可能不会显示到100%，所以此处手动将之设为100%
                    progressChangListener.onProgressChanged(stringTransfer, 0, 100, 0, 0, 0, 0);
                    progressChangListener.onTransferSucceed(stringTransfer);
                }
            } catch (Exception e) {
                Log.e(TAG, "字符串发送异常 Exception: " + e.getMessage());
                if (progressChangListener != null) {
                    progressChangListener.onTransferFailed(stringTransfer, e);
                }
            } finally {
                clean();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }

    public void clean() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
                objectOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stopCallback();
        total = 0;
        tempTotal = 0;
        startTime = null;
        stringTransfer = null;
    }

    public static void startActionTransfer(Context context, String content, String ipAddress) {
        Intent intent = new Intent(context, StringSenderService.class);
        intent.setAction(ACTION_START_SEND);
        intent.putExtra(EXTRA_PARAM_STRING_TRANSFER, content);
        intent.putExtra(EXTRA_PARAM_IP_ADDRESS, ipAddress);
        context.startService(intent);
    }

    public void setProgressChangListener(OnSendProgressChangListener progressChangListener) {
        this.progressChangListener = progressChangListener;
    }

}