package com.xinrui.secen.scene_activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_adapter.EquipmentAdapter;
import com.xinrui.secen.scene_pojo.Equipment;
import com.xinrui.secen.scene_util.GetUrl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.LoginActivity;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.activity.MainControlActivity;
import com.xinrui.smart.activity.PersonInfoActivity;
import com.xinrui.smart.activity.device.AboutAppActivity;
import com.xinrui.smart.activity.device.CommonProblemActivity;
import com.xinrui.smart.activity.device.CommonSetActivity;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.Function;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.GlideCircleTransform;
import com.xinrui.smart.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/29.
 */

public class AddEquipmentActivity extends AppCompatActivity implements EquipmentAdapter.CheckItemListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.listview)
    ListView listview;
    @BindView(R.id.return_button)
    ImageView returnButton;
    GetUrl getUrl = new GetUrl();
    @BindView(R.id.sure)
    Button sure;
    @BindView(R.id.tv_user)
    TextView tv_user;
    /**
     * 用户账号
     */
    @BindView(R.id.image_user)
    ImageView image_user;
    /**
     * 用户头像
     */
    private Context mContext;
    @BindView(R.id.tv_home)
    TextView tv_home;
    /**
     * 家名称
     */
    @BindView(R.id.tv_exit)
    TextView tv_exit;
    /**
     * 退出程序
     */
    //网络返回的数据
    List<Equipment> equipment_network = new ArrayList<>();
    //列表数据
    private List<Equipment> dataArray = new ArrayList<>();
    //适配器
    private EquipmentAdapter equipmentAdapter;
    private LinearLayoutManager linearLayoutManager;
    //选中后的数据
    public List<Equipment> checkedList;
    private boolean isSelectAll;
    //全选操作
    private List<Equipment> check_all_cb;

    DeviceGroupDaoImpl deviceGroupDao;

    MyApplication application;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_equipment);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //设置左上角的图标响应
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
        if (application == null) {
            application = (MyApplication) getApplication();
            application.addActivity(this);
        }
        deviceGroupDao = new DeviceGroupDaoImpl(MyApplication.getContext());
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        long house_Id = sharedPreferences.getLong("house_id", 0);
        DeviceGroup deviceGroup = deviceGroupDao.findById(house_Id);
        if (deviceGroup != null) {
            String header = deviceGroup.getHeader();
            if (!Utils.isEmpty(header)) {
                tv_home.setText(header);
            }
        }
        getUnboundDevice();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        application.removeActivity(this);
    }

    SharedPreferences preferences;
    private Uri outputUri;//裁剪完照片保存地址
    File file;

    @Override
    protected void onStart() {
        super.onStart();
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        file = new File(getExternalCacheDir(), "crop_image2.jpg");

        SharedPreferences preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        String phone = preferences.getString("phone", "");
        String username = preferences.getString("username", "");
        try {
            if (file.exists()) {
                outputUri = Uri.fromFile(file);
                file.createNewFile();
                Glide.with(AddEquipmentActivity.this).load(file).transform(new GlideCircleTransform(getApplicationContext())).into(image_user);
            } else {
                String userId = preferences.getString("userId", "");
                String url = "http://120.77.36.206:8082/warmer/v1.0/user/" + userId + "/headImg";
                Glide.with(AddEquipmentActivity.this).load(url).transform(new GlideCircleTransform(getApplicationContext())).error(R.mipmap.touxiang).into(image_user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (!Utils.isEmpty(username)) {
            tv_user.setText(username);
        } else if (!Utils.isEmpty(phone)) {
            tv_user.setText(phone);
        }

    }

    @OnClick({R.id.tv_exit, R.id.return_button, R.id.image_user, R.id.tv_user})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_exit:
                if (file != null && file.exists()) {
                    file.delete();
                }
                DeviceChildDaoImpl deviceChildDao = new DeviceChildDaoImpl(this);
                SharedPreferences smart = getSharedPreferences("smart", Context.MODE_PRIVATE);
                SharedPreferences fragmentPreferences = getSharedPreferences("fragment", Context.MODE_PRIVATE);
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
                fragmentPreferences.edit().clear().commit();
                deviceGroupDao.deleteAll();
                deviceChildDao.deleteAll();

                application.removeAllFragment();

                deviceChildDao.closeDaoSession();
                deviceGroupDao.closeDaoSession();
                timeDao.closeDaoSession();
                timeTaskDao.closeDaoSession();

                Intent intent2 = new Intent(AddEquipmentActivity.this, LoginActivity.class);
                startActivity(intent2);
                break;
            case R.id.return_button:
                Intent intent = new Intent(AddEquipmentActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Activity_return", "Activity_return");
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.image_user:
                Intent change = new Intent(this, PersonInfoActivity.class);
                change.putExtra("change", "change");
                startActivity(change);
                break;
            case R.id.tv_user:
                Intent change2 = new Intent(this, PersonInfoActivity.class);
                change2.putExtra("change", "change");
                startActivity(change2);
                break;

        }
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
                        Intent main = new Intent(AddEquipmentActivity.this, MainActivity.class);
                        startActivity(main);
                        break;
                    case 1:
                        Intent common = new Intent(AddEquipmentActivity.this, CommonProblemActivity.class);
                        common.putExtra("common", "common");
                        startActivity(common);
                        break;
                    case 2:
                        Intent common2 = new Intent(AddEquipmentActivity.this, CommonSetActivity.class);
                        common2.putExtra("common", "common");
                        startActivity(common2);
                        break;
                    case 3:
                        Intent common3 = new Intent(AddEquipmentActivity.this, AboutAppActivity.class);
                        common3.putExtra("common", "common");
                        startActivity(common3);
                        break;
                }
            }
        });
    }

    public void initView() {
        checkedList = new ArrayList<>();
        //初始化控件
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.add_equipment);
        //在加载数据之前配置
        linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        //创建一个适配器
        equipmentAdapter = new EquipmentAdapter(mContext, dataArray, this);
        recyclerView.setAdapter(equipmentAdapter);

    }

    @Override
    public void itemChecked(Equipment equipment, boolean isChecked) {
        //处理Item点击选中回调事件
        if (isChecked) {
            //选中处理
            if (!checkedList.contains(equipment)) {
                checkedList.add(equipment);
            }
        } else {
            //未选中处理
            if (checkedList.contains(equipment)) {
                checkedList.remove(equipment);
            }
        }
    }

    @OnClick(R.id.sure)
    public void onViewClicked() {
        if (null == checkedList || checkedList.size() == 0) {

        } else {
            SharedPreferences sharedPreferences1 = getSharedPreferences("roomId", Activity.MODE_PRIVATE);
            int roomId = sharedPreferences1.getInt("roomId", 0);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < checkedList.size(); i++) {
                jsonArray.put(checkedList.get(i).getId());
            }
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("roomId", roomId);
                jsonObject.put("deviceIds", jsonArray);
                new AddDevicesAsyncTask().execute(jsonObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
            Intent intent = new Intent(AddEquipmentActivity.this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("Activity_return", "Activity_return");
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }

    //回调接口，用于异步返回数据
    public interface AsyncResponse {
        void onDataReceivedSuccess(List<Equipment> listData);

        void onDataReceivedFailed();
    }


    //房间内添加设备
    class AddDevicesAsyncTask extends AsyncTask<JSONObject, Void, Integer> {

        @Override
        protected Integer doInBackground(JSONObject... s) {
            int code = 0;
            JSONObject params = s[0];
            String url = "http://120.77.36.206:8082/warmer/v1.0/room/addDevice";
            String result = HttpUtils.postOkHpptRequest3(url, params);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    String message = jsonObject.getString("message");
                    if (code == 2000) {
                        JSONObject content = jsonObject.getJSONObject("content");
                    } else if (code == -4004) {
                        String error = jsonObject.getString("error");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }
    }

    //查询住所内未绑定房间的设备
    class GetUnboundDeviceAsyncTask extends AsyncTask<Void, Void, List<Equipment>> {

        AsyncResponse asyncResponse;

        void setOnAsyncResponse(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected List<Equipment> doInBackground(Void... voids) {
            int code = 0;
            int house_Id = (int) sharedPreferences.getLong("house_id", 0);
            Map<String, Object> map = new HashMap<>();
            map.put("houseId", house_Id);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/getUnboundDevice", map);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    JSONArray jsonArray = jsonObject.getJSONArray("content");
                    //把设备数据保存起来
                    List<Equipment> equipment_list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        String deviceName = object.getString("deviceName");
                        int type = object.getInt("type");
                        int houseId = object.getInt("houseId");
                        int masterControllerUserId = object.getInt("masterControllerUserId");
                        int isUnlock = object.getInt("isUnlock");

                        int device_drawable = 0;
                        if (type == 1) {
                            device_drawable = R.drawable.equipment_warmer;
                        } else if (type == 2) {
                            device_drawable = R.drawable.equipment_external_sensor;
                        }
                        Equipment equipment = new Equipment(type, id, deviceName, device_drawable, houseId, masterControllerUserId, isUnlock, false);
                        equipment_list.add(equipment);
                    }
                    return equipment_list;
                } else if (code == -4003) {
                    Toast.makeText(mContext, "暂无可以查询的设备", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Equipment> equipment_list) {
            try {
                if (null != equipment_list && equipment_list.size() != 0) {
                    asyncResponse.onDataReceivedSuccess(equipment_list);
                } else {
                    asyncResponse.onDataReceivedFailed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(equipment_list);
        }

    }

    private void initDatas(List<Equipment> equipment) {
        dataArray = new ArrayList<>();
        for (int i = 0; i < equipment.size(); i++) {
            Equipment equipment1 = new Equipment(equipment.get(i).getType(), equipment.get(i).getId(), equipment.get(i).getDeviceName(), equipment.get(i).getDevice_type(), 0, 0, 0, false);
            dataArray.add(equipment1);
        }
    }

    //获取未绑定设备
    public void getUnboundDevice() {
        final GetUnboundDeviceAsyncTask getUnboundDeviceAsyncTask = new GetUnboundDeviceAsyncTask();
        getUnboundDeviceAsyncTask.execute();
        getUnboundDeviceAsyncTask.setOnAsyncResponse(new AsyncResponse() {
            @Override
            public void onDataReceivedSuccess(List<Equipment> listData) {
                equipment_network = listData;
                initDatas(equipment_network);
                initView();
            }

            @Override
            public void onDataReceivedFailed() {

            }
        });

    }

    //按下back键的跳转处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
        Intent intent = new Intent(AddEquipmentActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Activity_return", "Activity_return");
        intent.putExtras(bundle);
        startActivity(intent);
        return super.onKeyDown(keyCode, event);
    }

}
