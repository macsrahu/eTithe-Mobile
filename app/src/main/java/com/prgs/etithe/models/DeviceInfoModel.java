package com.prgs.etithe.models;

import android.os.ParcelUuid;

import java.io.Serializable;

public class DeviceInfoModel implements Serializable {

    public DeviceInfoModel(){}

    String name;
    String address;
    int type;
    int bondState;
    ParcelUuid uuids;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

    public ParcelUuid getUuids() {
        return uuids;
    }

    public void setUuids(ParcelUuid uuids) {
        this.uuids = uuids;
    }

    @Override
    public String toString() {
        return name;
    }
}
