package com.applexis.aimos_android.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DialogMinimal implements Parcelable {

    protected Long id;

    protected String name;

    protected List<UserMinimalInfo> users;

    public DialogMinimal() {
    }

    public DialogMinimal(Long id, String name, List<UserMinimalInfo> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserMinimalInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserMinimalInfo> users) {
        this.users = users;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeList(users);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public DialogMinimal createFromParcel(Parcel in) {
            return new DialogMinimal(in);
        }
        public DialogMinimal[] newArray(int size) {
            return new DialogMinimal[size];
        }
    };
    private DialogMinimal(Parcel in) {
        id = in.readLong();
        name = in.readString();
        users = new ArrayList<>();
        in.readList(users, getClass().getClassLoader());
    }

}
