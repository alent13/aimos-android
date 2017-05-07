package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

public class AddContactResponse extends ResponseBase {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN,
        INCORRECT_ID,
        DATABASE_ERROR,
        ALREADY_FRIENDS
    }

    private UserMinimalInfo userMinimalInfo;

    public AddContactResponse() {
    }

    public AddContactResponse(String success, String errorType, UserMinimalInfo userMinimalInfo) {
        super(success, errorType);
        this.userMinimalInfo = userMinimalInfo;
    }

    public AddContactResponse(AESCrypto aes) {
        super(aes);
    }

    public AddContactResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public AddContactResponse(UserMinimalInfo userMinimalInfo, AESCrypto aes) {
        if(userMinimalInfo != null) {
            this.userMinimalInfo = userMinimalInfo;
            this.success = aes.encrypt("true");
        }
    }

    public UserMinimalInfo getUserMinimalInfo() {
        return userMinimalInfo;
    }

    public void setUserMinimalInfo(UserMinimalInfo userMinimalInfo) {
        this.userMinimalInfo = userMinimalInfo;
    }
}
