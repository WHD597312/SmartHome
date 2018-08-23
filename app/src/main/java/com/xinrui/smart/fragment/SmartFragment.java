package com.xinrui.smart.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainControlActivity;
import com.xinrui.smart.adapter.SmartSetAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.SmartSet;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class SmartFragment extends Fragment {

    Unbinder unbinder;
    View view;
    @BindView(R.id.tv_home)
    TextView tv_home;

    @BindView(R.id.smart_set)
    ListView smart_set;
    @BindView(R.id.relative)
    RelativeLayout relative;
    private String name;

    private List<SmartSet> list;
    private SmartSetAdapter adapter;//智能适配器
    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;
    String houseId;
    @BindView(R.id.tv_temp)
    TextView temp;
    /**
     * 温度
     */
    @BindView(R.id.tv_cur_temp)
    TextView tv_cur_temp;
    /**
     * 温度
     */
    @BindView(R.id.hum)
    TextView hum;
    /**
     * 湿度
     */
    @BindView(R.id.tv_hum)
    TextView tv_hum;

    /**
     * 湿度
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_smart, container, false);
        unbinder = ButterKnife.bind(this, view);
        deviceGroupDao = new DeviceGroupDaoImpl(MyApplication.getContext());
        deviceChildDao = new DeviceChildDaoImpl(MyApplication.getContext());
        IntentFilter intentFilter = new IntentFilter("SmartFragmentManager");
        receiver = new MessageReceiver();
        getActivity().registerReceiver(receiver, intentFilter);

        adapter = new SmartSetAdapter(getActivity());
        smart_set.setAdapter(adapter);
        return view;
    }

    private List<DeviceChild> list2 = new ArrayList<>();

    DeviceChild estDeviceChild;
    @Override
    public void onStart() {
        super.onStart();

        if (houseId != null) {/**判断是不是有外置传感器*/
            DeviceGroup deviceGroup = deviceGroupDao.findById(Long.parseLong(houseId));
            estDeviceChild=deviceChildDao.findEstControlDevice(Long.parseLong(houseId),2,1);
//            List<DeviceChild> deviceChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 2);
//            if (deviceChildren != null && !deviceChildren.isEmpty()) {
//                for (DeviceChild deviceChild : deviceChildren) {
//                    if (deviceChild.getControlled() == 1) {
//                        estDeviceChild = deviceChild;
//                        break;
//                    }
//                }
                if (estDeviceChild == null) {
                    relative.setVisibility(View.GONE);
                } else {
                    relative.setVisibility(View.VISIBLE);
                    int extTemp = estDeviceChild.getTemp();
                    int extHum = estDeviceChild.getHum();
                    temp.setText(extTemp + "℃");
                    tv_cur_temp.setText(extTemp + "℃");
                    hum.setText(extHum + "%");
                    tv_hum.setText(extHum + "%");
//                }
            }
            if (deviceGroup != null) {
                tv_home.setText(deviceGroup.getHeader());
            }
        }

        smart_set.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (NoFastClickUtils.isFastClick()) {
                    TextView tv_smart = (TextView) view.findViewById(R.id.tv_smart);
                    if (tv_smart != null) {
                        String content = tv_smart.getText().toString();
                        Intent intent = new Intent(getActivity(), MainControlActivity.class);
                        if ("主控机设置".equals(content)) {
                            if (!Utils.isEmpty(houseId)) {
                                List<DeviceChild> deviceChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 1,true);
                                if (deviceChildren.size() < 2) {
                                    Utils.showToast(getActivity(), "在线设备数量不足");
                                } else if (deviceChildren.size() >= 2) {
                                    list2.clear();
                                    list2=deviceChildDao.findDeviceMayControl(Long.parseLong(houseId),1,true,"M");
//                                    for (DeviceChild deviceChild : deviceChildren) {
//                                        String machAttr = deviceChild.getMachAttr();
//                                        if ("M".equals(machAttr)) {
//
//                                        } else {
//                                            list2.add(deviceChild);
//                                        }
//                                    }
                                    if (list2.size() >= 1) {
                                        intent.putExtra("houseId", houseId);
                                        intent.putExtra("content", content);
                                        startActivityForResult(intent,7000);
                                    } else if (list2.size() < 1) {
                                        Utils.showToast(getActivity(), "在线设备数量不足");
                                    }
                                }
                            }
                        } else if ("受控机设置".equals(content)) {
                            if (!Utils.isEmpty(houseId)) {
                                List<DeviceChild> deviceChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 1);
                                if (deviceChildren.size() < 2) {
                                    Utils.showToast(getActivity(), "设备数量不足");
                                } else {
                                    DeviceChild masterDeviceChild = deviceChildDao.findMainControlDevice(Long.parseLong(houseId),1,2);
//                                    for (DeviceChild deviceChild : deviceChildren) {
//                                        if (deviceChild.getControlled() == 2) {
//                                            masterDeviceChild = deviceChild;
//                                            break;
//                                        }
//                                    }
                                    if (masterDeviceChild == null) {
                                        Utils.showToast(getActivity(), "请先设置主控设备");
                                    } else {
                                        intent.putExtra("houseId", houseId);
                                        intent.putExtra("content", content);
                                        startActivityForResult(intent,7000);
                                    }
                                }
                            }
                        } else if ("外置传感器".equals(content)) {
                            if (!Utils.isEmpty(content)) {
//                                List<DeviceChild> deviceChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 1);
//                                if (deviceChildren.size() == 0) {
//                                    Utils.showToast(getActivity(), "设备数量不足");
//                                } else if (deviceChildren.size() == 1) {
//                                    List<DeviceChild> estChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 2);
//                                    if (estChildren.isEmpty()) {
//                                        Utils.showToast(getActivity(), "没有外置传感器");
//                                    } else {
//                                        intent.putExtra("houseId", houseId);
//                                        intent.putExtra("content", content);
//                                        startActivity(intent);
//                                    }
//                                } else if (deviceChildren.size() >= 2) {
//                                    DeviceChild masterDeviceChild = null;
//                                    for (DeviceChild deviceChild : deviceChildren) {
//                                        if (deviceChild.getControlled() == 2) {
//                                            masterDeviceChild = deviceChild;
//                                            break;
//                                        }
//                                    }
//                                    if (masterDeviceChild == null) {
//                                        Utils.showToast(getActivity(), "请先设置主控设备");
//                                    } else {
//                                        List<DeviceChild> estChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 2);
//                                        if (estChildren.isEmpty()) {
//                                            Utils.showToast(getActivity(), "没有外置传感器");
//                                        } else {
//                                            intent.putExtra("houseId", houseId);
//                                            intent.putExtra("content", content);
//                                            startActivity(intent);
//                                        }
//                                    }
//                                }

                                List<DeviceChild> estChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 2);
                                if (estChildren.isEmpty()) {
                                    Utils.showToast(getActivity(), "没有外置传感器");
                                } else {
                                    intent.putExtra("houseId", houseId);
                                    intent.putExtra("content", content);
                                    startActivityForResult(intent,7000);
                                }
                            }
                        }
                    }
                }else {
                    Utils.showToast(getActivity(),"请检查网络");
                }
            }
        });
    }


    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    MessageReceiver receiver;

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            List<DeviceChild> deviceChildren = deviceChildDao.findDeviceType(Long.parseLong(houseId), 2);
            DeviceChild deviceChild2 = (DeviceChild) intent.getSerializableExtra("deviceChild");
            String macAddress=intent.getStringExtra("macAddress");
            try {
                if (deviceChild2 != null) {
                    if (estDeviceChild!=null && deviceChild2.getMacAddress().equals(estDeviceChild.getMacAddress())){
                        estDeviceChild=deviceChild2;
                        int extTemp = estDeviceChild.getTemp();
                        int extHum = estDeviceChild.getHum();
                        temp.setText(extTemp + "℃");
                        tv_cur_temp.setText(extTemp + "℃");
                        hum.setText(extHum + "%");
                        tv_hum.setText(extHum + "%");
                    }
                }else {
                    if (!Utils.isEmpty(macAddress) && estDeviceChild!=null && macAddress.equals(estDeviceChild.getMacAddress())){
                        Toast.makeText(getActivity(),"该设备已重置",Toast.LENGTH_SHORT).show();
                        relative.setVisibility(View.INVISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (receiver!=null){
            getActivity().unregisterReceiver(receiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
