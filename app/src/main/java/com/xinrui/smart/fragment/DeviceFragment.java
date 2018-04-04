package com.xinrui.smart.fragment;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.squareup.okhttp.Response;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddDeviceActivity;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.adapter.CityAdapter;
import com.xinrui.smart.adapter.DeviceAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;

import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.DeviceUpdateHomeDialog;
import com.xinrui.smart.view_custom.DividerItemDecoration;
import com.xinrui.smart.view_custom.OnRecyclerItemClickListener;
import com.zhy.http.okhttp.OkHttpUtils;

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
 * Created by win7 on 2018/3/8.
 */

public class DeviceFragment extends Fragment implements AdapterView.OnItemClickListener{
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private View view;
    private Unbinder unbinder;
    /** children items with a key and value list */
    @BindView(R.id.rv_list) RecyclerView rv_list;

    @BindView(R.id.listview) ListView listview;

    @BindView(R.id.btn_add_residence) Button btn_add_residence;
    List<DeviceGroup> deviceGroups;
    List<List<DeviceChild>> childern;
    DeviceAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;
    private List<Integer> list;

    private CityAdapter cityAdapter;
    List<String> strings;
    private String province;
    private String city;
    private boolean first=true;
    private String helper;
    private String homeUrl="http://120.77.36.206:8082/warmer/v1.0/house/registerHouse";
    private String wifiConnectionUrl="http://120.77.36.206:8082/warmer/v1.0/device/registerDevice";
    String createOrUpdate="";
    private DeviceGroup updateDeviceGroup;
    private int updateGroupPosition=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.fragment_device,container,false);

        unbinder=ButterKnife.bind(this,view);

        deviceGroupDao=new DeviceGroupDaoImpl(getActivity());

        deviceChildDao=new DeviceChildDaoImpl(getActivity());

        rv_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        List<DeviceGroup> groups=deviceGroupDao.findAllDevices();
        deviceGroups=new ArrayList<>();
        childern=new ArrayList<>();

        for (DeviceGroup group:groups){
            deviceGroups.add(group);
        }
        for (DeviceGroup deviceGroup:deviceGroups){
           List<DeviceChild> deviceChildren=deviceChildDao.findGroupIdAllDevice(deviceGroup.getId());
           if (deviceChildren!=null && !deviceChildren.isEmpty()){
               childern.add(deviceChildren);
           }
        }
        adapter=new DeviceAdapter(getActivity(),deviceGroups,childern);
        rv_list.setAdapter(adapter);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    @OnClick({R.id.btn_add_residence})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_add_residence:/**添加住所*/
                createOrUpdate="create";
                createOrUpdateHome();
                break;
        }
    }

    /**创建新家或修改新家名称*/
    private void createOrUpdateHome(){
        listview.setVisibility(View.VISIBLE);
        btn_add_residence.setVisibility(View.GONE);
        if (strings.size()>1){
            return;
        }
        if (!first){
            try {
                String s=Utils.getJson("china_city_data.json",getActivity());
                JSONArray jsonArray=new JSONArray(s);
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    String name=jsonObject.getString("name");
                    Log.d("sssss",name);
                    if (name.equals(province)){
                        JSONArray array=jsonObject.getJSONArray("cityList");
                        for (int j=0;j<array.length();j++){
                            JSONObject object=array.getJSONObject(j);
                            String name2=object.getString("name");
                            strings.add(name2);
                        }
                        stopLocation();
                        Message msg=handler.obtainMessage();
                        msg.what=1;
                        handler.sendMessage(msg);
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){
                adapter.notifyDataSetChanged();
            }
        }
    };
    SharedPreferences preferences;
    @Override
    public void onStart() {
        super.onStart();
        //初始化定位
        initLocation();
        strings=new ArrayList<>();
        strings.add("选择城市");
//        strings.add("帮我定位");
        cityAdapter=new CityAdapter(getActivity(),strings);
        listview.setAdapter(cityAdapter);
        listview.setOnItemClickListener(this);
        startLocation();//开始定位
        preferences = getActivity().getSharedPreferences("my", Context.MODE_PRIVATE);
        adapter.setOnHeaderClickListener(new GroupedRecyclerViewAdapter.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(GroupedRecyclerViewAdapter adapter, BaseViewHolder holder, int groupPosition) {
                if (childern.get(groupPosition).isEmpty()){
                    return;
                }else {
                    updateGroupPosition=groupPosition;
                    updateDeviceGroup=deviceGroups.get(groupPosition);
                    createOrUpdateHome();
                    createOrUpdate="update";
                }
            }
        });
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position!=0){
            city=strings.get(position);
            if ("create".equals(createOrUpdate)){
                buildCreateHomeDialog();
            } else if ("update".equals(createOrUpdate)) {
                /**先开始定位，然后才开始修改设备组名称*/
                updateDeviceGroup.setLocation(city);
                new UpdateHomeLocationAsync().execute(updateDeviceGroup);
            }
        }
        listview.setVisibility(View.GONE);
        btn_add_residence.setVisibility(View.VISIBLE);
    }
    private void buildCreateHomeDialog(){
        final DeviceHomeDialog dialog=new DeviceHomeDialog(getActivity());
        dialog.setOnNegativeClickListener(new DeviceHomeDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DeviceHomeDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String name=dialog.getName();
                if (Utils.isEmpty(name)){
                    Utils.showToast(getActivity(),"住所名称不能为空");

                }else {
                    Map<String,Object> params=new HashMap<>();
                    String userId=preferences.getString("userId","");
                    params.put("houseName",name);
                    params.put("location",city);
                    params.put("userId",userId);
                    new AddHomeAsync().execute(params);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void buildUpdateHomeDialog(){
        final DeviceUpdateHomeDialog dialog=new DeviceUpdateHomeDialog(getActivity());
       dialog.setOnNegativeClickListener(new DeviceUpdateHomeDialog.OnNegativeClickListener() {
           @Override
           public void onNegativeClick() {
               dialog.dismiss();
           }
       });
        dialog.setOnPositiveClickListener(new DeviceUpdateHomeDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String name=dialog.getName();
                if (Utils.isEmpty(name)){
                    Utils.showToast(getActivity(),"住所名称不能为空");
                }else {
                    if (updateDeviceGroup!=null){
                        updateDeviceGroup.setHouseName(name);
                        new UpdateHomeNameAsync().execute(updateDeviceGroup);
                        dialog.dismiss();
                    }
                }
            }
        });
        dialog.show();
    }

    class AddHomeAsync extends AsyncTask<Map<String,Object>,Void,Integer>{

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code=0;
            Map<String,Object> params=maps[0];

            String result=HttpUtils.postOkHpptRequest(homeUrl,params);
            if (!Utils.isEmpty(result)){
                try {
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    JSONObject content=jsonObject.getJSONObject("content");
                    String houseName=content.getString("houseName");
                    String location=content.getString("location");
                    int houseId=content.getInt("id");
                    int masterControllerDeviceId=content.getInt("masterControllerDeviceId");
                    if (code==2001){
                        DeviceGroup deviceGroup=new DeviceGroup();
                        deviceGroup.setHeader(houseName+"."+location);
                        deviceGroup.setId((long)houseId);
                        deviceGroup.setLocation(location);
                        deviceGroup.setHouseName(houseName);
                        deviceGroup.setMasterControllerDeviceId(masterControllerDeviceId);
                        deviceGroupDao.insert(deviceGroup);/**添加设备组*/
                        deviceGroups.add(deviceGroup);
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
                case 2001:
                    Utils.showToast(getActivity(),"创建成功");
                    List<DeviceGroup> groups=deviceGroupDao.findAllDevices();
                    deviceGroups.clear();
                    childern.clear();
                    for (DeviceGroup group:groups){
                        deviceGroups.add(group);
                    }
                    for (DeviceGroup deviceGroup:deviceGroups){
                        List<DeviceChild> deviceChildren=deviceChildDao.findGroupIdAllDevice(deviceGroup.getId());
                        if (deviceChildren!=null && !deviceChildren.isEmpty()){
                            childern.add(deviceChildren);
                        }
                    }
                    break;
                case -3001:
                    Utils.showToast(getActivity(),"新建住所失败");
                    break;
            }
        }
    }
    class UpdateHomeNameAsync extends AsyncTask<DeviceGroup,Void,Integer>{

        @Override
        protected Integer doInBackground(DeviceGroup... deviceGroups) {
            int code=0;
            updateDeviceGroup=deviceGroups[0];

            try {
                String updateHomeUrl="http://120.77.36.206:8082/warmer/v1.0/house/changeHouseName?houseId="+
                        URLEncoder.encode(updateDeviceGroup.getId()+"","UTF-8")+"&houseName="+URLEncoder.encode(updateDeviceGroup.getHouseName(),"UTF-8");
                String result=HttpUtils.getOkHpptRequest(updateHomeUrl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        updateDeviceGroup.setHeader(updateDeviceGroup.getHouseName()+"."+updateDeviceGroup.getLocation());
                        deviceGroupDao.update(updateDeviceGroup);
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
                    Utils.showToast(getActivity(),"修改成功");
                    deviceGroups.set(updateGroupPosition,updateDeviceGroup);
                    adapter.changeHeader(updateGroupPosition);
                    break;
            }
        }
    }
    class UpdateHomeLocationAsync extends AsyncTask<DeviceGroup,Void,Integer>{

        @Override
        protected Integer doInBackground(DeviceGroup... deviceGroups) {
            int code=0;
            updateDeviceGroup=deviceGroups[0];
            try {
                String updateHomeUrl="http://120.77.36.206:8082/warmer/v1.0/house/changeHouseLocation?houseId="+
                        URLEncoder.encode(updateDeviceGroup.getId()+"","UTF-8")+"&houseLocation="+URLEncoder.encode(updateDeviceGroup.getLocation(),"UTF-8");
                String result=HttpUtils.getOkHpptRequest(updateHomeUrl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        updateDeviceGroup.setHeader(updateDeviceGroup.getHouseName()+"."+updateDeviceGroup.getLocation());
                        deviceGroupDao.update(updateDeviceGroup);
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
                    buildUpdateHomeDialog();/**成功，就开始修改住所名称*/
                    break;
                case -3002:
                    Utils.showToast(getActivity(),"修改住所信息失败");
                    break;
            }
        }
    }

    /**
     * 初始化定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void initLocation(){
        //初始化client
        locationClient = new AMapLocationClient(getActivity().getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }
    /**
     * 默认的定位参数
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }
    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {

                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if(location.getErrorCode() == 0){
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");

                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + com.xinrui.location.Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                }
                sb.append("***定位质量报告***").append("\n");
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + com.xinrui.location.Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                //解析定位结果，
                String result = sb.toString();
                city=location.getCity();

                String s=location.getProvince();
                if (first && !Utils.isEmpty(s)){
                    province=s;
                    first=false;
                }
            } else {

            }
        }
    };
    /**
     * 获取GPS状态的字符串
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode){
        String str = "";
        switch (statusCode){
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }
    /**
     * 开始定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void startLocation(){
        //根据控件的选择，重新设置定位参数
//        resetOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void stopLocation(){
        // 停止定位
        locationClient.stopLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyLocation();
    }

    /**
     * 销毁定位
     *
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private void destroyLocation(){
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }


}