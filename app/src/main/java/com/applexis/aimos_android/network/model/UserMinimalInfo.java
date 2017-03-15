package com.applexis.aimos_android.network.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserMinimalInfo implements Parcelable {

    private long id;

    private String name;

    private String surname;

    private String login;

    private String img;

    public UserMinimalInfo() {
    }

    public UserMinimalInfo(long id, String name, String surname, String login) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.login = login;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(surname);
        parcel.writeString(login);
        parcel.writeString(img);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UserMinimalInfo createFromParcel(Parcel in) {
            return new UserMinimalInfo(in);
        }
        public UserMinimalInfo[] newArray(int size) {
            return new UserMinimalInfo[size];
        }
    };
    private UserMinimalInfo(Parcel in) {
        id = in.readLong();
        name = in.readString();
        surname = in.readString();
        login = in.readString();
        img = in.readString();
    }
}
