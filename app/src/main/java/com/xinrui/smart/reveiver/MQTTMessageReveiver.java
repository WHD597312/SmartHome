package com.xinrui.smart.reveiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MQTTMessageReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message=intent.getStringExtra("message");
        Intent mqttIntent=new Intent("mqtt");
        mqttIntent.putExtra("message",message);
        context.sendBroadcast(mqttIntent);
    }
}
