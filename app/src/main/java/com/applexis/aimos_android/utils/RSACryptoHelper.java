package com.applexis.aimos_android.utils;

import android.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSACryptoHelper {

    public static KeyPair generateKeyPair() {
        KeyPairGenerator generator = null;
        KeyPair pair = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            pair = generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return pair;
    }

    public static KeyPair generateKeyPair(int keySize) {
        KeyPairGenerator generator = null;
        KeyPair pair = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            pair = generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return pair;
    }

    public static byte[] encrypt(PublicKey key, byte[] plaintext) {
        Cipher cipher = null;
        byte[] encrypted = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(plaintext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public static byte[] decrypt(PrivateKey key, byte[] ciphertext) {
        Cipher cipher = null;
        byte[] decrypted = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = cipher.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public static byte[] encrypt(PrivateKey key, byte[] plaintext) {
        Cipher cipher = null;
        byte[] encrypted = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(plaintext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public static byte[] decrypt(PublicKey key, byte[] ciphertext) {
        Cipher cipher = null;
        byte[] decrypted = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = cipher.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public static PublicKey getPublicKey(String key) {
        PublicKey publicKey = null;
        try {
            byte[] byteKey = Base64.decode(key, Base64.DEFAULT);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String key) {
        PrivateKey privateKey = null;
        try {
            byte[] byteKey = Base64.decode(key, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return privateKey;
    }

    public static String getPublicKeyString(PublicKey key) {
        KeyFactory kf = null;
        String str = null;
        try {
            kf = KeyFactory.getInstance("RSA");
            str = Base64.encodeToString(kf.getKeySpec(key, X509EncodedKeySpec.class).getEncoded(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getPrivateKeyString(PrivateKey key) {
        KeyFactory kf = null;
        String str = null;
        try {
            kf = KeyFactory.getInstance("RSA");
            str = Base64.encodeToString(kf.getKeySpec(key, PKCS8EncodedKeySpec.class).getEncoded(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return str;
    }

}
