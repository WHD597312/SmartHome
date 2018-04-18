package com.xinrui;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

/**
 * Created by win7 on 2018/4/17.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

    }

    public static Context getInstance() {
        return mContext;
    }

}
