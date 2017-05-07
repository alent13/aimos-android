package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

import java.util.List;

public class ContactResponse extends ResponseBase {

    public enum ErrorType {
        SUCCESS,
        DATABASE_ERROR,
        INCORRECT_ID,
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN
    }

    private List<UserMinimalInfo> userList;

    public ContactResponse() {
    }

    public ContactResponse(String success, String errorType, List<UserMinimalInfo> userList) {
        super(success, errorType);
        this.userList = userList;
    }

    public ContactResponse(AESCrypto aes) {
        super(aes);
    }

    public ContactResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public ContactResponse(List<UserMinimalInfo> userList, AESCrypto aes) {
        if (userList != null) {
            this.userList = userList;
        }
        this.success = aes.encrypt("true");
    }

    public List<UserMinimalInfo> getUserList() {
        return userList;
    }

    public void setUserList(List<UserMinimalInfo> userList) {
        this.userList = userList;
    }
}
