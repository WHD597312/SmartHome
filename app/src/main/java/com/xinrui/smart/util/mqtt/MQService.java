package com.xinrui.smart.util.mqtt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_activity.RoomContentActivity;
import com.xinrui.secen.scene_fragment.Btn1_fragment;
import com.xinrui.secen.scene_fragment.Btn2_fragment;
import com.xinrui.secen.scene_fragment.Btn3_fragment;
import com.xinrui.secen.scene_fragment.Btn4_fragment;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddDeviceActivity;
import com.xinrui.smart.activity.DeviceListActivity;
import com.xinrui.smart.activity.LoginActivity;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.activity.SmartTerminalActivity;
import com.xinrui.smart.activity.TempChartActivity;
import com.xinrui.smart.activity.TimeTaskActivity;
import com.xinrui.smart.activity.device.ShareDeviceActivity;
import com.xinrui.smart.fragment.ControlledFragment;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.fragment.MainControlFragment;
import com.xinrui.smart.fragment.SmartFragmentManager;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.UUID;
import com.xinrui.smart.util.Utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MQService extends Service {

    private String TAG = "MQService";
    private String host = "tcp://47.98.131.11:1883";
    private String userName = "admin";
    private String passWord = "Xr7891122";


    private MqttClient client;

    public String myTopic = "rango/dc4f220aa96e/transfer";
    private LinkedList<String> offlineList = new LinkedList<String>();

    private DeviceChildDaoImpl deviceChildDao;
    DeviceGroupDaoImpl deviceGroupDao;
    private MqttConnectOptions options;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private LocalBinder binder = new LocalBinder();
    String clientId;
    private int times = 0;
    private Map<String, DeviceChild> offlineDevices = new LinkedHashMap<>();
    String reconnect = null;

    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        deviceChildDao = new DeviceChildDaoImpl(this);
        deviceGroupDao = new DeviceGroupDaoImpl(MQService.this);
        Log.i(TAG, "onCreate");
        clientId = UUID.getUUID(this);
        Log.i("clientId", "-->" + clientId);
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        reconnect = intent.getStringExtra("reconnect");
        connect();
        if (!Utils.isEmpty(reconnect)) {
            CountTimer countTimer = new CountTimer(2000, 1000);
            countTimer.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {
        public MQService getService() {
            Log.i(TAG, "Binder");
            return MQService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        try {
            Log.i(TAG, "onDestroy");
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean stopService(Intent name) {
        Log.i(TAG, "stopService");
        return super.stopService(name);
    }

    public void connect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
            new ConAsync().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ConAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if (!client.isConnected()) {
                    client.connect(options);
                }
                List<String> topicNames = getTopicNames();
                if (client.isConnected() && !topicNames.isEmpty()) {
                    for (String topicName : topicNames) {
                        if (!TextUtils.isEmpty(topicName)) {
                            client.subscribe(topicName, 1);
                            Log.i("client", "-->" + topicName);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private String result;

    private void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存

            client = new MqttClient(host, clientId,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(15);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
//            options.setKeepAliveInterval(20);


            //设置回调
            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    startReconnect();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) {
                    try {
                        new LoadAsyncTask().execute(topicName, message.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int groupPostion = 0;
    int childPosition = 0;
    int timerTaskWeek = 0;
    private int falling=-1;

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch, R.mipmap.image_switch2};

    class LoadAsyncTask extends AsyncTask<String, Void, Object> {
        @Override
        protected Object doInBackground(String... strings) {

            String topicName = strings[0];
            Log.i("topicName", "-->" + topicName);
//            Log.i()
            String message = strings[1];
            String macAddress = "";
            int deviceType = -1;/**设备类型*/
            if (topicName.startsWith("p99/sensor1")) {
                macAddress = topicName.substring(12, topicName.lastIndexOf("/"));
            } else if (topicName.startsWith("rango/")) {
                macAddress = topicName.substring(6, topicName.lastIndexOf("/"));
            } else if (topicName.startsWith("p99")) {
                macAddress = topicName.substring(4, topicName.lastIndexOf("/"));
            }

            String topicShare = "rango/" + macAddress + "/refresh";

            String refresh = null;
            if (topicShare.equals(topicName) && "refresh".equals(message)) {
                refresh = "refresh";
                Log.i("refresh", "-->" + "refresh");
            }



            Log.i("sssss", message);
            Log.i("ssss", message);


            if (AddDeviceActivity.running && !TextUtils.isEmpty(message) && message.startsWith("{") && message.endsWith("}")) {
                try {
                    Log.i("AddDeviceActivity", "-->" + message);
                    JSONObject device = new JSONObject(message);
                    if (device.has("productType")) {

                        AddDeviceActivity.running = false;
                        String productType = device.getString("productType");
                        deviceType = Integer.parseInt(productType);

                        Intent mqttIntent = new Intent("AddDeviceActivity");
                        mqttIntent.putExtra("deviceType", deviceType);
                        mqttIntent.putExtra("macAddress", macAddress);
                        mqttIntent.putExtra("add", "add");
                        sendBroadcast(mqttIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (AddDeviceActivity.running && !TextUtils.isEmpty(message) && "online".equals(message)) {
                Intent mqttIntent = new Intent("AddDeviceActivity");
                mqttIntent.putExtra("online", "online");
                mqttIntent.putExtra("macAddress", macAddress);
                sendBroadcast(mqttIntent);
            }

            if ("upgradeFinish".equals(message) || "mcu no response yet!".equals(message) || "There is no need to upgrade!".equals(message) || "upgradeFinish".equals(message) || "online".equals(message) || "machine_dump!!!".equals(message)) {
                if (message != null && message.length() == 0) {
                    return null;
                }
                return null;
            }
            String updateGrade="";
            if ("Have upgrade task!".equals(message)){
                updateGrade="updateGrade";
            }
            Log.i("updateGrade","-->"+updateGrade);
            String reSet = null;

            if ("reSet".equals(message)) {
                reSet = "reSet";
                String topicOffline = "rango/" + macAddress + "/lwt";
                String topicShare2 = "rango/" + macAddress + "/refresh";
                String topicOffline2="p99/sensor1"+macAddress+"/lwt";
                String topicShares="p99/sensor1"+macAddress+"/transfer";
                String topic="rango/"+macAddress+"/transfer";
                String topic2="p99/"+macAddress+"/transfer";
                unsubscribe(topic2);
                unsubscribe(topicShares);
                unsubscribe(topicShare);
                unsubscribe(topic);
                unsubscribe(topicOffline);
                unsubscribe(topicOffline2);
                unsubscribe(topicShare2);
                unsubscribe(topicName);
            }
            if (!Utils.isEmpty(message))
                Log.i("messsage", "-->" + message);
            try {
                Log.i("macAddress", "-->" + macAddress);
                if (!Utils.isEmpty(macAddress) && AddDeviceActivity.running == false) {
                    JSONObject device = null;
                    String wifiVersion = "";
                    String MCUVerion = "";
                    int MatTemp = 0;
                    String workMode = "";/**manual:手动模式	timer:定时模式*/
                    String LockScreen = "";/** open:上锁  close:解锁*/
                    String BackGroundLED = "";/**open:照明  close:节能*/
                    String deviceState = "";/**open:开机  close：关机*/
                    String tempState = "";/**nor:正常 err:异常*/
                    String outputMode = "";
                    int curTemp = 0;
                    int ratedPower = 0;
                    String protectEnable = "";
                    String timerShutDown = "";
                    String ctrlMode = "";
                    int powerValue = 0;
                    int voltageValue = 0;
                    int currentValue = 0;
                    String machineFall = "";
                    int protectSetTemp = 0;
                    int protectProTemp = 0;
                    int extTemp = 0;
                    int extHum = 0;
                    int TimerTemp = 0;
                    String machAttr = "";
                    int grade = -1;


                    DeviceChild child = null;

                    TimeDaoImpl timeDao = new TimeDaoImpl(MQService.this);
                    TimeTaskDaoImpl timeTaskDao = new TimeTaskDaoImpl(MQService.this);
                    List<DeviceGroup> deviceGroups = deviceGroupDao.findAllDevices();
                    List<List<DeviceChild>> childern = new ArrayList<>();

                    for (DeviceGroup deviceGroup : deviceGroups) {
                        List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(deviceGroup.getId());
                        childern.add(deviceChildren);
                    }
                    groupPostion = 0;
                    for (List<DeviceChild> deviceChildren : childern) {
                        childPosition = 0;
                        for (DeviceChild deviceChild : deviceChildren) {
                            String mac = deviceChild.getMacAddress();
                            if (!Utils.isEmpty(mac) && macAddress.equals(mac)) {
                                child = deviceChild;
                                if (!Utils.isEmpty(refresh)) {
                                    String url = "http://47.98.131.11:8082/warmer/v1.0/device/getDeviceById?deviceId=" + child.getId();
                                    new LoadDevice().execute(url);
                                }
                                break;
                            }
                            childPosition++;
                        }

                        if (child != null) {
                            break;
                        }
                        groupPostion++;
                    }

                    if (child!=null){
                        child.setUpdateGrade(updateGrade);
                        deviceChildDao.update(child);
                    }
                    Log.i("groupPostion2", "-->" + groupPostion);
                    if (!Utils.isEmpty(reSet)) {
                        Log.i("groupPostion2", "-->" + groupPostion);
                        if (child != null) {/**删除和这个设备相关的所有数据*/
                            Log.i("groupPostion", "-->" + groupPostion);
//                            new DeleteDeviceAsync().execute(child);
                            deviceChildDao.delete(child);
                            Log.i("controlled", "-->" + child.getType() + "," + child.getControlled());
                            List<TimeTask> timeTasks = timeTaskDao.findTimeTasks(child.getId());
                            for (TimeTask timeTask : timeTasks) {
                                timeTaskDao.delete(timeTask);/**删除定时任务的时间段*/
                            }
                            List<Timer> timers = timeDao.findAll(child.getId());
                            for (Timer timer : timers) {
                                timeDao.delete(timer);/**删除定时任务的时间刻*/
                            }
                        }
                        child = null;/**将该设备重置为null*/
                    } else if (topicName.equals("rango/" + macAddress + "/transfer") && Utils.isEmpty(reSet) && Utils.isEmpty(refresh)) {
                        if (!Utils.isEmpty(message) && message.startsWith("{") && message.endsWith("}")) {
                            device = new JSONObject(message);
                            if (device.has("wifiVersion")) {
                                wifiVersion = device.getString("wifiVersion");/**版本*/
                            }
                            if (device.has("MCUVersion")) {
                                MCUVerion = device.getString("MCUVersion");
                            }
                            if (device.has("MatTemp")) {
                                MatTemp = device.getInt("MatTemp");/**手动/定时模式下的温度*/
                            }
                            if (device.has("workMode")) {
                                workMode = device.getString("workMode");/**manual:手动模式	timer:定时模式*/
                            }
                            if (device.has("LockScreen")) {
                                LockScreen = device.getString("LockScreen");
                            }
                            if (device.has("BackGroundLED")) {
                                BackGroundLED = device.getString("BackGroundLED");
                            }
                            if (device.has("deviceState")) {
                                deviceState = device.getString("deviceState");
                            }
                            if (device.has("tempState")) {
                                tempState = device.getString("tempState");
                            }
                            if (device.has("outputMode")) {
                                outputMode = device.getString("outputMode");
                            }
                            if (device.has("curTemp")) {
                                curTemp = device.getInt("curTemp");
                            }
                            if (device.has("ratedPower")) {
                                ratedPower = device.getInt("ratedPower");
                            }
                            if (device.has("protectEnable")) {
                                protectEnable = device.getString("protectEnable");
                            }
                            if (device.has("ctrlMode")) {
                                ctrlMode = device.getString("ctrlMode");
                            }
                            if (device.has("powerValue")) {
                                powerValue = device.getInt("powerValue");
                            }

                            if (device.has("voltageValue")) {
                                voltageValue = device.getInt("voltageValue");
                            }
                            if (device.has("voltageValue")) {
                                voltageValue = device.getInt("voltageValue");
                            }
                            if (device.has("currentValue")) {
                                currentValue = device.getInt("currentValue");
                            }
                            if (device.has("machineFall")) {
                                machineFall = device.getString("machineFall");
                            }
                            if (device.has("protectSetTemp")) {
                                protectSetTemp = device.getInt("protectSetTemp");
                            }
                            if (device.has("protectProTemp")) {
                                protectProTemp = device.getInt("protectProTemp");
                            }
                            if (device.has("timerShutDown")) {
                                timerShutDown = device.getString("timerShutDown");
                            }
                            if (device.has("grade")) {
                                grade = device.getInt("grade");
                            }

                            if (device.has("extTemp")) {
                                extTemp = device.getInt("extTemp");
                            }
                            if (device.has("extHum")) {
                                extHum = device.getInt("extHum");
                            }
                            if (device.has("TimerTemp")) {
                                TimerTemp = device.getInt("TimerTemp");
                            }
                            if (device.has("machAttr")) {
                                machAttr = device.getString("machAttr");
                            }
                            if (device != null && device.has("timerTaskWeek")) {
                                timerTaskWeek = device.getInt("timerTaskWeek");
                                long deviceId = child.getId();
                                List<Timer> timers = timeDao.findAll(deviceId, timerTaskWeek);
                                if (!timers.isEmpty()) {
                                    for (Timer timer : timers) {
                                        timeDao.delete(timer);
                                    }
                                }
                                List<TimeTask> timeTasks = timeTaskDao.findWeekAll(deviceId, timerTaskWeek);
                                for (TimeTask timeTask : timeTasks) {
                                    timeTaskDao.delete(timeTask);
                                }
                                for (int i = 0; i < 24; i++) {
                                    int temp = device.getInt("t" + i);
                                    String open = device.getString("h" + i);
                                    Timer timer = new Timer(deviceId, timerTaskWeek, temp, open, i);
                                    timeDao.insert(timer);
                                    timers.add(timer);
                                }
                                timers = timeDao.findAll(deviceId, timerTaskWeek);
                                times = timeDao.findAll(deviceId).size();
                                //                            timers=timeDao.getTimers(deviceId,timerTaskWeek);

                                //设置开始时间、结束时间
                                int start = 0;
                                int end = 0;
                                timeTaskDao.deleteAllTask(deviceId, timerTaskWeek);
                                for (int i = start; i < 24; i++) {
                                    if (start > 23) {
                                        break;
                                    }
                                    String o = timers.get(start).getOpen();
                                    int temp = timers.get(start).getTemp();
                                    if (o.equals("off")) {
                                        end++;
                                        start = end;
                                        continue;
                                    }
                                    if (start == 23) {
                                        if (o.equals("on")) {
                                            end++;
                                            TimeTask controller = new TimeTask(deviceId, timerTaskWeek, start, end, temp);
                                            timeTaskDao.insert(controller);
                                            break;
                                        }
                                    }
                                    for (int j = start + 1; j < 24; j++) {
                                        end++;
                                        if (end == 23) {
                                            if (o.equals(timers.get(end).getOpen())) {
                                                if (temp == timers.get(end).getTemp()) {
                                                    end++;
                                                }
                                                break;
                                            } else {
                                                break;
                                            }
                                        }
                                        if (o.equals(timers.get(end).getOpen())) {
                                            if (temp == timers.get(end).getTemp()) {
                                                continue;
                                            } else {
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }

                                    TimeTask controller = new TimeTask(deviceId, timerTaskWeek, start, end, temp);
                                    start = end;
                                    System.out.println(start);
                                    timeTaskDao.insert(controller);
                                }
                            }
                            if (child != null) {
                                if (!Utils.isEmpty(wifiVersion))
                                    child.setWifiVersion(wifiVersion);

                                if (!Utils.isEmpty(MCUVerion))
                                    child.setMCUVerion(MCUVerion);

                                if (!Utils.isEmpty(workMode)) {
                                    child.setWorkMode(workMode);
                                    if (MatTemp != 0) {
                                        child.setMatTemp(MatTemp);
                                        if ("manual".equals(workMode)) {
                                            child.setManualMatTemp(MatTemp);
                                        } else if ("timer".equals(workMode)) {
                                            child.setTimerTemp(TimerTemp);
                                        }
                                    }
                                }
                                if (!Utils.isEmpty(LockScreen))
                                    child.setLockScreen(LockScreen);
                                if (!Utils.isEmpty(BackGroundLED))
                                    child.setBackGroundLED(BackGroundLED);
                                if (!Utils.isEmpty(deviceState)) {
                                    if ("open".equals(deviceState))
                                        child.setImg(imgs[1]);
                                    else {
                                        child.setImg(imgs[0]);
                                    }
                                    child.setDeviceState(deviceState);
                                }

                                if (!Utils.isEmpty(timerShutDown)) {
                                    child.setTimerShutdown(timerShutDown);
                                }
                                if (!Utils.isEmpty(tempState))
                                    child.setTempState(tempState);
                                if (!Utils.isEmpty(outputMode))
                                    child.setOutputMod(outputMode);
                                if (curTemp != 0)
                                    child.setCurTemp(curTemp);
                                if (ratedPower != 0)
                                    child.setRatedPower(ratedPower);
                                if (!Utils.isEmpty(protectEnable))
                                    child.setProtectEnable(protectEnable);
                                if (!Utils.isEmpty(ctrlMode)) {
                                    child.setCtrlMode(ctrlMode);
                                    int type = child.getType();
                                    if ("fall".equals(machineFall)) {
                                        Log.i("fall", "-->" + machineFall + "," + macAddress);
                                    } else {
                                        if (type == 1) {
                                            Log.i("DeviceFragment", "-->" + child.getDeviceName() + "," + child.getControlled());
                                            if ("master".equals(ctrlMode)) {
                                                child.setControlled(2);
                                            } else if ("slave".equals(ctrlMode)) {
                                                child.setControlled(1);
                                            } else if ("normal".equals(ctrlMode)) {
                                                child.setControlled(0);
                                            }
                                        }
                                    }
                                }
                                if (powerValue != 0)
                                    child.setPowerValue(powerValue);
                                if (voltageValue != 0)
                                    child.setVoltageValue(voltageValue);
                                if (currentValue != 0)
                                    child.setCurrentValue(currentValue);
                                if (grade != -1)
                                    child.setGrade(grade);
                                if (!Utils.isEmpty(machineFall)) {
                                    child.setMachineFall(machineFall);
//                                child.setOnLint(true);
                                    if ("fall".equals(machineFall)) {
                                        falling=1;
                                        child.setMachineFall("fall");
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                                        Intent notifyIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                        notifyIntent.putExtra("fall", "fall");

                                        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                        PendingIntent notifyPendingIntent =
                                                PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);

                                        builder.setContentText(child.getDeviceName() + "已倾倒")
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setDefaults(Notification.DEFAULT_ALL)
                                                .setAutoCancel(true);
                                        builder.setContentIntent(notifyPendingIntent);

                                        NotificationManager mNotificationManager =
                                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        mNotificationManager.notify(0, builder.build());

//                                        VibratorUtil.Vibrate(MQService.this, new long[]{1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000},false);   //震动10s
                                    } else {
//                                        VibratorUtil.StopVibrate(MQService.this);
                                        MainActivity.falling=false;
                                        falling=0;
                                        child.setMachineFall("nor");
                                    }
                                }
                                if (protectSetTemp != 0)
                                    child.setProtectSetTemp(protectSetTemp);
                                if (protectProTemp != 0)
                                    child.setProtectProTemp(protectProTemp);
                                if (!TextUtils.isEmpty(machAttr)) {
                                    child.setMachAttr(machAttr);
                                }

                                if (child != null) {

                                    if (!Utils.isEmpty(deviceState)){
                                        child.setOnLint(true);
                                    }
                                    child.setTemp(extTemp);
                                    child.setHum(extHum);
                                    deviceChildDao.update(child);
                                }
                            }
                        }
                    } else if (("p99/sensor1/" + macAddress + "/transfer").equals(topicName)) {
                        if (!TextUtils.isEmpty(message) && message.startsWith("{") && message.endsWith("}")) {
                            JSONArray messageJsonArray = null;
                            device = new JSONObject(message);
                            if (device != null && device.has("TempHumPM2_5")) {
                                messageJsonArray = device.getJSONArray("TempHumPM2_5");
                            }
                            if (messageJsonArray != null) {
                                int sensorSimpleTemp;/**传感器采样温度*/
                                int sensorSimpleHum;/**传感器采样湿度*/
                                int sorsorPm;/**PM2.5粉尘传感器数据*/
                                int sensorOx;/**氧浓度传感器数据*/
                                int sensorHcho;/**甲醛数据*/

                                int busModel = messageJsonArray.getInt(3);
                                int mMcuVersion = messageJsonArray.getInt(4);
                                String mcuVersion = "v" + mMcuVersion / 16 + "." + mMcuVersion % 16;
                                int mWifiVersion = messageJsonArray.getInt(5);
                                wifiVersion = "v" + mWifiVersion / 16 + "." + mWifiVersion % 16;
                                int sensorState = messageJsonArray.getInt(7);
                                sensorSimpleTemp = messageJsonArray.getInt(8) - 128;
                                sensorSimpleHum = messageJsonArray.getInt(9) - 128;
                                sorsorPm = messageJsonArray.getInt(10) - 128;
                                sensorOx = messageJsonArray.getInt(11) - 128;
                                sensorHcho = messageJsonArray.getInt(12) - 128;
                                extTemp = sensorSimpleTemp;
                                extHum = sensorSimpleHum;

                                if (child != null) {
                                    child.setSensorState(sensorState);
                                    child.setBusModel(busModel);
//                                    child.setMcuVersion(mcuVersion);
//                                    deviceChild.setWifiVersion(wifiVersion);
                                    child.setSensorSimpleTemp(sensorSimpleTemp);
                                    child.setSensorSimpleHum(sensorSimpleHum);
                                    child.setSorsorPm(sorsorPm);
                                    child.setSensorOx(sensorOx);
                                    child.setSensorHcho(sensorHcho);
                                    child.setOnLint(true);
                                    child.setTemp(sensorSimpleTemp);
                                    child.setHum(sensorSimpleHum);
                                    deviceChildDao.update(child);
                                }
                            }
                        }
                    } else if (("p99/sensor1/" + macAddress + "/lwt").equals(topicName)) {
                        if (child != null) {
//                            send(child);
//                            if ("open".equals(child.getDeviceState())) {
//                                child.setImg(imgs[2]);
//                            }
                            child.setOnLint(false);
                            deviceChildDao.update(child);
                        }
                    } else if (topicName.equals("rango/" + macAddress + "/lwt")) {
                        if (child != null) {
//                            send(child);
//                            if ("open".equals(child.getDeviceState())) {
//                                child.setImg(imgs[2]);
//                            }
                            child.setOnLint(false);
                            deviceChildDao.update(child);
                        }
                    }
                    Log.i("AddDeviceActivity", "-->" + AddDeviceActivity.running);
                    if (DeviceListActivity.running || ShareDeviceActivity.running || TimeTaskActivity.running|| TempChartActivity.running){
                        MainActivity.falling=false;
                        DeviceFragment.running=false;
                        falling=-1;
                    }
                    if (SmartTerminalActivity.running){
                        MainActivity.falling=false;
                        DeviceFragment.running=false;
                        falling=-1;
                    }
                    if (SmartFragmentManager.running){
                        MainActivity.falling=false;
                        DeviceFragment.running=false;
                        falling=-1;
                    }
                    if (Btn1_fragment.running==2 || Btn2_fragment.running==2 || Btn3_fragment.running==2 || Btn4_fragment.running==2){
                        MainActivity.falling=false;
                        DeviceFragment.running=false;
                        falling=-1;
                    }
                    if (DeviceFragment.running || falling==0 || falling==1) {
                        if (child == null) {
                            Intent mqttIntent = new Intent("DeviceFragment");
                            Log.i("groupPostion", "-->" + groupPostion);
                            mqttIntent.putExtra("groupPostion", groupPostion);
                            mqttIntent.putExtra("childPosition", childPosition);
                            mqttIntent.putExtra("deviceChild", child);
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        } else {
                            Intent mqttIntent = new Intent("DeviceFragment");
                            mqttIntent.putExtra("groupPostion", groupPostion);
                            mqttIntent.putExtra("childPosition", childPosition);
                            mqttIntent.putExtra("deviceChild", child);
                            sendBroadcast(mqttIntent);
                        }
                    }else if (SmartFragmentManager.running){
                        if (child!=null){
                            Intent mqttIntent = new Intent("SmartFragmentManager");
                            mqttIntent.putExtra("deviceChild", child);
                            sendBroadcast(mqttIntent);
                        }else {
                            Intent mqttIntent = new Intent("SmartFragmentManager");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        }
                    }else if (DeviceListActivity.running) {
                        if (!Utils.isEmpty(reSet)) {
                            Intent mqttIntent = new Intent("DeviceListActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        } else {

                            boolean online = false;
                            if (child != null) {
                                online = child.getOnLint();
                            }
                            if (online) {
                                Intent mqttIntent2 = new Intent("DeviceListActivity");
                                if ("slave".equals(child.getCtrlMode())) {
                                    mqttIntent2.putExtra("macAddress3", child.getMacAddress());
                                }
                                mqttIntent2.putExtra("online", "online");
                                mqttIntent2.putExtra("deviceChild", child);
                                mqttIntent2.putExtra("machineFall", child.getMachineFall());
                                sendBroadcast(mqttIntent2);
                            } else {
                                Intent mqttIntent = new Intent("DeviceListActivity");
                                mqttIntent.putExtra("deviceChild", child);
                                mqttIntent.putExtra("online", "offline");
                                sendBroadcast(mqttIntent);
                            }

                        }
                    } else if (TimeTaskActivity.running) {
                        if (!Utils.isEmpty(reSet)) {
                            Intent mqttIntent = new Intent("TimeTaskActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        } else {
                            if (child == null) {
                                Intent mqttIntent = new Intent("TimeTaskActivity");
                                mqttIntent.putExtra("deviceChild", child);
                                sendBroadcast(mqttIntent);
                            } else {
                                Intent mqttIntent = new Intent("TimeTaskActivity");
                                if ("slave".equals(child.getCtrlMode())) {
                                    mqttIntent.putExtra("macAddress3", child.getMacAddress());
                                }
                                mqttIntent.putExtra("timerTaskWeek", timerTaskWeek);
                                mqttIntent.putExtra("deviceId", child.getId());
                                List<TimeTask> timerTasks = timeTaskDao.findWeekAll(child.getId(), timerTaskWeek);
                                mqttIntent.putExtra("list", (Serializable) timerTasks);
                                sendBroadcast(mqttIntent);
                            }
                        }
                    } else if (ShareDeviceActivity.running) {
                        if (!Utils.isEmpty(reSet)) {
                            Intent mqttIntent = new Intent("ShareDeviceActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        } else {
                            Intent mqttIntent = new Intent("ShareDeviceActivity");
                            mqttIntent.putExtra("deviceChild", child);
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        }
                    } else if (TempChartActivity.running) {
                        if (!Utils.isEmpty(reSet)) {
                            Intent mqttIntent = new Intent("TempChartActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        } else {
                            Intent mqttIntent = new Intent("TempChartActivity");
                            mqttIntent.putExtra("deviceChild", child);
                            sendBroadcast(mqttIntent);
                        }
                    } else if (SmartTerminalActivity.running) {
                        if (!Utils.isEmpty(reSet)) {
                            Intent mqttIntent = new Intent("SmartTerminalActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        } else {
                            if (child != null) {
                                Intent mqttIntent = new Intent("SmartTerminalActivity");
                                mqttIntent.putExtra("macAddress", macAddress);
                                mqttIntent.putExtra("deviceChild", child);
                                sendBroadcast(mqttIntent);
                            }
                        }
                    } else if (Btn1_fragment.running == 2) {
                        Intent mqttIntent = new Intent("Btn1_fragment");
                        mqttIntent.putExtra("extTemp", extTemp);
                        mqttIntent.putExtra("extHum", extHum);
                        mqttIntent.putExtra("deviceChild", child);
                        mqttIntent.putExtra("message", "测试");
                        sendBroadcast(mqttIntent);
                    } else if (Btn2_fragment.running == 2) {
                        Intent mqttIntent = new Intent("Btn1_fragment");
                        mqttIntent.putExtra("extTemp", extTemp);
                        mqttIntent.putExtra("extHum", extHum);
                        mqttIntent.putExtra("deviceChild", child);
                        mqttIntent.putExtra("message", "测试");
                        sendBroadcast(mqttIntent);
                    } else if (Btn3_fragment.running == 2) {
                        Intent mqttIntent = new Intent("Btn1_fragment");
                        mqttIntent.putExtra("extTemp", extTemp);
                        mqttIntent.putExtra("extHum", extHum);
                        mqttIntent.putExtra("deviceChild", child);
                        mqttIntent.putExtra("message", "测试");
                        sendBroadcast(mqttIntent);
                    } else if (Btn4_fragment.running == 2) {
                        Intent mqttIntent = new Intent("Btn1_fragment");
                        mqttIntent.putExtra("extTemp", extTemp);
                        mqttIntent.putExtra("extHum", extHum);
                        mqttIntent.putExtra("deviceChild", child);
                        mqttIntent.putExtra("message", "测试");
                        sendBroadcast(mqttIntent);
                    } else if (RoomContentActivity.running) {
                        child = deviceChildDao.findDeviceById(child.getId());
                        Intent mqttIntent = new Intent("RoomContentActivity");
                        mqttIntent.putExtra("extTemp", extTemp);
                        mqttIntent.putExtra("extHum", extHum);
                        mqttIntent.putExtra("deviceChild", child);
                        sendBroadcast(mqttIntent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (DeviceListActivity.running) {
                MainActivity.running = false;
                DeviceFragment.running=false;
            }
            switch (msg.what) {
                case 1:
                    DeviceChild child = (DeviceChild) msg.obj;
                    Intent mqttIntent = new Intent("DeviceFragment");
                    mqttIntent.putExtra("groupPostion", groupPostion);
                    mqttIntent.putExtra("childPosition", childPosition);
                    mqttIntent.putExtra("deviceState", child.getDeviceState());
                    mqttIntent.putExtra("deviceChild", child);
                    sendBroadcast(mqttIntent);
                    break;
                case 2:
                    if (DeviceFragment.running) {
                        DeviceChild child2 = (DeviceChild) msg.obj;
                        Intent mqttIntent2 = new Intent("DeviceFragment");
                        mqttIntent2.putExtra("macAddress2", child2.getMacAddress());
                        mqttIntent2.putExtra("deviceChild2", child2);
                        mqttIntent2.putExtra("groupPostion", groupPostion);
                        mqttIntent2.putExtra("refresh", "refresh");
                        sendBroadcast(mqttIntent2);
                    } else if (DeviceListActivity.running) {
                        DeviceChild child2 = (DeviceChild) msg.obj;
                        Intent mqttIntent2 = new Intent("DeviceListActivity");
                        mqttIntent2.putExtra("deviceName", child2.getDeviceName());
                        mqttIntent2.putExtra("macAddress2", child2.getMacAddress());
                        sendBroadcast(mqttIntent2);
                    }
                    break;
            }
        }
    };

    private void startReconnect() {

        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void insert(DeviceChild deviceChild) {
        deviceChildDao.insert(deviceChild);
    }

    public void updateDevice(DeviceChild deviceChild) {
        deviceChildDao.update(deviceChild);
    }

    public boolean publish(String topicName, int qos, String payload) {
        boolean flag = false;
        if (client != null && client.isConnected()) {
            try {
                MqttMessage message = new MqttMessage(payload.getBytes("utf-8"));
                qos = 1;
                message.setQos(qos);
                client.publish(topicName, message);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public boolean subscribe(String topicName, int qos) {
        boolean flag = false;
        if (client != null && client.isConnected()) {
            try {
                client.subscribe(topicName, qos);
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public String getResult() {
        return result;
    }

    SharedPreferences preferences;

    public List<String> getTopicNames() {
        List<String> topicNames = new ArrayList<>();
        List<DeviceChild> list = deviceChildDao.findAllDevice();

        for (DeviceChild deviceChild : list) {
            String macAddress = deviceChild.getMacAddress();
            int type = deviceChild.getType();
            if (!Utils.isEmpty(macAddress)) {
                String topicOffline = "rango/" + macAddress + "/lwt";
                String topicName = "rango/" + macAddress + "/transfer";
                String topicShare = "rango/" + macAddress + "/refresh";
                if (type == 2) {
                    String onlineTopicName = "p99/sensor1/" + macAddress + "/transfer";
                    String offlineTopicName = "p99/sensor1/" + macAddress + "/lwt";
                    topicNames.add(onlineTopicName);
                    topicNames.add(offlineTopicName);
                } else {
                    topicNames.add(topicOffline);
                    topicNames.add(topicName);
                    topicNames.add(topicShare);
                    addOffineDevice(macAddress);
                }
            }
        }
        return topicNames;
    }

    public void unsubscribe(String topicName) {
        if (client != null && client.isConnected()) {
            try {
                client.unsubscribe(topicName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return "ssss";
    }

    public void send(DeviceChild deviceChild) {
        try {
            if (deviceChild != null) {
                JSONObject maser = new JSONObject();

                maser.put("ctrlMode", deviceChild.getCtrlMode());
                maser.put("workMode", deviceChild.getWorkMode());
                maser.put("MatTemp", deviceChild.getMatTemp());
                maser.put("LockScreen", deviceChild.getLockScreen());
                maser.put("BackGroundLED", deviceChild.getBackGroundLED());
                maser.put("deviceState", deviceChild.getDeviceState());
                maser.put("tempState", deviceChild.getTempState());
                maser.put("outputMode", deviceChild.getOutputMod());
                maser.put("protectProTemp", deviceChild.getProtectProTemp());
                maser.put("protectSetTemp", deviceChild.getProtectSetTemp());
                String s = maser.toString();
                boolean success = false;
                String topicName;
                String mac = deviceChild.getMacAddress();
                if (deviceChild.getType() == 1 && deviceChild.getControlled() == 2) {
                    String houseId = deviceChild.getHouseId() + "";
                    topicName = "rango/masterController/" + houseId + "/" + mac + "/set";

                    publish(topicName, 1, s);

                } else {
                    topicName = "rango/" + mac + "/set";
                    publish(topicName, 1, s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class DeleteDeviceAsync extends AsyncTask<DeviceChild, Void, Integer> {

        @Override
        protected Integer doInBackground(DeviceChild... deviceChildren) {
            int code = 0;
            DeviceChild deviceChild = deviceChildren[0];
            try {
                SharedPreferences preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
                String userId = preferences.getString("userId", "");
                String updateDeviceNameUrl = "http://47.98.131.11:8082/warmer/v1.0/device/deleteDevice?deviceId=" +
                        URLEncoder.encode(deviceChild.getId() + "", "UTF-8") + "&userId=" + URLEncoder.encode(userId, "UTF-8")
                        + "&houseId=" + URLEncoder.encode(deviceChild.getHouseId() + "", "UTF-8");
//                String updateDeviceNameUrl="http://192.168.168.3:8082/warmer/v1.0/device/deleteDevice?deviceId=6&userId=1&houseId=1000";
//                String updateDeviceNameUrl="http://192.168.168.10:8082/warmer/v1.0/device/deleteDevice?deviceId=1004&userId=1&&houseId=1001";
                String result = HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    if (deviceChild != null) {
                        deviceChildDao.delete(deviceChild);
                        TimeTaskDaoImpl timeTaskDao = new TimeTaskDaoImpl(MQService.this);
                        TimeDaoImpl timeDao = new TimeDaoImpl(MQService.this);
                        List<TimeTask> timeTasks = timeTaskDao.findTimeTasks(deviceChild.getId());
                        for (TimeTask timeTask : timeTasks) {
                            timeTaskDao.delete(timeTask);
                        }
                        List<Timer> timers = timeDao.findAll(deviceChild.getId());
                        for (Timer timer : timers) {
                            timeDao.delete(timer);
                        }
                    }
                }
                Log.d("sss", "-->" + code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }
    }

    public static boolean isGoodJson(String json) {
        try {
            if (!TextUtils.isEmpty(json)) {
                new JsonParser().parse(json);
                return true;
            } else {
                return false;
            }
        } catch (JsonParseException e) {
            System.out.println("bad json: " + json);
            return false;
        }
    }

    public Map<String, DeviceChild> getOfflineDevices() {
        return offlineDevices;
    }

    public void setOffineDevices() {
        if (offlineDevices != null) {
            offlineDevices.clear();
        }
    }

    public void deleteAll() {
        if (deviceChildDao != null) {
            deviceChildDao.deleteAll();
        }
        if (deviceGroupDao != null) {
            deviceGroupDao.deleteAll();
        }
    }

    class LoadDevice extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            int code = 0;
            String url = strings[0];
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        JSONObject content = jsonObject.getJSONObject("content");
                        int deviceId = content.getInt("id");
                        String deviceName = content.getString("deviceName");
                        int type = content.getInt("type");
                        int houseId = content.getInt("houseId");
                        int masterControllerUserId = content.getInt("masterControllerUserId");
                        int isUnlock = content.getInt("isUnlock");
                        int version = content.getInt("version");
                        String macAddress = content.getString("macAddress");
                        int controlled = content.getInt("controlled");
                        DeviceChild deviceChild = deviceChildDao.findDeviceChild((long) deviceId);

                        deviceChild.setDeviceName(deviceName);
                        deviceChild.setType(type);
                        deviceChild.setControlled(controlled);

                        deviceChildDao.update(deviceChild);

                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        msg.obj = deviceChild;
                        handler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }
    }

    /**
     * 添加离线设备
     *
     * @param macAddress
     */
    public void addOffineDevice(String macAddress) {
        offlineList.add(macAddress);
    }

    /**
     * 移除离线设备
     *
     * @param macAddress
     */
    public void removeOfflineDevice(String macAddress) {
        offlineList.remove(macAddress);
    }

    public void clearAllOfflineDevice() {
        offlineList.clear();
    }

    /***
     * 取消所有的订阅
     */
    public void cancelAllsubscibe() {
        List<String> list = getTopicNames();
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            unsubscribe(s);
        }
    }

    class LoadMqttAsync extends AsyncTask<List<String>, Void, Void> {

        @Override
        protected Void doInBackground(List<String>... lists) {
            List<String> deviceChildren = lists[0];
            try {

                if (NetWorkUtil.isConn(MQService.this)) {
                    for (int i = 0; i < deviceChildren.size(); i++) {
                        String mac = deviceChildren.get(i);
                        try {
                            String topic = "rango/" + mac + "/set";
                            Log.i("macAddress2", "-->" + mac);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("loadDate", "1");
                            String s = jsonObject.toString();
                            boolean success = false;
                            success = publish(topic, 1, s);
                            if (!success) {
                                success = publish(topic, 1, s);
                            }
                            if (success) {
                                Log.i("macAddress3", "-->" + mac);
                                Thread.sleep(200);
//                                Thread.currentThread().sleep(300);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                init();
                connect();
            }
            return null;
        }
    }

    class LoadMqttAsync3 extends AsyncTask<List<DeviceChild>, Void, String> {


        @Override
        protected String doInBackground(List<DeviceChild>... lists) {
            List<DeviceChild> deviceChildren = lists[0];
            String result = null;
            try {
                for (int i = 0; i < deviceChildren.size(); i++) {
                    DeviceChild deviceChild = deviceChildren.get(i);
                    String mac = deviceChild.getMacAddress();
                    String topic = "rango/" + mac + "/set";
                    Log.i("macAddress2", "-->" + mac);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("loadDate", "1");
                    String s = jsonObject.toString();
                    boolean success = false;

                    success = publish(topic, 1, s);
                    if (!success) {
                        success = publish(topic, 1, s);
                    } else {
                        Thread.currentThread().sleep(300);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    class CountTimer extends CountDownTimer {
        public CountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**
         * 倒计时过程中调用
         *
         * @param millisUntilFinished
         */
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Tag", "倒计时=" + (millisUntilFinished / 1000));
//            btn_get_code.setBackgroundColor(Color.parseColor("#c7c7c7"));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
//            btn_get_code.setTextSize(16);
        }

        /**
         * 倒计时完成后调用
         */
        @Override
        public void onFinish() {
            Log.e("Tag", "倒计时完成");
            //设置倒计时结束之后的按钮样式
//            btn_get_code.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_blue_light));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
//            btn_get_code.setTextSize(18);
//            if (progressDialog != null) {
//                progressDialog.dismiss();
//            }
            List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();
            new LoadMqttAsync3().execute(deviceChildren);
        }
    }
}