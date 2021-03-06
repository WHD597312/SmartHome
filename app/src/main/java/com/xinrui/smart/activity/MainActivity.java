package com.xinrui.smart.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.xinrui.database.dao.DeviceChildDao;
import com.xinrui.database.dao.TimeTaskDao;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.location.CheckPermissionsActivity;
import com.xinrui.secen.scene_fragment.LiveFragment;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.device.AboutAppActivity;
import com.xinrui.smart.activity.device.CommonProblemActivity;
import com.xinrui.smart.activity.device.CommonSetActivity;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.NoDeviceFragment;
import com.xinrui.smart.fragment.SmartFragment;
import com.xinrui.smart.fragment.SmartFragmentManager;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.Function;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.reveiver.MQTTMessageReveiver;
import com.xinrui.smart.util.DensityUtil;
import com.xinrui.smart.util.DensityUtils;
import com.xinrui.smart.util.GlideCacheUtil;
import com.xinrui.smart.util.GlideCircleTransform;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.ScreenUtil;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.util.mqtt.VibratorUtil;
import com.xinrui.smart.view_custom.AppUpdateDialog;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.DialogLoad;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_user)
    TextView tv_user;
    @BindView(R.id.image_user)
    ImageView image_user;
    /**
     * 用户
     */
    MyApplication application;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.tv_exit)
    TextView tv_exit;//退出
    @BindView(R.id.tv_device)
    TextView tv_device;//设备
    @BindView(R.id.tv_smart)
    TextView tv_smart;//智能
    @BindView(R.id.tv_live)
    TextView tv_live;//实景
    @BindView(R.id.listview)
    ListView listview;
    private FragmentManager fragmentManager;//碎片管理者
    private DeviceFragment deviceFragment;//设备碎片
    private NoDeviceFragment noDeviceFragment;
    private SmartFragmentManager smartFragmentManager;
    private LiveFragment liveFragment;
    @BindView(R.id.nav_view)
    RelativeLayout nav_view;
    @BindView(R.id.device_view)
    View device_view;//设备页
    @BindView(R.id.smart_view)
    View smart_view;//智能页
    @BindView(R.id.live_view)
    View live_view;//实景页
    private int[] colors = {R.color.color_blue, R.color.color_dark_gray};
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;
    private long exitTime = 0;
    android.support.v4.app.Fragment fragment = new android.support.v4.app.Fragment();
    public static boolean running = false;
    Unbinder unbinder;
    private ProgressDialog progressDialog;
    public static boolean isRunning = false;
    private int load = -1;
    MQTTMessageReveiver myReceiver;
    String fall;
    String login;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //设置左上角的图标响应
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (application == null) {
            application = (MyApplication) getApplication();
//            Intent service=new Intent(this,MQService.class);
//            startService(service);
        }
        application.addActivity(this);

        fragmentManager = getSupportFragmentManager();


        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("mqttmessage2");
        myReceiver = new MQTTMessageReveiver();
        this.registerReceiver(myReceiver, filter);
        Intent service = new Intent(MainActivity.this, MQService.class);
        isConnected = bindService(service, connection, Context.BIND_AUTO_CREATE);

        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);//修改DrawerLayout侧滑菜单图标
        //这样修改了图标，但是这个图标的点击事件会消失，点击图标不能打开侧边栏
        //所以还要加上如下代码
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
//        noDeviceFragment = new NoDeviceFragment();
//        deviceFragment = new DeviceFragment();
        smartFragmentManager = new SmartFragmentManager();
        liveFragment = new LiveFragment();
        function();
        progressDialog = new ProgressDialog(this);
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String phone = preferences.getString("phone", "");
        if (!Utils.isEmpty(username)) {
            tv_user.setText(username);
        } else if (!Utils.isEmpty(phone)) {
            tv_user.setText(phone);
        }

        try {

            String image = preferences.getString("image", "");
            if (!Utils.isEmpty(image)) {
                File file = new File(image);
                if (file.exists()) {
                    Glide.with(MainActivity.this).load(file).transform(new GlideCircleTransform(getApplicationContext())).into(image_user);
                } else {
                    String userId = preferences.getString("userId", "");
                    String url = "http://47.98.131.11:8082/warmer/v1.0/user/" + userId + "/headImg";
                    Glide.with(MainActivity.this).load(url).transform(new GlideCircleTransform(getApplicationContext())).error(R.mipmap.touxiang).into(image_user);
                }
            } else {
                String userId = preferences.getString("userId", "");
                String url = "http://47.98.131.11:8082/warmer/v1.0/user/" + userId + "/headImg";
                Glide.with(MainActivity.this).load(url).transform(new GlideCircleTransform(getApplicationContext())).error(R.mipmap.touxiang).into(image_user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        deviceGroupDao = new DeviceGroupDaoImpl(getApplicationContext());
        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());
        preferences.edit().putString("main", "1").commit();

        smart = getSharedPreferences("smart", Context.MODE_PRIVATE);
        deviceChildren = deviceChildDao.findAllDevice();

        deviceGroups = deviceGroupDao.findAllDevices();


        Intent intent = getIntent();
        String Activity_return = "";
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Activity_return = bundle.getString("Activity_return");
        }
        mainControl = intent.getStringExtra("mainControl");
        deviceList = intent.getStringExtra("deviceList");
        live = intent.getStringExtra("live");
        fall = intent.getStringExtra("fall");

        fragmentPreferences = getSharedPreferences("fragment", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();


        boolean isConn = NetWorkUtil.isConn(MyApplication.getContext());

        if (Utils.isEmpty(mainControl) && Utils.isEmpty(deviceList) && Utils.isEmpty(Activity_return) && Utils.isEmpty(live)) {
            login = intent.getStringExtra("login");
//            if (!TextUtils.isEmpty(login)){
//                if ("cancel".equals(MyApplication.update)){
//                    try {
//                        PackageManager packageManager=application.getPackageManager();
//                        try {
//                            PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
//                            versionName=packageInfo.versionName;
//                            versionCode=packageInfo.versionCode;
//                            System.out.println("pakageInfo:"+versionName+","+versionCode);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                        new UpdateAppAsync().execute().get(3,TimeUnit.SECONDS);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
            if (!Utils.isEmpty(fall)) {
                falling = true;
            }
            fragmentPreferences.edit().putString("fragment", "1").commit();
            Log.i("login", "-->" + login);
            if (isConn && Utils.isEmpty(login) && Utils.isEmpty(fall)) {
                Log.i("NetWorkUtil", "-->" + "NetWorkUtil");
                try {
                    PackageManager packageManager=application.getPackageManager();
                    try {
                        PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
                        versionName=packageInfo.versionName;
                        versionCode=packageInfo.versionCode;
                        System.out.println("pakageInfo:"+versionName+","+versionCode);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
//                    if ("cancel".equals(MyApplication.update)){
//                        new UpdateAppAsync().execute().get(3,TimeUnit.SECONDS);
//                    }
//                    new LoadDeviceAsync().execute().get(5, TimeUnit.SECONDS);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else if (!Utils.isEmpty(deviceList) && Utils.isEmpty(Activity_return) && Utils.isEmpty(live)) {
            fragmentPreferences.edit().putString("fragment", "1").commit();
            deviceId = intent.getStringExtra("deviceId");
//            if (!Utils.isEmpty(deviceId)){

//            }
//            preferences.edit().putString("deviceList","deviceList").commit();

        } else if (!Utils.isEmpty(mainControl) && Utils.isEmpty(Activity_return) && Utils.isEmpty(live)) {
            fragmentPreferences.edit().putString("fragment", "2").commit();
        } else if (!Utils.isEmpty(Activity_return) || !Utils.isEmpty(live)) {
            fragmentPreferences.edit().putString("fragment", "3").commit();
        }
    }

//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 0:
//                    break;
//            }
//        }
//    };


    public void goLiveFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LiveFragment liveFragment = new LiveFragment();
        fragmentTransaction.replace(R.id.layout_body, liveFragment);
        fragmentTransaction.commit();
    }

    SharedPreferences preferences;
    SharedPreferences fragmentPreferences;
    SharedPreferences smart;
    FragmentTransaction fragmentTransaction;
    public static int LOGIN = 1;

    List<DeviceChild> deviceChildren;
    List<Fragment> fragments;
    List<DeviceGroup> deviceGroups;
    String deviceId;

    private boolean isConnected = false;
    String deviceList;
    String mainControl;

    private Uri outputUri;//裁剪完照片保存地址
    String live;
    public static boolean falling = false;

    @Override
    protected void onStart() {

        super.onStart();
        DensityUtils.setOrientation(this, ScreenUtil.WIDTH);
        String fragmentS = fragmentPreferences.getString("fragment", "");
        if ("1".equals(fragmentS) && result2==0) {
            deviceGroups = deviceGroupDao.findAllDevices();
            deviceChildren = deviceChildDao.findAllDevice();
            fragmentTransaction = fragmentManager.beginTransaction();//开启碎片事务
            if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {
                fragmentTransaction.replace(R.id.layout_body, new NoDeviceFragment()).commit();
            } else {
                deviceFragment = new DeviceFragment();
                Bundle bundle = new Bundle();
                deviceFragment.setArguments(bundle);
                preferences.edit().putString("main", "1").commit();

                if (!Utils.isEmpty(deviceId)) {
//                    bundle.putString("load","load");
                    bundle.putString("deviceId", deviceId);
                } else {
                    bundle.putString("load", "load");
                    bundle.putString("deviceId", "");
                }
                fragmentTransaction.replace(R.id.layout_body, deviceFragment).commit();
            }
            device_view.setVisibility(View.VISIBLE);
            smart_view.setVisibility(View.GONE);
            live_view.setVisibility(View.GONE);
            smart.edit().clear().commit();
            clicked = 1;
            clicked2 = 0;
            clicked3 = 0;
        } else if ("2".equals(fragmentS) && result2==0) {
            smartFragmentManager=new SmartFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_body, smartFragmentManager).commit();
            device_view.setVisibility(View.GONE);
            smart_view.setVisibility(View.VISIBLE);
            live_view.setVisibility(View.GONE);
            clicked = 0;
            clicked2 = 1;
            clicked3 = 0;
        }
//        else if ("3".equals(fragmentS)) {
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.layout_body, liveFragment).commit();
//
//            fragmentPreferences.edit().putString("fragment", "3").commit();
//            device_view.setVisibility(View.GONE);
//            smart_view.setVisibility(View.GONE);
//            live_view.setVisibility(View.VISIBLE);
//            smart.edit().clear();
//            clicked = 0;
//            clicked2 = 0;
//            clicked3 = 1;
//        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isEmpty(deviceList) && Utils.isEmpty(mainControl)) {
            running = true;
        } else {
            running = false;
        }

//        Intent service = new Intent(MainActivity.this, MQService.class);
//        isConnected = bindService(service, connection, Context.BIND_AUTO_CREATE);
    }

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
    MQService mqService;
    private boolean bound = false;



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * 设置功能菜单
     */
    private void function() {
        int[] imgs = {R.mipmap.leftbar_main, R.mipmap.leftbar_problum, R.mipmap.leftbar_commen, R.mipmap.leftbar_about};
        String[] strings = {"主页", "常见问题", "通用设置", "关于应用"};
        List<Function> functions = new ArrayList<>();
        for (int i = 0; i < imgs.length; i++) {
            Function function = new Function(imgs[i], strings[i]);
            functions.add(function);
        }
        FunctionAdapter adapter = new FunctionAdapter(this, functions);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
//                        Intent main = new Intent(MainActivity.this, MainActivity.class);
//                        main.putExtra("deviceList", "deviceList");
//                        startActivity(main);
                        drawer.closeDrawer(GravityCompat.START);
                        String fragmentS = fragmentPreferences.getString("fragment", "");
                        if ("1".equals(fragmentS)){
                            Log.i("fragmentS","-->"+fragmentS);
                        }else {
                            Log.i("fragmentS","-->"+fragmentS);
                            deviceGroups = deviceGroupDao.findAllDevices();
                            deviceChildren = deviceChildDao.findAllDevice();
                            fragmentTransaction = fragmentManager.beginTransaction();
                            if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {
                                noDeviceFragment=new NoDeviceFragment();
                                fragmentTransaction.replace(R.id.layout_body, noDeviceFragment).commit();
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString("load", "");
                                deviceFragment=new DeviceFragment();
                                deviceFragment.setArguments(bundle);
                                fragmentTransaction.replace(R.id.layout_body, deviceFragment).commit();
                            }
                            device_view.setVisibility(View.VISIBLE);
                            smart_view.setVisibility(View.GONE);
                            live_view.setVisibility(View.GONE);
                            clicked = 1;
                            clicked2 = 0;
                            smart.edit().clear().commit();
                            clicked3 = 0;
                            fragmentPreferences.edit().putString("fragment", "1").commit();
                        }
                        break;
                    case 1:
                        drawer.closeDrawer(GravityCompat.START);
                        Intent common = new Intent(MainActivity.this, CommonProblemActivity.class);
                        common.putExtra("main", "main");
                        if (DeviceFragment.running || NoDeviceFragment.running) {
                            common.putExtra("device", "device");
                        } else if (SmartFragmentManager.running) {
                            common.putExtra("smart", "smart");
                        } else if (LiveFragment.running) {
                            common.putExtra("live", "live");
                        }
                        startActivityForResult(common,6000);
                        break;
                    case 2:
                        drawer.closeDrawer(GravityCompat.START);
                        Intent common2 = new Intent(MainActivity.this, CommonSetActivity.class);
                        if (DeviceFragment.running || NoDeviceFragment.running) {
                            common2.putExtra("device", "device");
                        } else if (SmartFragmentManager.running) {
                            common2.putExtra("smart", "smart");
                        } else if (LiveFragment.running) {
                            common2.putExtra("live", "live");
                        }
                        common2.putExtra("main", "main");
                        startActivityForResult(common2,6000);
                        break;
                    case 3:
                        drawer.closeDrawer(GravityCompat.START);
                        Intent common3 = new Intent(MainActivity.this, AboutAppActivity.class);
                        if (DeviceFragment.running || NoDeviceFragment.running) {
                            common3.putExtra("device", "device");
                        } else if (SmartFragmentManager.running) {
                            common3.putExtra("smart", "smart");
                        } else if (LiveFragment.running) {
                            common3.putExtra("live", "live");
                        }
                        common3.putExtra("main", "main");
                        startActivityForResult(common3,6000);
                        break;
                }
            }
        });
    }

    int clicked = 0;
    int clicked2 = 0;
    int clicked3 = 3;

    @OnClick({R.id.tv_exit, R.id.tv_device, R.id.tv_smart, R.id.tv_live, R.id.layout_user})
    public void onClick(View view) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (view.getId()) {
            case R.id.layout_user:
                drawer.closeDrawer(GravityCompat.START);
                Intent persion = new Intent(this, PersonInfoActivity.class);
//                Intent common = new Intent(MainActivity.this, CommonProblemActivity.class);
                persion.putExtra("main", "main");
                if (DeviceFragment.running || NoDeviceFragment.running) {
                    persion.putExtra("device", "device");
                } else if (SmartFragmentManager.running) {
                    persion.putExtra("smart", "smart");
                } else if (LiveFragment.running) {
                    persion.putExtra("live", "live");
                }
                startActivityForResult(persion,6000);
                break;
            case R.id.tv_exit:
                if (NoFastClickUtils.isFastClick()) {
                    smart.edit().clear().commit();
//                preferences.edit().clear().commit();/**清空当前用户的所有数据*/
                    if (preferences.contains("password")) {
                        preferences.edit().remove("password").commit();
                        preferences.edit().remove("login").commit();
                        if (preferences.contains("username")) {
                            preferences.edit().remove("username").commit();
                        }
                        fragmentPreferences.edit().clear().commit();
                        smart.edit().clear().commit();
                    }
                    preferences.edit().remove("password").commit();
                    if (mqService != null) {
                        mqService.cancelAllsubscibe();
                        mqService.clearAllOfflineDevice();
                        mqService.setOffineDevices();
                    }
                    deviceChildDao.deleteAll();
                    deviceGroupDao.deleteAll();
                    GlideCacheUtil glideCacheUtil = new GlideCacheUtil(MainActivity.this);
                    glideCacheUtil.clearImageAllCache();
                    glideCacheUtil.clearImageDiskCache();
                    glideCacheUtil.clearImageMemoryCache();
                    fragmentPreferences.edit().clear().commit();
                    application.removeAllFragment();
                    running = false;

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

                break;
            case R.id.tv_device:
                back = 0;
                if (clicked == 1) {
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
                    isRunning = false;
                    deviceGroups = deviceGroupDao.findAllDevices();
                    deviceChildren = deviceChildDao.findAllDevice();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {
                        fragmentTransaction.replace(R.id.layout_body, noDeviceFragment).commit();
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("load", "");
                        deviceFragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.layout_body, deviceFragment).commit();
                    }
                    device_view.setVisibility(View.VISIBLE);
                    smart_view.setVisibility(View.GONE);
                    live_view.setVisibility(View.GONE);
                    clicked = 1;
                    clicked2 = 0;
                    smart.edit().clear().commit();
                    clicked3 = 0;
                    fragmentPreferences.edit().putString("fragment", "1").commit();
                } else {
                    break;
                }
                break;
            case R.id.tv_smart:
                back = 0;
                if (clicked2 == 1) {
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
                    result=0;
                    isRunning = false;
                    fragmentTransaction = fragmentManager.beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", 0);
                    smartFragmentManager.setArguments(bundle);
                    fragmentTransaction.replace(R.id.layout_body, smartFragmentManager).commit();
                    device_view.setVisibility(View.GONE);
                    smart_view.setVisibility(View.VISIBLE);
                    live_view.setVisibility(View.GONE);
                    clicked = 0;
                    clicked2 = 1;
                    clicked3 = 0;
                    smart.edit().clear().commit();
                    fragmentPreferences.edit().putString("fragment", "2").commit();
                } else {
                    break;
                }
                break;
            case R.id.tv_live:
                back = 0;
                if (clicked3 == 1) {
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
                    LiveFragment liveFragment = new LiveFragment();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.layout_body, liveFragment);
                    transaction.commit();
                    device_view.setVisibility(View.GONE);
                    smart_view.setVisibility(View.GONE);
                    live_view.setVisibility(View.VISIBLE);
                    smart.edit().clear();
                    clicked = 0;
                    clicked2 = 0;
                    clicked3 = 1;
                    fragmentPreferences.edit().putString("fragment", "3").commit();
                } else {
                    break;
                }
                break;
        }
    }

    int back = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        falling = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出Rango",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return false;
            }
            if (mqService!=null){
                mqService.setOffineDevices();
                mqService.cancelNotition();
            }
            if (preferences.contains("login"))
                preferences.edit().remove("login").commit();
            if (preferences.contains("deviceList")) {
                preferences.edit().remove("deviceList").commit();
            }
            VibratorUtil.StopVibrate(this);
            application.removeAllActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    int result = 0;
    int result2=0;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        result2=1;
        if (resultCode == 6000) {
            deviceGroups = deviceGroupDao.findAllDevices();
            deviceChildren = deviceChildDao.findAllDevice();
            fragmentTransaction = fragmentManager.beginTransaction();
            noDeviceFragment = new NoDeviceFragment();
            if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {
                fragmentTransaction.replace(R.id.layout_body, noDeviceFragment).commit();
            } else {
                deviceFragment = new DeviceFragment();
                Bundle bundle = new Bundle();
                bundle.putString("load", "");
                deviceId = data.getStringExtra("deviceId");
                if (!Utils.isEmpty(deviceId)) {
                    bundle.putString("deviceId", deviceId);
                }
                String macAddress=data.getStringExtra("macAddress");
                if (!Utils.isEmpty(macAddress)){
                    bundle.putString("macAddress",macAddress);
                }
                deviceFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.layout_body, deviceFragment).commit();
            }
            device_view.setVisibility(View.VISIBLE);
            smart_view.setVisibility(View.GONE);
            live_view.setVisibility(View.GONE);
            smart.edit().clear().commit();
            clicked = 1;
            clicked2 = 0;
            clicked3 = 0;
        } else if (resultCode == 7000) {
            fragmentTransaction = fragmentManager.beginTransaction();
            smartFragmentManager = new SmartFragmentManager();
            fragmentTransaction.replace(R.id.layout_body, smartFragmentManager).commit();
            device_view.setVisibility(View.GONE);
            smart_view.setVisibility(View.VISIBLE);
            live_view.setVisibility(View.GONE);
            clicked = 0;
            clicked2 = 1;
            clicked3 = 0;
            fragmentPreferences.edit().putString("fragment", "2").commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preferences.contains("deviceList")) {
            preferences.edit().remove("deviceList").commit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().remove("login").commit();
        if (preferences.contains("deviceList")) {
            preferences.edit().remove("deviceList").commit();
        }
        fall = "";
        result2=0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (unbinder != null) {
            unbinder.unbind();
        }
        VibratorUtil.StopVibrate(this);
        falling = false;
        running = false;
//        deviceChildDao.closeDaoSession();
//        deviceGroupDao.closeDaoSession();

        progressDialog.dismiss();
        deviceChildren.clear();
        deviceGroups.clear();

        if (isConnected && connection != null) {
            unbindService(connection);
        }
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }

    }

    public void querryQllDevice(List<DeviceChild> deviceChildren) {

    }

    long shareHouseId = 0;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};


    class LoadDeviceAsync extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("正在加载数据...");
            progressDialog.setCancelable(false);
            progressDialog.show();
