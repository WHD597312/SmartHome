package com.xinrui.smart.activity;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.location.CheckPermissionsActivity;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.LiveFragment;
import com.xinrui.smart.fragment.NoDeviceFragment;
import com.xinrui.smart.fragment.SmartFragmentManager;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.Function;
import com.xinrui.smart.util.Utils;

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

public class MainActivity extends CheckPermissionsActivity {

    MyApplication application;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.tv_exit) TextView tv_exit;//退出
    @BindView(R.id.tv_device) TextView tv_device;//设备
    @BindView(R.id.tv_smart) TextView tv_smart;//智能
    @BindView(R.id.tv_live) TextView tv_live;//实景
    @BindView(R.id.listview) ListView listview;
    private FragmentManager fragmentManager;//碎片管理者
    private DeviceFragment deviceFragment;//设备碎片
    @BindView(R.id.device_view) View device_view;//设备页
    @BindView(R.id.smart_view) View smart_view;//智能页
    @BindView(R.id.live_view) View live_view;//实景页
    private int[] colors={R.color.color_blue,R.color.color_dark_gray};
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //设置左上角的图标响应
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (application==null){
            application= (MyApplication) getApplication();
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

    public void goLiveFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LiveFragment liveFragment = new LiveFragment();
        fragmentTransaction.replace(R.id.layout_body, liveFragment);
        fragmentTransaction.commit();
    }

    SharedPreferences preferences;
    public static int LOGIN=1;
    @Override
    protected void onStart() {
        super.onStart();


        deviceGroupDao=new DeviceGroupDaoImpl(this);
        deviceChildDao=new DeviceChildDaoImpl(this);
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        if (deviceChildDao.findAllDevice().isEmpty()){
            new LoadDeviceAsync().execute();
        }
        List<DeviceGroup> deviceGroups=deviceGroupDao.findAllDevices();
        fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();//开启碎片事务
        Intent intent=getIntent();
        String mainControl=intent.getStringExtra("mainControl");
        DeviceChildDaoImpl deviceChildDao=new DeviceChildDaoImpl(this);
        List<DeviceChild> deviceChildren=deviceChildDao.findAllDevice();
        if (Utils.isEmpty(mainControl)){
            if (deviceGroups.size()==1 && deviceChildDao.findAllDevice().size()==0){
                fragmentTransaction.replace(R.id.layout_body,new NoDeviceFragment()).commit();
            }else {
                deviceFragment=new DeviceFragment();
                fragmentTransaction.replace(R.id.layout_body,deviceFragment);
                fragmentTransaction.commit();
            }
            device_view.setVisibility(View.VISIBLE);
            smart_view.setVisibility(View.GONE);
            live_view.setVisibility(View.GONE);
        }else {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_body,new SmartFragmentManager());
            fragmentTransaction.commit();
            device_view.setVisibility(View.GONE);
            smart_view.setVisibility(View.VISIBLE);
            live_view.setVisibility(View.GONE);
        }

    }

    private LocationManager locationManager;
    private String provider;
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * 设置功能菜单
     */
    private void function(){
        int[] imgs={R.drawable.example,R.drawable.example,R.drawable.example,R.drawable.example};
        String[] strings={"主页","常见问题","通用设置","关于应用"};
        List<Function> functions=new ArrayList<>();
        for(int i=0;i<imgs.length;i++){
            Function function=new Function(imgs[i],strings[i]);
            functions.add(function);
        }
        FunctionAdapter adapter=new FunctionAdapter(this,functions);
        listview.setAdapter(adapter);
    }
    @OnClick({R.id.tv_exit,R.id.tv_device,R.id.tv_smart,R.id.tv_live})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_exit:
