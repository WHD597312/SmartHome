package com.xinrui.smart.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SmartLinkedActivity extends AppCompatActivity {
    Unbinder unbinder;
    private MyApplication application;
    private DeviceChildDaoImpl deviceChildDao;
    @BindView(R.id.list_linked) ListView list_linked;/**可联动的设备视图列表*/
    private Map<Long,DeviceChild> linkedMap=new LinkedHashMap<>();/**已联动的设备*/
    private String chooseDevicesIp="http://47.98.131.11:8082/warmer/v1.0/device/chooseDeviceLinked";
    private List<DeviceChild> list=new LinkedList<>();/**可联动的设备*/
    private LinkdAdapter adapter;
    private long sensorId;/**传感器Id*/
    long houseId;
    long roomId;
    int linkedSensorId;
    DeviceChild deviceChild;
    MessageReceiver receiver;
    public static boolean running=false;
    private List<DeviceChild> linkedList2=new ArrayList<>();
    private boolean isBound=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_linked);
        unbinder=ButterKnife.bind(this);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }
        deviceChildDao=new DeviceChildDaoImpl(getApplicationContext());
//        list=deviceChildDao.findLinkDevice(3);
        Intent intent=getIntent();
        sensorId=intent.getLongExtra("sensorId",0);
        deviceChild = deviceChildDao.findDeviceById(sensorId);
        houseId=deviceChild.getHouseId();
        list= (List<DeviceChild>) intent.getSerializableExtra("deviceList");

        boolean conn=NetWorkUtil.isConn(this);
        if (conn){
            new GetLinkedAsync().execute();
        }

        IntentFilter intentFilter = new IntentFilter("SmartLinkedActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);
        adapter=new LinkdAdapter(this,list);
        list_linked.setAdapter(adapter);

        Intent service = new Intent(SmartLinkedActivity.this, MQService.class);
        isBound = bindService(service, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    MQService mqService;
    private boolean bound;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        running=true;
        deviceChild = deviceChildDao.findDeviceById(sensorId);
        if (deviceChild==null){
            Utils.showToast(SmartLinkedActivity.this,"该设备已重置");
            Intent intent2=new Intent(SmartLinkedActivity.this,MainActivity.class);
            intent2.putExtra("deviceList","deviceList");
            startActivity(intent2);
        }else {
            if (mqService!=null){
                if (!list.isEmpty()){
                    int first=list.size();
                    for (int i=0;i<list.size();i++){
                        DeviceChild deviceChild2=list.get(i);
                        DeviceChild deviceChild3=mqService.findDeviceByMacAddress(deviceChild2.getMacAddress());
                        if (deviceChild3==null){
                            String macAddress=deviceChild2.getMacAddress();
                            if (linkedMap.containsKey(macAddress)){
                                linkedMap.remove(deviceChild2);
                            }
                            list.remove(i);
                        }else {
                            if (deviceChild3.getType()==1 && deviceChild3.getControlled()==1){
                                String macAddress=deviceChild2.getMacAddress();
                                if (linkedMap.containsKey(macAddress)){
                                    linkedMap.remove(deviceChild2);
                                }
                                list.remove(i);
                            }
                        }
                    }
                    int second=list.size();
                    if (list.isEmpty()){
                        Intent intent=new Intent();
                        intent.putExtra("list",(Serializable) list);
                        setResult(100,intent);
                        finish();
                    }else {
                        if (first!=second)
                            adapter.notifyDataSetChanged();
                    }
                }
            }
        }

    }

    @OnClick({R.id.image_back,R.id.btn_ensure})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_back:
                finish();
                break;
            case R.id.btn_ensure:
                Map<String,Object> params=new HashMap<>();
                params.put("sensorsId",sensorId);
                params.put("houseId",houseId);
                List<Long> list=new ArrayList<>();
                try {
                    for (Map.Entry<Long, DeviceChild> entry : linkedMap.entrySet()) {
                        long deviceId=entry.getKey();
                        DeviceChild deviceChild=entry.getValue();
                        int linked=deviceChild.getLinked();
                        if (linked==1){
                            list.add(deviceId);
                        }
                    }
                    long arr[]=new long[list.size()];
                    for (int i = 0; i <list.size() ; i++) {
                        arr[i]=list.get(i);
                    }
                    params.put("deviceIds",arr);
                    new ChooseAsync().execute(params);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        if (isBound){
            unbindService(connection);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        running=false;
    }

    class LinkdAdapter extends BaseAdapter{
        private Context context;
        private List<DeviceChild> list;

        public LinkdAdapter(Context context, List<DeviceChild> list) {
            this.context = context;
            this.list = list;
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int position) {
            return list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;
            if (convertView==null){
                convertView=View.inflate(context,R.layout.item_linked,null);
                viewHolder=new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            final CheckBox check=viewHolder.check;
            final DeviceChild deviceChild=list.get(position);
            if (deviceChild!=null){
                viewHolder.tv_linked.setText(deviceChild.getDeviceName());
                int controlled=deviceChild.getControlled();
                if (controlled == 2) {
                    viewHolder.image_linked.setImageResource(R.mipmap.master);
                } else if (controlled == 0) {
                    viewHolder.image_linked.setImageResource(R.mipmap.heater2);
                }
                int linked=deviceChild.getLinked();
                if (linked==1){
                    check.setChecked(true);
                }else if (linked==0){
                    check.setChecked(false);
                }
            }
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (check.isChecked()){
                        deviceChild.setLinked(1);
                        long deviceId=deviceChild.getId();
                        linkedMap.put(deviceId,deviceChild);
                    }else {
                        deviceChild.setLinked(0);
                        long deviceId=deviceChild.getId();
                        linkedMap.put(deviceId,deviceChild);
                    }
                }
            });
            return convertView;
        }
        class ViewHolder{
            @BindView(R.id.image_linked) ImageView image_linked;
            @BindView(R.id.tv_linked) TextView tv_linked;
            @BindView(R.id.check) CheckBox check;
            public ViewHolder(View view){
                ButterKnife.bind(this,view);
            }
        }
    }
    class ChooseAsync extends AsyncTask<Map<String,Object>,Void,Integer>{
        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code=0;
            Map<String,Object> params=maps[0];
            String result=HttpUtils.postOkHpptRequest(chooseDevicesIp, params);
            if (!TextUtils.isEmpty(result)){
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code==2000){
                Toast.makeText(SmartLinkedActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                List<DeviceChild> list=new ArrayList<>();
                for (Map.Entry<Long, DeviceChild> entry : linkedMap.entrySet()) {
                    DeviceChild deviceChild=entry.getValue();
                    list.add(deviceChild);
                }
                Intent intent=new Intent();
                intent.putExtra("list",(Serializable) list);
                setResult(100,intent);
                finish();
            }else {
                Toast.makeText(SmartLinkedActivity.this,"设置失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class GetLinkedAsync extends AsyncTask<Void, Void, List<DeviceChild>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<DeviceChild> doInBackground(Void... voids) {
            int code = 0;
            List<DeviceChild> list2=new ArrayList<>();
            String url = "http://47.98.131.11:8082/warmer/v1.0/device/getDeviceLinked?sensorsId=" + sensorId + "&houseId=" + houseId;
            String result = HttpUtils.getOkHpptRequest(url);
            if (!TextUtils.isEmpty(result)) {
                Log.i("result", "-->" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        linkedMap.clear();
                        JSONArray content = jsonObject.getJSONArray("content");
                        for (int i = 0; i < content.length(); i++) {
                            if (list2.size()>8){
                                break;
                            }
                            JSONObject device = content.getJSONObject(i);
                            long deviceId = device.getLong("deviceId");
                            int linked = device.getInt("linked");
                            int controlled=device.getInt("controlled");
                            String macAddress = device.getString("macAddress");
                            DeviceChild deviceChild = deviceChildDao.findDeviceById(deviceId);
                            deviceChild.setLinked(linked);
                            if (deviceChild!=null && deviceChild.getType()==1 && deviceChild.getControlled()!=1){
                                linkedMap.put(deviceId,deviceChild);
                                if (!list2.contains(deviceChild)){
                                    list2.add(deviceChild);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return list2;
        }

        @Override
        protected void onPostExecute(List<DeviceChild> linkedList) {
            super.onPostExecute(linkedList);
            try {
                if (linkedList!=null && !linkedList.isEmpty()){
                    list.clear();
                    list.addAll(linkedList);
                    adapter.notifyDataSetChanged();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    class MessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String macAddress=intent.getStringExtra("macAddress");
            DeviceChild deviceChild4= (DeviceChild) intent.getSerializableExtra("deviceChild");
            if (deviceChild4==null){
                if (deviceChild!=null && macAddress.equals(deviceChild.getMacAddress())){
                    String name=deviceChild.getDeviceName();
                    Utils.showToast(SmartLinkedActivity.this,name+"设备已重置");
                    Intent intent2=new Intent(SmartLinkedActivity.this,MainActivity.class);
                    intent2.putExtra("deviceList","deviceList");
                    startActivity(intent2);
                }else {
                    DeviceChild deviceChild3=null;
                    for(DeviceChild deviceChild2:list){
                        if (macAddress.equals(deviceChild2.getMacAddress())){
                            deviceChild3=deviceChild2;
                            break;
                        }
                    }
                    if (deviceChild3!=null){
                        String name=deviceChild3.getDeviceName();
                        Utils.showToast(SmartLinkedActivity.this,name+"设备已重置");
                        list.remove(deviceChild3);
                        if (linkedMap.containsKey(macAddress)){
                            linkedMap.remove(deviceChild3);
                        }
                        if (!list.isEmpty()){
                            adapter.notifyDataSetChanged();
                        }else {
                            Intent intent2=new Intent();
                            intent2.putExtra("list",(Serializable) list);
                            SmartLinkedActivity.this.setResult(100,intent2);
                            finish();
                        }
                    }
                }
            }else {
                if (deviceChild4.getType()==1 && deviceChild4.getControlled()==1){
                    DeviceChild deviceChild3=null;
                    for(DeviceChild deviceChild2:list){
                        if (macAddress.equals(deviceChild2.getMacAddress())){
                            deviceChild3=deviceChild2;
                            break;
                        }
                    }
                    if (deviceChild3!=null){
                        String name=deviceChild3.getDeviceName();
                        list.remove(deviceChild3);
                        deviceChildDao.update(deviceChild4);
                        if (linkedMap.containsKey(macAddress)){
                            linkedMap.remove(deviceChild3);
                        }
                        Utils.showToast(SmartLinkedActivity.this,name+"设备已为受控机");
                        if (!list.isEmpty()){
                            adapter.notifyDataSetChanged();
                        }else {
                            Intent intent2=new Intent();
                            intent2.putExtra("list",(Serializable) list);
                            SmartLinkedActivity.this.setResult(100,intent2);
                            finish();
                        }
                    }
                }
            }
        }
    }

}
