package com.applexis.aimos_android.utils;

import java.security.MessageDigest;

/**
 * @author applexis
 */

public class SHAHelper {

    public static byte[] getSHA(byte[] data, String salt) {
        MessageDigest md = null;
        byte[] resault = null;
        try {
            md = MessageDigest.getInstance("SHA");
            md.update(salt.getBytes("UTF-8"));
            resault = md.digest(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resault;
    }

}
