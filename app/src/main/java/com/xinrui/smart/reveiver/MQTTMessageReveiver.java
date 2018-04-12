package com.xinrui.smart.reveiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MQTTMessageReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message=intent.getStringExtra("message");
        String topicName=intent.getStringExtra("topicName");
        Intent mqttIntent=new Intent("mqtt");
        mqttIntent.putExtra("message",message);
        mqttIntent.putExtra("topicName",topicName);
        context.sendBroadcast(mqttIntent);
    }
}
