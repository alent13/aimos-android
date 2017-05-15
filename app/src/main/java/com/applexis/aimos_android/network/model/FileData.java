package com.applexis.aimos_android.network.model;


import com.applexis.utils.crypto.AESCrypto;

import java.io.File;
import java.util.Date;

public class FileData {

    public static final String OK = "OK";
    public static final String SYNC = "SYNC";
    public static final String ERROR = "ERROR";
    public static final String DOWNLOAD = "DOWNLOAD";
    public static final String UPLOAD = "UPLOAD";
    public static final String DELETE = "DELETE";

    private String id;
    private String name;
    private String path;
    private String isFolder;
    private String parentId;
    private String treeParent;
    private String createDatetime;
    private String lastModificationDatetime;
    private String size;
    private String isPublic;
    private String status;
    private String hash;

    public FileData() {
    }

    public FileData(File file, int treeParent, String hash, AESCrypto aes) {
        this.name = aes.encrypt(file.getName());
        this.isFolder = aes.encrypt(String.valueOf(file.isDirectory()));
        this.treeParent = aes.encrypt(String.valueOf(treeParent));
        this.createDatetime = aes.encrypt(String.valueOf(0));
        this.lastModificationDatetime = aes.encrypt(String.valueOf(file.lastModified()));
        this.size = aes.encrypt(String.valueOf(file.length()));
        this.hash = aes.encrypt(hash);
    }

    public FileData(String id, String name, String isFolder, String parentId,
                    String treeParent, String createDatetime,
                    String lastModificationDatetime, String size,
                    String isPublic, String status, String hash) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.isFolder = isFolder;
        this.parentId = parentId;
        this.treeParent = treeParent;
        this.createDatetime = createDatetime;
        this.lastModificationDatetime = lastModificationDatetime;
        this.size = size;
        this.isPublic = isPublic;
        this.status = status;
        this.hash = hash;
    }

    public FileData(FileData item, String status, AESCrypto aes) {
        this.id = item.id;
        this.name = item.name;
        this.path = item.path;
        this.isFolder = item.isFolder;
        this.parentId = item.parentId;
        this.treeParent = item.treeParent;
        this.createDatetime = item.createDatetime;
        this.lastModificationDatetime = item.lastModificationDatetime;
        this.size = item.size;
        this.isPublic = item.isPublic;
        this.status = aes.encrypt(status);
        this.hash = item.hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileData)) return false;

        FileData fileData = (FileData) o;

        if (id != null ? !id.equals(fileData.id) : fileData.id != null)
            return false;
        if (!name.equals(fileData.name)) return false;
        if (path != null ? !path.equals(fileData.path) : fileData.path != null)
            return false;
        if (!isFolder.equals(fileData.isFolder)) return false;
        if (parentId != null ? !parentId.equals(fileData.parentId) : fileData.parentId != null)
            return false;
        if (treeParent != null ? !treeParent.equals(fileData.treeParent) : fileData.treeParent != null)
            return false;
        if (!createDatetime.equals(fileData.createDatetime)) return false;
        if (!lastModificationDatetime.equals(fileData.lastModificationDatetime))
            return false;
        if (!size.equals(fileData.size)) return false;
        if (!isPublic.equals(fileData.isPublic)) return false;
        if (!status.equals(fileData.status)) return false;
        return hash.equals(fileData.hash);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (treeParent != null ? treeParent.hashCode() : 0);
        result = 31 * result + createDatetime.hashCode();
        return result;
    }

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

    public String getPath(AESCrypto aes) {
        return aes.decrypt(path);
    }

    public void setPath(String path, AESCrypto aes) {
        this.path = aes.encrypt(path);
    }

    public boolean isFolder(AESCrypto aes) {
        return Boolean.valueOf(aes.decrypt(isFolder));
    }

    public void setIsFolder(boolean isFolder, AESCrypto aes) {
        this.isFolder = aes.decrypt(String.valueOf(isFolder));
    }

    public Long getParentId(AESCrypto aes) {
        return Long.valueOf(aes.decrypt(parentId));
    }

    public void setParentId(Long parentId, AESCrypto aes) {
        this.parentId = aes.encrypt(String.valueOf(parentId));
    }

    public int getTreeParent(AESCrypto aes) {
        return Integer.valueOf(aes.decrypt(treeParent));
    }

    public void setTreeParent(int treeParent, AESCrypto aes) {
        this.treeParent = aes.decrypt(String.valueOf(treeParent));
    }

    public Date getCreateDatetime(AESCrypto aes) {
        return new Date(Long.valueOf(aes.decrypt(createDatetime)));
    }

    public void setCreateDatetime(Date createDatetime, AESCrypto aes) {
        this.createDatetime = aes.encrypt(String.valueOf(createDatetime.getTime()));
    }

    public Date getLastModificationDatetime(AESCrypto aes) {
        return new Date(Long.valueOf(aes.decrypt(lastModificationDatetime)));
    }

    public void setLastModificationDatetime(Date lastModificationDatetime, AESCrypto aes) {
        this.lastModificationDatetime = aes.encrypt(String.valueOf(lastModificationDatetime.getTime()));
    }

    public Long getSize(AESCrypto aes) {
        return Long.valueOf(aes.decrypt(size));
    }

    public void setSize(Long size, AESCrypto aes) {
        this.size = aes.encrypt(String.valueOf(size));
    }

    public boolean isPublic(AESCrypto aes) {
        return Boolean.valueOf(aes.decrypt(isPublic));
    }

    public void setIsPublic(boolean isPublic, AESCrypto aes) {
        this.isPublic = aes.encrypt(String.valueOf(isPublic));
    }

    public String getStatus(AESCrypto aes) {
        return aes.decrypt(status);
    }

    public void setStatus(String status, AESCrypto aes) {
        this.status = aes.encrypt(status);
    }

    public String getHash(AESCrypto aes) {
        return aes.decrypt(hash);
    }

    public void setHash(String hash, AESCrypto aes) {
        this.hash = aes.encrypt(hash);
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String isFolder() {
        return isFolder;
    }

    public void setIsFolder(String isFolder) {
        this.isFolder = isFolder;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTreeParent() {
        return treeParent;
    }

    public void setTreeParent(String treeParent) {
        this.treeParent = treeParent;
    }

    public String getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(String createDatetime) {
        this.createDatetime = createDatetime;
    }

    public String getLastModificationDatetime() {
        return lastModificationDatetime;
    }

    public void setLastModificationDatetime(String lastModificationDatetime) {
        this.lastModificationDatetime = lastModificationDatetime;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String isPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

}
