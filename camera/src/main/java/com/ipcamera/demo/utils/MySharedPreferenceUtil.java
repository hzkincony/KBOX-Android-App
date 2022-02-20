package com.ipcamera.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * store package Name
 *
 * @author Administrator
 */
public class MySharedPreferenceUtil {

    private static SharedPreferences prefer;
    public static final String STR_CAMERA_SYSTEMFIRM = "system_firm";


    //保存设备 的信息
    public static void saveDeviceInformation(Context context, String uid, String informationType, String information) {
        prefer = context.getSharedPreferences(uid, Context.MODE_PRIVATE);
        prefer.edit().putString(informationType, information).commit();
    }

    //获取设备 的信息
    public static String getDeviceInformation(Context context, String uid, String informationType) {
        prefer = context.getSharedPreferences(uid, Context.MODE_PRIVATE);
        String information = prefer.getString(informationType, "");
        return information;
    }

    //保存设备版本 的信息
    public static void saveSystemVer(Context context, String did, String ver) {
        prefer = context.getSharedPreferences(
                STR_CAMERA_SYSTEMFIRM, Context.MODE_PRIVATE);
        prefer.edit().putString(did, ver).commit();
    }

    // 获取系统版本
    public static String getSystemVer(Context context, String did) {
        prefer = context.getSharedPreferences(
                STR_CAMERA_SYSTEMFIRM, Context.MODE_PRIVATE);
        String path = prefer.getString(did, "0");
        return path;
    }
}
