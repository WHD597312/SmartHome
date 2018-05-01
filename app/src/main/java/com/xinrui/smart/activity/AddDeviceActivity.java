package com.xinrui.smart.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
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
import com.xinrui.esptouch.EspWifiAdminSimple;
import com.xinrui.esptouch.EsptouchTask;
import com.xinrui.esptouch.IEsptouchListener;
import com.xinrui.esptouch.IEsptouchResult;
import com.xinrui.esptouch.IEsptouchTask;
import com.xinrui.esptouch.task.__IEsptouchTask;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.util.udp.Client;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddDeviceActivity extends AppCompatActivity {

    MyApplication application;
    @BindView(R.id.btn_wifi)
    Button btn_wifi;
    @BindView(R.id.btn_scan)
    Button btn_scan;
    @BindView(R.id.tv_result)
    TextView tv_result;
    @BindView(R.id.et_ssid)
    EditText et_ssid;
    @BindView(R.id.et_pswd)
    EditText et_pswd;
    @BindView(R.id.btn_match)
    Button btn_match;
    String group;
    String groupPosition;
    private long houseId;
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;

    int[] wifi_drawables = {R.drawable.shape_btnwifi_connect, R.drawable.shape_btnwifi_noconnect};
    int[] wifi_colors = new int[2];

    int[] scan_drawables = {R.drawable.shape_btnzxscan_connect, R.drawable.shape_btnzxscan_noconnect};
    @BindView(R.id.linearout_add_wifi_device)
    LinearLayout linearout_add_wifi_device;
    @BindView(R.id.linearout_add_scan_device)
    LinearLayout linearout_add_scan_device;
    int[] visibilities = {View.GONE, View.VISIBLE};
    int visibility;
    int wifi_drawable;
    int wifi_color;

    int scan_drawable;


    private String userId;
    private String wifiConnectionUrl = "http://120.77.36.206:8082/warmer/v1.0/device/registerDevice";
    private String qrCodeConnectionUrl = "http://120.77.36.206:8082/warmer/v1.0/device/createShareDevice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);

        mWifiAdmin = new EspWifiAdminSimple(this);
        SharedPreferences my = getSharedPreferences("my", MODE_PRIVATE);
        userId = my.getString("userId", "");
        for (int i = 0; i < wifi_colors.length; i++) {
            if (0 == i) {
                wifi_colors[0] = getResources().getColor(R.color.white);
            } else if (1 == i) {
                wifi_colors[1] = getResources().getColor(R.color.color_blue);
            }
        }
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        Intent intent = getIntent();
        houseId = Integer.parseInt(intent.getStringExtra("houseId"));
        String wifi = intent.getStringExtra("wifi");

        if (!Utils.isEmpty(wifi)) {
            if ("wifi".equals(wifi)) {
                linearout_add_wifi_device.setVisibility(View.VISIBLE);
                linearout_add_scan_device.setVisibility(View.GONE);
                btn_wifi.setVisibility(View.GONE);
                btn_scan.setVisibility(View.GONE);
            } else if ("share".equals(wifi)) {
                linearout_add_scan_device.setVisibility(View.VISIBLE);
                linearout_add_wifi_device.setVisibility(View.GONE);
                btn_wifi.setVisibility(View.GONE);
                btn_scan.setVisibility(View.GONE);
            }
            btn_wifi.setVisibility(View.GONE);
            btn_scan.setVisibility(View.GONE);
        }
    }

    private String sharedDeviceId;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};

    @OnClick({R.id.img_back, R.id.btn_wifi, R.id.btn_scan, R.id.btn_scan2, R.id.btn_match})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                intent.putExtra("deviceList", "deviceList");
                startActivity(intent);
                break;
            case R.id.btn_wifi:
                wifi_drawable = wifi_drawables[1];
                wifi_drawables[1] = wifi_drawables[0];
                wifi_drawables[0] = wifi_drawable;

                scan_drawable = scan_drawables[0];
                scan_drawables[0] = scan_drawables[1];
                scan_drawables[1] = scan_drawable;

                wifi_color = wifi_colors[1];
                wifi_colors[1] = wifi_colors[0];
                wifi_colors[0] = wifi_color;

                btn_wifi.setBackgroundResource(wifi_drawable);
                btn_scan.setBackgroundResource(scan_drawable);
                btn_wifi.setTextColor(wifi_colors[0]);
                btn_scan.setTextColor(wifi_colors[1]);
                linearout_add_wifi_device.setVisibility(View.GONE);

                visibility = visibilities[1];
                visibilities[1] = visibilities[0];
                visibilities[0] = visibility;
                linearout_add_wifi_device.setVisibility(visibilities[1]);
                linearout_add_scan_device.setVisibility(visibilities[0]);
                break;
            case R.id.btn_scan:
                wifi_drawable = wifi_drawables[1];
                wifi_drawables[1] = wifi_drawables[0];
                wifi_drawables[0] = wifi_drawable;

                scan_drawable = scan_drawables[0];
                scan_drawables[0] = scan_drawables[1];
                scan_drawables[1] = scan_drawable;

                wifi_color = wifi_colors[1];
                wifi_colors[1] = wifi_colors[0];
                wifi_colors[0] = wifi_color;

                btn_wifi.setBackgroundResource(wifi_drawable);
                btn_scan.setBackgroundResource(scan_drawable);
                btn_wifi.setTextColor(wifi_colors[0]);
                btn_scan.setTextColor(wifi_colors[1]);

                visibility = visibilities[1];
                visibilities[1] = visibilities[0];
                visibilities[0] = visibility;
                linearout_add_wifi_device.setVisibility(visibilities[1]);
                linearout_add_scan_device.setVisibility(visibilities[0]);
                break;
            case R.id.btn_scan2:
                scanQrCode();
