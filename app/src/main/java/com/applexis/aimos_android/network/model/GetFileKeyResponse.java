package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

public class GetFileKeyResponse extends ResponseBase {

    public enum ErrorType {
        DATABASE_ERROR,
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN
    }

    private String key;

    public GetFileKeyResponse(String key, AESCrypto aes, boolean success) {
        this.key = aes.encrypt(key);
        this.success = aes.encrypt(String.valueOf(success));
    }

    public GetFileKeyResponse(AESCrypto aes) {
        super(aes);
    }

    public GetFileKeyResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey(AESCrypto aes) {
        return aes.decrypt(key);
    }

    public void setKey(String key, AESCrypto aes) {
        this.key = aes.encrypt(key);
    }
}
