package com.kincony.KControl.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class BatteryUtils {

    /**
     * 判断我们的应用是否在白名单中
     */
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        boolean isIgnoring = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        }
        return isIgnoring;
    }

    /**
     * 申请加入白名单
     */
    public static void requestIgnoreBatteryOptimizations(Fragment fragment, int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + fragment.getActivity().getPackageName()));
                fragment.startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 申请后台管理
     */
    public static void requestBootStart(Context context) {
        if (isLeTV()) {
            goLetvSetting(context);
        } else if (isMeizu()) {
            goMeizuSetting(context);
        } else if (isOPPO()) {
            goOPPOSetting(context);
        } else if (isSamsung()) {
            goSamsungSetting(context);
        } else if (isSmartisan()) {
            goSmartisanSetting(context);
        } else if (isVIVO()) {
            goVIVOSetting(context);
        } else if (isXiaomi()) {
            goXiaomiSetting(context);
        }
    }

    /**
     * 跳转到指定应用的首页
     */
    private static void showActivity(Context context, @NonNull String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到指定应用的指定页面
     */
    private static void showActivity(Context context, @NonNull String packageName, @NonNull String activityDir) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityDir));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 华为厂商判断
     *
     * @return
     */
    public boolean isHuawei() {
        if (Build.BRAND == null) {
            return false;
        } else {
            return Build.BRAND.toLowerCase().equals("huawei") || Build.BRAND.toLowerCase().equals("honor");
        }
    }

    /**
     * 小米厂商判断
     *
     * @return
     */
    public static boolean isXiaomi() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("xiaomi");
    }

    /**
     * OPPO厂商判断
     *
     * @return
     */
    public static boolean isOPPO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("oppo");
    }

    /**
     * VIVO厂商判断
     *
     * @return
     */
    public static boolean isVIVO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("vivo");
    }

    public static boolean isMeizu() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("meizu");
    }

    public static boolean isSamsung() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("samsung");
    }

    public static boolean isLeTV() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("letv");
    }

    public static boolean isSmartisan() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("smartisan");
    }

    /**
     * 跳转华为手机管家的启动管理页
     * 操作步骤：应用启动管理 -> 关闭应用开关 -> 打开允许自启动
     *
     * @param context
     */
    public static void goHuaweiSetting(Context context) {
        try {
            showActivity(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
        } catch (Exception e) {
            showActivity(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
        }
    }

    /**
     * 跳转小米安全中心的自启动管理页面
     * 操作步骤：授权管理 -> 自启动管理 -> 允许应用自启动
     *
     * @param context
     */
    public static void goXiaomiSetting(Context context) {
        showActivity(context, "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity");
    }

    /**
     * 跳转 OPPO 手机管家
     * 操作步骤：权限隐私 -> 自启动管理 -> 允许应用自启动
     *
     * @param context
     */
    public static void goOPPOSetting(Context context) {
        try {
            showActivity(context, "com.coloros.phonemanager");
        } catch (Exception e1) {
            try {
                showActivity(context, "com.oppo.safe");
            } catch (Exception e2) {
                try {
                    showActivity(context, "com.coloros.oppoguardelf");
                } catch (Exception e3) {
                    showActivity(context, "com.coloros.safecenter");
                }
            }
        }
    }

    /**
     * 跳转 VIVO 手机管家
     * 操作步骤：权限管理 -> 自启动 -> 允许应用自启动
     *
     * @param context
     */
    public static void goVIVOSetting(Context context) {
        showActivity(context, "com.iqoo.secure");
    }

    /**
     * 跳转魅族手机管家
     * 操作步骤：权限管理 -> 后台管理 -> 点击应用 -> 允许后台运行
     *
     * @param context
     */
    public static void goMeizuSetting(Context context) {
        showActivity(context, "com.meizu.safe");
    }

    /**
     * 跳转三星智能管理器
     * 操作步骤：自动运行应用程序 -> 打开应用开关 -> 电池管理 -> 未监视的应用程序 -> 添加应用
     *
     * @param context
     */
    public static void goSamsungSetting(Context context) {
        try {
            showActivity(context, "com.samsung.android.sm_cn");
        } catch (Exception e) {
            showActivity(context, "com.samsung.android.sm");
        }
    }

    /**
     * 跳转乐视手机管家
     * 操作步骤：自启动管理 -> 允许应用自启动
     *
     * @param context
     */
    public static void goLetvSetting(Context context) {
        showActivity(context, "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity");
    }

    public static void goSmartisanSetting(Context context) {
        showActivity(context, "com.smartisanos.security");
    }
}