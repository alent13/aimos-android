package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

public class LoginResponse extends ResponseBase {

    public enum ErrorType {
        USER_ALREADY_EXIST,
        BAD_PUBLIC_KEY,
        INCORRECT_PASSWORD,
        INCORRECT_TOKEN
    }

    private UserMinimalInfo userMinimalInfo;

    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String success, String errorType, UserMinimalInfo userMinimalInfo, String token) {
        super(success, errorType);
        this.userMinimalInfo = userMinimalInfo;
        this.token = token;
    }

    public LoginResponse(AESCrypto aes) {
        super(aes);
    }

    public LoginResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public String getToken(AESCrypto aes) {
        return aes.decrypt(token);
    }

    public void setToken(String token, AESCrypto aes) {
        this.token = aes.encrypt(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserMinimalInfo getUserMinimalInfo() {
        return userMinimalInfo;
    }

    public void setUserMinimalInfo(UserMinimalInfo userMinimalInfo) {
        this.userMinimalInfo = userMinimalInfo;
    }
}
