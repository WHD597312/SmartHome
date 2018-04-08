package com.xinrui.smart.activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.xinrui.smart.R;

import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.util.wifi.WifiHelper;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClockActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    WifiHelper wifiUtil;//定义Wife工具类
    ListView listView;//显示Wife的数据列表
    ArrayAdapter<String> adapter;//列表的适配器
    List<String> wifiSSIDs = new ArrayList<>();//列表的数据
    WifiManager wifiManager;//Wife管理器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        wifiUtil = new WifiHelper(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        listView = (ListView) findViewById(R.id.lv);
        //创建适配器，并把适配器设置到ListView中
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wifiSSIDs);
        listView.setAdapter(adapter);
        //给ListView设置点击事件，点击后连接Wife
        listView.setOnItemClickListener(this);
    }

    /**
     * 打开Wife
     */
    public void open(View view) {
        wifiUtil.openWifi();
    }
    /* 搜索wifi热点
     */
    private void search() {
        if (!wifiManager.isWifiEnabled()) {
            //开启wifi
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, mIntentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // wifi已成功扫描到可用wifi。
                List<ScanResult> scanResults = wifiManager.getScanResults();

            }
        }
    };

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 关闭Wife
     */
    public void close(View view) {
        wifiUtil.closeWifi();
    }

    /**
     * 扫描Wife
     */
    List<ScanResult> wifiList = new ArrayList<>();

    public void scan(View view) {
        isClickHistory = false;//显示的列表不是历史记录
        //扫描先清除数据
        wifiSSIDs.clear();
        wifiList.clear();

        wifiUtil.startScan();//扫描Wife
        wifiList = wifiUtil.getWifiList();
        //获取列表中的SSID并显示出来
        for (ScanResult scanResult : wifiList) {
            wifiSSIDs.add(scanResult.SSID);
        }
        //刷新适配器
        adapter.notifyDataSetChanged();
    }

    /**
     * 获取连接过的Wife数据
     */
    List<WifiConfiguration> configuredNetworks = new ArrayList<>();

    public void getGood(View view) {
        isClickHistory = true;//显示的列表是历史记录
        //扫描先清除数据
        wifiSSIDs.clear();
        if (configuredNetworks != null) {
            configuredNetworks.clear();

            //获取历史记录
            configuredNetworks = wifiUtil.getConfiguration();
            if (configuredNetworks == null)
                return;
            for (WifiConfiguration result : configuredNetworks) {
                wifiSSIDs.add(result.SSID);
            }
            //刷新适配器
            adapter.notifyDataSetChanged();
        }
        //获取列表中的SSID并显示出来

    }

    /**
     * 点击Wife视图列表数据后的回调方法，这里是连接WIfe
     */
    boolean isClickHistory = false;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //判断是否是点击的历史记录
        if (isClickHistory) {
            boolean connOk = wifiUtil.connetionConfiguration(position);
            if (connOk) {
                Toast.makeText(ClockActivity.this, "Wife连接成功", Toast.LENGTH_SHORT).show();
//                finish();
            } else {
                Toast.makeText(ClockActivity.this, "Wife连接失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            //连接wifi
            ScanResult sr = wifiList.get(position);//获取点击的扫描信息
            final String SSID = sr.SSID;//获取Wife的SSID

            final int type = wifiUtil.getType(sr.capabilities);
            if (type == 1) {//没有密码的Wife情况
                WifiConfiguration config = wifiUtil.createWifiInfo(SSID, "", type);//第二个空就是密码
                wifiUtil.addNetWork(config);
            } else {
                //有密码
                final EditText et = new EditText(ClockActivity.this);
                et.setHint("输入wifi密码");
                new AlertDialog.Builder(ClockActivity.this)
                        .setTitle("设置密码")
                        .setView(et)
                        .setNeutralButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WifiConfiguration config = wifiUtil.createWifiInfo(SSID, et.getText().toString(), type);
                                boolean conn = wifiUtil.addNetWork(config);
                                //判断密码是否连接成功
                                if (conn) {
                                    Toast.makeText(ClockActivity.this, "Wife连接成功", Toast.LENGTH_SHORT).show();
                                    finish();//关闭页面
                                } else {
                                    Toast.makeText(ClockActivity.this, "Wife连接失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).create().show();
            }
        }
    }
}