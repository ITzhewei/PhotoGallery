package com.example.john.photogallery.base;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by ZheWei on 2016/9/29.
 */
public class MyApplication extends Application {

    public static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Fresco.initialize(getApplicationContext());
    }
}
