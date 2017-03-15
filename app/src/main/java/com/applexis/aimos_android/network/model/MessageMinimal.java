package com.applexis.aimos_android.network.model;

import java.util.Date;

/**
 * @author applexis
 */

public class MessageMinimal {

    private Long idUserFrom;
    private String eText;
    private String key;
    private String signature;
    private String publicKey;
    private Date datetime;

    public MessageMinimal() {
    }

    public MessageMinimal(Long idUserFrom, String eText, String key, String signature, String publicKey, Date datetime) {
        this.idUserFrom = idUserFrom;
        this.eText = eText;
        this.key = key;
        this.signature = signature;
        this.publicKey = publicKey;
        this.datetime = datetime;
    }

    public Long getIdUserFrom() {
        return idUserFrom;
    }

    public void setIdUserFrom(Long idUserFrom) {
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

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
}
