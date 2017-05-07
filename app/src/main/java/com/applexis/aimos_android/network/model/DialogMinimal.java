package com.applexis.aimos_android.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.applexis.utils.crypto.AESCrypto;

import java.util.ArrayList;
import java.util.List;

public class DialogMinimal implements Parcelable {

    protected String id;

    protected String name;

    private UserMinimalInfo lastSender;

    private MessageMinimal lastMessage;

    protected List<UserMinimalInfo> users;

    public DialogMinimal() {
    }

    public DialogMinimal(String id, String name, UserMinimalInfo lastSender, MessageMinimal lastMessage, List<UserMinimalInfo> users) {
        this.id = id;
        this.name = name;
        this.lastSender = lastSender;
        this.lastMessage = lastMessage;
        this.users = users;
    }

    public DialogMinimal(Long id, String name, List<UserMinimalInfo> users, AESCrypto aes) {
        this.id = aes.encrypt(String.valueOf(id));
        this.name = aes.encrypt(name);
        this.users = users;
    }

    private DialogMinimal(Parcel in) {
        id = in.readString();
        name = in.readString();
        users = new ArrayList<>();
        in.readList(users, getClass().getClassLoader());
    }

    public static final Creator<DialogMinimal> CREATOR = new Creator<DialogMinimal>() {
        @Override
        public DialogMinimal createFromParcel(Parcel in) {
            return new DialogMinimal(in);
        }

        @Override
        public DialogMinimal[] newArray(int size) {
            return new DialogMinimal[size];
        }
    };

    public Long getId(AESCrypto aes) {
        return Long.valueOf(aes.decrypt(id));
    }

    public void setId(Long id, AESCrypto aes) {
        this.id = aes.encrypt(String.valueOf(id));
    }

    public String getName(AESCrypto aes) {
        return aes.decrypt(name);
    }

    public void setName(String name, AESCrypto aes) {
        this.name = aes.encrypt(name);
    }

    public List<UserMinimalInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserMinimalInfo> users) {
        this.users = users;
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

    public UserMinimalInfo getLastSender() {
        return lastSender;
    }

    public void setLastSender(UserMinimalInfo lastSender) {
        this.lastSender = lastSender;
    }

    public MessageMinimal getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageMinimal lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeList(users);
        dest.writeParcelable(lastMessage, flags);
        dest.writeParcelable(lastSender, flags);
    }
}
