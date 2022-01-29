package com.prgs.etithe.models;

import java.io.Serializable;

public class FundType implements Serializable {
    String fundtype;
    String code;
    String key;
    Boolean isactive;
    public FundType(){
    }

    public String getFundtype() {
        return fundtype;
    }

    public void setFundtype(String fundtype) {
        this.fundtype = fundtype;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    @Override
    public String toString() {
        return fundtype;
    }
}
