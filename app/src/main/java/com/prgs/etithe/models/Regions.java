package com.prgs.etithe.models;

import java.io.Serializable;

public class Regions implements Serializable {
    String region;
    String key;
    String addressline1;
    String addressline2;
    String city;
    String pincode;
    String mobile;
    String email;
    Boolean isactive;
    String cpemail;
    String epmobile;
    String cpname;

    public Regions() {

    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public String getCpemail() {
        return cpemail;
    }

    public void setCpemail(String cpemail) {
        this.cpemail = cpemail;
    }

    public String getEpmobile() {
        return epmobile;
    }

    public void setEpmobile(String epmobile) {
        this.epmobile = epmobile;
    }

    public String getCpname() {
        return cpname;
    }

    public void setCpname(String cpname) {
        this.cpname = cpname;
    }
}
