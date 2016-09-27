package com.example.john.photogallery;

/**
 * Created by ZheWei on 2016/9/27.
 */
public class GalleryItem {
    private String mCaption;
    private String mId;
    private String murl;


    @Override
    public String toString() {
        return "GalleryItem{" +
                "mCaption='" + mCaption + '\'' +
                ", mId='" + mId + '\'' +
                ", murl='" + murl + '\'' +
                '}';
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getMurl() {
        return murl;
    }

    public void setMurl(String murl) {
        this.murl = murl;
    }
}

