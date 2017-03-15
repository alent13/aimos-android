package com.applexis.aimos_android.network.model;

import org.apache.commons.codec.binary.Base64;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class GetMessageResponse {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN,
        INCORRECT_ID,
        DATABASE_ERROR
    }

    private List<MessageMinimal> messageMinimals;

    public boolean success;

    public String errorType;

    public GetMessageResponse() {
    }

    public List<MessageMinimal> getMessageMinimals() {
        return messageMinimals;
    }

    public void setMessageMinimals(List<MessageMinimal> messageMinimals) {
        this.messageMinimals = messageMinimals;
    }

    public GetMessageResponse(List<MessageMinimal> messageMinimals, boolean success, String errorType) {
        this.messageMinimals = messageMinimals;
        this.success = success;
        this.errorType = errorType;
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
