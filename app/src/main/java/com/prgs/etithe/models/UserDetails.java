package com.prgs.etithe.models;

import java.io.Serializable;

public class UserDetails implements Serializable {
    private String key;
    private String name;
    private String regionkey;
    private String email;
    private String mobile;

    private int isactive;
    private int deleted;
    private String createdon;
    private int usertype;
    private String userkey;
    private String userid;


    public String getUserid() {
        return userid;
    }

    public String getUserkey() {
        return userkey;
    }

    public void setUserkey(String userkey) {
        this.userkey = userkey;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public UserDetails() {
    }

    public String getCreatedon() {
        return createdon;
    }

    public void setCreatedon(String createdon) {
        this.createdon = createdon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailid) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionkey() {
        return regionkey;
    }

    public void setRegionkey(String regionkey) {
        this.regionkey = regionkey;
    }

    public int getIsactive() {
        return isactive;
    }

    public void setIsactive(int isactive) {
        this.isactive = isactive;
    }

    public int getUsertype() {
        return usertype;
    }

    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
}


