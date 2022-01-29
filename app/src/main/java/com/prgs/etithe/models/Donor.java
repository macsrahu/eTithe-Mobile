package com.prgs.etithe.models;

import java.io.Serializable;

public class Donor implements Serializable {
    String key;
    String donor;
    String birthdate;
    String gender;
    String married;
    String weddate;
    String addrline1;
    String addrline2;
    String city;
    String pincode;
    String district;
    String state;
    String country;
    String email;
    String mobile;
    String whatsapp;
    String aadhar;
    String pan;
    String imgurl;
    String regionkey;
    String personkey;
    String officerkey;
    String enrolledby;
    String membertype;
    boolean isactive;
    int deleted;
    String createdon;
    String updatedon;
    String areakey;
    String area;
    long transactionon;
    public Donor() {
    }

    public long getTransactionon() {
        return transactionon;
    }

    public void setTransactionon(long transactionon) {
        this.transactionon = transactionon;
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

    public String getMembertype() {
        return membertype;
    }

    public void setMembertype(String membertype) {
        this.membertype = membertype;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDonor() {
        return donor;
    }

    public void setDonor(String donor) {
        this.donor = donor;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
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

    public String getWeddate() {
        return weddate;
    }

    public void setWeddate(String weddate) {
        this.weddate = weddate;
    }

    public String getAddrline1() {
        return addrline1;
    }

    public void setAddrline1(String addrline1) {
        this.addrline1 = addrline1;
    }

    public String getAddrline2() {
        return addrline2;
    }

    public void setAddrline2(String addrline2) {
        this.addrline2 = addrline2;
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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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


    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getRegionkey() {
        return regionkey;
    }

    public void setRegionkey(String regionkey) {
        this.regionkey = regionkey;
    }

    public String getPersonkey() {
        return personkey;
    }

    public void setPersonkey(String personkey) {
        this.personkey = personkey;
    }

    public String getOfficerkey() {
        return officerkey;
    }

    public void setOfficerkey(String officerkey) {
        this.officerkey = officerkey;
    }

    public String getEnrolledby() {
        return enrolledby;
    }

    public void setEnrolledby(String enrolledby) {
        this.enrolledby = enrolledby;
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

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsappno) {
        this.whatsapp = whatsappno;
    }

    public String getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(String updatedon) {
        this.updatedon = updatedon;
    }
}
