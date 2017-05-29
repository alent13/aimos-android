package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

import java.util.List;

public class SyncResponse extends ResponseBase {

    public enum ErrorType {
        BAD_PUBLIC_KEY,
        INCORRECT_TOKEN,
        DATABASE_ERROR
    }

    private List<FileData> fileDataList;

    public SyncResponse() {
    }

    public SyncResponse(List<FileData> fileDataList, AESCrypto aes) {
        this.fileDataList = fileDataList;
        success = aes.encrypt(String.valueOf(true));
    }

    public SyncResponse(AESCrypto aes) {
        super(aes);
    }

    public SyncResponse(String errorType, AESCrypto aes) {
        super(errorType, aes);
    }

    public List<FileData> getFileDataList() {
        return fileDataList;
    }

    public void setFileDataList(List<FileData> fileDataList) {
        this.fileDataList = fileDataList;
    }
}
