package com.xinrui.smart.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.MyApplication;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.SmartTerminalInfo;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.HomeDialog;
import com.xinrui.smart.view_custom.SmartTerminalBar;
import com.xinrui.smart.view_custom.SmartTerminalCircle;
import com.xinrui.smart.view_custom.SmartTerminalHumBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**智能终端*/
public class SmartTerminalActivity extends AppCompatActivity implements View.OnTouchListener{

    Unbinder unbinder;
    MyApplication myApplication;
    /**
     *  -12 到6 为寒冷 / 潮湿
     *  8 到 17为舒适 / 舒适
     *  19 到 34为酷热 / 干燥
     * */
    @BindView(R.id.smartTerminalBar) SmartTerminalBar smartTerminalBar;
    @BindView(R.id.tv_title) TextView tv_title;/**设备名称*/
    @BindView(R.id.tv_temp_value) TextView tv_temp_value;/**温度采样值*/
    @BindView(R.id.tv_hum_value) TextView tv_hum_value;/**湿度采样值*/
    @BindView(R.id.tv_air_value) TextView tv_air_value;/**pm2.5*/
    @BindView(R.id.tv_air_state) TextView tv_air_state;/**空气质量*/
    @BindView(R.id.smartTerminalHumBar) SmartTerminalHumBar  smartTerminalHumBar;
    @BindView(R.id.smart_temp_decrease) ImageView smart_temp_decrease;/**减温度*/
    @BindView(R.id.smart_temp_add) ImageView smart_temp_add;/**加温度*/
    @BindView(R.id.smart_hum_decrease) ImageView smart_hum_decrease;/**减湿度*/
    @BindView(R.id.smart_hum_add) ImageView smart_hum_add;/**加湿度*/
    @BindView(R.id.tv_smart_temp) TextView tv_smart_temp;/**设定温度*/
    @BindView(R.id.image_more) ImageView image_more;/**修改设备名称*/
    private List<SmartTerminalInfo> list=new ArrayList<>();
    private String[] mStrs = new String[]{"", "","", "","","","",""};
    @BindView(R.id.smartTerminalCircle)
    SmartTerminalCircle smartTerminalCircle;
    private DeviceChild deviceChild;
    private DeviceChildDaoImpl deviceChildDao;
    private String linkedUrl= HttpUtils.ipAddress+"/family/device/sensors/getDevicesInRoom";
    private String updateDeviceNameUrl= HttpUtils.ipAddress+"/family/device/changeDeviceName";
    MessageReceiver receiver;
    public static boolean running=false;
    private List<DeviceChild> linkList=new ArrayList<>();
    long sensorId;
    long houseId;
    long roomId;
    private ProgressDialog progressDialog;
    List<DeviceChild> warmers=new ArrayList<>();
    private Map<String,DeviceChild> linkDeviceChildMap=new LinkedHashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_smart_terminal);
        unbinder=ButterKnife.bind(this);
        if (myApplication==null){
            myApplication= (MyApplication) getApplication();
            myApplication.addActivity(this);
        }

        progressDialog = new ProgressDialog(this);

        deviceChildDao=new DeviceChildDaoImpl(getApplicationContext());
        Intent intent=getIntent();
        sensorId=intent.getLongExtra("deviceId",0);
        deviceChild=deviceChildDao.findDeviceById(sensorId);
        houseId=deviceChild.getHouseId();

//        warmers=deviceChildDao.findDeviceByType(houseId,roomId,2,true);
//        linkList=deviceChildDao.findLinkDevice(houseId,roomId,3);
        boolean isConn=NetWorkUtil.isConn(this);
        if (isConn){
            new GetLinkedAsync().execute();
        }else {
            Toast.makeText(this,"请检查网络",Toast.LENGTH_SHORT).show();
        }
        getBitWheelInfos();
