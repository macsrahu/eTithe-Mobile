package com.prgs.etithe.models;

import java.io.Serializable;

public class Relations implements Serializable {
    String relation;
    String key;
    int position;
    public Relations(){}

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    @Override
    public String toString() {
        return relation;
    }
}
