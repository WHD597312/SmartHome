package com.xinrui.smart.reveiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.formatter.IFillFormatter;
import com.xinrui.MyApplication;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.secen.scene_activity.RoomContentActivity;
import com.xinrui.secen.scene_fragment.Btn1_fragment;
import com.xinrui.secen.scene_fragment.Btn2_fragment;
import com.xinrui.secen.scene_fragment.Btn3_fragment;
import com.xinrui.secen.scene_fragment.Btn4_fragment;
import com.xinrui.secen.scene_pojo.MessageEvent;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.DeviceListActivity;
import com.xinrui.smart.activity.TempChartActivity;
import com.xinrui.smart.activity.TimeTaskActivity;
import com.xinrui.smart.activity.device.ShareDeviceActivity;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.fragment.SmartFragmentManager;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class MQTTMessageReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isConn = NetWorkUtil.isConn(com.xinrui.smart.MyApplication.getContext());
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo  wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            Utils.showToast(context, "网络不可用");
            //改变背景或者 处理网络的全局变量
        }else if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()){
//            Utils.showToast(context,"网络已连接");
//            DeviceChildDaoImpl deviceChildDao = new DeviceChildDaoImpl(context);
//            List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();

            Intent mqttIntent = new Intent(context,MQService.class);
            mqttIntent.putExtra("reconnect","reconnect");
            context.startService(mqttIntent);
        }
        int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch,R.mipmap.image_switch2};
        if (!isConn) {
            SharedPreferences preferences=context.getSharedPreferences("net",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferences.edit();
            editor.putString("from","1");
            editor.putString("to","0");
            editor.commit();
            try {
                DeviceChildDaoImpl deviceChildDao = new DeviceChildDaoImpl(context);
                List<DeviceChild> deviceChildren =
                        deviceChildDao.findAllDevice();
                for (DeviceChild deviceChild : deviceChildren) {
                    deviceChild.setOnLint(false);
                    if ("open".equals(deviceChild.getDeviceState())) {
                        deviceChild.setImg(imgs[2]);
                    }
                    deviceChildDao.update(deviceChild);
                }
                if (DeviceFragment.running) {
                    Intent mqttIntent = new Intent("DeviceFragment");
                    mqttIntent.putExtra("noNet","noNet");
                    context.sendBroadcast(mqttIntent);
                } else if (DeviceListActivity.running){
                    Intent mqttIntent = new Intent("DeviceListActivity");
                    mqttIntent.putExtra("noNet","noNet");
                    context.sendBroadcast(mqttIntent);
                }else if (DeviceListActivity.running){
                    Intent mqttIntent = new Intent("HeaterFragment");
                    mqttIntent.putExtra("noNet","noNet");
                    context.sendBroadcast(mqttIntent);
                }else if (TimeTaskActivity.running){
                    Intent mqttIntent = new Intent("TimeTaskActivity");
                    mqttIntent.putExtra("noNet","noNet");
                    context.sendBroadcast(mqttIntent);
                }else if (TempChartActivity.running){
                    Intent mqttIntent = new Intent("TempChartActivity");
                    mqttIntent.putExtra("noNet","noNet");
                    context.sendBroadcast(mqttIntent);
                }else if (ShareDeviceActivity.running){
                    Intent mqttIntent = new Intent("ShareDeviceActivity");
                    mqttIntent.putExtra("noNet","noNet");
                    context.sendBroadcast(mqttIntent);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            SharedPreferences preferences=context.getSharedPreferences("net",Context.MODE_PRIVATE);
            if (preferences.contains("from") && preferences.contains("to")){
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString("from","0");
                editor.putString("to","1");
                editor.commit();

            }
        }
    }
    /**
     * 检测网络是否连接
     * @return
     */


}
