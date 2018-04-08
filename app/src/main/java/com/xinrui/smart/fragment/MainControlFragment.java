package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.ETSControlAdapter;
import com.xinrui.smart.adapter.MainControlAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.MainControl;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

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
    private List<DeviceChild> mainControls;//主控机数量
    private MainControlAdapter adapter;//主控制设置适配器
    public int runing=0;
    private DeviceChildDaoImpl deviceChildDao;
    private String masterUrl="http://120.77.36.206:8082/warmer/v1.0/house/setMasterDevice";
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
        deviceChildDao=new DeviceChildDaoImpl(getActivity());
        mainControls=getMainControls();
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
    @OnClick({R.id.btn_ensure})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_ensure:
                Map<String,Object> params=new HashMap<>();
//                params.put("masterControllerDeviceId",);
//                params.put("id",)
//                new MasterAsync().execute()
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
                    break;
                case -3010:
                    Utils.showToast(getActivity(),"设置主控设备失败");
                    break;
            }
        }
    }
}
