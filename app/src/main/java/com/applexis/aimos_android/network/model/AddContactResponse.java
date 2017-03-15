package com.applexis.aimos_android.network.model;

public class AddContactResponse {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN,
        INCORRECT_ID,
        DATABASE_ERROR
    }

    private UserMinimalInfo userMinimalInfo;

    public boolean success;

    public String errorType;

    public AddContactResponse() {
        this.success = false;
    }

    public AddContactResponse(String errorType) {
        this.success = false;
        this.errorType = errorType;
    }

    public AddContactResponse(UserMinimalInfo userMinimalInfo) {
        if(userMinimalInfo != null) {
            this.userMinimalInfo = userMinimalInfo;
            this.success = true;
        }
    }

    public UserMinimalInfo getUserMinimalInfo() {
        return userMinimalInfo;
    }

    public void setUserMinimalInfo(UserMinimalInfo userMinimalInfo) {
        this.userMinimalInfo = userMinimalInfo;
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
}
