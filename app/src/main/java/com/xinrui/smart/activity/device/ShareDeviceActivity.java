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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.http.HttpUtils;
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

    public static boolean running=false;
    SharedPreferences preferences;
    Unbinder unbinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device);
        unbinder=ButterKnife.bind(this);
    }

    DeviceChild deviceChild;
    @Override
    protected void onStart() {
        super.onStart();
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        deviceChildDao=new DeviceChildDaoImpl(getApplicationContext());
        Intent intent=getIntent();
        long childPosition=Long.parseLong(intent.getStringExtra("deviceId"));
        deviceChild=deviceChildDao.findDeviceChild(childPosition);

        if (deviceChild!=null){
            String mcuVeriosn=deviceChild.getMCUVerion();
            if(Utils.isEmpty(mcuVeriosn)){
                mcuVeriosn="v1.0";
            }
            tv_version.setText(deviceChild.getWifiVersion()+","+mcuVeriosn);
            long deviceId=deviceChild.getId();
            String deviceName=deviceChild.getDeviceName();
            int type=deviceChild.getType();
            String macAddress=deviceChild.getMacAddress();
            int controlled=deviceChild.getControlled();
            tv_device.setText(deviceName);
            try {
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("deviceId",deviceId);
                jsonObject.put("deviceName",deviceName);
                jsonObject.put("type",type);
                jsonObject.put("macAddress",macAddress);
                jsonObject.put("controlled",controlled);
                jsonObject.put("shareHouseId",deviceChild.getHouseId());
                share=jsonObject.toString();
                Log.d("ss",share);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (!Utils.isEmpty(share)){
                Message msg=handler.obtainMessage();
                msg.what=1;
                handler.sendMessage(msg);
            }
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){
//                createQrCode();
            }
        }
    };
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
        Intent intent = new Intent(this, MQService.class);
        isBound=bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter("ShareDeviceActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);
        running=true;
        new ShareQrCodeAsync().execute();
    }

    @OnClick({R.id.img_back,R.id.btn_update_version})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_back:
                deviceChildDao.closeDaoSession();
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

        deviceChild=null;
    }

    /**生成二维码*/
    private void createQrCode(){
        Bitmap bitmap = ZXingUtils.createQRImage(share,img_qrCode.getWidth(), img_qrCode.getHeight());
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
                String url="http://120.77.36.206:8082/warmer/v1.0/device/getVersions?type="+type;
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
                            String mcuVersion=deviceChild.getMCUVerion();
                            if (wifiVersion2.equals(wifiVersion) && mcuVersion2.equals(mcuVersion)){
                                s="已经是最新版本啦!";
                            }else {
                                s="版本更新成功";
                                deviceChild.setWifiVersion(wifiVersion2);
                                deviceChild.setMCUVerion(mcuVersion2);
                                deviceChildDao.update(deviceChild);
                                String mac=deviceChild.getMacAddress();
                                String topic="rango/"+mac+"/update/firmware";
                                JSONObject jsonObject2=new JSONObject();
                                jsonObject2.put("updateWifi",wifiVersion2);
                                jsonObject2.put("updateMCU",mcuVersion2);
                                String ss=jsonObject2.toString();
                                boolean succes=mqService.publish(topic,2,ss);
                                Log.d("sss","-->"+succes);
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
            if (!Utils.isEmpty(s)){
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
            if (!Utils.isEmpty(noNet)){
                Utils.showToast(ShareDeviceActivity.this,"网络已断开，请设置网络");
            }else {
                if (!Utils.isEmpty(macAddress) && deviceChild.getMacAddress().equals(macAddress)){
                    Utils.showToast(ShareDeviceActivity.this,"该设备已被重置");
                    Intent intent2=new Intent(ShareDeviceActivity.this,MainActivity.class);
                    intent2.putExtra("deviceList","deviceList");
                    startActivity(intent2);
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
                String url="http://120.77.36.206:8082/warmer/v1.0/device/createQRCode?deviceId="+deviceId+"&userId="+userId;
                try {
                    bitmap=Glide.with(ShareDeviceActivity.this)
                            .load(url)
                            .asBitmap()
                            .centerCrop()
                            .into(180,180)
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
