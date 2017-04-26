package com.applexis.aimos_android.network.model;

/**
 * Created by applexis on 4/21/2017.
 */

public class StorageData {

    public enum Type {
        FOLDER,
        FILE
    }

    public enum Status {
        OK,
        SYNC,
        ERROR
    }

    private Type type;
    private String name;
    private Long createDatetime;
    private Long size;
    private Boolean isPublic;
    private Status status;

    public StorageData(Type type, String name, Long createDatetime, Long size, Boolean isPublic, Status status) {
        this.type = type;
        this.name = name;
        this.createDatetime = createDatetime;
        this.size = size;
        this.isPublic = isPublic;
        this.status = status;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Long createDatetime) {
        this.createDatetime = createDatetime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
