package com.xinrui.smart.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
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
import com.xinrui.location.CheckPermissionsActivity;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.IsChinese;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.AddDeviceDialog;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class AddDeviceActivity extends CheckPermissionsActivity {
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
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
    public static long houseId;

    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;

    int[] wifi_drawables = {R.drawable.shape_btnwifi_connect, R.drawable.shape_btnwifi_noconnect};
    int[] wifi_colors = new int[2];

    int[] scan_drawables = {R.drawable.shape_btnzxscan_connect, R.drawable.shape_btnzxscan_noconnect};
    @BindView(R.id.linearout_add_wifi_device)
    LinearLayout linearout_add_wifi_device;
    @BindView(R.id.linearout_add_scan_device)
    LinearLayout linearout_add_scan_device;
    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.linear)
    LinearLayout linear;
    int[] visibilities = {View.GONE, View.VISIBLE};
    int visibility;
    int wifi_drawable;
    int wifi_color;
    @BindView(R.id.layout_help)
    RelativeLayout layout_help;

    int scan_drawable;

    String houseAddress;
    private String userId;
    private String wifiConnectionUrl = "http://47.98.131.11:8082/warmer/v1.0/device/registerDevice";
    private String qrCodeConnectionUrl = "http://47.98.131.11:8082/warmer/v1.0/device/createShareDevice";

    private AddDeviceDialog addDeviceDialog;

    GifDrawable gifDrawable;
    int getAlpha;
    int getAlpha2;

    float alpha = 0;
    private boolean isBound = false;
    private String mac = null;
    MessageReceiver receiver;
    public static boolean running = false;
    WindowManager.LayoutParams lp;
    DeviceChild deviceChild = null;
    private String deviceName;
    private String province;
    /**
     * 省
     */
    private String city;
    /**
     * 市
     */
    private String distrct;

    /**
     * 区
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);

        getAlpha = linear.getBackground().mutate().getAlpha();

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
        if (application != null) {
            application.addActivity(this);
        }
        Intent intent = getIntent();

        houseId = Long.parseLong(intent.getStringExtra("houseId"));
        deviceGroupDao = new DeviceGroupDaoImpl(getApplicationContext());
        DeviceGroup deviceGroup = deviceGroupDao.findById(houseId);
        houseAddress = deviceGroup.getLocation();
        String wifi = intent.getStringExtra("wifi");

        if (!Utils.isEmpty(wifi)) {
            if ("wifi".equals(wifi)) {
                linearout_add_wifi_device.setVisibility(View.VISIBLE);
                linearout_add_scan_device.setVisibility(View.GONE);
                btn_wifi.setVisibility(View.GONE);
                btn_scan.setVisibility(View.GONE);

                et_pswd.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        char chars[]=s.toString().toCharArray();
                        for (char c:chars){
                            if (IsChinese.isChinese(c)){
                                et_pswd.setText("");
                                Utils.showToast(AddDeviceActivity.this,"不能输入中文");
                                break;
                            }
                        }
                    }
                });
//                Intent service = new Intent(this, MQService.class);
//                startService(service);
            } else if ("share".equals(wifi)) {
                linearout_add_scan_device.setVisibility(View.VISIBLE);
                linearout_add_wifi_device.setVisibility(View.GONE);
                btn_wifi.setVisibility(View.GONE);
                btn_scan.setVisibility(View.GONE);
            }
            btn_wifi.setVisibility(View.GONE);
            btn_scan.setVisibility(View.GONE);
        }
        Intent service = new Intent(this, MQService.class);
        startService(service);

        initLocation();
        startLocation();//开始定位
    }

    private String sharedDeviceId;
    private PopupWindow popupWindow;

    public void popupmenuWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }

        View view = View.inflate(this, R.layout.popup_help, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        RelativeLayout rl_heater = (RelativeLayout) view.findViewById(R.id.rl_heater);
        RelativeLayout rl_sensor = (RelativeLayout) view.findViewById(R.id.rl_sensor);


        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow.showAsDropDown(layout_help, 0, -20);
//        popupWindow.showAtLocation(tv_home_manager, Gravity.RIGHT, 0, 0);
        //添加按键事件监听

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.rl_heater:
                        window3=0;
                        popupmenuWindow2();
                        popupWindow.dismiss();
                        break;
                    case R.id.rl_sensor:
                        window3=0;
                        popupmenuWindow4();
                        popupWindow.dismiss();
                        break;
                }
            }
        };

        rl_heater.setOnClickListener(listener);
        rl_sensor.setOnClickListener(listener);
    }

    PopupWindow popupWindow2;
    GifImageView image_heater_help;
    CountTimer2 countTimer2 = null;
    CountTimer3 countTimer3 = null;

    public void popupmenuWindow2() {
        if (popupWindow2 != null && popupWindow2.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popup_help2, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        image_heater_help = (GifImageView) view.findViewById(R.id.image_heater_help);
        try {
            gifDrawable = new GifDrawable(getResources(), R.mipmap.help1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        image_heater_help.setVisibility(View.VISIBLE);
        if (gifDrawable != null) {
            gifDrawable.start();
            image_heater_help.setImageDrawable(gifDrawable);

            CountTimer2 countTimer2 = new CountTimer2(10000, 1000);
            countTimer2.start();
        }

        popupWindow2 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow2.setFocusable(true);
        popupWindow2.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow2.setAnimationStyle(R.style.Popupwindow);
        backgroundAlpha(0.6f);
        popupWindow2.setFocusable(false);
        popupWindow2.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow2.showAsDropDown(btn_match, 0, -20);
//        popupWindow.showAtLocation(tv_home_manager, Gravity.RIGHT, 0, 0);
        //添加按键事件监听
    }


    //设置蒙版
    private void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }

    class CountTimer2 extends CountDownTimer {
        public CountTimer2(long millisInFuture, long countDownInterval) {
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
            if (window3 == 0) {
                try {
                    gifDrawable = new GifDrawable(getResources(), R.mipmap.help2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                image_heater_help.setVisibility(View.VISIBLE);
                if (gifDrawable != null) {
                    gifDrawable.start();
                    image_heater_help.setImageDrawable(gifDrawable);
                    if (countTimer3 == null) {
                        countTimer3 = new CountTimer3(6000, 1000);
                        countTimer3.start();
                    } else if (countTimer3 != null) {
                        countTimer3.start();
                    }
                }
            }
        }
    }

    class CountTimer3 extends CountDownTimer {
        public CountTimer3(long millisInFuture, long countDownInterval) {
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
            if (window3 == 0) {
                try {
                    gifDrawable = new GifDrawable(getResources(), R.mipmap.help1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                image_heater_help.setVisibility(View.VISIBLE);
                if (gifDrawable != null) {
                    gifDrawable.start();
                    image_heater_help.setImageDrawable(gifDrawable);
                    if (countTimer2 == null) {
                        countTimer2 = new CountTimer2(10000, 1000);
                        countTimer2.start();
                    } else if (countTimer2 != null) {
                        countTimer2.start();
                    }

                }
            }

        }
    }

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};
    private int match = 0;

    @OnClick({R.id.img_back, R.id.btn_wifi, R.id.btn_scan, R.id.btn_scan2, R.id.btn_match, R.id.layout_help})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                match = 0;
                Log.i("dialog", "sssssss");
                window3 = 1;
                et_ssid.setEnabled(true);
                et_pswd.setEnabled(true);
                btn_match.setEnabled(true);
                if (mEsptouchTask != null) {
                    mEsptouchTask.interrupt();
                }
                if (countTimer2 != null) {
                    countTimer2.cancel();
                }
                if (countTimer3 != null) {
                    countTimer3.cancel();
                }
                if (popupWindow != null && popupWindow.isShowing()) {
                    if (gifDrawable != null && gifDrawable.isRunning()) {
                        gifDrawable.stop();
                    }
                    popupWindow.dismiss();
                    backgroundAlpha(1f);
                    break;
                }
                if (popupWindow2 != null && popupWindow2.isShowing()) {
                    if (gifDrawable != null && gifDrawable.isRunning()) {
                        gifDrawable.stop();
                    }
                    popupWindow2.dismiss();
                    backgroundAlpha(1f);
                    break;
                }
//                Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
//                intent.putExtra("deviceList", "deviceList");
//                startActivity(intent);
                Intent intent = new Intent();
                intent.putExtra("houseId", houseId);
                setResult(6000, intent);
                finish();
                break;
            case R.id.layout_help:
                match = 0;
                window3=1;
                et_ssid.setEnabled(false);
                et_pswd.setEnabled(false);
                btn_match.setEnabled(false);
                popupmenuWindow();
                break;
            case R.id.btn_wifi:
                match = 0;
                window3=1;
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
                match = 0;
                window3=1;
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
                match = 0;
                window3=1;
                startActivity(new Intent(this, QRScannerActivity.class));
                break;
            case R.id.btn_match:
                boolean conn= NetWorkUtil.isConn(AddDeviceActivity.this);
                if (conn){
                    String ssid = et_ssid.getText().toString();
                    String apPassword = et_pswd.getText().toString();
                    String apBssid = mWifiAdmin.getWifiConnectedBssid();
                    String taskResultCountStr = "1";
                    if (__IEsptouchTask.DEBUG) {
//                    Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
//                            + ", " + " mEdtApPassword = " + apPassword);
                    }
                    if (Utils.isEmpty(ssid)){
                        Utils.showToast(AddDeviceActivity.this, "请连接英文名称的WiFi");
                        break;
                    }
                    if (Utils.isEmpty(apPassword)) {
                        Utils.showToast(AddDeviceActivity.this, "请输入wifi密码");
                        break;
                    }
                    if (!Utils.isEmpty(ssid)) {
                        if (isBound){
                            unbindService(connection);
                        }
//                    popupWindow();
                        match = 1;
                        window3 = 1;
                        et_ssid.setEnabled(false);
                        et_pswd.setEnabled(false);
                        btn_match.setEnabled(false);
                        popupmenuWindow3();
                        new EsptouchAsyncTask3().execute(ssid, apBssid, apPassword, taskResultCountStr);
//                        Intent service = new Intent(AddDeviceActivity.this, MQService.class);
//                        isBound = bindService(service, connection, Context.BIND_AUTO_CREATE);
//                        mac="5asdfghi69m";
                    }
                }else {
                    Utils.showToast(AddDeviceActivity.this,"请检查网络");
                }
                break;
        }
    }

    private int window3 = 0;

    public void popupmenuWindow3() {
        if (popupWindow2 != null && popupWindow2.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popup_help2, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        image_heater_help = (GifImageView) view.findViewById(R.id.image_heater_help);
        try {
            gifDrawable = new GifDrawable(getResources(), R.mipmap.touxiang3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        image_heater_help.setVisibility(View.VISIBLE);
        if (gifDrawable != null) {
            gifDrawable.start();
            image_heater_help.setImageDrawable(gifDrawable);
        }
        if (countTimer2 != null) {
            countTimer2.cancel();
        }
        if (countTimer3 != null) {
            countTimer3.cancel();
        }

        popupWindow2 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow2.setFocusable(true);
        popupWindow2.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow2.setAnimationStyle(R.style.Popupwindow);
        backgroundAlpha(0.6f);
        popupWindow2.setFocusable(false);
        popupWindow2.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow2.showAsDropDown(btn_match, 0, -20);
//        popupWindow.showAtLocation(tv_home_manager, Gravity.RIGHT, 0, 0);
        //添加按键事件监听
    }

    public void popupmenuWindow4() {
        if (popupWindow2 != null && popupWindow2.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popup_help2, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        image_heater_help = (GifImageView) view.findViewById(R.id.image_heater_help);
        window3 = 1;
        try {
            gifDrawable = new GifDrawable(getResources(), R.mipmap.help3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        image_heater_help.setVisibility(View.VISIBLE);
        if (gifDrawable != null) {
            gifDrawable.start();
            image_heater_help.setImageDrawable(gifDrawable);
        }

        popupWindow2 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow2.setFocusable(true);
        popupWindow2.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow2.setAnimationStyle(R.style.Popupwindow);
        backgroundAlpha(0.6f);
        popupWindow2.setFocusable(false);
        popupWindow2.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
//        popupWindow2.showAsDropDown(btn_match, 0, -20);
        popupWindow2.showAsDropDown(btn_match, 0, -20);
        //添加按键事件监听
    }

    String macAddress;
    int deviceId;

    private String success;

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
                        SharedPreferences wifi = getSharedPreferences("wifi", MODE_PRIVATE);
                        wifi.edit().putString(et_ssid.getText().toString(), et_pswd.getText().toString()).commit();
                        JSONObject content = jsonObject.getJSONObject("content");
                        deviceId = content.getInt("id");
                        String deviceName = content.getString("deviceName");
                        int type = content.getInt("type");
                        long houseId = content.getLong("houseId");
                        int masterControllerUserId = content.getInt("masterControllerUserId");
                        int isUnlock = content.getInt("isUnlock");
                        int version = content.getInt("version");
                        macAddress = content.getString("macAddress");
                        int controlled = content.getInt("controlled");
                        String onlineTopicName = "p99/sensor1/" + macAddress + "/transfer";
                        String offlineTopicName = "p99/sensor1/" + macAddress + "/lwt";
                        String topicName2 = "rango/" + macAddress + "/transfer";
                        String topicOffline = "rango/" + macAddress + "/lwt";
                        boolean succ = false;
                        if (!TextUtils.isEmpty(onlineTopicName)) {
                            boolean success2 = mqService.subscribe(offlineTopicName, 1);
                            boolean success = mqService.subscribe(onlineTopicName, 1);
                            succ = mqService.subscribe(topicName2, 1);
                            succ = mqService.subscribe(topicOffline, 1);
                            if (!success) {
                                mqService.subscribe(onlineTopicName, 1);
                            }
                            if (!success2) {
                                mqService.subscribe(offlineTopicName, 1);
                            }
                            if (type == 2) {
                                String topicName = "p99/sensor1/" + macAddress + "/set";
                                if (TextUtils.isEmpty(city)) {
                                    city = houseAddress;
                                    if (city.contains("市")) {
                                        city = city.substring(0, city.length() - 1);
                                    }
                                    String info = "url:http://apicloud.mob.com/v1/weather/query?key=257a640199764&city=" + URLEncoder.encode(city, "utf-8");
                                    mqService.publish(topicName, 1, info);
                                } else {
                                    if (city.contains("市")) {
                                        city = city.substring(0, city.length() - 1);
                                    }
                                    String info = "url:http://apicloud.mob.com/v1/weather/query?key=257a640199764&city=" + URLEncoder.encode(city, "utf-8") + "&province=" + URLEncoder.encode(province, "utf-8");
                                    mqService.publish(topicName, 1, info);
                                }
                            }
                        }
                        deviceChild = new DeviceChild((long) deviceId, houseId, deviceName, macAddress, type);
                        deviceChild.setImg(imgs[0]);
                        deviceChild.setControlled(controlled);
                        deviceChild.setOnLint(true);

                        List<DeviceChild> deleteDevices=deviceChildDao.findDeviceByMacAddress(macAddress);
                        if (deleteDevices!=null && !deleteDevices.isEmpty()){
                            deviceChildDao.deleteDevices(deleteDevices);
                        }
//                        List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();
//                        for (DeviceChild deviceChild2 : deviceChildren) {
//                            if (macAddress.equals(deviceChild2.getMacAddress())) {
//                                deviceChildDao.delete(deviceChild2);
//                                break;
//                            }
//                        }
                        deviceChildDao.insert(deviceChild);
                        if (type == 1) {
                            if (succ) {
                                JSONObject jsonObject2 = new JSONObject();
                                jsonObject2.put("loadDate", "1");
                                String s = jsonObject2.toString();
                                String topicName = "rango/" + macAddress + "/set";
                                boolean success = mqService.publish(topicName, 1, s);
                                if (success)
                                    if (!success) {
                                        success = mqService.publish(topicName, 1, s);
                                    }
                                Log.i("sss", "-->" + success);
                            }
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
                    if (isBound) {
                        unbindService(connection);
                    }
                    if (receiver != null) {
                        unregisterReceiver(receiver);
                    }
                    success = "success";
                    Log.i("WifiConectionAsync", "-->" + "onPostExecute");

                    if (popupWindow2 != null && popupWindow2.isShowing()) {
                        if (gifDrawable != null && gifDrawable.isPlaying()) {
                            gifDrawable.stop();
                        }
                        match = 0;
                        popupWindow2.dismiss();
                        backgroundAlpha(1f);
                    }

                    Utils.showToast(AddDeviceActivity.this, "创建成功");
                    Intent intent = new Intent();
                    intent.putExtra("deviceId", deviceId + "");
                    intent.putExtra("houseId", houseId);
                    setResult(6000, intent);
                    finish();
//                    Intent intent2 = new Intent(AddDeviceActivity.this, MainActivity.class);
//                    intent2.putExtra("deviceList", "deviceList");
//
//                    startActivity(intent2);
                    break;
                default:
                    if (popupWindow2 != null && popupWindow2.isShowing()) {
                        if (gifDrawable != null && gifDrawable.isPlaying()) {
                            gifDrawable.stop();

                            if (et_ssid != null) {
                                et_ssid.setEnabled(true);
                            }
                            if (et_pswd != null) {
                                et_pswd.setEnabled(true);
                            }
                            if (btn_match != null) {
                                btn_match.setEnabled(true);
                                Utils.showToast(AddDeviceActivity.this, "配置失败");
                            }

                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                        match = 0;
                        popupWindow2.dismiss();
                        backgroundAlpha(1f);
                    }
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

        IntentFilter intentFilter = new IntentFilter("AddDeviceActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);
        Log.i("AddDevice", "-->" + "onStart");
        match=0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // display the connected ap's ssid
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        Log.i("apSsid","-->"+apSsid);

        // check whether the wifi is connected
//        boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
//        btn_match.setEnabled(!isApSsidEmpty);



        SharedPreferences wifi = getSharedPreferences("wifi", MODE_PRIVATE);
        if (wifi.contains(apSsid)) {
            et_ssid.setText(apSsid);
            et_ssid.setFocusable(false);
            String pswd = wifi.getString(apSsid, "");
            et_pswd.setText(pswd);
        }else {
            et_ssid.setText(apSsid);
            et_ssid.setFocusable(false);
            et_pswd.setText("");
        }

        et_pswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                char chars[]=s.toString().toCharArray();
                for (char c:chars){
                    if (IsChinese.isChinese(c)){
                        et_pswd.setText("");
                        Utils.showToast(AddDeviceActivity.this,"不能输入中文");
                        break;
                    }
                }
            }
        });
        if (!TextUtils.isEmpty(apSsid)) {
            et_ssid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.showToast(AddDeviceActivity.this,"WiFi名称不可编辑");
                }
            });
        } else {
            et_ssid.setText("");
            et_ssid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.showToast(AddDeviceActivity.this,"请连接英文名称的wifi");
                }
            });
            et_pswd.setText("");
        }

        running = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("AddDevice", "-->" + "onPause");
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
        Log.i("AddDevice", "-->" + "onStop");
        running = false;
        match=0;
//        receiverCount=0;
    }


    private static final String TAG = "Esptouch";
    private EspWifiAdminSimple mWifiAdmin;


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

    MQService mqService;
    private boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
            if (bound == true && !TextUtils.isEmpty(mac)) {
                String wifiName = et_ssid.getText().toString();
                macAddress = wifiName + mac;
                deviceName = mac;
                if (!TextUtils.isEmpty(macAddress)) {
                    new AddDeviceAsync().execute(macAddress);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    private class AddDeviceAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... macs) {

            String macAddress = macs[0];
            deviceChild = new DeviceChild();
            deviceChild.setMacAddress(macAddress);
            String topicName2 = "p99/" + macAddress + "/transfer";
            if (mqService != null) {
                boolean success = mqService.subscribe(topicName2, 1);
                if (success) {
                    String topicName = "p99/" + macAddress + "/set";
                    String payLoad = "getType";
                    boolean step2 = mqService.publish(topicName, 1, payLoad);
                    if (!step2){
                       step2 = mqService.publish(topicName, 1, payLoad);
                    }
                }
            }
            return null;
        }
    }

    private int stopMatching = 0;
    private ProgressDialog mProgressDialog;


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
            stopMatching = 1;
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
            stopMatching = 0;
            if (match == 1) {
                if (popupWindow2 != null && popupWindow2.isShowing()) {
                    if (gifDrawable != null && gifDrawable.isPlaying()) {
                        gifDrawable.stop();

                        if (et_ssid != null) {
                            et_ssid.setEnabled(true);
                        }
                        if (et_pswd != null) {
                            et_pswd.setEnabled(true);
                        }
                        if (btn_match != null) {
                            btn_match.setEnabled(true);
                            Utils.showToast(AddDeviceActivity.this, "配置失败");
                        }

                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                    match = 0;
                    popupWindow2.dismiss();
                    backgroundAlpha(1f);
                }
            }
            //设置倒计时结束之后的按钮样式
//            btn_get_code.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_blue_light));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
//            btn_get_code.setTextSize(18);
//            if (progressDialog != null) {
//                progressDialog.dismiss();
//            }
        }
    }

    private IEsptouchTask mEsptouchTask;

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
//            CountTimer countTimer = new CountTimer(30000, 1000);
//            countTimer.start();
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
                    String ssid = "";
                    for (IEsptouchResult resultInList : result) {
                        ssid = resultInList.getBssid();
                        Log.i("ssidssid", "-->" + ssid);
                        sb.append("配置成功");
                        if (!TextUtils.isEmpty(ssid)) {
                            Intent service = new Intent(AddDeviceActivity.this, MQService.class);
                            isBound = bindService(service, connection, Context.BIND_AUTO_CREATE);
                            mac = ssid;
                        }
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                } else {
                    if (popupWindow2 != null && popupWindow2.isShowing()) {
                        if (gifDrawable != null && gifDrawable.isPlaying()) {
                            gifDrawable.stop();

                            if (et_ssid != null) {
                                et_ssid.setEnabled(true);
                            }
                            if (et_pswd != null) {
                                et_pswd.setEnabled(true);
                            }
                            if (btn_match != null) {
                                btn_match.setEnabled(true);
                                Utils.showToast(AddDeviceActivity.this, "配置失败");
                            }

                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                        match = 0;
                        popupWindow2.dismiss();
                        backgroundAlpha(1f);
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            et_ssid.setEnabled(true);
            et_pswd.setEnabled(true);
            btn_match.setEnabled(true);
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
            window3 = 1;
            match=0;
            if (countTimer2 != null) {
                countTimer2.cancel();
            }
            if (countTimer3 != null) {
                countTimer3.cancel();
            }
            if (popupWindow2 != null && popupWindow2.isShowing()) {
                if (gifDrawable != null && gifDrawable.isRunning()) {
                    gifDrawable.stop();
                }
                popupWindow2.dismiss();
                backgroundAlpha(1f);
                return false;
            }
            if (popupWindow != null && popupWindow.isShowing()) {
                if (gifDrawable != null && gifDrawable.isRunning()) {
                    gifDrawable.stop();
                }
                popupWindow.dismiss();
                backgroundAlpha(1f);
                return false;
            }
//            Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
//            intent.putExtra("deviceList", "deviceList");
//            startActivity(intent);
            Intent intent = new Intent();
            intent.putExtra("houseId", houseId);
            setResult(6000, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    int duration = 0;
    int MESSAGE_SUCCESS = 0;

    public void popupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }

        View view = View.inflate(this, R.layout.dialog_add_device, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels;

//        ImageView add_image= (ImageView) view.findViewById(R.id.add_image);
        GifImageView add_image = (GifImageView) view.findViewById(R.id.add_image);

        if (gifDrawable != null) {
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

//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (popupWindow != null) {
//                popupWindow.dismiss();
//            }
//        }
//    };

    int receiverCount = 0;

    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String macAddress = intent.getStringExtra("macAddress");
            String online = intent.getStringExtra("online");
            int type = intent.getIntExtra("deviceType", 0);
            if (!TextUtils.isEmpty(macAddress) && macAddress.equals(AddDeviceActivity.this.macAddress)) {
                if (!TextUtils.isEmpty(online)) {
                    String topicName2 = "p99/" + macAddress + "/transfer";
                    if (mqService != null) {
                        boolean success = mqService.subscribe(topicName2, 1);
                        if (success) {
                            String topicName = "p99/" + macAddress + "/set";
                            String payLoad = "getType";
                            boolean step2 = mqService.publish(topicName, 1, payLoad);
                        }
                    }
                } else {
                    if (type == 1) {
                        AddDeviceActivity.running = false;
                        Log.i("receiverCount", "-->" + receiverCount);
                        Log.i("type2", "-->" + type);
                        Map<String, Object> params = new HashMap<>();
                        params.put("deviceName", deviceName);
                        params.put("houseId", houseId);
                        params.put("masterControllerUserId", Integer.parseInt(userId));
                        params.put("type", type);
                        params.put("macAddress", macAddress);
                        new WifiConectionAsync().execute(params);
                    } else if (type == 3) {
                        AddDeviceActivity.running = false;
                        type = 2;
                        Map<String, Object> params = new HashMap<>();
                        params.put("deviceName", deviceName);
                        params.put("houseId", houseId);
                        params.put("masterControllerUserId", Integer.parseInt(userId));
                        params.put("type", type);
                        params.put("macAddress", macAddress);
                        new WifiConectionAsync().execute(params);
                        receiverCount++;
                    } else {
                        AddDeviceActivity.running=true;
                        new NoResultAsync().execute();
                    }
                }
            }
        }
    }

    class NoResultAsync extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            return 200;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code == 200) {
                if (gifDrawable != null && gifDrawable.isPlaying()) {
                    gifDrawable.stop();
                    et_ssid.setEnabled(true);
                    et_pswd.setEnabled(true);
                    btn_match.setEnabled(true);
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (TextUtils.isEmpty(success)) {
                if (isBound) {
                    unbindService(connection);
                }

                if (receiver != null) {
                    unregisterReceiver(receiver);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        running = false;
    }

    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {

                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");

                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + com.xinrui.location.Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                }
                sb.append("***定位质量报告***").append("\n");
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("\n");
                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + com.xinrui.location.Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                //解析定位结果，
                String result = sb.toString();
                Log.i("reSult", "-->" + result);

                if ("定位失败".equals(result)) {

                }

                province = location.getProvince();
                city = location.getCity();
                distrct = location.getDistrict();
                if (!TextUtils.isEmpty(province) && !TextUtils.isEmpty(city) && !TextUtils.isEmpty(distrct)) {
                    stopLocation();
                    destroyLocation();
                }
            }
        }
    };

    /**
     * 获取GPS状态的字符串
     *
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode) {
        String str = "";
        switch (statusCode) {
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
        //根据控件的选择，重新设置定位参数
//        resetOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }
}