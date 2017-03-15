package com.applexis.aimos_android.utils;

import android.util.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author applexis
 */

public class DSACryptoHelper {

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            keyGen.initialize(1024, random);

            return keyGen.generateKeyPair();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] generateSignature(PrivateKey priv, byte[] info) {
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initSign(priv);
            dsa.update(info);
            return dsa.sign();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean verifySignature(PublicKey pub, byte[] data, byte[] signature) {
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initVerify(pub);
            dsa.update(data);
            return dsa.verify(signature);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getPublicKeyString(PublicKey key) {
        KeyFactory kf = null;
        String str = null;
        try {
            kf = KeyFactory.getInstance("DSA");
            str = Base64.encodeToString(kf.getKeySpec(key, X509EncodedKeySpec.class).getEncoded(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static PublicKey getPublicKey(String key) {
        PublicKey publicKey = null;
        try {
            byte[] byteKey = Base64.decode(key, Base64.DEFAULT);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("DSA");
            publicKey = kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

}
