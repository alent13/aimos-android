package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

public class DialogResponse extends ResponseBase {

    public enum ErrorType {
        SUCCESS,
        INCORRECT_ID,
        DATABASE_ERROR,
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN
    }

    DialogMinimal dialog;

    public DialogResponse() {
    }

    public DialogResponse(String success, String errorType, DialogMinimal dialog) {
        super(success, errorType);
        this.dialog = dialog;
    }

    public DialogResponse(AESCrypto aes) {
        super(aes);
    }

    public DialogResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public DialogMinimal getDialog() {
        return dialog;
    }

    public void setDialog(DialogMinimal dialog) {
        this.dialog = dialog;
    }
}
