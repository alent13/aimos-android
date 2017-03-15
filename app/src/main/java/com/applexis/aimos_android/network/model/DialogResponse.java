package com.applexis.aimos_android.network.model;

import java.util.ArrayList;
import java.util.List;

public class DialogResponse extends DialogMinimal {

    public enum ErrorType {
        SUCCESS,
        INCORRECT_ID,
        DATABASE_ERROR,
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN
    }

    private boolean success;

    private String errorType;

    public DialogResponse() {
        this.success = false;
    }

    public DialogResponse(String errorType) {
        if (errorType == ErrorType.SUCCESS.name()) {
            this.success = true;
        } else {
            this.success = false;
        }
        this.errorType = errorType;
    }

    public DialogResponse(Long id, String name, List<UserMinimalInfo> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserMinimalInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserMinimalInfo> users) {
        this.users = users;
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
