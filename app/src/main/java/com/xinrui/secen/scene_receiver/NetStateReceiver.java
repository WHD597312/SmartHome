package com.xinrui.secen.scene_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xinrui.MyApplication;
import com.xinrui.secen.scene_pojo.MessageEvent;
import com.xinrui.secen.scene_util.NetWorkUtil;

import org.greenrobot.eventbus.EventBus;

public class NetStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNet = NetWorkUtil.isConn(com.xinrui.smart.MyApplication.getContext());
        if (isNet) {
            EventBus.getDefault().post(new MessageEvent("0"));
        }else
            EventBus.getDefault().post(new MessageEvent("1"));
    }
}
