package com.example.administrator.myapplication;

import android.app.Application;

/**
 * Created by Administrator on 2016/9/11.
 */
public class MyApp extends Application {

    private static MyApp instance;

    public static MyApp getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
