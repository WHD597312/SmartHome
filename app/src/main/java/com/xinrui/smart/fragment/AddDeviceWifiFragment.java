package com.xinrui.smart.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddDeviceWifiFragment extends Fragment {

    View view;
    Unbinder unbinder;
    @BindView(R.id.et_ssid) EditText et_ssid;
    @BindView(R.id.et_pswd) EditText et_pswd;
    String group;
    String groupPosition;
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_add_device_wifi,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    int[] imgs={R.mipmap.image_unswitch, R.mipmap.image_switch};
    @OnClick({R.id.btn_match})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_match:
                String ssid=et_ssid.getText().toString();
                DeviceChild deviceChild=new DeviceChild();
                if (!Utils.isEmpty(group) && !Utils.isEmpty(ssid)){
                    if (deviceGroupDao!=null){
                        DeviceGroup deviceGroup=new DeviceGroup();
                        deviceGroup.setHeader(group);
                        deviceGroup.setId(0L);
                        deviceGroupDao.insert(deviceGroup);

                        deviceChild.setDeviceName(ssid);
                        deviceChild.setImg(imgs[0]);
                        deviceChild.setHouseId(deviceGroup.getId());

//                        if (deviceChildDao.insert(deviceChild)){
//                            DeviceGroup lastGroup=new DeviceGroup();
//                            lastGroup.setId(1L);/**用户第一次进来的时候，将分享设备组的ID设置为1，以后再添加新的设备组时，不但占用分享组的ID，否则会引起数组库主键重用，出现异常*/
//                            lastGroup.setHeader("分享的设备");
//                            deviceGroupDao.insert(lastGroup);
//                        }
                    }
                }else if (!Utils.isEmpty(groupPosition) && !Utils.isEmpty(ssid)){
                    long group=Long.parseLong(groupPosition);
                    deviceChild.setHouseId(group);
                    deviceChild.setDeviceName(ssid);
                    deviceChild.setImg(imgs[0]);
                    deviceChildDao.insert(deviceChild);
                }
                Intent intent=new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        deviceGroupDao=new DeviceGroupDaoImpl(getActivity());
        deviceChildDao=new DeviceChildDaoImpl(getActivity());
        Bundle bundle=getArguments();
        if (bundle!=null){
            group=bundle.getString("group");
            groupPosition=bundle.getString("groupPosition");
        }
    }
}
