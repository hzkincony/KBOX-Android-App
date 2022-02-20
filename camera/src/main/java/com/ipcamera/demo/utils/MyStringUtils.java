package com.ipcamera.demo.utils;

import android.annotation.SuppressLint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyStringUtils {

    @SuppressLint("NewApi")
    public static boolean isEmpty(String string) {
        if (null == string || string.isEmpty()) {
            return true;
        }
        return false;
    }

    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /*
     * public static String setNubAddOne(String content, String type) { try { if
     * (content.contains(":")) { String pp[] = content.split(":"); String before
     * = pp[pp.length - 1]; int after = (Integer.parseInt(getNumbers(content)) +
     * 1); return content.replace(before, after + "") .replace(type + ":",
     * "").replace(":", ""); } else { return content; } } catch (Exception e) {
     * return content; } }
     */


    /*摄像机返回var 后面的字段*/
    public static String spitValue(String name, String tag) {
        String[] strs = name.split(";");
        for (int i = 0; i < strs.length; i++) {
            String str1 = strs[i].trim();
            if (str1.startsWith("var ")) {
                str1 = str1.substring(4, str1.length());
            }
            // Log.i(TAG, "str1:" + str1);
            if (str1.startsWith(tag)) {
                String result = str1.substring(str1.indexOf("=") + 1);
                return result;
            }
        }
        return -1 + "";
    }


}
