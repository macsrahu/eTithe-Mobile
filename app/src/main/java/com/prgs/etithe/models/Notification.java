package com.prgs.etithe.models;

import java.io.Serializable;

public class Notification implements Serializable {
    String title;
    String key;
    String regionkey;
    String messageto;
    String message;
    long messagedon;
    boolean ispublished;
    boolean isactive;

    public Notification() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRegionkey() {
        return regionkey;
    }

    public void setRegionkey(String regionkey) {
        this.regionkey = regionkey;
    }

    public String getMessageto() {
        return messageto;
    }

    public void setMessageto(String messageto) {
        this.messageto = messageto;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMessagedon() {
        return messagedon;
    }

    public void setMessagedon(long messagedon) {
        this.messagedon = messagedon;
    }

    public boolean isIspublished() {
        return ispublished;
    }

    public void setIspublished(boolean ispublished) {
        this.ispublished = ispublished;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }
}