//        smartTerminalCircle.setBitInfos(list);
    }

    public void getBitWheelInfos() {
        for (int i = 0; i < mStrs.length; i++) {
            list.add(new SmartTerminalInfo(mStrs[i], BitmapFactory.decodeResource(getResources(), R.mipmap.humidifier)));
        }
    }
    double tempCurProgress=0;/**温度进度*/
    double humCurProgress=0;/**湿度进度*/
    private boolean isBound=false;
    @Override
    protected void onStart() {
        super.onStart();
        running=true;
        Intent service=new Intent(SmartTerminalActivity.this,MQService.class);
        isBound=bindService(service,connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter("SmartTerminalActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);

        tempCurProgress=smartTerminalBar.getCurProcess();
        humCurProgress=smartTerminalHumBar.getCurProcess();
        smart_temp_decrease.setOnTouchListener(this);
        smart_temp_add.setOnTouchListener(this);
        smart_hum_decrease.setOnTouchListener(this);
        smart_hum_add.setOnTouchListener(this);
        if (deviceChild!=null){
            String name=deviceChild.getDeviceName();
            tv_title.setText(name);
            setMode(deviceChild);
        }
    }
    private void setMode(DeviceChild deviceChild){
        boolean online=deviceChild.getOnLint();
        int sensorSimpleTemp=deviceChild.getSensorSimpleTemp();
        int sensorSimpleHum=deviceChild.getSensorSimpleHum();
        int sorsorPm=deviceChild.getSorsorPm();
        tv_temp_value.setText(sensorSimpleTemp+"");
        tv_hum_value.setText(sensorSimpleHum+"");
        tv_air_value.setText(sorsorPm+"");
        if (sorsorPm>0 && sensorSimpleHum<=35){
            tv_air_state.setText("优");
        }else if (sorsorPm>35 && sorsorPm<=75){
            tv_air_state.setText("良");
        }else if (sorsorPm>75){
            tv_air_value.setText("差");
        }
    }

    /**
     * 向取暖器发送数据
     * @param deviceChild
     */
    public void send(DeviceChild deviceChild){
        try {

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    int temp;
    @OnClick({R.id.image_more,R.id.image_back,R.id.smart_temp_decrease,R.id.smart_temp_add,R.id.smart_hum_decrease,R.id.smart_hum_add,R.id.image_linkage})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_more:
                popupmenuWindow();
                break;
            case R.id.image_back:
                Intent intent=new Intent();
                intent.putExtra("houseId",houseId);
                setResult(6000,intent);
                finish();
                break;
            case R.id.smart_temp_decrease:
                tempCurProgress=smartTerminalBar.getCurProcess();
                tempCurProgress=tempCurProgress-1;
                if (tempCurProgress<=-12){
                    tempCurProgress=-12;
                }
                temp=(int)tempCurProgress+16;
                if (temp<=5){
                    temp=5;
                }else if (temp>=42){
                    temp=42;
                }
                Message tempDecrease=handler.obtainMessage();
                tempDecrease.arg1=0;/**减温度标记*/
                handler.sendMessage(tempDecrease);

                break;
            case R.id.smart_temp_add:
                tempCurProgress=smartTerminalBar.getCurProcess();
                tempCurProgress=tempCurProgress+1;
                if (tempCurProgress>=34){
                    tempCurProgress=34;
                }
                temp=(int)tempCurProgress+16;
                if (temp>=42){
                    temp=42;
                }
                Message tempAdd=handler.obtainMessage();
                tempAdd.arg1=1;/**加温度标记*/
                handler.sendMessage(tempAdd);
                break;
            case R.id.smart_hum_decrease:
                humCurProgress=smartTerminalHumBar.getCurProcess();
                humCurProgress=humCurProgress-1;
                if (humCurProgress<=-12){
                    humCurProgress=-12;
                }
                Message humDecrease=handler.obtainMessage();
                humDecrease.arg1=2;/**减湿度标记*/
                handler.sendMessage(humDecrease);
                break;
            case R.id.smart_hum_add:
                humCurProgress=smartTerminalHumBar.getCurProcess();
                humCurProgress=humCurProgress+1;
                if (humCurProgress>=34){
                    humCurProgress=34;
                }
                Message humAdd=handler.obtainMessage();
                humAdd.arg1=3;/**加湿度标记*/
                handler.sendMessage(humAdd);
                break;
            case R.id.image_linkage:
                if (linkList.isEmpty()){
                    Toast.makeText(this,"没有可联动的设备",Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent2=new Intent(this,SmartLinkedActivity.class);
                    intent2.putExtra("sensorId",sensorId);
                    intent2.putExtra("deviceList",(Serializable) linkList);
                    startActivityForResult(intent2,100);
                }
                break;
        }
    }

    String deviceName;
    private void buildUpdateDeviceDialog() {
        final HomeDialog dialog = new HomeDialog(this);
        dialog.setOnNegativeClickListener(new HomeDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new HomeDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                deviceName = dialog.getName();
                if (TextUtils.isEmpty(deviceName)) {
                    Utils.showToast(SmartTerminalActivity.this, "设备名称不能为空");
                } else {
                    new UpdateDeviceAsync().execute();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
    class UpdateDeviceAsync extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            try {

//                String url=updateDeviceNameUrl+"?deviceName="+ URLEncoder.encode(deviceName,"utf-8")+"&deviceId="+deviceId;
//                String result=HttpUtils.getOkHpptRequest(url);
//                JSONObject jsonObject=new JSONObject(result);
//                String returnCode=jsonObject.getString("returnCode");
//                if ("100".equals(returnCode)){
//                    code=100;
//                    deviceChild.setName(deviceName);
//                    deviceChildDao.update(deviceChild);
//                }
//                Log.i("result","-->"+result);
            }catch (Exception e){
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code){
                case 100:
                    Utils.showToast(SmartTerminalActivity.this, "修改成功");
                    tv_title.setText(deviceName);
                    break;
                default:
                    Utils.showToast(SmartTerminalActivity.this, "修改失败");
                    break;
            }
        }
    }
    private PopupWindow popupWindow1;
    public void popupmenuWindow() {
        if (popupWindow1 != null && popupWindow1.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popview_update_device, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        RelativeLayout rl_room_rename = (RelativeLayout) view.findViewById(R.id.rl_room_rename);
        TextView tv_rname_r1 = (TextView) view.findViewById(R.id.tv_rname_r1);
        tv_rname_r1.setText("修改名称");

        popupWindow1 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow1.setFocusable(true);
        popupWindow1.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow1.setAnimationStyle(R.style.Popupwindow);

//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow1.showAsDropDown(image_more, 0, -20);
//        popupWindow.showAtLocation(tv_home_manager, Gravity.RIGHT, 0, 0);
        //添加按键事件监听

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.rl_room_rename:
                        buildUpdateDeviceDialog();
                        popupWindow1.dismiss();
                        break;
                }
            }
        };

        rl_room_rename.setOnClickListener(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==100){
            linkList.clear();
            linkList= (List<DeviceChild>) data.getSerializableExtra("list");
            linkDeviceChildMap.clear();
            List<SmartTerminalInfo> infoList=new ArrayList<>();
//            for (int i = 0; i <linkList.size() ; i++) {
//                DeviceChild deviceChild=linkList.get(i);
//                String macAddress = deviceChild.getMacAddress();
//                int linked=deviceChild.getLinked();
//                if (linked==1){
//                    SmartTerminalInfo terminalInfo=list.get(i);
//                    infoList.add(terminalInfo);
//                    linkDeviceChildMap.put(macAddress,deviceChild);
//                }
//                int type = deviceChild.getType();
//                String onlineTopicName = "";
//                String offlineTopicName = "";
//                switch (type) {
//                    case 2:
//                        onlineTopicName = "p99/warmer/" + macAddress + "/transfer";
//                        offlineTopicName = "p99/warmer/" + macAddress + "/lwt";
//                        mqService.subscribe(onlineTopicName, 1);
//                        mqService.subscribe(offlineTopicName, 2);
//                        break;
//                    case 3:
//                        onlineTopicName = "p99/sensor1/" + macAddress + "/transfer";
//                        offlineTopicName = "p99/sensor1/" + macAddress + "/lwt";
//                        mqService.subscribe(onlineTopicName, 1);
//                        mqService.subscribe(offlineTopicName, 2);
//                        break;
//                }
//            }
            smartTerminalCircle.setBitInfos(infoList);
        }
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("handler","-->"+tempCurProgress);
            switch (msg.arg1){
                case 0:
                    smartTerminalBar.setmCurProcess(tempCurProgress);
                    smartTerminalBar.invalidate();
                    break;
                case 1:
                    smartTerminalBar.setmCurProcess(tempCurProgress);
                    smartTerminalBar.invalidate();
                    break;
                case 2:
                    smartTerminalHumBar.setmCurProcess(humCurProgress);
                    smartTerminalHumBar.invalidate();
                    break;
                case 3:
                    smartTerminalHumBar.setmCurProcess(humCurProgress);
                    smartTerminalHumBar.invalidate();
                    break;
            }

        }
    };
    class GetLinkedAsync extends AsyncTask<Void,Void,Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("正在加载数据");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            String url="http://47.98.131.11:8082/warmer/v1.0/device/getDeviceLinked?sensorsId="+sensorId+"&houseId="+houseId;
            String result=HttpUtils.getOkHpptRequest(url);
            if (!TextUtils.isEmpty(result)){
                Log.i("result","-->"+result);
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        JSONArray content=jsonObject.getJSONArray("content");
                        for (int i = 0; i < content.length(); i++) {
                            JSONObject device=content.getJSONObject(i);
                            int deviceId=device.getInt("deviceId");
                            int linked=device.getInt("linked");
//                            String macAddress=device.getInt("macAddress");
                            DeviceChild deviceChild=deviceChildDao.findDeviceById((long)deviceId);
                            deviceChild.setLinked(linked);
                            deviceChildDao.update(deviceChild);
                            linkList.add(deviceChild);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if(progressDialog!=null){
                progressDialog.dismiss();
            }
            switch (code){
                case 2000:
                    List<SmartTerminalInfo> infoList=new ArrayList<>();
                    for (int i = 0; i <linkList.size() ; i++) {
                        DeviceChild deviceChild=linkList.get(i);
                        String macAddress = deviceChild.getMacAddress();
                        linkDeviceChildMap.put(macAddress,deviceChild);
                        int linked=deviceChild.getLinked();
                        if (linked==1){
                            SmartTerminalInfo terminalInfo=list.get(i);
                            infoList.add(terminalInfo);
                        }
                    }

                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("houseId",houseId);
        setResult(6000,intent);
        finish();
    }
    MQService mqService;
    private boolean bound;
    ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    class MessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String macAddress=intent.getStringExtra("macAddress");
            Log.i("macAddress","-->"+macAddress);
            DeviceChild deviceChild2= (DeviceChild) intent.getSerializableExtra("deviceChild");
            if (deviceChild2!=null && deviceChild!=null && macAddress.equals(deviceChild.getMacAddress())){
                Log.i("macAddress","-->2222");
                deviceChild=deviceChild2;
                setMode(deviceChild);
            }else if (deviceChild2==null && deviceChild!=null){
                if (macAddress.equals(deviceChild.getMacAddress())){
                    Toast.makeText(SmartTerminalActivity.this,"该设备已重置",Toast.LENGTH_SHORT).show();
                    Intent intent2=new Intent();
                    intent.putExtra("houseId",houseId);
                    SmartTerminalActivity.this.setResult(6000,intent2);
                    SmartTerminalActivity.this.finish();
                }else {
                    for (Map.Entry<String,DeviceChild> entry:linkDeviceChildMap.entrySet()){
                        String mac=entry.getKey();
                        DeviceChild deviceChild3=entry.getValue();
                        for (int i = 0; i < linkList.size(); i++) {
                            DeviceChild deviceChild4=linkList.get(i);
                            String macAddress2=deviceChild4.getMacAddress();
                            if (macAddress2.equals(mac)){
//                                deviceChild4.setLinked(0);
                                linkList.set(i,deviceChild4);
                                break;
                            }
                        }
                        if (macAddress.equals(mac)){
                            String name=deviceChild3.getDeviceName();
                            Toast.makeText(SmartTerminalActivity.this,name+"设备已重置",Toast.LENGTH_SHORT).show();
                            linkDeviceChildMap.remove(deviceChild3);
                            break;
                        }

                    }
                    List<SmartTerminalInfo> infoList=new ArrayList<>();
                    for (int i = 0; i <linkDeviceChildMap.size() ; i++) {
                        SmartTerminalInfo terminalInfo=list.get(i);
                        infoList.add(terminalInfo);
                    }
                    if (smartTerminalCircle!=null){
                        smartTerminalCircle.setBitInfos(infoList);
                    }
                }
            }else if (deviceChild2!=null){
                for (Map.Entry<String,DeviceChild> entry:linkDeviceChildMap.entrySet()){
                    String mac=entry.getKey();
                    DeviceChild deviceChild3=entry.getValue();
                    if (mac.equals(deviceChild2.getMacAddress())){
                        linkDeviceChildMap.put(mac,deviceChild2);
                        deviceChildDao.update(deviceChild2);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound){
            unbindService(connection);
        }
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
        running=false;
    }

    private boolean onClick=false;
    Thread thread;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.smart_temp_decrease:

                if (event.getAction()==MotionEvent.ACTION_DOWN){
                    onClick=true;
                    new Thread(){
                        @Override
                        public void run() {
                            while (onClick){
                                tempCurProgress=smartTerminalBar.getCurProcess();
                                tempCurProgress=tempCurProgress-1;
                                if (tempCurProgress<=-12){
                                    tempCurProgress=-12;
                                }
                                try {
                                    Thread.sleep(100);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                Message tempDecrease=handler.obtainMessage();
                                tempDecrease.arg1=0;/**减温度标记*/
                                handler.sendMessage(tempDecrease);
                            }
                        }
                    }.start();
                }else if (event.getAction()==MotionEvent.ACTION_UP){
                    onClick=false;
                    temp=(int)tempCurProgress+15;
                    if (temp<=5){
                        temp=5;
                    }else if (temp>=42){
                        temp=42;
                    }
//                    for (Map.Entry<String,DeviceChild> entry:linkDeviceChildMap.entrySet()){
//                        DeviceChild deviceChild3=entry.getValue();
//                        boolean online=deviceChild3.getOnline();
//                        if (online){
//                            deviceChild3.setWaramerSetTemp(temp);
//                            send(deviceChild3);
//                        }
//                    }
                    tv_smart_temp.setText(temp+"℃");
                }
                break;
            case R.id.smart_temp_add:
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    onClick = true;
                    new Thread() {
                        @Override
                        public void run() {
                            while (onClick) {
                                tempCurProgress = smartTerminalBar.getCurProcess();
                                tempCurProgress++;
                                if (tempCurProgress >= 34) {
                                    tempCurProgress = 34;
                                }

                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Message tempAdd = handler.obtainMessage();
                                tempAdd.arg1 = 1;/**加温度标记*/
                                handler.sendMessage(tempAdd);
                            }
                        }
                    }.start();
                }else if (event.getAction()==MotionEvent.ACTION_UP){
                    onClick=false;
                    temp=(int)tempCurProgress+15;
                    if (temp<=5){
                        temp=5;
                    } else if (temp>=42){
                        temp=42;
                    }
                    for (Map.Entry<String,DeviceChild> entry:linkDeviceChildMap.entrySet()){
                        DeviceChild deviceChild3=entry.getValue();
//                        boolean online=deviceChild3.getOnline();
//                        if (online){
//                            deviceChild3.setWaramerSetTemp(temp);
//                            send(deviceChild3);
//                        }
                    }
                    tv_smart_temp.setText(temp+"℃");
                }
                break;
            case R.id.smart_hum_decrease:
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    onClick = true;
                    new Thread() {
                        @Override
                        public void run() {
                            while (onClick) {
                                humCurProgress = smartTerminalHumBar.getCurProcess();
                                humCurProgress = humCurProgress - 1;
                                if (humCurProgress <= -12) {
                                    humCurProgress = -12;
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Message humDecrease = handler.obtainMessage();
                                humDecrease.arg1 = 2;/**减湿度标记*/
                                handler.sendMessage(humDecrease);
                            }
                        }
                    }.start();
                }else if (event.getAction()==MotionEvent.ACTION_UP){
                    onClick=false;
                }
                break;
            case R.id.smart_hum_add:
                if (event.getAction()==MotionEvent.ACTION_DOWN) {
                    onClick = true;
                    new Thread() {
                        @Override
                        public void run() {
                            while (onClick) {
                                humCurProgress = smartTerminalHumBar.getCurProcess();
                                humCurProgress = humCurProgress + 1;
                                if (humCurProgress >= 34) {
                                    humCurProgress = 34;
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Message humAdd = handler.obtainMessage();
                                humAdd.arg1 = 3;/**加湿度标记*/
                                handler.sendMessage(humAdd);
                            }
                        }
                    }.start();
                }else if (event.getAction()==MotionEvent.ACTION_UP){
                    onClick=false;
                }
                break;
        }
        return false;
    }

}
