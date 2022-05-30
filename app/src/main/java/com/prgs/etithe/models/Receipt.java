package com.prgs.etithe.models;

public class Receipt {
    String key;
    String donorkey;
    String receiptno;
    String donor;
    String address;
    String regionkey;
    String repkey;
    String reptype;
    double amount;
    String receiptdate;
    long createdon;
    String signurl;
    String paymonth;
    String notes;
    int cancel;
    String reason;
    public Receipt() {
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymonth() {
        return paymonth;
    }

    public void setPaymonth(String paymonth) {
        this.paymonth = paymonth;
    }

    public String getDonor() {
        return donor;
    }

    public void setDonor(String donor) {
        this.donor = donor;
    }

    public String getSignurl() {
        return signurl;
    }

    public void setSignurl(String signurl) {
        this.signurl = signurl;
    }

    public String getDonorkey() {
        return donorkey;
    }

    public void setDonorkey(String donorkey) {
        this.donorkey = donorkey;
    }

    public String getReceiptno() {
        return receiptno;
    }

    public void setReceiptno(String receiptno) {
        this.receiptno = receiptno;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getRepkey() {
        return repkey;
    }

    public void setRepkey(String repkey) {
        this.repkey = repkey;
    }

    public String getReptype() {
        return reptype;
    }

    public void setReptype(String reptype) {
        this.reptype = reptype;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReceiptdate() {
        return receiptdate;
    }

    public void setReceiptdate(String receiptdate) {
        this.receiptdate = receiptdate;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public int getCancel() {
        return cancel;
    }

    public void setCancel(int cancel) {
        this.cancel = cancel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
