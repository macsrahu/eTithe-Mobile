package com.prgs.etithe.models;

import java.io.Serializable;

public class Settings implements Serializable {

    long lastorderon;
    long ordercount;
    String prefix;
    String quotaprefix;
    long quotacount;
    int leadingzerolenth;

    public void Settings(){}

    public long getLastorderon() {
        return lastorderon;
    }

    public void setLastorderon(long lastorderon) {
        this.lastorderon = lastorderon;
    }

    public long getOrdercount() {
        return ordercount;
    }

    public void setOrdercount(long ordercount) {
        this.ordercount = ordercount;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getLeadingzerolenth() {
        return leadingzerolenth;
    }

    public void setLeadingzerolenth(int leadingzerolenth) {
        this.leadingzerolenth = leadingzerolenth;
    }

    public String getQuotaprefix() {
        return quotaprefix;
    }

    public void setQuotaprefix(String quotaprefix) {
        this.quotaprefix = quotaprefix;
    }

    public long getQuotacount() {
        return quotacount;
    }

    public void setQuotacount(long quotacount) {
        this.quotacount = quotacount;
    }
}
