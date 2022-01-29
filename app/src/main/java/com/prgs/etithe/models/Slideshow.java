package com.prgs.etithe.models;

public class Slideshow {

    String key;
    String title;
    String imagedesc;
    String imageurl;
    int isview;

    public Slideshow(){}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIsview() {
        return isview;
    }

    public void setIsview(int isview) {
        this.isview = isview;
    }

    public String getImagedesc() {
        return imagedesc;
    }

    public void setImagedesc(String imagedesc) {
        this.imagedesc = imagedesc;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }



}
