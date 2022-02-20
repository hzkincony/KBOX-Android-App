package com.ipcamera.demo.utils;

public class VuidUtils {
    /**
     * 判断是否是VUID
     *
     * @param uid
     * @return
     */
    public static boolean isVuid(String uid) {
        String uidRegex = "[a-zA-Z]{1,}\\d{7,}.*[a-zA-Z]{1,}";
        if (uid.matches(uidRegex)) {
            return true;
        }
        return false;
    }
}