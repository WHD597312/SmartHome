package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;

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
    @BindView(R.id.tv_home)
    TextView tv_home;
    private List<DeviceChild> mainControls;//主控机数量
    private MainControlAdapter adapter;//主控制设置适配器
    public int runing = 0;
    private Map<Integer, Boolean> isSelected = new HashMap<>();

    private List<DeviceChild> beSelectedData = new ArrayList();

    private String masterUrl = "http://47.98.131.11:8082/warmer/v1.0/house/setMasterDevice";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_control, container, false);
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        houseId = bundle.getString("houseId");
        deviceGroupDao = new DeviceGroupDaoImpl(MyApplication.getContext());
        deviceChildDao = new DeviceChildDaoImpl(MyApplication.getContext());
        long id = Long.parseLong(houseId);
        DeviceGroup deviceGroup = deviceGroupDao.findById(id);

        tv_home.setBackgroundResource(R.drawable.shape_main_control_header);
        houseName = deviceGroup.getHeader();
        if (!Utils.isEmpty(houseName)) {
            tv_home.setText(houseName);
        }
        progressDialog = new ProgressDialog(getActivity());

        mainControls = new ArrayList<>();
//        mainControls=deviceChildDao.findDeviceControl(id,1,1);


        new GetMainControlAsync().execute();
        adapter = new MainControlAdapter(mainControls, getActivity());
        lv_homes.setAdapter(adapter);

        return view;
    }

    private String houseId;
    private String houseName;
    private DeviceChildDaoImpl deviceChildDao;
    private DeviceGroupDaoImpl deviceGroupDao;
    private int unbindPosition = -1;
    private ProgressDialog progressDialog;

    @Override
    public void onStart() {
        super.onStart();

    }

    private boolean isBound = false;

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), MQService.class);
        isBound = getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private List<DeviceChild> getMainControls() {
        long id = 0;
        if (!Utils.isEmpty(houseId)) {
            id = Long.parseLong(houseId);
        }
        List<DeviceChild> mainControls = deviceChildDao.findGroupIdAllDevice(id);
        return mainControls;
    }

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};


    private List<DeviceChild> selectedlist = new ArrayList<>();
    private Map<String, Boolean> map = new HashMap<>();

    //    private Map<String,>
    public class MainControlAdapter extends BaseAdapter {

        private List<DeviceChild> children;
        private Context context;

        public MainControlAdapter(List<DeviceChild> children, Context context) {
            this.children = children;
            this.context = context;
        }

        @Override
        public int getCount() {
            return children.size();
        }

        @Override
        public DeviceChild getItem(int position) {
            return children.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void clear() {
            children.clear();
        }

        public void setList(List<DeviceChild> list) {
            children = list;
        }

        public void addAll(List<DeviceChild> list) {
            children.addAll(list);
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_main_control, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.img_main.setImageResource(R.mipmap.master);
            DeviceChild control = getItem(position);
            viewHolder.check.setChecked(isSelected.get(position));
            if (control != null) {
                viewHolder.tv_main.setText(control.getDeviceName());

            }

            viewHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                }
            });
            final CheckBox check = viewHolder.check;
            check.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    unbindPosition = position;
                    // 当前点击的CB
                    boolean cu = !isSelected.get(position);
                    // 先将所有的置为FALSE

                    for (Integer p : isSelected.keySet()) {
                        isSelected.put(p, false);
                        DeviceChild deviceChild = children.get(p);
                        deviceChild.setControlled(0);
                        children.set(p, deviceChild);
                    }
                    // 再将当前选择CB的实际状态
                    isSelected.put(position, cu);
                    if (cu) {
                        DeviceChild deviceChild = children.get(position);
                        deviceChild.setControlled(2);
                        children.set(position, deviceChild);
                    }

                    notifyDataSetChanged();
                    beSelectedData.clear();

                    if (cu) {
                        beSelectedData.add(children.get(position));
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.img_main)
            ImageView img_main;
            @BindView(R.id.tv_main)
            TextView tv_main;
            @BindView(R.id.check)
            CheckBox check;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    class GetMainControlAsync extends AsyncTask<Void, Void, List<DeviceChild>> {
        @Override
        protected List<DeviceChild> doInBackground(Void... voids) {
            int code = 0;
            List<DeviceChild> list = new ArrayList<>();
            try {
                String getAllMainControl = "http://47.98.131.11:8082/warmer/v1.0/device/getMasterControlledDevice?houseId=" + URLEncoder.encode(houseId, "utf-8");
                String result = HttpUtils.getOkHpptRequest(getAllMainControl);
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    DeviceChild deviceChild2 = null;
                    code = jsonObject.getInt("code");
                    if (code == 2000) {

                        JSONArray content = jsonObject.getJSONArray("content");
                        for (int i = 0; i < content.length(); i++) {
                            JSONObject device = content.getJSONObject(i);
                            if (device != null) {
                                int id = device.getInt("id");
                                String deviceName = device.getString("deviceName");
                                int type = device.getInt("type");
                                int houseId = device.getInt("houseId");
                                int masterControllerUserId = device.getInt("masterControllerUserId");
                                int isUnlock = device.getInt("isUnlock");
                                int controlled = device.getInt("controlled");

                                DeviceChild deviceChild = deviceChildDao.findDeviceById(id);

                                String machAttr = deviceChild.getMachAttr();
                                boolean online = deviceChild.getOnLint();
                                if (online) {
                                    if ("M".equals(machAttr)) {

                                    } else {
                                        deviceChild.setControlled(controlled);
                                        list.add(deviceChild);
                                    }
                                }

//                                list.add(deviceChild);

//                                deviceChildDao.update(deviceChild);

                                if (controlled == 2) {
                                    bindMainControlled = deviceChild;
                                    deviceChild2 = deviceChild;
                                }
                            }
                        }
                        if (isSelected != null) {
                            isSelected.clear();
                        }
//                        if (isSelected != null)
//                            isSelected = null;
//                        isSelected = new HashMap<Integer, Boolean>();
                        for (int i = 0; i < list.size(); i++) {
                            DeviceChild deviceChild = list.get(i);

                            if (deviceChild.getControlled() == 2) {
                                isSelected.put(i, true);
                            } else {
                                isSelected.put(i, false);
                            }
                        }
                        // 清除已经选择的项
                        if (beSelectedData.size() > 0) {
                            beSelectedData.clear();
                        }
                        if (deviceChild2 != null) {
                            beSelectedData.add(deviceChild2);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<DeviceChild> list) {
            super.onPostExecute(list);
            try {
                if (list != null && !list.isEmpty()) {
                    mainControls.addAll(list);
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private DeviceChild bindMainControlled = null;

    @OnClick({R.id.btn_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ensure:
                boolean conn = NetWorkUtil.isConn(getActivity());
                if (conn) {
                    List<DeviceChild> children = deviceChildDao.findGroupIdAllDevice(Long.parseLong(houseId));
                    if (children.size() > 1) {
                        long masterControllerDeviceId;
                        long id;
                        if (!beSelectedData.isEmpty()) {
                            DeviceChild mastetDevice = null;
                            mastetDevice = beSelectedData.get(0);
                            if (mastetDevice != null) {
                                masterControllerDeviceId = mastetDevice.getId();
                                id = mastetDevice.getHouseId();
                                Map<String, Object> params = new HashMap<>();
                                params.put("masterControllerDeviceId", masterControllerDeviceId);
                                params.put("id", id);
                                new MasterAsync().execute(params);
                            }
                        } else if (beSelectedData.isEmpty()) {
                            DeviceChild mastetDevice = null;
                            if (bindMainControlled != null) {
                                mastetDevice = bindMainControlled;
                                mastetDevice.setControlled(0);
                                deviceChildDao.update(mastetDevice);
                                mastetDevice.setId(0L);
                                masterControllerDeviceId = mastetDevice.getId();
                                id = mastetDevice.getHouseId();
                                Map<String, Object> params = new HashMap<>();
                                params.put("masterControllerDeviceId", masterControllerDeviceId);
                                params.put("id", id);
                                new MasterAsync().execute(params);
                            } else {
                                Utils.showToast(getActivity(), "未选择主控制设备");
                            }
//                        for (Map.Entry<Integer,Boolean> entry : isSelected.entrySet()){
//                            int postion=entry.getKey();
//                            DeviceChild deviceChild=mainControls.get(postion);
//
//                            boolean value=entry.getValue();
//                            if (deviceChild!=null){
//                                if (value==false&&postion==unbindPosition && deviceChild.getType()==1 && deviceChild.getControlled()==0){
//                                    mastetDevice=deviceChild;
//                                    deviceChild.setControlled(0);
//                                    deviceChildDao.update(deviceChild);
//                                    mastetDevice.setId(0L);
//                                    break;
//                                }
//                            }
//                        }
//                        if (mastetDevice!=null){
//                            masterControllerDeviceId=mastetDevice.getId();
//                            id=mastetDevice.getHouseId();
//                            Map<String,Object> params=new HashMap<>();
//                            params.put("masterControllerDeviceId",masterControllerDeviceId);
//                            params.put("id",id);
//                            new MasterAsync().execute(params);
//                        }
                        }

                    } else if (children.size() < 2) {
                        Utils.showToast(getActivity(), "设备数量不足");
                    }
                } else {
                    Utils.showToast(getActivity(), "请检查网络");
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
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (isBound) {
                if (connection != null) {
                    getActivity().unbindService(connection);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MasterAsync extends AsyncTask<Map<String, Object>, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null) {
                progressDialog.setMessage("请稍等...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];
            long masterControllerDeviceId = (long) params.get("masterControllerDeviceId");
            String result = HttpUtils.postOkHpptRequest(masterUrl, params);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        if (!mainControls.isEmpty()) {
                            List<DeviceChild> list = deviceChildDao.findDeviceType(Long.parseLong(houseId), 1);
                            DeviceChild mainConrol = null;
                            for (DeviceChild deviceChild : mainControls) {
                                int controlled = deviceChild.getControlled();
                                if (controlled == 2) {/**查找主控设备*/
                                    mainConrol = deviceChild;
                                    break;
                                }
                            }
                            for (DeviceChild deviceChild : list) {
                                if (mainConrol != null && mainConrol.getMacAddress().equals(deviceChild.getMacAddress())) {
                                    deviceChild.setCtrlMode("master");
                                    deviceChild.setControlled(2);
                                    deviceChildDao.update(deviceChild);
                                    send(deviceChild);
                                } else {
                                    deviceChild.setCtrlMode("normal");
                                    deviceChild.setControlled(0);
                                    deviceChildDao.update(deviceChild);
                                    send(deviceChild);
                                }
                            }
                        } else {
                            List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(Long.parseLong(houseId));
                            for (DeviceChild deviceChild : deviceChildren) {
                                if (deviceChild.getType() == 1) {
                                    deviceChild.setCtrlMode("normal");
                                    deviceChild.setControlled(0);
                                    deviceChildDao.update(deviceChild);
                                    send(deviceChild);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            try {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                switch (code) {
                    case 2000:
//                    Intent intent=new Intent(getActivity(),MainActivity.class);
//                    intent.putExtra("mainControl","mainControl");
//                    startActivity(intent);
                        Utils.showToast(getActivity(), "设置成功");
                        break;
                    case -3010:
                        Utils.showToast(getActivity(), "设置失败");
                        break;
                }
                getActivity().setResult(7000);
                getActivity().finish();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    MQService mqService;
    private boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    public void send(DeviceChild deviceChild) {
        try {
            if (deviceChild != null) {
                JSONObject maser = new JSONObject();
                maser.put("ctrlMode", deviceChild.getCtrlMode());
                maser.put("workMode", deviceChild.getWorkMode());
                maser.put("MatTemp", deviceChild.getManualMatTemp());
                maser.put("TimerTemp", deviceChild.getTimerTemp());
                maser.put("LockScreen", deviceChild.getLockScreen());
                maser.put("BackGroundLED", deviceChild.getBackGroundLED());
                maser.put("deviceState", deviceChild.getDeviceState());
                maser.put("tempState", deviceChild.getTempState());
                maser.put("outputMode", deviceChild.getOutputMod());
                maser.put("protectProTemp", deviceChild.getProtectProTemp());
                maser.put("protectSetTemp", deviceChild.getProtectSetTemp());
                maser.put("timerShutDown", deviceChild.getTimerShutdown());
                String s = maser.toString();
                boolean success = false;
                String mac = deviceChild.getMacAddress();
                String topicName = "rango/" + mac + "/set";
                String topicName2 = "rango/" + mac + "/transfer";
                if (bound) {
                    success = mqService.publish(topicName, 2, s);
                    mqService.publish(topicName2, 2, s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}