//                sharedDeviceId="userId";
//                Map<String,Object> params2=new HashMap<>();
//                params2.put("deviceId","1067");
//                params2.put("userId",userId);
//                new QrCodeAsync().execute(params2);
                break;
            case R.id.btn_match:
                String ssid = et_ssid.getText().toString();
                String apPassword = et_pswd.getText().toString();
                String apBssid = mWifiAdmin.getWifiConnectedBssid();
                String taskResultCountStr = "1";
                if (__IEsptouchTask.DEBUG) {
//                    Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
//                            + ", " + " mEdtApPassword = " + apPassword);
                }
                if (!Utils.isEmpty(ssid)) {
//                    new EsptouchAsyncTask3().execute(ssid, apBssid, apPassword, taskResultCountStr);
                    String macAddress="vlinks_test18c63ad6d3ce";
                    Map<String, Object> params = new HashMap<>();
                    params.put("deviceName", "外置2");
                    params.put("houseId", houseId);
                    params.put("masterControllerUserId", Integer.parseInt(userId));
                    params.put("type", 1);
                    params.put("macAddress", macAddress);
                    new WifiConectionAsync().execute(params);

                } else if (!Utils.isEmpty(groupPosition) && !Utils.isEmpty(ssid)) {
                    long group = Long.parseLong(groupPosition);
                    DeviceChild deviceChild = new DeviceChild();
                    deviceChild.setHouseId(group);
                    deviceChild.setDeviceName(ssid);
                    deviceChild.setImg(imgs[0]);
                    deviceChildDao.insert(deviceChild);
                }
                break;
        }
    }

    class WifiConectionAsync extends AsyncTask<Map<String, Object>, Void, Integer> {

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];
            String result = HttpUtils.postOkHpptRequest(wifiConnectionUrl, params);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2001) {

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

                        DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, (long) houseId, masterControllerUserId, type, isUnlock);
                        deviceChild.setImg(imgs[0]);
                        deviceChild.setMacAddress(macAddress);
                        deviceChild.setVersion(version);
                        deviceChild.setControlled(controlled);
                        deviceChild.setOnLint(true);

                        List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice((long) houseId);
                        DeviceChild deviceChild3 = null;

                        for (DeviceChild deviceChild2 : deviceChildren) {
                            if (macAddress.equals(deviceChild2.getMacAddress())) {
                                deviceChild3 = deviceChild2;
                                break;
                            }
                        }
                        if (deviceChild3 == null) {
                            deviceChildDao.insert(deviceChild);

                        } else {
                            deviceChild3 = deviceChildDao.findDeviceById(deviceChild3.getId());
                            deviceChild3.setType(type);
                            deviceChild3.setDeviceName(deviceName);
                            deviceChild3.setHouseId((long) houseId);
                            deviceChild3.setMasterControllerUserId(masterControllerUserId);
                            deviceChild3.setIsUnlock(isUnlock);
                            deviceChild3.setVersion(version);
                            deviceChild3.setMacAddress(macAddress);
                            deviceChild3.setControlled(controlled);
                            deviceChild3.setOnLint(true);
                            deviceChildDao.update(deviceChild3);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }


        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2001:

                    Utils.showToast(AddDeviceActivity.this, "创建成功");
                    Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                    intent.putExtra("deviceList", "deviceList");
                    startActivity(intent);
                    break;
                case -3005:
                    Utils.showToast(AddDeviceActivity.this, "创建失败");
                    break;
            }
        }
    }




    class QrCodeAsync extends AsyncTask<Map<String, Object>, Void, Integer> {
        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];
            String result = HttpUtils.postOkHpptRequest(qrCodeConnectionUrl, params);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    Utils.showToast(AddDeviceActivity.this, "分享设备创建成功");
                    Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
                case -3007:
                    Utils.showToast(AddDeviceActivity.this, "分享设备添加失败");
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        deviceGroupDao = new DeviceGroupDaoImpl(this);
        deviceChildDao = new DeviceChildDaoImpl(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // display the connected ap's ssid
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        if (apSsid != null) {
            et_ssid.setText(apSsid);
        } else {
            et_ssid.setText("");
        }
        // check whether the wifi is connected
        boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
        btn_match.setEnabled(!isApSsidEmpty);
        SharedPreferences wifi = getSharedPreferences("wifi", MODE_PRIVATE);
        if (wifi.contains(et_ssid.getText().toString())) {
            String pswd = wifi.getString(et_ssid.getText().toString(), "");
            et_pswd.setText(pswd);
            et_pswd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(this, "内容为空", Toast.LENGTH_LONG).show();
            } else {
                String content = intentResult.getContents();
                if (content != null) {
                    String deviceId = content;
                    long deviceId2 = Long.parseLong(deviceId);
                    DeviceChild deviceChild = deviceChildDao.findDeviceChild(deviceId2);
                    if (deviceChild != null) {
                        Utils.showToast(AddDeviceActivity.this, "该设备已经存在");
                        Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Map<String, Object> params = new HashMap<>();
                        params.put("deviceId", deviceId);
                        params.put("userId", userId);
                        new QrCodeAsync().execute(params);
                    }

                }
//                tv_result.setText(content);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 扫描二维码
     */
    public void scanQrCode() {
        new IntentIntegrator(this)
                .setOrientationLocked(true)
                .setCaptureActivity(ScanActivity.class)
                .initiateScan();
    }

    private static final String TAG = "Esptouch";
    private EspWifiAdminSimple mWifiAdmin;

    private class EsptouchAsyncTask2 extends AsyncTask<String, Void, IEsptouchResult> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(AddDeviceActivity.this);
            mProgressDialog
                    .setMessage("Esptouch is configuring, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }

        @Override
        protected IEsptouchResult doInBackground(String... params) {
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, AddDeviceActivity.this);
            }
            IEsptouchResult result = mEsptouchTask.executeForResult();
            return result;
        }

        @Override
        protected void onPostExecute(IEsptouchResult result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    "Confirm");
            // it is unnecessary at the moment, add here just to show how to use isCancelled()
            if (!result.isCancelled()) {
                if (result.isSuc()) {
                    mProgressDialog.setMessage("Esptouch success, bssid = "
                            + result.getBssid() + ",InetAddress = "
                            + result.getInetAddress().getHostAddress());
                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                }
            }
        }
    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(AddDeviceActivity.this, text,
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private int type;
    int count = 0;

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(AddDeviceActivity.this);
            mProgressDialog
                    .setMessage("正在配置, 请耐心等待...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE

                String apSsid = mWifiAdmin.getWifiConnectedSsidAscii(params[0]);
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, AddDeviceActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    "确认");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        //                String ssid=et_ssid.getText().toString();
                        DeviceChild deviceChild = new DeviceChild();
                        String ssid = resultInList.getBssid();
                        if (!Utils.isEmpty(ssid)) {
                            String s = ssid.substring(0, 1);
                            type = Integer.parseInt(s);
                            if (type == 0)
                                type = 2;
                        }
//                            DatagramPacket datagramPacket = null;
//                            DatagramSocket datagramSocket=null;
//                            String result2=null;
//                            try {
//                                datagramSocket=new DatagramSocket(1112);
//                                while(true){
//
//                                    byte[] buffer=new byte[50];
//                                    datagramPacket=new DatagramPacket(buffer, buffer.length);
//                                    datagramSocket.receive(datagramPacket);
//                                    byte[] bytes=datagramPacket.getData();
//                                    String data=new String(bytes, 0, bytes.length);
//                                    result2=data;
//                                    if (!Utils.isEmpty(result2)) {
////                                        result2.substring()
//
//                                        if (result2.contains(ssid)){
//                                            String type2=result2.charAt(22)+"";
//                                            type=Integer.parseInt(type2);
//                                            Thread.sleep(500);
//                                            count++;
//                                            Client.send("255.255.255.255","mac:"+ssid+";ok", 2525);
//                                        }
//                                    }
//                                    if (count>10){
//                                        if (datagramSocket!=null){
//                                            datagramSocket.close();
//                                            break;
//                                        }
//                                    }
//                                }
//                            } catch (Exception e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }


                        if (deviceGroupDao != null) {
                            DeviceGroup deviceGroup = deviceGroupDao.findById(houseId);
                            if (deviceGroup != null) {
                                String s = et_ssid.getText().toString().trim();
                                String macAddress = s + ssid;
                                Map<String, Object> params = new HashMap<>();
                                params.put("deviceName", "设备");
                                params.put("houseId", houseId);
                                params.put("masterControllerUserId", Integer.parseInt(userId));
                                params.put("type", type);
                                params.put("macAddress", macAddress);
                                SharedPreferences wifi = getSharedPreferences("wifi", MODE_PRIVATE);
                                boolean success = wifi.edit().putString(et_ssid.getText().toString(), et_pswd.getText().toString()).commit();
                                if (success) {
                                    new WifiConectionAsync().execute(params);
                                }
                            }
                        }

                        sb.append("配置成功");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage(sb.toString());
                } else {
                    mProgressDialog.setMessage("配置失败");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
