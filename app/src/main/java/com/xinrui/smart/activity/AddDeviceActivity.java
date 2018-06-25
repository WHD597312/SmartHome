package com.xinrui.smart.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
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
import com.xinrui.smart.view_custom.AddDeviceDialog;

import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class AddDeviceActivity extends AppCompatActivity {

    private boolean running=false;
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
    @BindView(R.id.add_image) pl.droidsonroids.gif.GifImageView add_image;
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;

    int[] wifi_drawables = {R.drawable.shape_btnwifi_connect, R.drawable.shape_btnwifi_noconnect};
    int[] wifi_colors = new int[2];

    int[] scan_drawables = {R.drawable.shape_btnzxscan_connect, R.drawable.shape_btnzxscan_noconnect};
    @BindView(R.id.linearout_add_wifi_device)
    LinearLayout linearout_add_wifi_device;
    @BindView(R.id.linearout_add_scan_device)
    LinearLayout linearout_add_scan_device;
    @BindView(R.id.img_back) ImageView img_back;
    @BindView(R.id.linear) LinearLayout linear;
    int[] visibilities = {View.GONE, View.VISIBLE};
    int visibility;
    int wifi_drawable;
    int wifi_color;

    int scan_drawable;


    private String userId;
    private String wifiConnectionUrl = "http://47.98.131.11:8082/warmer/v1.0/device/registerDevice";
    private String qrCodeConnectionUrl = "http://47.98.131.11:8082/warmer/v1.0/device/createShareDevice";

    private AddDeviceDialog addDeviceDialog;

    GifDrawable gifDrawable;
    int getAlpha;
    int getAlpha2;

    float alpha=0;
    WindowManager.LayoutParams lp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);
        add_image= (GifImageView) findViewById(R.id.add_image);


        getAlpha=linear.getBackground().mutate().getAlpha();
        getAlpha2=add_image.getBackground().mutate().getAlpha();

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

        houseId = Long.parseLong(intent.getStringExtra("houseId"));
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
        et_pswd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    private String sharedDeviceId;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};


    @OnClick({R.id.img_back, R.id.btn_wifi, R.id.btn_scan, R.id.btn_scan2, R.id.btn_match})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Log.i("dialog","sssssss");
                if (gifDrawable!=null && gifDrawable.isPlaying()){
                    gifDrawable.stop();
                    add_image.setVisibility(View.GONE);
                    et_ssid.setEnabled(true);
                    et_pswd.setEnabled(true);
                    btn_match.setEnabled(true);
                    break;
                }
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
                startActivity(new Intent(this,QRScannerActivity.class));
//                scanQrCode();
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
                if (Utils.isEmpty(apPassword)){
                    Utils.showToast(AddDeviceActivity.this,"请输入wifi密码");
                    break;
                }
                if (!Utils.isEmpty(ssid)) {
//                    popupWindow();
                    add_image.setVisibility(View.VISIBLE);
                    et_ssid.setEnabled(false);
                    et_pswd.setEnabled(false);
                    btn_match.setEnabled(false);
                    try {
                        gifDrawable=new GifDrawable(getResources(),R.mipmap.touxiang3);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (gifDrawable!=null){
                        gifDrawable.start();
                        add_image.setImageDrawable(gifDrawable);
                    }


//                    lp.alpha = 0.4f;
//                    getWindow().setAttributes(lp);
//                    linear.getBackground().mutate().setAlpha(100);

                    add_image.getBackground().mutate().setAlpha(0);
//                    add_image.getBackground().mutate().setAlpha((int) alpha);

//                    WindowManager.LayoutParams lp=getWindow().getAttributes();
//                    lp.alpha = 0.4f;
//                    getWindow().setAttributes(lp);

                    new EsptouchAsyncTask3().execute(ssid, apBssid, apPassword, taskResultCountStr);
//                    String macAddress="vlinks_test18d634d6d3c6";
//                    Map<String, Object> params = new HashMap<>();
//                    params.put("deviceName", "设备3");
//                    params.put("houseId", houseId);
//                    params.put("masterControllerUserId", Integer.parseInt(userId));
//                    params.put("type", 1);
//                    params.put("macAddress", macAddress);
//                    new WifiConectionAsync().execute(params);

                }
                break;
        }
    }


    String macAddress;
    int deviceId;

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
                        deviceId = content.getInt("id");
                        String deviceName = content.getString("deviceName");
                        int type = content.getInt("type");
                        int houseId = content.getInt("houseId");
                        int masterControllerUserId = content.getInt("masterControllerUserId");
                        int isUnlock = content.getInt("isUnlock");
                        int version = content.getInt("version");
                        macAddress = content.getString("macAddress");
                        int controlled = content.getInt("controlled");

                        DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, (long) houseId, masterControllerUserId, type, isUnlock);
                        deviceChild.setImg(imgs[0]);
                        deviceChild.setMacAddress(macAddress);
                        deviceChild.setVersion(version);
                        deviceChild.setControlled(controlled);
