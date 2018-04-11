package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
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

/**
 * 受控机
 */
public class ControlledFragment extends Fragment{

    View view;
    Unbinder unbinder;
    @BindView(R.id.lv_homes)
    ListView lv_homes;
    @BindView(R.id.tv_home) TextView tv_home;//受控机头部
    @BindView(R.id.view) View view2;//受控机尾部
    @BindView(R.id.textView) TextView textView;//受控机提示用户
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
        deviceGroupDao=new DeviceGroupDaoImpl(getActivity());
        deviceChildDao=new DeviceChildDaoImpl(getActivity());
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
        view2.setBackgroundResource(R.drawable.shape_footer);
        textView.setText("选中的主控制设备不出现在受控制设备中");
        textView.setPadding(60,0,0,0);

    }
    @OnClick({R.id.btn_ensure})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_ensure:
                if (controlledDeviceChildren.isEmpty()){
                    Utils.showToast(getActivity(),"未选中受控机");
                }else {
                        Map<String,Object> params=new HashMap<>();
                        params.put("houseId",houseId);
                        long arr[]=new long[controlledDeviceChildren.size()];
                        for (int i=0;i<controlledDeviceChildren.size();i++){
                            arr[i]=controlledDeviceChildren.get(i).getId();
                        }
                        params.put("controlledId",arr);
                        new ControlledAsync().execute(params);
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
            ControlledAdapter.ViewHolder viewHolder=null;
            if (convertView==null){
                convertView= View.inflate(context, R.layout.item_controled,null);
                viewHolder=new ControlledAdapter.ViewHolder(convertView);
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
                       }

                   }else {
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

                                DeviceChild deviceChild = new DeviceChild((long)id, deviceName, imgs[0],0, (long)houseId,
                                        masterControllerUserId, type,isUnlock);
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
            String result=HttpUtils.postOkHpptRequest(controlledUrl,params);
            if (!Utils.isEmpty(result)){
                try{
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
//                        List<DeviceChild> deviceChildren=adapter.getSelected();
//                        deviceChildDao.updateAll(deviceChildren);
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
}
