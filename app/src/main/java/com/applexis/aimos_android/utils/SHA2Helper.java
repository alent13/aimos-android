package com.applexis.aimos_android.utils;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;

public class SHA2Helper {

    public static byte[] getSHA512(byte[] data, String salt) {
        MessageDigest md = null;
        byte[] resault = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes("UTF-8"));
            resault = md.digest(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resault;
    }

    public static byte[] getSHA512(String data, String salt) {
        byte[] resault = null;
        try {
            resault = getSHA512(data.getBytes("UTF-8"), salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resault;
    }

    public static String getSHA512String(byte[] data, String salt) {
        byte[] resault = null;
        try {
            resault = getSHA512(data, salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Hex.encodeHexString(resault);
    }

    public static String getSHA512String(String data, String salt) {
        byte[] resault = null;
        try {
            resault = getSHA512(data.getBytes("UTF-8"), salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Hex.encodeHexString(resault);
    }

}
