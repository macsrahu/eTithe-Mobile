package com.prgs.etithe.models;

import java.io.Serializable;

public class ReceiptSettings implements Serializable {
    String key;
    String regionkey;
    int receiptno;
    String prefix;

    public ReceiptSettings() {
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

    public int getReceiptno() {
        return receiptno;
    }

    public void setReceiptno(int receiptno) {
        this.receiptno = receiptno;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
