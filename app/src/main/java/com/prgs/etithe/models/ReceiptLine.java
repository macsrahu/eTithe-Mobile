package com.prgs.etithe.models;

import java.io.Serializable;

public class ReceiptLine implements Serializable {
    String fundtype;
    String paymode;
    String bankname;
    String chequeno;
    String chequedate;
    Double amount;
    String fundkey;
    String lineitem;
    String key;

    public ReceiptLine(){}

    public String getFundkey() {
        return fundkey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setFundkey(String fundkey) {
        this.fundkey = fundkey;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getLineitem() {
        return lineitem;
    }

    public void setLineitem(String lineitem) {
        this.lineitem = lineitem;
    }

    public String getFundtype() {
        return fundtype;
    }

    public void setFundtype(String fundtype) {
        this.fundtype = fundtype;
    }

    public String getPaymode() {
        return paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getChequeno() {
        return chequeno;
    }

    public void setChequeno(String chequeno) {
        this.chequeno = chequeno;
    }

    public String getChequedate() {
        return chequedate;
    }

    public void setChequedate(String chequedate) {
        this.chequedate = chequedate;
    }
}
