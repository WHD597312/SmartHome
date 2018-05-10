package com.xinrui.smart.fragment;

import android.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
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

/**
 * 受控机
 */
public class ControlledFragment extends Fragment{

    View view;
    Unbinder unbinder;
    @BindView(R.id.lv_homes)
    ListView lv_homes;
    @BindView(R.id.tv_home) TextView tv_home;//受控机头部
    View view2;//受控机尾部

    private List<DeviceChild> controlleds;
    private ControlledAdapter adapter;//受控机适配器
    private String controlledUrl="http://120.77.36.206:8082/warmer/v1.0/device/setControlled";
    private Map<Integer, Boolean> isSelected;
    private List<DeviceChild> beSelectedData = new ArrayList();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_control,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }

    private List<DeviceChild> getControlleds(){

        if (!Utils.isEmpty(houseId)){
            long id=Long.parseLong(houseId);
            controlleds=deviceChildDao.findGroupIdAllDevice(id);
        }
        return controlleds;
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
        deviceGroupDao=new DeviceGroupDaoImpl(MyApplication.getContext());
        deviceChildDao=new DeviceChildDaoImpl(MyApplication.getContext());
        long id=Long.parseLong(houseId);
        DeviceGroup deviceGroup=deviceGroupDao.findById(id);

        houseName=deviceGroup.getHeader();
        if (!Utils.isEmpty(houseName)){
            tv_home.setText(houseName);
        }
//        controlleds=getControlleds();
        controlleds=new ArrayList<>();
//        controlleds=deviceChildDao.findDeviceType(id,1);
        new GetControlledAsync().execute();
        adapter=new ControlledAdapter(getActivity(),controlleds);
        lv_homes.setAdapter(adapter);

        tv_home.setBackgroundResource(R.drawable.shape_header);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), MQService.class);
        isBound=getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (isBound){
                if (connection != null) {
                    getActivity().unbindService(connection);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btn_ensure})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_ensure:

                Map<String,Object> params=new HashMap<>();
                params.put("houseId",houseId);
                long arr[]=new long[controlledDeviceChildren.size()];
                for (int i=0;i<controlledDeviceChildren.size();i++){
                    arr[i]=controlledDeviceChildren.get(i).getId();
                }
                params.put("controlledId",arr);
                new ControlledAsync().execute(params);

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
    }

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};
    private DeviceChild masterDevice;


    private List<DeviceChild>  controlledDeviceChildren=new ArrayList<>();


    public class ControlledAdapter extends BaseAdapter {
        private Context context;
        private List<DeviceChild> list;

        public ControlledAdapter(Context context, List<DeviceChild> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public DeviceChild getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;
            if (convertView==null){
                convertView= View.inflate(context, R.layout.item_controled,null);
                viewHolder=new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            viewHolder.img_main.setImageResource(R.mipmap.controlled);
            final DeviceChild controlled=getItem(position);
            final CheckBox check=viewHolder.check;

            if (controlled!=null){
                viewHolder.tv_main.setText(controlled.getDeviceName());
                if (controlled.getControlled()==1){
                    viewHolder.check.setChecked(true);
                }else {
                    viewHolder.check.setChecked(false);
                }
            }

             check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if (check.isChecked()){
                       if (!controlledDeviceChildren.contains(controlled)){
                           controlledDeviceChildren.add(list.get(position));
                           DeviceChild deviceChild=list.get(position);
                           deviceChild.setControlled(1);
                           deviceChildDao.update(deviceChild);
                       }
                   }else {
                       DeviceChild deviceChild=list.get(position);
                       deviceChild.setControlled(0);
                       deviceChildDao.update(deviceChild);
                       controlledDeviceChildren.remove(list.get(position));
                   }
                }
            });
            return convertView;
        }


        class ViewHolder{
            @BindView(R.id.img_main)
            ImageView img_main;
            @BindView(R.id.tv_main)
            TextView tv_main;
            @BindView(R.id.check)
            CheckBox check;
            public ViewHolder(View view){
                ButterKnife.bind(this,view);
            }
        }
    }

    class GetControlledAsync extends AsyncTask<Void,Void,Integer>{
        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            try {
                String getAllMainControl="http://120.77.36.206:8082/warmer/v1.0/device/getControlledDevice?houseId="+ URLEncoder.encode(houseId,"utf-8");
                String result=HttpUtils.getOkHpptRequest(getAllMainControl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        JSONArray content=jsonObject.getJSONArray("content");
                        controlleds.clear();
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
                                DeviceChild deviceChild=deviceChildDao.findDeviceById(id);

                                deviceChild.setControlled(controlled);
                                deviceChildDao.update(deviceChild);
                                controlleds.add(deviceChild);
                                if (controlled==1){
                                    controlledDeviceChildren.add(deviceChild);
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
                case -3013:
                    Utils.showToast(getActivity(),"请先设置主控设备");
                    Intent intent=new Intent(getActivity(),MainActivity.class);
                    intent.putExtra("mainControl","mainControl");
                    startActivity(intent);
                    break;
            }
        }
    }

    class ControlledAsync extends AsyncTask<Map<String,Object>,Void,Integer>{

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code=0;
            Map<String,Object> params=maps[0];
            long arr[]= (long[]) params.get("controlledId");
            String result=HttpUtils.postOkHpptRequest(controlledUrl,params);
            if (!Utils.isEmpty(result)){
                try{
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        if (arr!=null && arr.length>0){
                            for (int i = 0; i < arr.length; i++) {
                                DeviceChild deviceChild=deviceChildDao.findDeviceChild(arr[i]);
                                deviceChild.setControlled(1);
                                deviceChild.setCtrlMode("slave");
                                deviceChildDao.update(deviceChild);
                                send(deviceChild);
                            }
                        }else {
                            for (DeviceChild deviceChild:controlleds){
                                deviceChild.setCtrlMode("normal");
                                deviceChildDao.update(deviceChild);
                                send(deviceChild);
                            }
                        }
                    }
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
                    Utils.showToast(getActivity(),"设置受控设备成功");
                    break;
                case -3011:
                    Utils.showToast(getActivity(),"设置受控设备失败");
                    break;
            }
            Intent intent=new Intent(getActivity(),MainActivity.class);
            intent.putExtra("mainControl","mainControl");
            startActivity(intent);
        }
    }
    MQService mqService;
    private boolean bound = false;
    private boolean isBound=false;
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

                String s = maser.toString();
                boolean success = false;
                String mac = deviceChild.getMacAddress();
                String topicName = "rango/" + mac + "/set";
                if (bound) {
                    success = mqService.publish(topicName, 2, s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
