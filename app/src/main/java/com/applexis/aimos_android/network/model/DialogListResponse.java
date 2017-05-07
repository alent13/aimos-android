package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

import java.util.List;

public class DialogListResponse extends ResponseBase {

    public enum ErrorType {
        DATABASE_ERROR,
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN
    }

    private List<DialogMinimal> dialogs;

    public DialogListResponse() {
    }

    public DialogListResponse(String success, String errorType, List<DialogMinimal> dialogs) {
        super(success, errorType);
        this.dialogs = dialogs;
    }

    public DialogListResponse(AESCrypto aes) {
        super(aes);
    }

    public DialogListResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public DialogListResponse(List<DialogMinimal> dialogs, AESCrypto aes) {
        this.dialogs = dialogs;
        this.success = aes.encrypt(String.valueOf(true));
    }

    public List<DialogMinimal> getDialogs() {
        return dialogs;
    }

    public void setDialogs(List<DialogMinimal> dialogs) {
        this.dialogs = dialogs;
    }
}
