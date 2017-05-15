package com.applexis.aimos_android.network.model;

import com.applexis.utils.crypto.AESCrypto;

public class FileUploadResponse extends ResponseBase {

    private FileData fileData;

    public FileUploadResponse(AESCrypto aes) {
        super(aes);
    }

    public FileUploadResponse(String errorType, AESCrypto aes, FileData fileData) {
        super(errorType, aes);
        this.fileData = fileData;
    }

    public FileUploadResponse(FileData fileData, AESCrypto aes) {
        this.success = aes.encrypt(String.valueOf(true));
        this.fileData = fileData;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }
}
