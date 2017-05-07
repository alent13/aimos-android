package com.applexis.aimos_android.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.applexis.utils.crypto.AESCrypto;

public class UserMinimalInfo implements Parcelable {

    private String id;

    private String name;

    private String surname;

    private String login;

    private String img;

    public UserMinimalInfo() {
    }

    public UserMinimalInfo(String id, String name, String surname, String login, String img) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.img = img;
    }

    public UserMinimalInfo(long id, String name, String surname,
                           String login, AESCrypto aes) {
        this.id = aes.encrypt(String.valueOf(id));
        this.name = aes.encrypt(name);
        this.surname = aes.encrypt(surname);
        this.login = aes.encrypt(login);
    }

    protected UserMinimalInfo(Parcel in) {
        id = in.readString();
        name = in.readString();
        surname = in.readString();
        login = in.readString();
        img = in.readString();
    }

    public static final Creator<UserMinimalInfo> CREATOR = new Creator<UserMinimalInfo>() {
        @Override
        public UserMinimalInfo createFromParcel(Parcel in) {
            return new UserMinimalInfo(in);
        }

        @Override
        public UserMinimalInfo[] newArray(int size) {
            return new UserMinimalInfo[size];
        }
    };

    public long getId(AESCrypto aes) {
        return Long.valueOf(aes.decrypt(id));
    }

    public void setId(long id, AESCrypto aes) {
        this.id = aes.encrypt(String.valueOf(id));
    }

    public String getName(AESCrypto aes) {
        return aes.decrypt(name);
    }

    public void setName(String name, AESCrypto aes) {
        this.name = aes.encrypt(name);
    }

    public String getSurname(AESCrypto aes) {
        return aes.decrypt(surname);
    }

    public void setSurname(String surname, AESCrypto aes) {
        this.surname = aes.encrypt(surname);
    }

    public String getLogin(AESCrypto aes) {
        return aes.decrypt(login);
    }

    public void setLogin(String login, AESCrypto aes) {
        this.login = aes.encrypt(login);
    }

    public String getImg(AESCrypto aes) {
        return aes.decrypt(img);
    }

    public void setImg(String img, AESCrypto aes) {
        this.img = aes.encrypt(img);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(login);
        dest.writeString(img);
    }
}
