package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

public class MessageSendResponse extends ResponseBase {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        BAD_HASH,
        INCORRECT_TOKEN,
        INCORRECT_ID,
        DATABASE_ERROR
    }

    private String id;

    public MessageSendResponse() {
    }

    public MessageSendResponse(String success, String errorType, String id) {
        super(success, errorType);
        this.id = id;
    }

    public MessageSendResponse(AESCrypto aes) {
        super(aes);
    }

    public MessageSendResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public Long getId(AESCrypto aes) {
        return Long.valueOf(aes.decrypt(id));
    }

    public void setId(Long id, AESCrypto aes) {
        this.id = aes.encrypt(String.valueOf(id));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
