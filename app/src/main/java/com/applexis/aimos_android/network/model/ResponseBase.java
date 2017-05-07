package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

public abstract class ResponseBase {

    protected String success;

    protected String errorType;

    public ResponseBase() {
    }

    public ResponseBase(String success, String errorType) {
        this.success = success;
        this.errorType = errorType;
    }

    public ResponseBase(AESCrypto aes) {
        this.success = aes.encrypt("false");
    }

    public ResponseBase(String errorType, AESCrypto aes) {
        this.success = aes.encrypt("false");
        this.errorType = aes.encrypt(errorType);
    }

    public boolean check(AESCrypto aes) {
        return !success.equals("false") || (aes != null && isSuccess(aes));
    }

    public boolean isSuccess(AESCrypto aes) {
        return Boolean.getBoolean(aes.decrypt(success));
    }

    public void setSuccess(boolean success, AESCrypto aes) {
        this.success = aes.encrypt(String.valueOf(success));
    }

    public String getErrorType(AESCrypto aes) {
        return aes.decrypt(errorType);
    }

    public void setErrorType(String errorType, AESCrypto aes) {
        this.errorType = aes.encrypt(errorType);
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
}
