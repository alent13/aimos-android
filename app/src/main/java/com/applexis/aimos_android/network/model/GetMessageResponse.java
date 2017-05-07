package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

import java.util.List;

public class GetMessageResponse extends ResponseBase {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN,
        INCORRECT_ID,
        DATABASE_ERROR
    }

    private List<MessageMinimal> messageMinimals;

    public GetMessageResponse() {
    }

    public GetMessageResponse(String success, String errorType, List<MessageMinimal> messageMinimals) {
        super(success, errorType);
        this.messageMinimals = messageMinimals;
    }

    public GetMessageResponse(AESCrypto aes) {
        super(aes);
    }

    public GetMessageResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public List<MessageMinimal> getMessageMinimals() {
        return messageMinimals;
    }

    public void setMessageMinimals(List<MessageMinimal> messageMinimals) {
        this.messageMinimals = messageMinimals;
    }
}
