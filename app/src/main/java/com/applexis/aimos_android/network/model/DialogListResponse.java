package com.applexis.aimos_android.network.model;

import java.util.List;

public class DialogListResponse {

    public enum ErrorType {
        DATABASE_ERROR,
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN
    }

    private boolean success;

    private String errorType;

    private List<DialogMinimal> dialogs;

    public DialogListResponse() {
        this.success = false;
    }

    public DialogListResponse(String errorType) {
        this.errorType = errorType;
        this.success = false;
    }

    public DialogListResponse(List<DialogMinimal> dialogs) {
        this.dialogs = dialogs;
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

    public List<DialogMinimal> getDialogs() {
        return dialogs;
    }

    public void setDialogs(List<DialogMinimal> dialogs) {
        this.dialogs = dialogs;
    }
}
