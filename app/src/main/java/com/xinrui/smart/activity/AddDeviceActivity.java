package com.xinrui.smart.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.fragment.AddDeviceScanFragment;
import com.xinrui.smart.fragment.AddDeviceWifiFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddDeviceActivity extends AppCompatActivity {

    MyApplication application;
    @BindView(R.id.btn_wifi) Button btn_wifi;
    @BindView(R.id.btn_scan) Button btn_scan;
    @BindView(R.id.tv_result) TextView tv_result;
    @BindView(R.id.et_ssid) EditText et_ssid;
    String group;
    String groupPosition;
    private long houseId;
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;

    int[] wifi_drawables={R.drawable.shape_btnwifi_connect,R.drawable.shape_btnwifi_noconnect};
    int[] wifi_colors=new int[2];

    int[] scan_drawables={R.drawable.shape_btnzxscan_connect,R.drawable.shape_btnzxscan_noconnect};
    @BindView(R.id.linearout_add_wifi_device) LinearLayout linearout_add_wifi_device;
    @BindView(R.id.linearout_add_scan_device) LinearLayout linearout_add_scan_device;
    int []visibilities={View.GONE,View.VISIBLE};
    int visibility;
    int wifi_drawable;
    int wifi_color;

    int scan_drawable;


    private String wifiConnectionUrl="http://120.77.36.206:8082/warmer/v1.0/device/registerDevice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);
        for (int i=0;i<wifi_colors.length;i++){
            if (0==i){
                wifi_colors[0]=getResources().getColor(R.color.white);
            }else if(1==i){
                wifi_colors[1]=getResources().getColor(R.color.color_blue);
            }
        }
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
        Intent intent=getIntent();
        houseId=Integer.parseInt(intent.getStringExtra("houseId"));
        String wifi=intent.getStringExtra("wifi");
        if (!Utils.isEmpty(wifi)){
            btn_wifi.setVisibility(View.GONE);
            btn_scan.setVisibility(View.GONE);
        }
    }

    int[] imgs={R.mipmap.image_unswitch, R.mipmap.image_switch};
    @OnClick({R.id.img_back,R.id.btn_wifi,R.id.btn_scan,R.id.btn_scan2,R.id.btn_match})
    public void onClick(View view){
        switch(view.getId()){
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_wifi:
                wifi_drawable=wifi_drawables[1];
                wifi_drawables[1]=wifi_drawables[0];
                wifi_drawables[0]=wifi_drawable;

                scan_drawable=scan_drawables[0];
                scan_drawables[0]=scan_drawables[1];
                scan_drawables[1]=scan_drawable;

                wifi_color=wifi_colors[1];
                wifi_colors[1]=wifi_colors[0];
                wifi_colors[0]=wifi_color;

                btn_wifi.setBackgroundResource(wifi_drawable);
                btn_scan.setBackgroundResource(scan_drawable);
                btn_wifi.setTextColor(wifi_colors[0]);
                btn_scan.setTextColor(wifi_colors[1]);
                linearout_add_wifi_device.setVisibility(View.GONE);

                visibility=visibilities[1];
                visibilities[1]=visibilities[0];
                visibilities[0]=visibility;
                linearout_add_wifi_device.setVisibility(visibilities[1]);
                linearout_add_scan_device.setVisibility(visibilities[0]);
                break;
            case R.id.btn_scan:
                wifi_drawable=wifi_drawables[1];
                wifi_drawables[1]=wifi_drawables[0];
                wifi_drawables[0]=wifi_drawable;

                scan_drawable=scan_drawables[0];
                scan_drawables[0]=scan_drawables[1];
                scan_drawables[1]=scan_drawable;

                wifi_color=wifi_colors[1];
                wifi_colors[1]=wifi_colors[0];
                wifi_colors[0]=wifi_color;

                btn_wifi.setBackgroundResource(wifi_drawable);
                btn_scan.setBackgroundResource(scan_drawable);
                btn_wifi.setTextColor(wifi_colors[0]);
                btn_scan.setTextColor(wifi_colors[1]);

                visibility=visibilities[1];
                visibilities[1]=visibilities[0];
                visibilities[0]=visibility;
                linearout_add_wifi_device.setVisibility(visibilities[1]);
                linearout_add_scan_device.setVisibility(visibilities[0]);
                break;
            case R.id.btn_scan2:
                scanQrCode();
                break;
            case R.id.btn_match:
                String ssid=et_ssid.getText().toString();
                DeviceChild deviceChild=new DeviceChild();
                if (!Utils.isEmpty(ssid)){
                    if (deviceGroupDao!=null){
                        DeviceGroup deviceGroup = deviceGroupDao.findById(houseId);
                        if (deviceGroup!=null){
                            Map<String,Object> params=new HashMap<>();
                            params.put("deviceName",ssid);
                            params.put("houseId",houseId);
                            params.put("masterControllerUserId",1);
                            params.put("type",1);
                            new WifiConectionAsync().execute(params);

                        }
                    }
                }else if (!Utils.isEmpty(groupPosition) && !Utils.isEmpty(ssid)){
                    long group=Long.parseLong(groupPosition);
                    deviceChild.setGroupId(group);
                    deviceChild.setChild(ssid);
                    deviceChild.setImg(imgs[0]);
                    deviceChildDao.insert(deviceChild);
                }
                break;
        }
    }

    class WifiConectionAsync extends AsyncTask<Map<String,Object>,Void,Integer>{

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code=0;
            Map<String,Object> params=maps[0];
            String result=HttpUtils.postOkHpptRequest(wifiConnectionUrl,params);
            if (!Utils.isEmpty(result)){
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2001){
                        JSONObject content=jsonObject.getJSONObject("content");
                        int deviceId=content.getInt("id");
                        String deviceName=content.getString("deviceName");
                        int type=content.getInt("type");
                        int houseId=content.getInt("houseId");
                        int masterControllerUserId=content.getInt("masterControllerUserId");
                        int isUnlock=content.getInt("isUnlock");
                        DeviceChild deviceChild=new DeviceChild((long)deviceId, deviceName, imgs[0], 0, (long)houseId, type, masterControllerUserId, isUnlock);
                        if (deviceChild!=null){
                            deviceChildDao.insert(deviceChild);
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
            switch (code){
                case 2001:
                    Utils.showToast(AddDeviceActivity.this,"创建成功");
                    Intent intent=new Intent(AddDeviceActivity.this,MainActivity.class);
                    startActivity(intent);
                    break;
                case -3005:
                    Utils.showToast(AddDeviceActivity.this,"创建失败");
                    break;
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        deviceGroupDao=new DeviceGroupDaoImpl(this);
        deviceChildDao=new DeviceChildDaoImpl(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult= IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult!=null){
            if(intentResult.getContents()==null){
                Toast.makeText(this,"内容为空",Toast.LENGTH_LONG).show();
            }else{
                String content=intentResult.getContents();
                tv_result.setText(content);
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**扫描二维码*/
    public void scanQrCode(){
        new IntentIntegrator(this)
                .setOrientationLocked(true)
                .setCaptureActivity(ScanActivity.class)
                .initiateScan();
    }
}