//                        deviceChild.setOnLint(true);
                        deviceChild.setOnLint(true);
                        List<DeviceChild> deviceChildren2=deviceChildDao.findGroupIdAllDevice((long)houseId);
//                        deviceChild.setChildPosition(deviceChildren2.size());
                        DeviceGroup deviceGroup=deviceGroupDao.findById((long)houseId);
                        Log.i("position","-->"+deviceGroup.getGroupPosition());
                        Log.i("position","-->"+deviceChild.getChildPosition());
//                        deviceChild.setGroupPosition(deviceGroup.getGroupPosition());

                        List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();
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
                            deviceChildDao.delete(deviceChild3);
                            deviceChildDao.insert(deviceChild);
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
//                    if (popupWindow!=null && popupWindow.isShowing()){
//                        popupWindow.dismiss();
//                    }
                    if (gifDrawable!=null){
                        gifDrawable.stop();
                    }

                    Utils.showToast(AddDeviceActivity.this, "创建成功");
                    Intent intent2 = new Intent(AddDeviceActivity.this, MainActivity.class);
                    intent2.putExtra("deviceList", "deviceList");
                    intent2.putExtra("deviceId", deviceId + "");
                    startActivity(intent2);
                    break;
                case -3005:
                    Utils.showToast(AddDeviceActivity.this, "创建失败");
                    break;
            }
        }
    }

    //http://host:port/app/version/device/getDeviceById?deviceId='deviceId'
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
                        deviceId = content.getInt("id");
                        String deviceName = content.getString("deviceName");
                        int type = content.getInt("type");
                        int shareHouseId = content.getInt("houseId");
                        int masterControllerUserId = content.getInt("masterControllerUserId");
                        int isUnlock = content.getInt("isUnlock");
                        int version = content.getInt("version");
                        macAddress = content.getString("macAddress");
                        int controlled = content.getInt("controlled");

                        long houseId = Long.MAX_VALUE;
                        DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, houseId, masterControllerUserId, type, isUnlock);
                        deviceChild.setImg(imgs[0]);
                        deviceChild.setMacAddress(macAddress);
                        deviceChild.setVersion(version);
                        deviceChild.setControlled(controlled);
                        deviceChild.setOnLint(true);
                        deviceChild.setDeviceState("open");
                        deviceChild.setShareHouseId(shareHouseId);

                        List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(houseId);
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
                            deviceChild3.setDeviceState("open");
                            deviceChildDao.update(deviceChild3);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    Utils.showToast(AddDeviceActivity.this, "创建成功");
                    Intent intent2 = new Intent(AddDeviceActivity.this, MainActivity.class);
                    intent2.putExtra("deviceList", "deviceList");
                    intent2.putExtra("deviceId", deviceId + "");
                    startActivity(intent2);
                    break;
                default:
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
                    if (code == 2000) {
                        String deviceId = (String) params.get("deviceId");
                        String url = "http://47.98.131.11:8082/warmer/v1.0/device/getDeviceById?deviceId=" + deviceId;
                        new LoadDevice().execute(url);
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
                case 2000:
//                    Utils.showToast(AddDeviceActivity.this, "分享设备创建成功");
//                    Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
//                    intent.putExtra("shareMacAddress", shareMacAddress);
//                    startActivity(intent);
                    break;
                case -3007:
                    Utils.showToast(AddDeviceActivity.this, "分享设备添加失败");
                    break;
            }
        }
    }


    SharedPreferences preferences;

    @Override
    protected void onStart() {
        super.onStart();
        deviceGroupDao = new DeviceGroupDaoImpl(getApplicationContext());
        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        Log.i("AddDevice","-->"+"onStart");
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
        Log.i("AddDevice","-->"+"onResume");

        running=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("AddDevice","-->"+"onPause");
        if (popupWindow != null && popupWindow.isShowing()) {
            img_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                    intent.putExtra("deviceList", "deviceList");
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("AddDevice","-->"+"onStop");
        running=false;
    }

    String shareDeviceId;
    String shareContent;
    String shareMacAddress;

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (intentResult != null) {
//            if (intentResult.getContents() == null) {
//                Toast.makeText(this, "内容为空", Toast.LENGTH_LONG).show();
//            } else {
//                String content = intentResult.getContents();
//                if (!Utils.isEmpty(content)) {
//                    content = new String(Base64.decode(content, Base64.DEFAULT));
//                    if (!Utils.isEmpty(content)) {
//                        String[] ss = content.split("&");
//                        String s0 = ss[0];
//                        String deviceId = s0.substring(s0.indexOf("'") + 1);
//                        String s2 = ss[2];
//                        String macAddress = s2.substring(s2.indexOf("'") + 1);
//                        shareMacAddress = macAddress;
//                        Map<String, Object> params = new HashMap<>();
//                        params.put("deviceId", deviceId);
//                        params.put("userId", userId);
//                        new QrCodeAsync().execute(params);
//                    }
//
//
//                }
////                tv_result.setText(content);
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

//    class LoadUserInfoAsync extends AsyncTask<String,Void,Void>{
//
//        @Override
//        protected Void doInBackground(String.. voids) {
//            return null;
//        }
//    }

//    /**
//     * 扫描二维码
//     */
//    public void scanQrCode() {
//
//        IntentIntegrator integrator = new IntentIntegrator(AddDeviceActivity.this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//        integrator.setCaptureActivity(ScanActivity.class);
//        integrator.setPrompt("请扫描二维码"); //底部的提示文字，设为""可以置空
//        integrator.setCameraId(0); //前置或者后置摄像头
//        integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
//        integrator.setBarcodeImageEnabled(true);//是否保留扫码成功时候的截图
//        integrator.initiateScan();
//    }

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
//                Toast.makeText(AddDeviceActivity.this, text,
//                        Toast.LENGTH_LONG).show();
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

    private ProgressDialog mProgressDialog;
    private PopupWindow popupWindow;

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {


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
//            popupWindow();
//            addDeviceDialog=new AddDeviceDialog(AddDeviceActivity.this);
//            addDeviceDialog.setCanceledOnTouchOutside(false);
//            addDeviceDialog.show();

//            mProgressDialog = new ProgressDialog(AddDeviceActivity.this);
//            mProgressDialog
//                    .setMessage("正在配置, 请耐心等待...");
//            mProgressDialog.setCanceledOnTouchOutside(false);
//            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    synchronized (mLock) {
//                        if (__IEsptouchTask.DEBUG) {
//                            Log.i(TAG, "progress dialog is canceled");
//                        }
//                        if (mEsptouchTask != null) {
//                            mEsptouchTask.interrupt();
//                        }
//                    }
//                }
//            });
//            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
//                    "Waiting...", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    });
//            mProgressDialog.show();
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
//                    .setEnabled(false);
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
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
//                    .setEnabled(true);
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
//                    "确认");
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
                            if (type == 0){
                                type = 2;
                            }
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
                                params.put("deviceName", ssid);
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
//                    mProgressDialog.setMessage(sb.toString());
                } else {
                    if (running){
                        if (gifDrawable!=null && gifDrawable.isPlaying()){
                            gifDrawable.stop();
                            if (add_image!=null){
                                add_image.setVisibility(View.GONE);
                                et_ssid.setEnabled(true);
                                et_pswd.setEnabled(true);
                                btn_match.setEnabled(true);
                            }
                        }
                        Utils.showToast(AddDeviceActivity.this,"配置失败");
                    }
//                    mProgressDialog.setMessage("配置失败");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (gifDrawable!=null && gifDrawable.isPlaying()){
            gifDrawable.stop();
            add_image.setVisibility(View.GONE);
            et_ssid.setEnabled(true);
            et_pswd.setEnabled(true);
            btn_match.setEnabled(true);
            return;
        }
        Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
        intent.putExtra("deviceList", "deviceList");
        startActivity(intent);
    }

    int duration=0;
    int MESSAGE_SUCCESS=0;
    public void popupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }

        View view = View.inflate(this, R.layout.dialog_add_device, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels;

//        ImageView add_image= (ImageView) view.findViewById(R.id.add_image);
        GifImageView add_image= (GifImageView) view.findViewById(R.id.add_image);

        if (gifDrawable!=null){
            gifDrawable.start();
            add_image.setImageDrawable(gifDrawable);
        }

//        // 从Assets中获取
//        GifDrawable gifFromAssets = new GifDrawable(getAssets(), "anim.gif");
//// 从drawable或者raw中获取


//从输入流中获取，如果GifDrawable不再使用，输入流会自动关闭。另外，你还可以通过调用recycle()关闭不再使用的输入流

//        Glide.with(AddDeviceActivity.this)
//                .load(R.mipmap.touxiang3)
//                .centerCrop()
//                .into(add_image);
        popupWindow = new PopupWindow(view, width, height, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);

        ColorDrawable dw = new ColorDrawable(0x30000000);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
//        popupWindow.showAtLocation(AddDeviceActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        popupWindow.showAtLocation(btn_match, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        //添加按键事件监听
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (popupWindow!=null){
                popupWindow.dismiss();
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceChildDao.closeDaoSession();
        deviceGroupDao.closeDaoSession();


    }
}