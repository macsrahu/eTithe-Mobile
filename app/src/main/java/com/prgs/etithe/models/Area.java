package com.prgs.etithe.models;

import java.io.Serializable;

public class Area implements Serializable {
    String area;
    String cretedon;
    int deleted;
    Boolean isactive;
    String regionkey;
    String key;
    public Area(){}

    public String toString(){
        return area;
    }
    public String getArea() {
        return area;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCretedon() {
        return cretedon;
    }

    public void setCretedon(String cretedon) {
        this.cretedon = cretedon;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public String getRegionkey() {
        return regionkey;
    }

    public void setRegionkey(String regionkey) {
        this.regionkey = regionkey;
    }
}
