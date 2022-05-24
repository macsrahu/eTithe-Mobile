package com.prgs.etithe.models;

import java.io.Serializable;

public class Salutations implements Serializable {

    String code;
    String salutation;

    public Salutations(){}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        salutation = salutation;
    }
}
