package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.adapter.ETSControlAdapter;
import com.xinrui.smart.adapter.MainControlAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.ETSControl;
import com.xinrui.smart.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/14.
 */

public class ETSControlFragment extends Fragment {
    View view;
    Unbinder unbinder;
    @BindView(R.id.lv_homes)
    ListView lv_homes;
    @BindView(R.id.tv_home) TextView tv_home;//外置传感器头部
    @BindView(R.id.view) View view2;//传感器尾部
    @BindView(R.id.textView) TextView textView;//外置传感器提示
    private List<DeviceChild> mainControls;//外置传感器数量
    private ETSControlAdapter adapter;//外置传感器适配器
    private String extSensorUrl="http://120.77.36.206:8082/warmer/v1.0/device/setExtSensor";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_control,container,false);
        unbinder= ButterKnife.bind(this,view);
        return view;
    }

    private String houseId;
    private String houseName;
    private DeviceChildDaoImpl deviceChildDao;
    private DeviceGroupDaoImpl deviceGroupDao;
    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle=getArguments();
        houseId=bundle.getString("houseId");
        deviceGroupDao=new DeviceGroupDaoImpl(getActivity());
        deviceChildDao=new DeviceChildDaoImpl(getActivity());
        long id=Long.parseLong(houseId);
        DeviceGroup deviceGroup=deviceGroupDao.findById(id);

        houseName=deviceGroup.getHeader();
        if (!Utils.isEmpty(houseName)){
            tv_home.setText(houseName);
        }
        mainControls=new ArrayList<>();
//        mainControls=deviceChildDao.findDeviceType(id,1);
        new GetExtSensorAsync().execute();
        adapter=new ETSControlAdapter(getActivity(),mainControls);
        lv_homes.setAdapter(adapter);
        tv_home.setBackgroundResource(R.drawable.shape_header_blue);
        view2.setBackgroundResource(R.drawable.shape_footer);
        textView.setText("外置传感器设备只能单选");
        textView.setPadding(140,0,0,0);
    }
    private List<DeviceChild> getETSControls(){
        if (!Utils.isEmpty(houseId)){
            mainControls=deviceChildDao.findGroupIdAllDevice(Long.parseLong(houseId));
        }
        return mainControls;
    }
    @OnClick({R.id.btn_ensure})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_ensure:
                ETSControlAdapter.SelectedEnsure selectedEnsure=adapter.getSelected();
                int selected=selectedEnsure.selected;
                boolean flag=selectedEnsure.flag;
                if (flag){
                    DeviceChild deviceChild=mainControls.get(selected);
                    long masterControllerDeviceId=deviceChild.getId();
                    long id=deviceChild.getHouseId();
                    Map<String,Object> params=new HashMap<>();
                    params.put("deviceId",masterControllerDeviceId);
                    params.put("houseId",id);
                    new ExtSensorAsync().execute(params);
                }else {
                    Utils.showToast(getActivity(),"请选择一个主控设备");
                }
                break;
        }
    }

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};
    class GetExtSensorAsync extends AsyncTask<Void,Void,Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            try {
                String getAllMainControl="http://120.77.36.206:8082/warmer/v1.0/device/getExtSensor?houseId="+ URLEncoder.encode(houseId,"utf-8");
                String result= HttpUtils.getOkHpptRequest(getAllMainControl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        JSONArray content=jsonObject.getJSONArray("content");
                        if (content.length()==0){
                            mainControls.clear();
                        }else {
                            mainControls.clear();
                            for (int i=0;i<content.length();i++){
                                JSONObject device=content.getJSONObject(i);
                                if (device!=null){
                                    int id=device.getInt("id");
                                    String deviceName=device.getString("deviceName");
                                    int type=device.getInt("type");
                                    int houseId=device.getInt("houseId");
                                    int masterControllerUserId=device.getInt("masterControllerUserId");
                                    int isUnlock=device.getInt("isUnlock");
                                    int controlled=device.getInt("controlled");

                                    DeviceChild deviceChild = new DeviceChild((long)id, deviceName, imgs[0],0, (long)houseId,
                                            masterControllerUserId, type,isUnlock);
                                    deviceChild.setControlled(controlled);
                                    deviceChildDao.update(deviceChild);
                                    mainControls.add(deviceChild);
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
            switch (code){
                case 2000:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    class ExtSensorAsync extends AsyncTask<Map<String,Object>,Void,Integer>{

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code=0;
            Map<String,Object> params=maps[0];
            String result=HttpUtils.postOkHpptRequest(extSensorUrl,params);
            if (!Utils.isEmpty(result)){
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code){
                case 2000:
                    Utils.showToast(getActivity(),"设置外置传感器成功");
                    Intent intent=new Intent(getActivity(),MainActivity.class);
                    intent.putExtra("mainControl","mainControl");
                    startActivity(intent);
                    break;
                case -3010:
                    Utils.showToast(getActivity(),"设置外置传感器失败");
                    Intent intent2=new Intent(getActivity(),MainActivity.class);
                    intent2.putExtra("mainControl","mainControl");
                    startActivity(intent2);
                    break;
            }
        }
    }
    /**
     * 解绑界面元素
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
