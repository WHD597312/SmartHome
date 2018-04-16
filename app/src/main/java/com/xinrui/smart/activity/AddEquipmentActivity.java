package com.xinrui.smart.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.EquipmentAdapter;
import com.xinrui.smart.pojo.Equipment;
import com.xinrui.smart.util.GetUrl;
import com.xinrui.smart.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class AddEquipmentActivity extends Activity implements EquipmentAdapter.CheckItemListener {
    @BindView(R.id.return_button)
    ImageButton returnButton;
    GetUrl getUrl = new GetUrl();
    @BindView(R.id.sure)
    Button sure;
    private Context mContext;
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

    @OnClick(R.id.return_button)
    public void return_MainActivity() {
        //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
        Intent intent = new Intent(AddEquipmentActivity.this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Activity_return", "Activity_return");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_equipment);
        ButterKnife.bind(this);
        getUnboundDevice();
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
    public void onViewClicked(){
        SharedPreferences sharedPreferences1 = getSharedPreferences("roomId",Activity.MODE_PRIVATE);
        int roomId = sharedPreferences1.getInt("roomId",0);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < checkedList.size(); i++) {
            jsonArray.put(checkedList.get(i).getId());
        }
//        JSONArray js = new JSONArray();
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("roomId",roomId);
            jsonObject.put("deviceIds",jsonArray);
             new AddDevicesAsyncTask().execute(jsonObject);

//            js.put(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
        }

//        new AddDevicesAsyncTask().execute(js);

//        jsonObject.put("roomId" )
        //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
        Intent intent = new Intent(AddEquipmentActivity.this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Activity_return", "Activity_return");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //回调接口，用于异步返回数据
    public interface AsyncResponse {
        void onDataReceivedSuccess(List<Equipment> listData);

        void onDataReceivedFailed();
    }

    //房间内添加设备
    class AddDevicesAsyncTask extends AsyncTask<JSONObject,Void,Integer>{

        @Override
        protected Integer doInBackground(JSONObject... s) {
            int code = 0;
            JSONObject params = s[0];
            String url = "http://120.77.36.206:8082/warmer/v1.0/room/addDevice";
            String result = HttpUtils.postOkHpptRequest3(url,params);
            if(!Utils.isEmpty(result)){
                try{
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    String message = jsonObject.getString("message");
                    if(code == 2000){
                        JSONObject content = jsonObject.getJSONObject("content");
                    }else if(code == -4004){
                        String error = jsonObject.getString("error");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return code;
        }
    }
    //查询住所内未绑定房间的设备
    class GetUnboundDeviceAsyncTask extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        AsyncResponse asyncResponse;

        void setOnAsyncResponse(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected String doInBackground(Void... voids) {
            int code = 0;
            int houseId = (int) sharedPreferences.getLong("house_id", 0);
            Map<String, Object> map = new HashMap<>();
            map.put("houseId", houseId);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/getUnboundDevice", map);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    return result;
                } else if (code == -4003) {
                    Toast.makeText(mContext, "暂无可以查询的设备", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!Utils.isEmpty(s)) {
                    JSONObject jsonObject = new JSONObject(s);
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

                        Equipment equipment = new Equipment(id, deviceName, type, houseId, masterControllerUserId, isUnlock, false);
                        equipment_list.add(equipment);
                    }
                    asyncResponse.onDataReceivedSuccess(equipment_list);
                } else {
                    asyncResponse.onDataReceivedFailed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }

    }

    private void initDatas(List<Equipment> equipment) {
        dataArray = new ArrayList<>();
        for (int i = 0; i < equipment.size(); i++) {
            Equipment equipment1 = new Equipment(equipment.get(i).getId(), equipment.get(i).getDeviceName(), equipment.get(i).getType(), 0, 0, 0, false);
            dataArray.add(equipment1);
        }
    }

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
}
