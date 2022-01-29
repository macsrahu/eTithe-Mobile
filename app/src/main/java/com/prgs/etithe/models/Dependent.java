package com.prgs.etithe.models;

import java.io.Serializable;

public class Dependent implements Serializable {
    String dependent;
    String depbirthdate;
    String relation;
    String key;
    int age;
    public Dependent(){}

    public String getDependent() {
        return dependent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDependent(String dependent) {
        this.dependent = dependent;
    }

    public String getDepbirthdate() {
        return depbirthdate;
    }

    public void setDepbirthdate(String depbirthdate) {
        this.depbirthdate = depbirthdate;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
