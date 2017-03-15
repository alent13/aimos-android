package com.applexis.aimos_android.network.model;

import java.util.List;

public class ContactResponse {

    public enum ErrorType {
        SUCCESS,
        DATABASE_ERROR,
        INCORRECT_ID,
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN
    }

    private boolean success;

    private String errorType;

    private List<UserMinimalInfo> userList;

    public ContactResponse() {
        this.success = false;
    }

    public ContactResponse(String errorType) {
        if (errorType == ErrorType.SUCCESS.name()) {
            this.success = true;
        } else {
            this.success = false;
        }
        this.errorType = errorType;
    }

    public ContactResponse(List<UserMinimalInfo> userList) {
        if (userList != null) {
            this.userList = userList;
        }
        this.success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public List<UserMinimalInfo> getUserList() {
        return userList;
    }

    public void setUserList(List<UserMinimalInfo> userList) {
        this.userList = userList;
    }
}
