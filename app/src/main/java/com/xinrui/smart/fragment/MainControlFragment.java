package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.adapter.ETSControlAdapter;
import com.xinrui.smart.adapter.MainControlAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.MainControl;
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

public class MainControlFragment extends Fragment {
    View view;
    Unbinder unbinder;
    @BindView(R.id.lv_homes)
    ListView lv_homes;
    @BindView(R.id.tv_home) TextView tv_home;
    private List<DeviceChild> mainControls;//主控机数量
    private MainControlAdapter adapter;//主控制设置适配器
    public int runing=0;
    private DeviceChildDaoImpl deviceChildDao;
    private DeviceGroupDaoImpl deviceGroupDao;
    private String masterUrl="http://120.77.36.206:8082/warmer/v1.0/house/setMasterDevice";
    String houseName;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_control,container,false);
        unbinder= ButterKnife.bind(this,view);
        return view;
    }

    String houseId;
    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle=getArguments();
        houseId=bundle.getString("houseId");
        deviceGroupDao=new DeviceGroupDaoImpl(getActivity());
        deviceChildDao=new DeviceChildDaoImpl(getActivity());
        long id=Long.parseLong(houseId);
//        DeviceGroup deviceGroup=deviceGroupDao.findById(id);
//
//        houseName=deviceGroup.getHeader();
//        if (!Utils.isEmpty(houseName)){
//            tv_home.setText(houseName);
//        }
//        mainControls=getMainControls();
        mainControls=new ArrayList<>();
        new GetMainControlAsync().execute();
        adapter=new MainControlAdapter(getActivity(),mainControls);
        lv_homes.setAdapter(adapter);


    }
    @Override
    public void onResume() {
        super.onResume();

    }
    private List<DeviceChild> getMainControls(){
        long id=0;
        if (!Utils.isEmpty(houseId)){
            id=Long.parseLong(houseId);
        }
        List<DeviceChild> mainControls=deviceChildDao.findGroupIdAllDevice(id);
        return mainControls;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ETSControlAdapter.checked==false){
            ETSControlAdapter.checked=true;
        }
    }

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};
    class GetMainControlAsync extends AsyncTask<Void,Void,Integer>{
        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            try {
                String getAllMainControl="http://120.77.36.206:8082/warmer/v1.0/device/getMasterControlledDevice?houseId="+ URLEncoder.encode(houseId,"utf-8");
                String result=HttpUtils.getOkHpptRequest(getAllMainControl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        mainControls.clear();
                        JSONArray content=jsonObject.getJSONArray("content");

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
    @OnClick({R.id.btn_ensure})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_ensure:
                MainControlAdapter.SelectedEnsure selectedEnsure=adapter.getSelected();
                int selected=selectedEnsure.selected;
                boolean flag=selectedEnsure.flag;
                if (flag){
                    DeviceChild deviceChild=mainControls.get(selected);
                    long masterControllerDeviceId=deviceChild.getId();
                    long id=deviceChild.getGroupId();
                    Map<String,Object> params=new HashMap<>();
                    params.put("masterControllerDeviceId",masterControllerDeviceId);
                    params.put("id",id);
                    new MasterAsync().execute(params);
                }else {
                    Utils.showToast(getActivity(),"请选择一个主控设备");
                }
                break;
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
        if (ETSControlAdapter.checked==false){
            ETSControlAdapter.checked=true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ETSControlAdapter.checked==false){
            ETSControlAdapter.checked=true;
        }
    }

    class MasterAsync extends AsyncTask<Map<String,Object>,Void,Integer>{

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code=0;
            Map<String,Object> params=maps[0];
            String result=HttpUtils.postOkHpptRequest(masterUrl,params);
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
                    Utils.showToast(getActivity(),"设置主控设备成功");
                    Intent intent=new Intent(getActivity(),MainActivity.class);
                    intent.putExtra("mainControl","mainControl");
                    startActivity(intent);
                    break;
                case -3010:
                    Utils.showToast(getActivity(),"设置主控设备失败");
                    Intent intent2=new Intent(getActivity(),MainActivity.class);
                    intent2.putExtra("mainControl","mainControl");
                    startActivity(intent2);
                    break;
            }
        }
    }
}