//                preferences.edit().clear().commit();/**清空当前用户的所有数据*/
                if (preferences.contains("password")){
                   preferences.edit().remove("password").commit();
                }
                deviceGroupDao.deleteAll();
                deviceChildDao.deleteAll();

                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);

                break;
            case R.id.tv_device:
                FragmentTransaction fragmentTransaction3=fragmentManager.beginTransaction();//开启碎片事务
                List<DeviceGroup> deviceGroups=deviceGroupDao.findAllDevices();
                if (deviceGroups.size()==1 && deviceChildDao.findAllDevice().size()==0){
                    fragmentTransaction3.replace(R.id.layout_body,new NoDeviceFragment()).commit();
                }else {
                    fragmentTransaction3.replace(R.id.layout_body,new DeviceFragment());
                    fragmentTransaction3.commit();
                }
                device_view.setVisibility(View.VISIBLE);
                smart_view.setVisibility(View.GONE);
                live_view.setVisibility(View.GONE);
                break;
            case R.id.tv_smart:
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.layout_body,new SmartFragmentManager());
                fragmentTransaction.commit();
                device_view.setVisibility(View.GONE);
                smart_view.setVisibility(View.VISIBLE);
                live_view.setVisibility(View.GONE);
                break;
            case R.id.tv_live:
                FragmentTransaction transaction=fragmentManager.beginTransaction();
                transaction.replace(R.id.layout_body,new LiveFragment());
                transaction.commit();
                device_view.setVisibility(View.GONE);
                smart_view.setVisibility(View.GONE);
                live_view.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            application.removeActivity(this);/**退出主页面*/
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int a=requestCode;
        if (requestCode==MainControlActivity.MAINCONTROL){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_body,new SmartFragmentManager());
            fragmentTransaction.commit();
            device_view.setVisibility(View.GONE);
            smart_view.setVisibility(View.VISIBLE);
            live_view.setVisibility(View.GONE);
        }

    }

    int[] imgs={R.mipmap.image_unswitch, R.mipmap.image_switch};
    class LoadDeviceAsync extends AsyncTask<String,Void,Integer>{

        @Override
        protected Integer doInBackground(String... strings) {
            int code=0;
            try {
                String userId=preferences.getString("userId","");
                String allDeviceUrl="http://120.77.36.206:8082/warmer/v1.0/device/findAll?userId="+ URLEncoder.encode(userId,"utf-8");
                String result=HttpUtils.getOkHpptRequest(allDeviceUrl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    JSONObject content=jsonObject.getJSONObject("content");
                    if (code==2000){
                        JSONArray houses=content.getJSONArray("houses");
                        for (int i=0;i<houses.length();i++){
                            JSONObject house=houses.getJSONObject(i);
                            if (house!=null){
                                int houseId=house.getInt("id");
                                String houseName=house.getString("houseName");
                                String location=house.getString("location");
                                int masterControllerDeviceId=house.getInt("masterControllerDeviceId");
                                int externalSensorsId=house.getInt("externalSensorsId");
                                String layers=house.getString("layers");
                                DeviceGroup deviceGroup=new DeviceGroup((long)houseId, houseName+"."+location,houseName,location, masterControllerDeviceId, externalSensorsId, layers);
                                if(deviceGroupDao.findById((long)houseId)!=null){
                                    deviceGroupDao.update(deviceGroup);
                                }else {
                                    deviceGroupDao.insert(deviceGroup);
                                }
                                JSONArray deviceList=house.getJSONArray("deviceList");
                                for (int j=0;j<deviceList.length();j++){
                                    JSONObject device=deviceList.getJSONObject(j);
                                    if (device!=null){
                                        int deviceId=device.getInt("id");
                                        String deviceName=device.getString("deviceName");
                                        int type=device.getInt("type");
                                        int groupId=device.getInt("houseId");

                                        int masterControllerUserId=device.getInt("masterControllerUserId");
                                        int isUnlock=device.getInt("isUnlock");
                                        DeviceChild deviceChild=new DeviceChild((long)deviceId, deviceName, imgs[0], 0, (long)groupId, type, masterControllerUserId, isUnlock);
                                        deviceChildDao.insert(deviceChild);
                                    }
                                }
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
            switch(code){
                case -3004:
                    Utils.showToast(MainActivity.this,"查询失败");
                    break;
                case 2000:
                    List<DeviceGroup> deviceGroups=deviceGroupDao.findAllDevices();
                    List<DeviceChild> deviceChildren=deviceChildDao.findAllDevice();
                    FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();//开启碎片事务
                    if (deviceGroups.size()==1 && deviceChildren.size()==0){
                        fragmentTransaction.replace(R.id.layout_body,new NoDeviceFragment()).commit();
                    }else {
                        deviceFragment=new DeviceFragment();
                        fragmentTransaction.replace(R.id.layout_body,deviceFragment);
                        fragmentTransaction.commit();
                    }
                    device_view.setVisibility(View.VISIBLE);
                    smart_view.setVisibility(View.GONE);
                    live_view.setVisibility(View.GONE);
                    break;
            }

        }
    }
}