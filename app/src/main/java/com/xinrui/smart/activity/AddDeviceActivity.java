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
    public static long houseId;
    @BindView(R.id.add_image)
    pl.droidsonroids.gif.GifImageView add_image;
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

    int scan_drawable;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);
        add_image = (GifImageView) findViewById(R.id.add_image);


        getAlpha = linear.getBackground().mutate().getAlpha();
        getAlpha2 = add_image.getBackground().mutate().getAlpha();

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
        if (application!=null){
            application.addActivity(this);
        }
        Intent intent = getIntent();

        houseId = Long.parseLong(intent.getStringExtra("houseId"));
        String wifi = intent.getStringExtra("wifi");

        if (!Utils.isEmpty(wifi)) {
            if ("wifi".equals(wifi)) {
                linearout_add_wifi_device.setVisibility(View.VISIBLE);
                linearout_add_scan_device.setVisibility(View.GONE);
                btn_wifi.setVisibility(View.GONE);
                btn_scan.setVisibility(View.GONE);

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
        et_pswd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);


        Intent service = new Intent(this, MQService.class);
        startService(service);
    }

    private String sharedDeviceId;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};


    @OnClick({R.id.img_back, R.id.btn_wifi, R.id.btn_scan, R.id.btn_scan2, R.id.btn_match})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Log.i("dialog", "sssssss");
                if (gifDrawable != null && gifDrawable.isPlaying()) {
                    gifDrawable.stop();
                    add_image.setVisibility(View.GONE);
                    et_ssid.setEnabled(true);
                    et_pswd.setEnabled(true);
                    btn_match.setEnabled(true);
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
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
                startActivity(new Intent(this, QRScannerActivity.class));
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
                if (Utils.isEmpty(apPassword)) {
                    Utils.showToast(AddDeviceActivity.this, "请输入wifi密码");
                    break;
                }
                if (!Utils.isEmpty(ssid)) {
//                    popupWindow();
                    add_image.setVisibility(View.VISIBLE);
                    et_ssid.setEnabled(false);
                    et_pswd.setEnabled(false);
                    btn_match.setEnabled(false);
                    try {
                        gifDrawable = new GifDrawable(getResources(), R.mipmap.touxiang3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (gifDrawable != null) {
                        gifDrawable.start();
                        add_image.setImageDrawable(gifDrawable);
                    }
                    add_image.getBackground().mutate().setAlpha(0);
                    new EsptouchAsyncTask3().execute(ssid, apBssid, apPassword, taskResultCountStr);
                }
                break;
        }
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
                        int houseId = content.getInt("houseId");
                        int masterControllerUserId = content.getInt("masterControllerUserId");
                        int isUnlock = content.getInt("isUnlock");
                        int version = content.getInt("version");
                        macAddress = content.getString("macAddress");
                        int controlled = content.getInt("controlled");

                        deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, (long) houseId, masterControllerUserId, type, isUnlock);
                        deviceChild.setImg(imgs[0]);
                        deviceChild.setMacAddress(macAddress);
                        deviceChild.setVersion(version);
                        deviceChild.setControlled(controlled);
                        deviceChild.setOnLint(true);

                        List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();
                        for (DeviceChild deviceChild2 : deviceChildren) {
                            if (macAddress.equals(deviceChild2.getMacAddress())) {
                                deviceChildDao.delete(deviceChild2);
                                break;
                            }
                        }
                        deviceChildDao.insert(deviceChild);
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
                    success="success";
                    Log.i("WifiConectionAsync", "-->" + "onPostExecute");
//                    if (popupWindow!=null && popupWindow.isShowing()){
//                        popupWindow.dismiss();
//                    }
                    if (gifDrawable != null) {
                        gifDrawable.stop();
                    }
                    Utils.showToast(AddDeviceActivity.this, "创建成功");
                    Intent intent2 = new Intent(AddDeviceActivity.this, MainActivity.class);
                    intent2.putExtra("deviceList", "deviceList");
                    intent2.putExtra("deviceId", deviceId + "");
                    startActivity(intent2);
                    break;
                case -3005:
                    if (deviceChild != null) {
                        deviceChildDao.delete(deviceChild);
                    }
                    Utils.showToast(AddDeviceActivity.this, "创建失败");
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
        Log.i("AddDevice", "-->" + "onResume");

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
                deviceName=mac;
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
                }
            }
            return null;
        }
    }

    private ProgressDialog mProgressDialog;
    private PopupWindow popupWindow;

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
            if (gifDrawable != null && gifDrawable.isPlaying()) {
                gifDrawable.stop();
                if (add_image != null) {
                    add_image.setVisibility(View.GONE);
                }
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
            CountTimer countTimer = new CountTimer(30000, 1000);
            countTimer.start();
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
                    if (running) {
                        if (gifDrawable != null && gifDrawable.isPlaying()) {
                            gifDrawable.stop();
                            if (add_image != null) {
                                add_image.setVisibility(View.GONE);
                                et_ssid.setEnabled(true);
                                et_pswd.setEnabled(true);
                                btn_match.setEnabled(true);
                            }
                        }
                        Utils.showToast(AddDeviceActivity.this, "配置失败");
                    }
//                    mProgressDialog.setMessage("配置失败");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (gifDrawable != null && gifDrawable.isPlaying()) {
            gifDrawable.stop();
            add_image.setVisibility(View.GONE);
            et_ssid.setEnabled(true);
            et_pswd.setEnabled(true);
            btn_match.setEnabled(true);
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
            return;
        }
        Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
        intent.putExtra("deviceList", "deviceList");
        startActivity(intent);
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
        }
    };

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
                    add_image.setVisibility(View.GONE);
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
            if (TextUtils.isEmpty(success)){
                if (isBound) {
                    unbindService(connection);
                }

                if (receiver != null) {
                    unregisterReceiver(receiver);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        running = false;
    }
}