package com.prgs.etithe.models;

import java.io.Serializable;

public class AreaPerson implements Serializable {
    String key;
    String areakey;
    String area;
    String person;
    String gender;
    String married;
    String addressline1;
    String addressline2;
    String city;
    String pincode;
    String state;
    String referedby;
    String email;
    String mobile;
    String panno;
    String regionkey;
    String officerkey;
    String whatsappno;
    boolean isactive;
    int deleted;
    String createdon;
    String updatedon;
    String imgurl;
    public AreaPerson() {
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getWhatsappno() {
        return whatsappno;
    }

    public void setWhatsappno(String whatsapp) {
        this.whatsappno = whatsapp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMarried() {
        return married;
    }

    public void setMarried(String married) {
        this.married = married;
    }

    public String getAddressline1() {
        return addressline1;
    }

    public void setAddressline1(String addressline1) {
        this.addressline1 = addressline1;
    }

    public String getAddressline2() {
        return addressline2;
    }

    public void setAddressline2(String addressline2) {
        this.addressline2 = addressline2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReferedby() {
        return referedby;
    }

    public void setReferedby(String referedby) {
        this.referedby = referedby;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPanno() {
        return panno;
    }

    public void setPanno(String panno) {
        this.panno = panno;
    }

    public String getRegionkey() {
        return regionkey;
    }

    public void setRegionkey(String regionkey) {
        this.regionkey = regionkey;
    }

    public String getOfficerkey() {
        return officerkey;
    }

    public void setOfficerkey(String officerkey) {
        this.officerkey = officerkey;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public String getCreatedon() {
        return createdon;
    }

    public void setCreatedon(String createdon) {
        this.createdon = createdon;
    }

    public String getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(String updatedon) {
        this.updatedon = updatedon;
    }

    public String getAreakey() {
        return areakey;
    }

    public void setAreakey(String areakey) {
        this.areakey = areakey;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
