package com.xinrui.smart.util.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MQService extends Service {

    private String TAG="MQService";
    private String host = "tcp://120.77.36.206:1883";
    private String userName = "admin";
    private String passWord = "Xr7891122";

    private MqttClient client;

    public String myTopic = "rango/dc4f220aa96e/transfer";


    private DeviceChildDaoImpl deviceChildDao;
    private MqttConnectOptions options;

    private ScheduledExecutorService scheduler= Executors.newSingleThreadScheduledExecutor();
    private LocalBinder binder=new LocalBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        deviceChildDao=new DeviceChildDaoImpl(this);
        Log.d(TAG,"onCreate");
        init();
        connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {
        public MQService getService(){
            return MQService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(options);
                    List<String> topicNames=getTopicNames();
                    if (!topicNames.isEmpty()){
                        for (String topicName :topicNames){
                            client.subscribe(topicName,1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
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
            options.setKeepAliveInterval(20);
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
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
//                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    System.out.println("topic:"+topicName+",message"+message.toString());
                    Intent intent=new Intent();
                    intent.setAction("mqttmessage");
                    intent.putExtra("topicName",topicName);
                    intent.putExtra("message",message.toString());
                    sendBroadcast(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startReconnect() {

        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }
    public boolean publish(String topicName, int qos, String payload) {
        boolean flag = false;
        if (client != null && client.isConnected()) {

            try {
                MqttMessage message = new MqttMessage(payload.getBytes("utf-8"));
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


    public List<String> getTopicNames(){
        List<String> topicNames=new ArrayList<>();
        List<DeviceChild> list=deviceChildDao.findAllDevice();
        for (DeviceChild deviceChild :list){
            String macAddress=deviceChild.getMacAddress();
            if (!Utils.isEmpty(macAddress)){
                String topicName="rango/"+macAddress+"/transfer";
                topicNames.add(topicName);
            }
        }
        return topicNames;
    }

    public String getName(){
        return "ssss";
    }
}
