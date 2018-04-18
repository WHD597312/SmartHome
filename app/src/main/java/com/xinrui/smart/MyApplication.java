package com.xinrui.smart;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.StrictMode;


import java.util.ArrayList;
import java.util.List;

import cn.smssdk.SMSSDK;


/**
 * Created by hongming.wang on 2018/1/23.
 */

public class MyApplication extends Application {
    private int count = 0;
    private List<Activity> activities;
    @Override
    public void onCreate() {
        super.onCreate();
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        SMSSDK.initSDK(this,"24c373291db44","eb329179014e3063ce241d718e8693da");
        activities=new ArrayList<>();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                count ++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if(count > 0) {
                    count--;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public void addActivity(Activity activity){
        if (!activities.contains(activity)){
            activities.add(activity);
        }
    }
    public void removeActivity(Activity activity){
        if (activities.contains(activity)){
            activities.remove(activity);
            activity.finish();
        }
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void removeAllActivity(){
        for (Activity activity:activities){
            activity.finish();
        }
    }
    /**
     * 判断app是否在后台
     * @return
     */
    public boolean isBackground(){
        if(count <= 0){
            return true;
        } else {
            return false;
        }
    }
}
