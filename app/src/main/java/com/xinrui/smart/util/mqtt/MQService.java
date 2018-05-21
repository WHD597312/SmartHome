package com.xinrui.smart.util.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
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
import com.xinrui.smart.R;
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

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MQService extends Service {

    private String TAG = "MQService";
    private String host = "tcp://120.77.36.206:1883";
    private String userName = "admin";
    private String passWord = "Xr7891122";

    private MqttClient client;

    public String myTopic = "rango/dc4f220aa96e/transfer";


    private DeviceChildDaoImpl deviceChildDao;
    private MqttConnectOptions options;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private LocalBinder binder = new LocalBinder();

    private  int times=0;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        deviceChildDao = new DeviceChildDaoImpl(this);
        Log.d(TAG, "onCreate");
        init();
        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {

        public MQService getService() {
            Log.d(TAG, "Binder");
            return MQService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Log.d(TAG, "onDestroy");
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        new ConAsync().execute();
    }

    class ConAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                client.connect(options);
                List<String> topicNames = getTopicNames();
                if (!topicNames.isEmpty()) {
                    for (String topicName : topicNames) {
                        client.subscribe(topicName, 1);

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
            client = new MqttClient(host, "",
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
            options.setConnectionTimeout(10);
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
                        new LoadAsyncTask().execute(topicName,message.toString());
                    }catch (Exception e){
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

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch,R.mipmap.image_switch2};
    class LoadAsyncTask extends AsyncTask<String,Void,Object>{

        @Override
        protected Object doInBackground(String... strings) {
            String topicName=strings[0];
            Log.i("sssss",topicName);
            String message=strings[1];
            Log.i("sssss",message);
            int timerTaskWeek =0;
            Log.i("ssss",message);

            if ("There is no need to upgrade".equals(message) || "upgradeFinish".equals(message) ||"online".equals(message)){
                if (message!=null && message.length()==0){
                    return null;
                }
                return null;
            }
            String reSet=null;
            if ("reSet".equals(message)){
                reSet="reSet";
            }

            try {
                String macAddress = topicName.substring(6, topicName.lastIndexOf("/"));

                if (!Utils.isEmpty(macAddress)) {

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
                    String timerShutDown="";
                    String ctrlMode = "";
                    int powerValue = 0;
                    int voltageValue = 0;
                    int currentValue = 0;
                    String machineFall = "";
                    int protectSetTemp = 0;
                    int protectProTemp = 0;
                    int extTemp = 0;
                    int extHum = 0;
                    int TimerTemp=0;


                    DeviceChild child = null;


                    DeviceGroupDaoImpl deviceGroupDao = new DeviceGroupDaoImpl(MQService.this);
                    DeviceChildDaoImpl deviceChildDao = new DeviceChildDaoImpl(MQService.this);
                    TimeDaoImpl timeDao = new TimeDaoImpl(MQService.this);
                    TimeTaskDaoImpl timeTaskDao = new TimeTaskDaoImpl(MQService.this);
                    List<DeviceGroup> deviceGroups = deviceGroupDao.findAllDevices();

                    List<List<DeviceChild>> childern = new ArrayList<>();

                    for (DeviceGroup deviceGroup : deviceGroups) {
                        List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(deviceGroup.getId());
                        childern.add(deviceChildren);
                    }
                    groupPostion=0;
                    for (List<DeviceChild> deviceChildren : childern) {
                        childPosition = 0;
                        for (DeviceChild deviceChild : deviceChildren) {
                            String mac = deviceChild.getMacAddress();
                            if (!Utils.isEmpty(mac) && macAddress.equals(mac)) {
                                child = deviceChild;
                                break;
                            }
                            childPosition++;
                        }
                        if (child != null) {
                            break;
                        }
                        groupPostion++;
                    }
                    if (!Utils.isEmpty(reSet)){
                        if (child!=null){/**删除和这个设备相关的所有数据*/

                            new DeleteDeviceAsync().execute(child);

//                            Map<String,Object> params=new HashMap<>();
//                            params.put("houseId",child.getHouseId());
//                            long arr[]=new long[0];
//                            params.put("controlledId",arr);
//                            new ControlledAsync().execute(params);

                            List<DeviceChild> deviceChildren=deviceChildDao.findGroupIdAllDevice(child.getHouseId());
                            for (DeviceChild deviceChild:deviceChildren){
                                if (deviceChild.getType()==1 && deviceChild.getControlled()==1){
                                    deviceChild.setControlled(0);
                                    deviceChild.setCtrlMode("normal");
                                    deviceChildDao.update(deviceChild);
                                    send(deviceChild);
                                }
                                if (deviceChild.getType()==2 && deviceChild.getControlled()==1){
                                    deviceChild.setControlled(0);
                                    deviceChildDao.update(deviceChild);
                                }
                            }

                            deviceChildDao.delete(child);
                            List<TimeTask> timeTasks = timeTaskDao.findTimeTasks(child.getId());
                            for (TimeTask timeTask : timeTasks) {
                                timeTaskDao.delete(timeTask);/**删除定时任务的时间段*/
                            }
                            List<Timer> timers = timeDao.findAll(child.getId());
                            for (Timer timer : timers) {
                                timeDao.delete(timer);/**删除定时任务的时间刻*/
                            }
                        }
                        child=null;/**将该设备重置为null*/
                    } else if (topicName.equals("rango/" + macAddress + "/transfer") && Utils.isEmpty(reSet)) {
                        if(!Utils.isEmpty(message) && isGoodJson(message)) {
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
                            if (device.has("timerShutDown")){
                                timerShutDown=device.getString("timerShutDown");
                            }

                            if (device.has("extTemp")) {
                                extTemp = device.getInt("extTemp");
                            }
                            if (device.has("extHum")) {
                                extHum = device.getInt("extHum");
                            }
                            if (device.has("TimerTemp")){
                                TimerTemp=device.getInt("TimerTemp");
                            }
                        }else {
                            return null;
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
                            if (!Utils.isEmpty(deviceState)){
                                if ("open".equals(deviceState))
                                    child.setImg(imgs[1]);
                                else {
                                    child.setImg(imgs[0]);
                                }
                                child.setDeviceState(deviceState);

                            }

                            if (!Utils.isEmpty(timerShutDown)){
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
                            if (!Utils.isEmpty(ctrlMode)){
                                child.setCtrlMode(ctrlMode);
                                int type=child.getType();
                                if (type==1){
                                    if ("master".equals(ctrlMode)){
                                        child.setControlled(2);
                                    }else if ("slave".equals(ctrlMode)){
                                        child.setControlled(1);
                                    }else if ("normal".equals(ctrlMode)){
                                        child.setControlled(0);
                                    }
                                }
                            }

                            if (powerValue != 0)
                                child.setPowerValue(powerValue);
                            if (voltageValue != 0)
                                child.setVoltageValue(voltageValue);
                            if (currentValue != 0)
                                child.setCurrentValue(currentValue);
                            if (!Utils.isEmpty(machineFall))
                                child.setMachineFall(machineFall);
                            if (protectSetTemp != 0)
                                child.setProtectSetTemp(protectSetTemp);
                            if (protectProTemp != 0)
                                child.setProtectProTemp(protectProTemp);

                            if (child!=null){
                                child.setOnLint(true);
                                child.setTemp(extTemp);
                                child.setHum(extHum);
                                deviceChildDao.update(child);
                            }

                            if (device!=null && device.has("timerTaskWeek")) {
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
                                times=timeDao.findAll(deviceId).size();
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
                                    if (start==23){
                                        if (o.equals("on")) {
                                            end++;
                                            TimeTask controller = new TimeTask(deviceId, timerTaskWeek, start, end, temp);
                                            timeTaskDao.insert(controller);
                                            break;
                                        }
                                    }
                                    for (int j = start + 1; j < 24; j++) {
                                        end++;
                                        if (end == 23){
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
                        }
                    } else if (topicName.equals("rango/" + macAddress + "/lwt")) {
                        if (child != null) {
                            send(child);
                            if ("open".equals(child.getDeviceState())){
                                child.setImg(imgs[2]);
                            }
                            child.setOnLint(false);
                            deviceChildDao.update(child);
                        }
                    }
                    if (DeviceFragment.running == 1) {
                        if (child==null){
                            Intent mqttIntent = new Intent("DeviceFragment");
                            mqttIntent.putExtra("groupPostion", groupPostion);
                            mqttIntent.putExtra("childPosition", childPosition);
                            mqttIntent.putExtra("deviceChild", child);
                            sendBroadcast(mqttIntent);
                        }else {
                            child = deviceChildDao.findDeviceById(child.getId());
                            boolean online=child.getOnLint();
                            if (!online){
                                child = deviceChildDao.findDeviceById(child.getId());

                                Intent mqttIntent = new Intent("DeviceFragment");
                                mqttIntent.putExtra("groupPostion", groupPostion);
                                mqttIntent.putExtra("childPosition", childPosition);
                                mqttIntent.putExtra("deviceState", child.getDeviceState());
                                mqttIntent.putExtra("deviceChild", child);
                                sendBroadcast(mqttIntent);
                            }else {
                                if (device!=null && device.has("deviceState")) {
                                    child = deviceChildDao.findDeviceById(child.getId());
                                    if (timerTaskWeek!=0){/**在线状态下，timerTaskWeek值不为0*/
                                        Log.i("ss","ss");
                                        Message msg=handler.obtainMessage();
                                        msg.what=1;
                                        msg.obj=child;
                                        handler.sendMessage(msg);
                                    }else {
                                        if (child.getType()==1){/**设备类型为1*/
                                            long shareHouseId = Long.MAX_VALUE;
                                            long houseId=child.getHouseId();
                                            if (child.getControlled()==0 || child.getControlled()==2 || houseId==shareHouseId){/**主控，普通设备，分享的设备*/
                                                Log.i("sss","sss");
                                                Message msg=handler.obtainMessage();
                                                msg.what=1;
                                                msg.obj=child;
                                                handler.sendMessage(msg);
                                            }
                                        }else if (child.getType()==2){/**外置温度传感器*/
                                            Message msg=handler.obtainMessage();
                                            msg.what=1;
                                            msg.obj=child;
                                            handler.sendMessage(msg);
                                            Log.i("sssssa","sssssa");
                                        }
                                    }
//                                    handler.sendMessage(msg);
                                }
                            }
                        }
                    } else if (HeaterFragment.running) {
                        if (!Utils.isEmpty(reSet)){
                            Intent mqttIntent = new Intent("HeaterFragment");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        }else {
                            boolean online=false;
                            if (child!=null){
                                child = deviceChildDao.findDeviceById(child.getId());
                                online=child.getOnLint();
                            }


                            if (online){
                                child = deviceChildDao.findDeviceById(child.getId());
                                long houseId = child.getHouseId();
                                long deviceId = child.getId();
                                Intent mqttIntent = new Intent("HeaterFragment");
                                mqttIntent.putExtra("houseId", houseId);
                                mqttIntent.putExtra("deviceId", deviceId);
                                mqttIntent.putExtra("deviceChild", child);
                                sendBroadcast(mqttIntent);

                                Intent mqttIntent2 = new Intent("DeviceListActivity");
                                mqttIntent2.putExtra("online", "online");
                                mqttIntent2.putExtra("deviceChild", child);
                                sendBroadcast(mqttIntent2);
                            }else {
                                Intent mqttIntent = new Intent("DeviceListActivity");
                                mqttIntent.putExtra("deviceChild", child);
                                mqttIntent.putExtra("online", "offline");
                                sendBroadcast(mqttIntent);
                            }
                        }
                    } else if (TimeTaskActivity.running) {
                        if (!Utils.isEmpty(reSet)){
                            Intent mqttIntent = new Intent("TimeTaskActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        }else {
                            if (child==null){
                                Intent mqttIntent = new Intent("TimeTaskActivity");
                                mqttIntent.putExtra("deviceChild", child);
                                sendBroadcast(mqttIntent);
                            }else {
                                Intent mqttIntent = new Intent("TimeTaskActivity");
                                mqttIntent.putExtra("timerTaskWeek", timerTaskWeek);
                                mqttIntent.putExtra("deviceId", child.getId());
                                List<TimeTask> timerTasks = timeTaskDao.findWeekAll(child.getId(), timerTaskWeek);
                                mqttIntent.putExtra("list", (Serializable) timerTasks);
                                sendBroadcast(mqttIntent);
                            }
                        }
                    }else if (ShareDeviceActivity.running){
                        if (!Utils.isEmpty(reSet)) {
                            Intent mqttIntent = new Intent("ShareDeviceActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        }
                    }else if (TempChartActivity.running){
                        if (!Utils.isEmpty(reSet)){
                            Intent mqttIntent = new Intent("TempChartActivity");
                            mqttIntent.putExtra("macAddress", macAddress);
                            sendBroadcast(mqttIntent);
                        }else {
                            Intent mqttIntent = new Intent("TempChartActivity");
                            mqttIntent.putExtra("deviceChild", child);
                            sendBroadcast(mqttIntent);
                        }
                    }else if (Btn1_fragment.running == 2) {
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
                    } else if (SmartFragmentManager.running) {
                        Intent mqttIntent = new Intent("SmartFragmentManager");
                        child = deviceChildDao.findDeviceById(child.getId());
                        long houseId = child.getHouseId();
                        long deviceId = child.getId();
                        mqttIntent.putExtra("houseId", houseId);
                        mqttIntent.putExtra("deviceId", deviceId);
                        mqttIntent.putExtra("deviceChild", child);
                        sendBroadcast(mqttIntent);
//                                context.sendBroadcast(mqttIntent);
                    } else if (RoomContentActivity.running) {
                        child = deviceChildDao.findDeviceById(child.getId());
                        Intent mqttIntent = new Intent("RoomContentActivity");
                        mqttIntent.putExtra("extTemp", extTemp);
                        mqttIntent.putExtra("extHum", extHum);
                        mqttIntent.putExtra("deviceChild", child);
                        sendBroadcast(mqttIntent);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    DeviceChild child= (DeviceChild) msg.obj;
                    Intent mqttIntent = new Intent("DeviceFragment");
                    mqttIntent.putExtra("groupPostion", groupPostion);
                    mqttIntent.putExtra("childPosition", childPosition);
                    mqttIntent.putExtra("deviceState", child.getDeviceState());
                    mqttIntent.putExtra("deviceChild", child);
                    sendBroadcast(mqttIntent);
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
        }, 0 * 1000, 5 * 1000, TimeUnit.MILLISECONDS);
    }

    public boolean publish(String topicName, int qos, String payload) {
        boolean flag = false;
        if (client != null && client.isConnected()) {

            try {
                MqttMessage message = new MqttMessage(payload.getBytes("utf-8"));
                qos=1;
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
            }
        }
        return flag;
    }

    public String getResult() {
        return result;
    }

    public List<String> getTopicNames() {
        List<String> topicNames = new ArrayList<>();
        List<DeviceChild> list = deviceChildDao.findAllDevice();
        for (DeviceChild deviceChild : list) {
            String macAddress = deviceChild.getMacAddress();
            if (!Utils.isEmpty(macAddress)) {
                String topicOffline = "rango/" + macAddress + "/lwt";
                String topicName = "rango/" + macAddress + "/transfer";
                topicNames.add(topicOffline);
                topicNames.add(topicName);
            }
        }
        return topicNames;
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
                    String houseId=deviceChild.getHouseId()+"";
                    topicName = "rango/masterController/"+houseId+"/"+mac+"/set";

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
                String updateDeviceNameUrl = "http://120.77.36.206:8082/warmer/v1.0/device/deleteDevice?deviceId=" +
                        URLEncoder.encode(deviceChild.getId() + "", "UTF-8") + "&userId=" + URLEncoder.encode(userId, "UTF-8")
                        + "&houseId=" + URLEncoder.encode(deviceChild.getHouseId() + "", "UTF-8");
//                String updateDeviceNameUrl="http://192.168.168.3:8082/warmer/v1.0/device/deleteDevice?deviceId=6&userId=1&houseId=1000";
//                String updateDeviceNameUrl="http://192.168.168.10:8082/warmer/v1.0/device/deleteDevice?deviceId=1004&userId=1&&houseId=1001";
                String result = HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                Log.d("sss","-->"+code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }


    }
    public static boolean isGoodJson(String json) {

        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            System.out.println("bad json: " + json);
            return false;
        }
    }
}