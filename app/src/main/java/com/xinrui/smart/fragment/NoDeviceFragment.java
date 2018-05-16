package com.xinrui.smart.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.bigkoo.pickerview.OptionsPickerView;
import com.google.gson.Gson;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddDeviceActivity;
import com.xinrui.smart.adapter.CityAdapter;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.JsonBean;
import com.xinrui.smart.util.JsonFileReader;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.DeviceUpdateHomeDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NoDeviceFragment extends Fragment{
    private DeviceGroupDaoImpl deviceGroupDao;
    List<DeviceGroup> deviceGroups;//设备组

    private ArrayList<JsonBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private View view;
    private Unbinder unbinder;
    @BindView(R.id.tv_city) TextView tv_city;
    @BindView(R.id.tv_myhome) TextView tv_myhome;
    @BindView(R.id.btn_add_device) Button btn_add_device;
    @BindView(R.id.image_position) ImageView image_position;
    private CityAdapter adapter;
    List<String> strings;
    private String province;
    private String city;
    private boolean first=true;
    private String homeUrl="http://120.77.36.206:8082/warmer/v1.0/house/registerHouse";
    DeviceGroup deviceGroup=null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_nodevice,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }
    SharedPreferences preferences;
    @Override
    public void onStart() {
        super.onStart();
        //初始化定位
        initLocation();
        startLocation();//开始定位
        preferences = getActivity().getSharedPreferences("my", Context.MODE_PRIVATE);
        deviceGroupDao=new DeviceGroupDaoImpl(MyApplication.getContext());

        List<DeviceGroup> deviceGroups=deviceGroupDao.findAllDevices();
        if (deviceGroups.size()==2){
            for (DeviceGroup deviceGroup2:deviceGroups){
                deviceGroup=deviceGroup2;
                break;
            }
        }
        if (deviceGroup!=null){
            updateDeviceGroup=deviceGroup;
            String location=deviceGroup.getLocation();
            String houseHome=deviceGroup.getHouseName();
            if (!Utils.isEmpty(location)){
                tv_city.setText(deviceGroup.getLocation());
            }else {
                tv_city.setText("北京市");
            }
            if (!Utils.isEmpty(houseHome)){
                tv_myhome.setText(houseHome);
            }

        }
    }
    @OnClick({R.id.btn_add_device,R.id.image_position,R.id.image_home,R.id.tv_myhome,R.id.tv_city})
    public void onClick(View view){
        switch(view.getId()){
            case R.id.btn_add_device:
                String group=tv_myhome.getText().toString();
                if (deviceGroup!=null) {
                        city=tv_city.getText().toString().trim();
                        if (Utils.isEmpty(city)){
                            city="北京市";
                        }
                        deviceGroup.setLocation(city);
                        new UpdateHomeLocationAsync().execute(deviceGroup);
                }
                break;
            case R.id.image_position:
                stopLocation();
                showPickerView();
                break;
            case R.id.tv_city:
                stopLocation();
                showPickerView();
                break;
            case R.id.image_home:
                buildUpdateHomeDialog();
                break;
            case R.id.tv_myhome:
                buildUpdateHomeDialog();
                break;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initJsonData();
    }


    private String houseName;
    private DeviceGroup updateDeviceGroup;
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
                houseName=dialog.getName();
                if (Utils.isEmpty(houseName)){
                    Utils.showToast(getActivity(),"住所名称不能为空");
                }else {
                    if (updateDeviceGroup!=null){
                        updateDeviceGroup.setHouseName(houseName);
                        new UpdateHomeNameAsync().execute(updateDeviceGroup);
                        dialog.dismiss();
                    }
                }
            }
        });
        dialog.show();
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
                    tv_myhome.setText(updateDeviceGroup.getHouseName());
                    break;
            }
        }
    }
    class UpdateHomeLocationAsync extends AsyncTask<DeviceGroup,Void,Integer>{
        @Override
        protected Integer doInBackground(DeviceGroup... deviceGroups) {
            int code=0;
            DeviceGroup updateDeviceGroup=deviceGroups[0];
            try {
                String updateHomeUrl="http://120.77.36.206:8082/warmer/v1.0/house/changeHouseLocation?houseId="+
                        URLEncoder.encode(updateDeviceGroup.getId()+"","UTF-8")+"&houseLocation="+URLEncoder.encode(updateDeviceGroup.getLocation(),"UTF-8");
                String result=HttpUtils.getOkHpptRequest(updateHomeUrl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        deviceGroup.setHeader(deviceGroup.getHouseName()+"."+deviceGroup.getLocation());
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
                    long houseId=deviceGroup.getId();
                    Intent intent=new Intent(getActivity(), AddDeviceActivity.class);
                    intent.putExtra("houseId",houseId+"");
                    startActivity(intent);
                    break;
                case -3002:
                    break;
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        deviceGroupDao.closeDaoSession();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
        destroyLocation();
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


                String s=location.getProvince();
                if (first && !Utils.isEmpty(s)){
                    tv_city.setText(location.getCity());
                    city=location.getCity();
                    first=false;
                }
                if (deviceGroup!=null){
                    String location2=deviceGroup.getLocation();
                    if (!Utils.isEmpty(location2)){
                        tv_city.setText(location2);
                    }
                }
                if (!first){
                    stopLocation();
                }
            }
        }
    };
    private void showPickerView() {

        OptionsPickerView pvOptions=new OptionsPickerView.Builder(getActivity(), new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String text = options2Items.get(options1).get(options2);
               tv_city.setText(text);
               city=text;
            }
        }).setTitleText("")
                .setDividerColor(Color.GRAY)
                .setTextColorCenter(Color.GRAY)
                .setContentTextSize(16)
                .setOutSideCancelable(false)
                .build();
          /*pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.setPicker(options1Items, options2Items);//二级选择器*/
        pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
        pvOptions.show();
    }



    private void initJsonData() {   //解析数据

        /**
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         * 关键逻辑在于循环体
         *
         * */
        //  获取json数据
        String JsonData = JsonFileReader.getJson(getActivity(), "province_data.json");
        ArrayList<JsonBean> jsonBean = parseData(JsonData);//用Gson 转成实体

        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        options1Items = jsonBean;

        for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
            ArrayList<String> CityList = new ArrayList<>();//该省的城市列表（第二级）
            ArrayList<ArrayList<String>> Province_AreaList = new ArrayList<>();//该省的所有地区列表（第三极）

            for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                String CityName = jsonBean.get(i).getCityList().get(c).getName();
                CityList.add(CityName);//添加城市

                ArrayList<String> City_AreaList = new ArrayList<>();//该城市的所有地区列表

                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                if (jsonBean.get(i).getCityList().get(c).getArea() == null
                        || jsonBean.get(i).getCityList().get(c).getArea().size() == 0) {
                    City_AreaList.add("");
                } else {
                    for (int d = 0; d < jsonBean.get(i).getCityList().get(c).getArea().size(); d++) {//该城市对应地区所有数据
                        String AreaName = jsonBean.get(i).getCityList().get(c).getArea().get(d);
                        City_AreaList.add(AreaName);//添加该城市所有地区数据
                    }
                }
                Province_AreaList.add(City_AreaList);//添加该省所有地区数据
            }
            /**
             * 添加城市数据
             */
            options2Items.add(CityList);

            /**
             * 添加地区数据
             */
            options3Items.add(Province_AreaList);
        }
    }

    public ArrayList<JsonBean> parseData(String result) {//Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // mHandler.sendEmptyMessage(MSG_LOAD_FAILED);
        }
        return detail;
    }
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
