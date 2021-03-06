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


    GetUrl getUrl = new GetUrl();
    @BindView(R.id.sure)
    Button sure;
    private Context mContext;
    @BindView(R.id.tv_home)
    TextView tv_home;
    @BindView(R.id.tv_main_device) TextView tv_main_device;

    String houseName;

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

    @Override
    protected void onStart() {
        super.onStart();
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);


        Intent intent=getIntent();
        String houseName=intent.getStringExtra("houseName");
        tv_main_device.setText(houseName);
    }

    @OnClick({R.id.img_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Intent intent = new Intent(AddEquipmentActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Activity_return", "Activity_return");
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
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
            String url = "http://47.98.131.11:8082/warmer/v1.0/room/addDevice";
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
            String url = getUrl.getRqstUrl("http://47.98.131.11:8082/warmer/v1.0/room/getUnboundDevice", map);
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
