package com.xinrui.smart.activity.device;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.activity.TempChartActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.ZXingUtils;
import com.xinrui.smart.util.mqtt.MQService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShareDeviceActivity extends AppCompatActivity {

    @BindView(R.id.img_back) ImageView img_back;
    @BindView(R.id.img_qrCode) ImageView img_qrCode;
    private DeviceChildDaoImpl deviceChildDao;
    private String share;
    @BindView(R.id.tv_version) TextView tv_version;
    @BindView(R.id.tv_device) TextView tv_device;
    @BindView(R.id.btn_update_version) Button btn_update_version;

    public static boolean running=false;
    SharedPreferences preferences;
    private MyApplication application;
    Unbinder unbinder;
    long deviceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device);

        unbinder=ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }

        preferences = getSharedPreferences("my", MODE_PRIVATE);
        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());

        Log.i("share","-->onCreate");
        Intent intent = getIntent();
        String id = intent.getStringExtra("deviceId");
        deviceId=Long.parseLong(id);
        deviceChild = deviceChildDao.findDeviceById(deviceId);


        if (deviceChild != null) {
            tv_version.setText(deviceChild.getWifiVersion()+","+deviceChild.getMCUVerion());
            String name = deviceChild.getDeviceName();
            tv_device.setText(name);
            String userId = preferences.getString("userId", "");
            String macAddress=deviceChild.getMacAddress();
            int controlled=deviceChild.getControlled();
            int type=deviceChild.getType();
            long houseId=deviceChild.getHouseId();
            String deviceName=deviceChild.getDeviceName();
            String share2="deviceId'"+deviceId+"&userId'"+userId+"&macAddress'"+macAddress+"&type'"+type+"&controlled'"+controlled+"&houseId'"+houseId+"&deviceName'"+deviceName;
            try {
                share=new String(Base64.encode(share2.getBytes("utf-8"), Base64.NO_WRAP),"UTF-8");
                Log.i("share","-->"+share);
            }catch (Exception e){
                e.printStackTrace();
            }
            createQrCode();
//            new ShareQrCodeAsync().execute();
        }
        IntentFilter intentFilter = new IntentFilter("ShareDeviceActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);

        Intent service = new Intent(this, MQService.class);
        isBound=bindService(service, connection, Context.BIND_AUTO_CREATE);

    }

    DeviceChild deviceChild;
    @Override
    protected void onStart() {
        super.onStart();
        running=true;
        deviceChild = deviceChildDao.findDeviceById(deviceId);
        if (deviceChild==null){
            Utils.showToast(this,"该设备已重置");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("deviceList","deviceList");
            startActivity(intent);
        }else {

        }
    }

    MQService mqService;
    private boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    boolean isBound=false;
    MessageReceiver receiver;
    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.img_back,R.id.btn_update_version})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_update_version:
                new UpdateVersionAsync().execute();
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound){
            if (connection!=null){
                unbindService(connection);
            }
        }
        if (unbinder!=null){
            unbinder.unbind();
        }
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        running=false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        running=false;
    }

    /**生成二维码*/
    private void createQrCode(){
        Bitmap bitmap = ZXingUtils.createQRImage(share,400, 400);
        img_qrCode.setImageBitmap(bitmap);
    }
    class UpdateVersionAsync extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... maps) {
            String s=null;
            int code=0;
            int type=-1;
            if(deviceChild!=null){
                type=deviceChild.getType();
            }
            if (type!=-1){
                String url="http://47.98.131.11:8082/warmer/v1.0/device/getVersions?type="+type;
                String result=HttpUtils.getOkHpptRequest(url);
                if (!Utils.isEmpty(result)){
                    try {
                        JSONObject jsonObject=new JSONObject(result);
                        code=jsonObject.getInt("code");
                        if (code==2000){
                            JSONArray content=jsonObject.getJSONArray("content");
                            String wifiVersion2=content.getString(0);
                            String mcuVersion2=content.getString(1);
                            String wifiVersion=deviceChild.getWifiVersion();
                            Log.i("wifiVersion2","wifiVersion2->"+wifiVersion2);
                            Log.i("mcuVersion2","mcuVersion2->"+mcuVersion2);
                            String mcuVersion=deviceChild.getMCUVerion();


                            double wifiVersion0=Double.parseDouble(deviceChild.getWifiVersion().substring(1));
                            double mcuVersion0=Double.parseDouble(deviceChild.getMCUVerion().substring(1));
                            Log.i("wifiVersion0","wifiVersion0->"+wifiVersion0);
                            Log.i("mcuVersion0","mcuVersion0->"+mcuVersion0);
                            if (wifiVersion2!=null && wifiVersion2.equals(wifiVersion) && mcuVersion2!=null&& mcuVersion2.equals(mcuVersion)){
                                s="已经是最新版本啦!";
                            }else {
                                s="版本更新成功";
                                double wifiVersion3=Double.parseDouble(wifiVersion2.substring(1));
                                double mcuVersion3=Double.parseDouble(mcuVersion2.substring(1));
//                                double wifiVersion0=Double.parseDouble(deviceChild.getWifiVersion().substring(1));
//                                double mcuVersion0=Double.parseDouble(deviceChild.getMCUVerion().substring(1));
                                if (wifiVersion3>wifiVersion0 || mcuVersion3>mcuVersion0){
                                    deviceChild.setWifiVersion(wifiVersion2);
                                    deviceChild.setMCUVerion(mcuVersion2);
                                    deviceChildDao.update(deviceChild);
                                    String mac=deviceChild.getMacAddress();
                                    String topic="rango/"+mac+"/update/firmware";
                                    JSONObject jsonObject2=new JSONObject();
                                    jsonObject2.put("updateWifi",wifiVersion2);
                                    jsonObject2.put("updateMCU",mcuVersion2);
                                    String ss=jsonObject2.toString();
                                    boolean succes=mqService.publish(topic,1,ss);
                                    Log.i("sss","-->"+succes);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if ("已经是最新版本啦!".equals(s)){
                Utils.showToast(ShareDeviceActivity.this,s);
//                btn_update_version.setVisibility(View.GONE);
                tv_version.setText(deviceChild.getWifiVersion()+","+deviceChild.getMCUVerion());
            }else {
//                btn_update_version.setVisibility(View.VISIBLE);
                Utils.showToast(ShareDeviceActivity.this,s);
                tv_version.setText(deviceChild.getWifiVersion()+","+deviceChild.getMCUVerion());
            }
        }
    }
    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String macAddress=intent.getStringExtra("macAddress");
            String noNet=intent.getStringExtra("noNet");
            DeviceChild deviceChild2= (DeviceChild) intent.getSerializableExtra("deviceChild");
            if (!Utils.isEmpty(noNet)){
                Utils.showToast(ShareDeviceActivity.this,"网络已断开，请设置网络");
            }else {
                if (!Utils.isEmpty(macAddress) && deviceChild2==null && deviceChild!=null && deviceChild.getMacAddress().equals(macAddress)){
                    Utils.showToast(ShareDeviceActivity.this,"该设备已被重置");
                    Intent intent2=new Intent(ShareDeviceActivity.this,MainActivity.class);
                    intent2.putExtra("deviceList","deviceList");
                    startActivity(intent2);
                }else if (!Utils.isEmpty(macAddress) && deviceChild2!=null && macAddress.equals(deviceChild.getMacAddress())){
                    deviceChild=deviceChild2;
                    deviceChildDao.update(deviceChild);
                    tv_version.setText(deviceChild.getWifiVersion()+","+deviceChild.getMCUVerion());
                }
            }
        }
    }
    class ShareQrCodeAsync extends AsyncTask<Void,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(Void... maps) {
            Bitmap bitmap=null;
            if (deviceChild!=null){
                long deviceId=deviceChild.getId();
                String userId=preferences.getString("userId","");
                String url="http://47.98.131.11:8082/warmer/v1.0/device/createQRCode?deviceId="+deviceId+"&userId="+userId;
                try {
                    bitmap=Glide.with(ShareDeviceActivity.this)
                            .load(url)
                            .asBitmap()
                            .centerCrop()
                            .into(250,250)
                            .get();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap!=null){
                img_qrCode.setImageBitmap(bitmap);
            }
        }
    }
}