//            CountTimer2 countTimer = new CountTimer2(5000, 1000);
//            countTimer.start();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int code = 0;
            try {
                String userId = preferences.getString("userId", "");
                String allDeviceUrl = "http://47.98.131.11:8082/warmer/v1.0/device/findAll?userId=" + URLEncoder.encode(userId, "utf-8");
//                String[] params={"findUserAllDevice",userId};
                String result = HttpUtils.requestGet(allDeviceUrl);
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    JSONObject content = jsonObject.getJSONObject("content");
                    if (code == 2000) {
                        deviceGroupDao.deleteAll();
                        deviceChildDao.deleteAll();
                        JSONArray houses = content.getJSONArray("houses");
                        for (int i = 0; i < houses.length(); i++) {
                            JSONObject house = houses.getJSONObject(i);
                            if (house != null) {
                                long houseId = house.getLong("id");
                                String houseName = house.getString("houseName");
                                String location = house.getString("location");
                                int masterControllerDeviceId = house.getInt("masterControllerDeviceId");
                                int externalSensorsId = house.getInt("externalSensorsId");
                                String layers = house.getString("layers");
                                DeviceGroup deviceGroup = new DeviceGroup(houseId, houseName + "." + location, houseName, location, masterControllerDeviceId, externalSensorsId, layers);
//                                deviceGroup.setGroupPosition(i);

                                if (deviceGroupDao.findById(houseId) != null) {
                                    deviceGroupDao.update(deviceGroup);
                                } else {
                                    deviceGroupDao.insert(deviceGroup);
                                }
                                JSONArray deviceList = house.getJSONArray("deviceList");
                                for (int j = 0; j < deviceList.length(); j++) {
                                    JSONObject device = deviceList.getJSONObject(j);

                                    if (device != null) {
                                        long deviceId = device.getLong("id");
                                        String deviceName = device.getString("deviceName");
                                        int type = device.getInt("type");
                                        long groupId = device.getLong("houseId");

                                        int masterControllerUserId = device.getInt("masterControllerUserId");
                                        int isUnlock = device.getInt("isUnlock");
                                        int version = device.getInt("version");
                                        String macAddress = device.getString("macAddress");
                                        int controlled = device.getInt("controlled");

                                        DeviceChild child = deviceChildDao.findDeviceChild(deviceId);
                                        if (child != null) {
                                            child.setType(type);
                                            child.setDeviceName(deviceName);
                                            child.setHouseId((long) groupId);
                                            child.setMasterControllerUserId(masterControllerUserId);
                                            child.setIsUnlock(isUnlock);
                                            child.setVersion(version);
                                            child.setMacAddress(macAddress);
                                            child.setControlled(controlled);
                                            child.setHouseAddress(location);
//                                            child.setGroupPosition(i);
//                                            child.setChildPosition(j);
                                            deviceChildDao.update(child);
                                        } else {
                                            DeviceChild deviceChild = new DeviceChild(deviceId, groupId, deviceName, macAddress, type);
//                                            DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, (long) groupId, masterControllerUserId, type, isUnlock);
                                            deviceChild.setVersion(version);
                                            deviceChild.setMacAddress(macAddress);
                                            deviceChild.setControlled(controlled);
//                                            deviceChild.setGroupPosition(i);
//                                            deviceChild.setChildPosition(j);
                                            deviceChild.setHouseAddress(location);
                                            deviceChildDao.insert(deviceChild);
                                        }
                                    }
                                }
                            }
                        }

                        JSONObject sharedDevice = content.getJSONObject("sharedDevice");
                        JSONArray deviceList = sharedDevice.getJSONArray("deviceList");
                        shareHouseId = Long.MAX_VALUE;
                        DeviceGroup deviceGroup = deviceGroupDao.findById(shareHouseId);
                        List<DeviceGroup> deviceGroups = deviceGroupDao.findAllDevices();
                        if (deviceGroup == null) {
                            deviceGroup = new DeviceGroup();
                            deviceGroup.setId(shareHouseId);
                            deviceGroup.setHeader("分享的设备");
                            deviceGroup.setGroupPosition(deviceGroups.size());
                            deviceGroupDao.insert(deviceGroup);
                        } else {
                            deviceGroupDao.update(deviceGroup);
                        }

                        for (int x = 0; x < deviceList.length(); x++) {
                            JSONObject device = deviceList.getJSONObject(x);
                            if (device != null) {
                                long deviceId = device.getLong("id");
                                String deviceName = device.getString("deviceName");
                                int type = device.getInt("type");
                                long groupId = shareHouseId;
                                long houseId = device.getLong("houseId");
                                int masterControllerUserId = device.getInt("masterControllerUserId");
                                int isUnlock = device.getInt("isUnlock");
                                int version = device.getInt("version");
                                String macAddress = device.getString("macAddress");
                                int controlled = device.getInt("controlled");
//                                DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, groupId, masterControllerUserId, type, isUnlock);
                                DeviceChild deviceChild = new DeviceChild(deviceId, groupId, deviceName, macAddress, type);
                                deviceChild.setControlled(controlled);
//                                deviceChild.setHouseId();
                                deviceChild.setShareHouseId(houseId);
//                                deviceChild.setGroupPosition(deviceGroups.size());
//                                deviceChild.setChildPosition(x);
                                deviceChildDao.insert(deviceChild);
                            }
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
            progressDialog.dismiss();
            try {
                switch (code) {
                    case -3004:
                        Utils.showToast(MainActivity.this, "查询失败");
                        break;
                    case 2000:
                        List<DeviceGroup> deviceGroups = deviceGroupDao.findAllDevices();
                        List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();//开启碎片事务

                        if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {
                            fragmentTransaction.replace(R.id.layout_body, new NoDeviceFragment()).commit();
                            device_view.setVisibility(View.VISIBLE);
                            smart_view.setVisibility(View.GONE);
                            live_view.setVisibility(View.GONE);
                            smart.edit().clear().commit();
                        } else {
                            deviceFragment = new DeviceFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("load", "load");
                            deviceFragment.setArguments(bundle);
                            fragmentTransaction.replace(R.id.layout_body, deviceFragment).commit();
                            device_view.setVisibility(View.VISIBLE);
                            smart_view.setVisibility(View.GONE);
                            live_view.setVisibility(View.GONE);
                            smart.edit().clear().commit();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class CountTimer2 extends CountDownTimer{

        public CountTimer2(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Tag", "倒计时=" + (millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            if (progressDialog!=null){
                progressDialog.dismiss();
            }
        }
    }
    String versionName;
    int versionCode;
    String updateAppUrl="https://www.pgyer.com/Bxi6";
    String appUrl="http://47.98.131.11:8082/warmer/v1.0/user/getAppVersion";
    class UpdateAppAsync extends AsyncTask<Void,Void,Integer>{
        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            try {
                String result=HttpUtils.requestGet(appUrl);
                if (!TextUtils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    int resultCode=jsonObject.getInt("code");
                    if (resultCode==2000){
                        JSONArray content=jsonObject.getJSONArray("content");
                        JSONObject appObject=content.getJSONObject(0);
                        if (appObject!=null){
                            int avCode=appObject.getInt("avCode");
                            String avName=appObject.getString("avName");
                            if (avCode==versionCode && versionName.equals(avName)){
                                code=-2000;
                            }else {
                                code=2000;
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code==2000){
                dialog = new AppUpdateDialog(MainActivity.this);
                dialog.setOnNegativeClickListener(new AppUpdateDialog.OnNegativeClickListener() {
                    @Override
                    public void onNegativeClick() {
                        MyApplication.update="refudsed";
                        dialog.dismiss();
                    }
                });
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnPositiveClickListener(new AppUpdateDialog.OnPositiveClickListener() {
                    @Override
                    public void onPositiveClick() {
                        MyApplication.update="update";
                        Intent intent = new Intent();
                        intent.setData(Uri.parse(updateAppUrl));//Url 就是你要打开的网址
                        intent.setAction(Intent.ACTION_VIEW);
                        MainActivity.this.startActivity(intent); //启动浏览器
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }
    AppUpdateDialog dialog;
}
