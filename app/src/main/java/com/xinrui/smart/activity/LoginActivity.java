package com.xinrui.smart.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.location.CheckPermissionsActivity;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.Mobile;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.util.mqtt.VibratorUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginActivity extends CheckPermissionsActivity {

    Unbinder unbinder;
    MyApplication application;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.et_pswd)
    EditText et_pswd;
    @BindView(R.id.btn_login) Button btn_login;/**登录按钮*/
    String url = "http://47.98.131.11:8082/warmer/v1.0/user/login";
    public static boolean runnning=false;
    private ProgressDialog progressDialog;
    public static boolean loading=false;
    String fall=null;
    public static int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unbinder = ButterKnife.bind(this);
        preferences = getSharedPreferences("my", MODE_PRIVATE);

        if (application == null) {
            application = (MyApplication) getApplication();
        }

        application.addActivity(this);

        progressDialog = new ProgressDialog(this);

        Intent intent=getIntent();
        fall=intent.getStringExtra("fall");
        if (preferences.contains("phone") && preferences.contains("password")){
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            runnning=false;
            Intent intent2=new Intent(this,MainActivity.class);
            intent2.putExtra("fall",fall);
            startActivity(intent2);
        } else if (preferences.contains("phone") && !preferences.contains("password")){
            String phone = preferences.getString("phone", "");
            et_name.setText(phone);
            et_pswd.setText("");
        }
    }

    SharedPreferences preferences;

    @Override
    protected void onStart() {
        super.onStart();



    }

    @Override
    protected void onResume() {
        super.onResume();
        runnning=true;
    }


    @OnClick({R.id.btn_login, R.id.tv_register,R.id.tv_forget_pswd})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_register:
                if (NoFastClickUtils.isFastClick()){
                    startActivity(new Intent(this, RegistActivity.class));
                }
                break;
            case R.id.btn_login:
                if (NoFastClickUtils.isFastClick()){
                    String phone = et_name.getText().toString().trim();
                    String password = et_pswd.getText().toString().trim();
                    if (Utils.isEmpty(phone)) {
                        Utils.showToast(this, "手机号码不能为空");
                        break;
                    }else if (!Mobile.isMobile(phone)){
                        Utils.showToast(this,"手机号码不合法");
                    }
                    if (Utils.isEmpty(password)) {
                        Utils.showToast(this, "请输入密码");
                        break;
                    }else {
                        if (password.length()<6){
                            Utils.showToast(this,"密码最少6位");
                            break;
                        }
                    }
                    Map<String, Object> params = new HashMap<>();
                    params.put("phone", phone);
                    params.put("password", password);
                    boolean isConn = NetWorkUtil.isConn(MyApplication.getContext());

                    if (isConn){
                        new LoginAsyncTask().execute(params);
                    }else {
                        Utils.showToast(this,"请检查你的网络");
                    }
                }
                break;
            case R.id.tv_forget_pswd:
                if (NoFastClickUtils.isFastClick()){
                    startActivity(new Intent(this, ForgetPswdActivity.class));
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
                btn_login.setClickable(true);
                return false;
            }
            application.removeAllActivity();/**退出主页面*/
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class LoginAsyncTask extends AsyncTask<Map<String, Object>, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog!=null){
                progressDialog.setMessage("正在登录中...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                btn_login.setClickable(false);
                CountTimer2 countTimer = new CountTimer2(3000, 1000);
                countTimer.start();
            }
        }

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];
            String result = HttpUtils.postOkHpptRequest(url, params);
            try {
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");

                    if (code==2000){
                        count=1;
                        JSONObject content=jsonObject.getJSONObject("content");
                        int userId=content.getInt("userId");
                        String phone=content.getString("phone");
                        String password=content.getString("password");
                        String username=content.getString("username");
                        SharedPreferences.Editor editor=preferences.edit();
                        if (preferences.contains("phone")){
                            String phone2=preferences.getString("phone","");
                            if (!phone2.equals(phone)){
                                if (preferences.contains("image")){
                                    String image=preferences.getString("image","");
                                    if (!Utils.isEmpty(image)){
                                        File file=new File(image);
                                        if (file.exists()){
                                            file.delete();
                                        }
                                    }
                                    preferences.edit().remove("image").commit();
                                }
                            }
                        }
                        if (!preferences.contains("password")) {
                            editor.putString("phone",phone);
                            editor.putString("password",password);
                        }

                        editor.putString("username",username);
                        editor.putString("userId",userId+"");
                        editor.putString("login","login");
                        editor.commit();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            try {
                if (progressDialog!=null){
                    progressDialog.dismiss();
                }
                switch (code) {
                    case -1006:
                        btn_login.setClickable(true);
                        Utils.showToast(LoginActivity.this, "手机号码未注册");
                        break;
                    case -1005:
                        btn_login.setClickable(true);
                        Utils.showToast(LoginActivity.this, "密码错误");
                        et_pswd.setText("");
                        break;
                    case 2000:
                        deviceGroupDao = new DeviceGroupDaoImpl(getApplicationContext());
                        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());
                        new LoadDeviceAsync().execute();
                        break;
                        default:
                            btn_login.setClickable(true);
                            break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        runnning=false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        runnning=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        runnning=false;
        if (unbinder != null) {
            unbinder.unbind();
        }

        if (deviceChildDao!=null){
            deviceChildDao.closeDaoSession();
        }
        if (deviceGroupDao!=null){
            deviceGroupDao.closeDaoSession();
        }
    }
    boolean isConnected=false;
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;
    MQService mqService;
    private boolean bound = false;


    long shareHouseId = 0;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};
    File file;
    class LoadDeviceAsync extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog!=null){
                progressDialog.setMessage("正在初始化数据...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                btn_login.setClickable(false);
                CountTimer2 countTimer = new CountTimer2(5000, 1000);
                countTimer.start();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int code = 0;
            try {
                file = new File(getExternalCacheDir(), "crop_image2.jpg");
                if (file!=null && file.exists()){
                    file.delete();
                }
                deviceGroupDao.deleteAll();
                deviceChildDao.deleteAll();
                TimeTaskDaoImpl timeTaskDao = new TimeTaskDaoImpl(getApplicationContext());
                List<TimeTask> timeTasks = timeTaskDao.findAll();
                for (TimeTask timeTask : timeTasks) {
                    timeTaskDao.delete(timeTask);
                }

                TimeDaoImpl timeDao = new TimeDaoImpl(getApplicationContext());
                List<Timer> timers = timeDao.findTimers();
                for (Timer timer : timers) {
                    timeDao.delete(timer);
                }
                deviceGroupDao.deleteAll();
                deviceChildDao.deleteAll();
//                deviceChildDao.closeDaoSession();
//                deviceGroupDao.closeDaoSession();
//                timeDao.closeDaoSession();
//                timeTaskDao.closeDaoSession();
                String userId = preferences.getString("userId", "");
                String allDeviceUrl = "http://47.98.131.11:8082/warmer/v1.0/device/findAll?userId=" + URLEncoder.encode(userId, "utf-8");
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
                        List<DeviceGroup> deviceGroups=deviceGroupDao.findAllDevices();
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
                                int houseId=device.getInt("houseId");
                                int masterControllerUserId = device.getInt("masterControllerUserId");
                                int isUnlock = device.getInt("isUnlock");
                                int version = device.getInt("version");
                                String macAddress = device.getString("macAddress");
                                int controlled = device.getInt("controlled");
//                                DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, groupId, masterControllerUserId, type, isUnlock);
                                DeviceChild deviceChild = new DeviceChild(deviceId, groupId, deviceName, macAddress, type);
                                deviceChild.setControlled(controlled);
                                deviceChild.setShareHouseId(houseId);
//                                deviceChild.setGroupPosition(deviceGroups.size());
//                                deviceChild.setChildPosition(x);
                                DeviceChild deviceChild2 = deviceChildDao.findDeviceChild(deviceId);
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
            try {
                if (progressDialog!=null){
                    progressDialog.dismiss();
                }
                switch (code) {
                    case 2000:
                        Utils.showToast(LoginActivity.this,"登录成功");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("login","login");
                        startActivity(intent);
                        break;
                        default:
                            Utils.showToast(LoginActivity.this,"登录失败");
                            if (btn_login!=null){
                                btn_login.setClickable(true);
                            }
                            break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    class CountTimer2 extends CountDownTimer {

        public CountTimer2(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Tag", "倒计时=" + (millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            if (btn_login!=null){
                btn_login.setClickable(true);
            }
            if (progressDialog!=null){
                progressDialog.dismiss();

            }
        }
    }
}
