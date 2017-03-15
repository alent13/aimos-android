package com.applexis.aimos_android.network.model;

public class MessageSendResponse {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        BAD_HASH,
        INCORRECT_TOKEN,
        INCORRECT_ID,
        DATABASE_ERROR
    }

    private Long id;

    private boolean success;

    private String errorType;

    public MessageSendResponse() {
        success = false;
    }

    public MessageSendResponse(String errorType) {
        this.success = false;
        this.errorType = errorType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
