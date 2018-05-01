package com.xinrui.smart.activity;

import android.Manifest;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.location.CheckPermissionsActivity;
import com.xinrui.secen.scene_fragment.LiveFragment;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.NoDeviceFragment;
import com.xinrui.smart.fragment.SmartFragment;
import com.xinrui.smart.fragment.SmartFragmentManager;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.Function;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_user)
    TextView tv_user;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //设置左上角的图标响应
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
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
        function();
    }

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

    @Override
    protected void onStart() {
        super.onStart();


        deviceGroupDao = new DeviceGroupDaoImpl(this);
        deviceChildDao = new DeviceChildDaoImpl(this);
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);




        String phone = preferences.getString("phone", "");
        if (!Utils.isEmpty(phone)) {
            tv_user.setText(phone);
        }
        smart = getSharedPreferences("smart", Context.MODE_PRIVATE);
        deviceChildren = deviceChildDao.findAllDevice();

        deviceGroups = deviceGroupDao.findAllDevices();
        fragmentManager = getSupportFragmentManager();


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String mainControl = intent.getStringExtra("mainControl");
        String deviceList = intent.getStringExtra("deviceList");

        fragmentPreferences = getSharedPreferences("fragment", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();


        if (Utils.isEmpty(mainControl) && Utils.isEmpty(deviceList)) {
            fragmentPreferences.edit().putString("fragment", "1").commit();
            if (!preferences.contains("login")){
                preferences.edit().putString("login", "login").commit();
                new LoadDeviceAsync().execute();
            }

        } else if (!Utils.isEmpty(deviceList)) {
            fragmentPreferences.edit().putString("fragment", "1").commit();
        } else if (!Utils.isEmpty(mainControl)) {
            fragmentPreferences.edit().putString("fragment", "2").commit();
        }
        if (bundle != null) {
            String Activity_return = bundle.getString("Activity_return");
            String return_homepage = bundle.getString("return_homepage");
            if (!Utils.isEmpty(Activity_return)) {
                fragmentPreferences.edit().putString("fragment", "3").commit();
            } else if (!Utils.isEmpty(return_homepage)) {
                fragmentPreferences.edit().putString("fragment", "1").commit();
            }
        }


        String fragmentS = fragmentPreferences.getString("fragment", "");
        if ("1".equals(fragmentS)) {
            deviceGroups = deviceGroupDao.findAllDevices();
            fragmentTransaction = fragmentManager.beginTransaction();//开启碎片事务
            if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {
                fragmentTransaction.replace(R.id.layout_body,  new NoDeviceFragment()).commit();

            } else {

                fragmentTransaction.replace(R.id.layout_body,  new DeviceFragment()).commit();

            }
            device_view.setVisibility(View.VISIBLE);
            smart_view.setVisibility(View.GONE);
            live_view.setVisibility(View.GONE);
            smart.edit().clear().commit();
        } else if ("2".equals(fragmentS)) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_body, new SmartFragmentManager()).commit();

            device_view.setVisibility(View.GONE);
            smart_view.setVisibility(View.VISIBLE);
            live_view.setVisibility(View.GONE);
        } else if ("3".equals(fragmentS)) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_body, new LiveFragment()).commit();
            fragment = smartFragmentManager;

            fragmentPreferences.edit().putString("fragment", "3").commit();
            device_view.setVisibility(View.GONE);
            smart_view.setVisibility(View.GONE);
            live_view.setVisibility(View.VISIBLE);
            smart.edit().clear();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onResume() {
        super.onResume();
//        showPopwindow();
        Intent intent=new Intent(this,MQService.class);
        bindService(intent,connection,Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection!=null){
            unbindService(connection);
        }
    }

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
    }

    android.support.v4.app.Fragment fragment = new android.support.v4.app.Fragment();

    @OnClick({R.id.tv_exit, R.id.tv_device, R.id.tv_smart, R.id.tv_live})
    public void onClick(View view) {

        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (view.getId()) {
            case R.id.tv_exit:
                smart.edit().clear().commit();
//                preferences.edit().clear().commit();/**清空当前用户的所有数据*/
                if (preferences.contains("password")) {
                    preferences.edit().remove("password").commit();
                    fragmentPreferences.edit().clear().commit();
                    smart.edit().clear().commit();
                }

                fragmentPreferences.edit().clear().commit();
                deviceGroupDao.deleteAll();
                deviceChildDao.deleteAll();
                application.removeAllFragment();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);


                break;
            case R.id.tv_device:
                fragmentTransaction = fragmentManager.beginTransaction();//开启碎片事务
                if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {

                    if (fragment instanceof NoDeviceFragment) {
                        break;
                    }
                    noDeviceFragment = new NoDeviceFragment();
                    fragmentTransaction.replace(R.id.layout_body, noDeviceFragment).commit();
                    fragment = noDeviceFragment;

                } else {

                    if (fragment instanceof DeviceFragment) {
                        break;
                    }
                    deviceFragment = new DeviceFragment();
                    fragmentTransaction.replace(R.id.layout_body, deviceFragment).commit();
                    fragment = deviceFragment;

                }


                device_view.setVisibility(View.VISIBLE);
                smart_view.setVisibility(View.GONE);
                live_view.setVisibility(View.GONE);
                smart.edit().clear().commit();
                break;
            case R.id.tv_smart:

                fragmentTransaction = fragmentManager.beginTransaction();

                if (fragment instanceof SmartFragmentManager) {
                    break;
                }
                smartFragmentManager = new SmartFragmentManager();
                fragmentTransaction.replace(R.id.layout_body, smartFragmentManager).commit();
                fragment = smartFragmentManager;

                device_view.setVisibility(View.GONE);
                smart_view.setVisibility(View.VISIBLE);
                live_view.setVisibility(View.GONE);
                smart.edit().clear().commit();
                break;
            case R.id.tv_live:
                if (fragment instanceof LiveFragment) {
                    fragmentPreferences.edit().putString("fragment", "3").commit();
                    break;
                } else {
                    fragment = new LiveFragment();
                }
                fragmentPreferences.edit().putString("fragment", "3").commit();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.layout_body, new LiveFragment());
                transaction.commit();
                device_view.setVisibility(View.GONE);
                smart_view.setVisibility(View.GONE);
                live_view.setVisibility(View.VISIBLE);
                smart.edit().clear();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            application.removeAllActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
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
            if (bound) {
                try {
                    List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();

                    for (DeviceChild deviceChild : deviceChildren) {
                        String macAddress = deviceChild.getMacAddress();
                        String topicName = "rango/" + macAddress + "/set";
                        JSONObject object = new JSONObject();
                        object.put("loadDate", "on");
                        String s = object.toString();
                        mqService.publish(topicName, 2, s);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
    long shareHouseId = 0;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};

    class LoadDeviceAsync extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            int code = 0;
            try {
                String userId = preferences.getString("userId", "");
                String allDeviceUrl = "http://120.77.36.206:8082/warmer/v1.0/device/findAll?userId=" + URLEncoder.encode(userId, "utf-8");
                String result = HttpUtils.getOkHpptRequest(allDeviceUrl);
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    JSONObject content = jsonObject.getJSONObject("content");
                    if (code == 2000) {
                        JSONArray houses = content.getJSONArray("houses");
                        for (int i = 0; i < houses.length(); i++) {
                            JSONObject house = houses.getJSONObject(i);

                            if (house != null) {
                                int houseId = house.getInt("id");
                                if (i == houses.length() - 1) {
                                    shareHouseId = houseId + 1;
                                }
                                String houseName = house.getString("houseName");
                                String location = house.getString("location");
                                int masterControllerDeviceId = house.getInt("masterControllerDeviceId");
                                int externalSensorsId = house.getInt("externalSensorsId");
                                String layers = house.getString("layers");
                                DeviceGroup deviceGroup = new DeviceGroup((long) houseId, houseName + "." + location, houseName, location, masterControllerDeviceId, externalSensorsId, layers);
                                if (deviceGroupDao.findById((long) houseId) != null) {
                                    deviceGroupDao.update(deviceGroup);
                                } else {
                                    deviceGroupDao.insert(deviceGroup);
                                }
                                JSONArray deviceList = house.getJSONArray("deviceList");
                                for (int j = 0; j < deviceList.length(); j++) {
                                    JSONObject device = deviceList.getJSONObject(j);

                                    if (device != null) {
                                        int deviceId = device.getInt("id");
                                        String deviceName = device.getString("deviceName");
                                        int type = device.getInt("type");
                                        int groupId = device.getInt("houseId");

                                        int masterControllerUserId = device.getInt("masterControllerUserId");
                                        int isUnlock = device.getInt("isUnlock");
                                        int version = device.getInt("version");
                                        String macAddress = device.getString("macAddress");
                                        int controlled = device.getInt("controlled");

                                        DeviceChild child = deviceChildDao.findDeviceChild((long) deviceId);
                                        if (child != null) {
                                            child.setType(type);
                                            child.setDeviceName(deviceName);
                                            child.setHouseId((long) groupId);
                                            child.setMasterControllerUserId(masterControllerUserId);
                                            child.setIsUnlock(isUnlock);
                                            child.setVersion(version);
                                            child.setMacAddress(macAddress);
                                            child.setControlled(controlled);
                                            deviceChildDao.update(child);
                                        } else {
                                            DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, (long) groupId, masterControllerUserId, type, isUnlock);
                                            deviceChild.setVersion(version);
                                            deviceChild.setMacAddress(macAddress);
                                            deviceChild.setControlled(controlled);
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
                        if (deviceGroup == null) {
                            deviceGroup = new DeviceGroup();
                            deviceGroup.setId(shareHouseId);
                            deviceGroup.setHeader("分享的设备");
                            deviceGroupDao.insert(deviceGroup);
                        } else {
                            deviceGroupDao.update(deviceGroup);
                        }

                        for (int x = 0; x < deviceList.length(); x++) {
                            JSONObject device = deviceList.getJSONObject(x);
                            if (device != null) {
                                int deviceId = device.getInt("id");
                                String deviceName = device.getString("deviceName");
                                int type = device.getInt("type");
                                long groupId = shareHouseId;
                                int masterControllerUserId = device.getInt("masterControllerUserId");
                                int isUnlock = device.getInt("isUnlock");
                                int version = device.getInt("version");
                                String macAddress = device.getString("macAddress");
                                int controlled = device.getInt("controlled");
                                DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, groupId, type, masterControllerUserId, isUnlock);
                                deviceChild.setVersion(version);
                                deviceChild.setMacAddress(macAddress);
                                deviceChild.setControlled(controlled);
                                DeviceChild deviceChild2 = deviceChildDao.findDeviceChild((long) deviceId);
                                if (deviceChild2 == null) {
                                    deviceChildDao.insert(deviceChild);
                                } else {
                                    deviceChildDao.update(deviceChild);
                                }
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
            switch (code) {
                case -3004:
                    Utils.showToast(MainActivity.this, "查询失败");
                    break;
                case 2000:
                    List<DeviceGroup> deviceGroups = deviceGroupDao.findAllDevices();
                    fragmentTransaction = fragmentManager.beginTransaction();//开启碎片事务


                    if (deviceGroups.size() == 2 && deviceChildren.size() == 0) {
                        fragmentTransaction.replace(R.id.layout_body, new NoDeviceFragment()).commit();

                    } else {
                        fragmentTransaction.replace(R.id.layout_body, new DeviceFragment()).commit();
                    }
                    break;
            }
        }
    }
}
