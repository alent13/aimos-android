package com.applexis.aimos_android.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.applexis.utils.crypto.AESCrypto;

import java.util.Date;

/**
 * @author applexis
 */

public class MessageMinimal implements Parcelable {

    private String idUserFrom;
    private String eText;
    private String key;
    private String signature;
    private String publicKey;
    private String datetime;

    public MessageMinimal() {
    }

    public MessageMinimal(String idUserFrom, String eText, String key, String signature, String publicKey, String datetime) {
        this.idUserFrom = idUserFrom;
        this.eText = eText;
        this.key = key;
        this.signature = signature;
        this.publicKey = publicKey;
        this.datetime = datetime;
    }

    public MessageMinimal(Long idUserFrom, String eText, String key,
                          String signature, String publicKey,
                          Date datetime, AESCrypto aes) {
        this.idUserFrom = aes.encrypt(String.valueOf(idUserFrom));
        this.eText = aes.encrypt(eText);
        this.key = aes.encrypt(key);
        this.signature = aes.encrypt(signature);
        this.publicKey = aes.encrypt(publicKey);
        this.datetime = aes.encrypt(String.valueOf(datetime.getTime()));
    }

    protected MessageMinimal(Parcel in) {
        idUserFrom = in.readString();
        eText = in.readString();
        key = in.readString();
        signature = in.readString();
        publicKey = in.readString();
        datetime = in.readString();
    }

    public static final Creator<MessageMinimal> CREATOR = new Creator<MessageMinimal>() {
        @Override
        public MessageMinimal createFromParcel(Parcel in) {
            return new MessageMinimal(in);
        }

        @Override
        public MessageMinimal[] newArray(int size) {
            return new MessageMinimal[size];
        }
    };

    public Long getIdUserFrom(AESCrypto aes) {
        return Long.valueOf(aes.decrypt(idUserFrom));
    }

    public void setIdUserFrom(Long idUserFrom, AESCrypto aes) {
        this.idUserFrom = aes.encrypt(String.valueOf(idUserFrom));
    }

    public String geteText(AESCrypto aes) {
        return aes.decrypt(eText);
    }

    public void seteText(String eText, AESCrypto aes) {
        this.eText = aes.encrypt(eText);
    }

    public String getKey(AESCrypto aes) {
        return aes.decrypt(key);
    }

    public void setKey(String key, AESCrypto aes) {
        this.key = aes.encrypt(key);
    }

    public String getSignature(AESCrypto aes) {
        return aes.decrypt(signature);
    }

    public void setSignature(String signature, AESCrypto aes) {
        this.signature = aes.encrypt(signature);
    }

    public String getPublicKey(AESCrypto aes) {
        return aes.decrypt(publicKey);
    }

    public void setPublicKey(String publicKey, AESCrypto aes) {
        this.publicKey = aes.encrypt(publicKey);
    }

    public Date getDatetime(AESCrypto aes) {
        return new Date(Long.valueOf(aes.decrypt(datetime)));
    }

    public void setDatetime(Date datetime, AESCrypto aes) {
        this.datetime = aes.encrypt(String.valueOf(datetime.getTime()));
    }

    public String getIdUserFrom() {
        return idUserFrom;
    }

    public void setIdUserFrom(String idUserFrom) {
        this.idUserFrom = idUserFrom;
    }

    public String geteText() {
        return eText;
    }

    public void seteText(String eText) {
        this.eText = eText;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idUserFrom);
        dest.writeString(eText);
        dest.writeString(key);
        dest.writeString(signature);
        dest.writeString(publicKey);
        dest.writeString(datetime);
    }
}
