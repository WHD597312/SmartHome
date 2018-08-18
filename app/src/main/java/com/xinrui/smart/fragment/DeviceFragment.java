package com.xinrui.smart.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.bigkoo.pickerview.OptionsPickerView;
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.google.gson.Gson;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_fragment.LiveFragment;
import com.xinrui.secen.scene_util.NetWorkUtil;
import com.xinrui.secen.scene_view_custom.DividerItemDecoration;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddDeviceActivity;
import com.xinrui.smart.activity.DeviceListActivity;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.activity.SmartTerminalActivity;
import com.xinrui.smart.adapter.CityAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;

import com.xinrui.smart.pojo.JsonBean;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.DensityUtil;
import com.xinrui.smart.util.JsonFileReader;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.TextToVoice;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.util.mqtt.VibratorUtil;
import com.xinrui.smart.view_custom.DeviceChildDialog;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.DeviceUpdateHomeDialog;
import com.xinrui.smart.view_custom.MyRecyclerViewItem;
import com.xinrui.smart.view_custom.OnRecyclerItemClickListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/8.
 */

public class DeviceFragment extends Fragment {
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private View view;
    private Unbinder unbinder;
    public static boolean running2 = false;
    public DeviceChild mDeviceChild;
    private boolean isLoading = false;

    /**
     * 可操作的设备
     */
    public boolean isKeySwitch = false;
    /**
     * children items with a key and value list
     */
    @BindView(R.id.rv_list)
    RecyclerView rv_list;

    @BindView(R.id.btn_add_residence)
    Button btn_add_residence;
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
    private boolean first = true;
    private String helper;
    private String homeUrl = "http://47.98.131.11:8082/warmer/v1.0/house/registerHouse";
    private String wifiConnectionUrl = "http://47.98.131.11:8082/warmer/v1.0/device/registerDevice";
    String createOrUpdate = "";
    private DeviceGroup updateDeviceGroup;
    private int updateGroupPosition = 0;

    private String location;
    private ArrayList<JsonBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
    MessageReceiver receiver;
    int sum = 0;
    public static boolean running = false;
    List<DeviceChild> allListData = new ArrayList();
    ItemTouchHelper touchHelper;
    public static boolean drag = false;
    private boolean isBound = false;
    private ProgressDialog progressDialog;
    private List<DeviceChild> offlineDevices = new ArrayList<>();
    public static boolean loading = false;
    private LinkedList<String> offlineList = new LinkedList<String>();
    private String deviceId;

    //    List
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        deviceGroupDao = new DeviceGroupDaoImpl(MyApplication.getContext());
        deviceChildDao = new DeviceChildDaoImpl(MyApplication.getContext());

        view = inflater.inflate(R.layout.fragment_device, container, false);

        unbinder = ButterKnife.bind(this, view);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int height = display.getHeight();

        int listHight = height - 400;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listHight);
        params.setMargins(10, 0, 10, 0);
        rv_list.setLayoutParams(params);
        Log.i("height", "-->" + height);
        rv_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        List<DeviceGroup> groups = deviceGroupDao.findAllDevices();
        deviceGroups = new ArrayList<>();
        childern = new ArrayList<>();

        progressDialog = new ProgressDialog(getActivity());

        for (DeviceGroup group : groups) {
            deviceGroups.add(group);
        }

        for (int i = 0; i < deviceGroups.size(); i++) {
            DeviceGroup deviceGroup=deviceGroups.get(i);
            if (deviceGroup != null) {
//                allListData.add(deviceGroup);
                List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(deviceGroup.getId());
                for (DeviceChild deviceChild : deviceChildren) {
                    allListData.add(deviceChild);
                    deviceChild.setGroupPosition(i);
                    deviceChildDao.update(deviceChild);
                    offlineList.add(deviceChild.getMacAddress());
                }
                childern.add(deviceChildren);
            }
        }
//        for (DeviceGroup deviceGroup : deviceGroups) {
//            if (deviceGroup != null) {
////                allListData.add(deviceGroup);
//                List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(deviceGroup.getId());
//                for (DeviceChild deviceChild : deviceChildren) {
//                    allListData.add(deviceChild);
//                    deviceChild
//                    offlineList.add(deviceChild.getMacAddress());
//                }
//                childern.add(deviceChildren);
//            }
////            allListData.add(new String());
//        }

        Bundle bundle = getArguments();
        load = bundle.getString("load");
        deviceId = bundle.getString("deviceId");
        adapter = new DeviceAdapter(getActivity(), deviceGroups, childern);
        rv_list.setAdapter(adapter);
        Intent service = new Intent(getActivity(), MQService.class);
        isBound = getActivity().bindService(service, connection, Context.BIND_AUTO_CREATE);
        for (int i = 0; i < allListData.size(); i++) {
            DeviceChild offlineDevice = allListData.get(i);
            if (!offlineDevice.getOnLint()) {
                offlineDevices.add(offlineDevice);
            }
        }
        IntentFilter intentFilter = new IntentFilter("DeviceFragment");
        receiver = new MessageReceiver();
        getActivity().registerReceiver(receiver, intentFilter);


        timeDao = new TimeDaoImpl(getActivity());

//        receiver = new


//        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
//            @Override
//            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                int position = viewHolder.getAdapterPosition();
//                Object o = allListData.get(position);
//                int dragFlags = 0;
//                if (o instanceof DeviceGroup || o instanceof String)
//                    dragFlags = 0;
//                else
//                    dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
//                //允许上下的拖动
//                return makeMovementFlags(dragFlags, 0);
//            }
//
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                drag = true;
//                int position = viewHolder.getAdapterPosition();
//                int fromPosition = viewHolder.getAdapterPosition();//得到拖动ViewHolder的position
//                int toPosition = target.getAdapterPosition();//得到目标ViewHolder的position
//
//                //使用集合工具类Collections，分别把中间所有的item的位置重新交换
//                if (fromPosition < toPosition) {/**从上往下移动*/
//                    Object to = allListData.get(toPosition);
//                    Object from = allListData.get(fromPosition);
//                    if (from instanceof DeviceGroup || from instanceof String || to instanceof DeviceGroup || to instanceof String) {/**拖拽到设备组头部或者是设备组尾部，就直接返回，什么也不做*/
//                        return false;
//                    } else {
//                        if (to instanceof DeviceChild && from instanceof DeviceChild) {/**两个交换的对象都是DeviceChild类*/
//
//                            DeviceChild fromChild = (DeviceChild) from;/**拖拽的对象*/
//                            DeviceChild toDeviceChild = (DeviceChild) to;/**目标对象*/
//                            int fromPoistion2 = fromChild.getChildPosition();/**拖拽的位置*/
//                            int toPosition2 = toDeviceChild.getChildPosition();/**目标位置*/
//
//                            if (fromPoistion2 == toPosition2) {/**如果交换的两个对象的原始位置相同,就重新开始排列这一设备组中的所有数据*/
//                                List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(fromChild.getHouseId());/**从数据库中拿出这一设备组中的所有数据*/
//                                for (int i = 0; i < deviceChildren.size(); i++) {
//                                    DeviceChild deviceChild = deviceChildren.get(i);
//                                    deviceChild.setChildPosition(i);
////                                    deviceChildDao.update(deviceChild);/**对设备的位置重新开始排列*/
//                                }
//                                deviceChildren = deviceChildDao.findGroupIdAllDevice(fromChild.getHouseId());/**重新从数据库中拿取这一组的所有设备数据*/
//                                childern.set(fromChild.getGroupPosition(), deviceChildren);
//                                adapter.changeChildren(fromChild.getGroupPosition());/**重新设置这一组的所有数据*/
//                            } else {/**否则就开始交换两个对象*/
//                                fromChild.setChildPosition(toPosition2);/**交换的时候，只是交换的顺序位置，不是交换的两个对象的实质内容*/
//                                toDeviceChild.setChildPosition(fromPoistion2);/**因此在更新数据的时候，就只需更新两个对象的顺序位置就可以了*/
////                                deviceChildDao.update(fromChild);
////                                deviceChildDao.update(toDeviceChild);
//                                Collections.swap(allListData, fromPosition, toPosition);
//                                adapter.notifyItemMoved(fromPosition, toPosition);
//                                drag = false;
//                            }
//                        }
//                    }
//
//                } else if (toPosition < fromPosition) {/**从下往上移动*/
//                    Object from = allListData.get(fromPosition);/**拖拽的对象*/
//                    Object to = allListData.get(toPosition);/**目标对象*/
//                    if (from instanceof DeviceGroup || from instanceof String || to instanceof DeviceGroup || to instanceof String) {/**如果拖拽对象与目标对象不是DeviceChild类，那么就什么都不用做*/
//                        return false;
//                    } else {
//                        if (to instanceof DeviceChild && from instanceof DeviceChild) {/**两个交换的对象都属于DeviceChild类，就开始交换*/
//                            DeviceChild fromChild = (DeviceChild) from;
//                            DeviceChild toDeviceChild = (DeviceChild) to;
//                            int fromPoistion2 = fromChild.getChildPosition();
//                            int toPosition2 = toDeviceChild.getChildPosition();
//                            if (fromPoistion2 == toPosition2) {
//                                List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(fromChild.getHouseId());
//                                for (int i = 0; i < deviceChildren.size(); i++) {
//                                    DeviceChild deviceChild = deviceChildren.get(i);
//                                    deviceChild.setChildPosition(i);
////                                    deviceChildDao.update(deviceChild);
//                                }
//                                deviceChildren = deviceChildDao.findGroupIdAllDevice(fromChild.getHouseId());
//                                childern.set(fromChild.getGroupPosition(), deviceChildren);
//                                adapter.changeChildren(fromChild.getGroupPosition());
//                            } else {
//                                Log.i("hhh", "from:" + fromPoistion2 + "," + fromChild.getId());
//                                Log.i("hhh", "to:" + toPosition2 + "," + toDeviceChild.getId());
//
//
//                                fromChild.setChildPosition(toPosition2);
//                                toDeviceChild.setChildPosition(fromPoistion2);
////                                deviceChildDao.update(fromChild);
////                                deviceChildDao.update(toDeviceChild);
//
//                                Collections.swap(allListData, toPosition, fromPosition);
//                                adapter.notifyItemMoved(fromPosition, toPosition);
//                                drag = false;
//                            }
//                        }
//                    }
//                }
//
//                //通知Adapter更新状态
//
//                return false;
//            }
//
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//
//            }
//
//            @Override
//            public boolean isLongPressDragEnabled() {
//                return true;
//            }
//
//            @Override
//            public boolean isItemViewSwipeEnabled() {
//                return true;
//            }
//        };
//        //用Callback构造ItemtouchHelper
//        touchHelper = new ItemTouchHelper(callback);
//        //调用ItemTouchHelper的attachToRecyclerView方法建立联系
//        touchHelper.attachToRecyclerView(rv_list);
//
//        rv_list.addOnItemTouchListener(new OnRecyclerItemClickListener(rv_list, getContext()) {
//            @Override
//            public void onItemClick(RecyclerView.ViewHolder vh) {
//
//            }
//
//            @Override
//            public void onItemLongClick(RecyclerView.ViewHolder vh) {
//                int position = vh.getAdapterPosition();
//                Object o = allListData.get(position);
//                if (o instanceof DeviceGroup || o instanceof String) {
//
//                } else {
//                    touchHelper.startDrag(vh);
//
//                    //获取系统震动服务
//                    Vibrator vib = (Vibrator) getActivity().getSystemService(Service.VIBRATOR_SERVICE);//震动70毫秒
//                    vib.vibrate(70);
//                }
//
//            }
//        });
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
        running = false;
    }


    @OnClick({R.id.btn_add_residence})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_residence:/**添加住所*/
                createOrUpdate = "create";
                showPickerView();
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    adapter.notifyDataSetChanged();
                    break;

            }
        }
    };

    class CountTimer extends CountDownTimer {
        public CountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**
         * 倒计时过程中调用
         *
         * @param millisUntilFinished
         */
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Tag", "倒计时=" + (millisUntilFinished / 1000));
//            btn_get_code.setBackgroundColor(Color.parseColor("#c7c7c7"));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
//            btn_get_code.setTextSize(16);
        }

        /**
         * 倒计时完成后调用
         */
        @Override
        public void onFinish() {
            Log.e("Tagssssssss", "倒计时完成");
            //设置倒计时结束之后的按钮样式
//            btn_get_code.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_blue_light));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
//            btn_get_code.setTextSize(18);
//            if (progressDialog != null) {
//                progressDialog.dismiss();
//            }
            if (mqService != null) {
                Map<String, DeviceChild> childMap = mqService.getOfflineDevices();
                for (int i = 0; i < deviceGroups.size(); i++) {
                    List<DeviceChild> deviceChildren = childern.get(i);
                    for (int j = 0; j < deviceChildren.size(); j++) {
                        DeviceChild deviceChild = deviceChildren.get(j);
                        for (Map.Entry<String, DeviceChild> childEntry : childMap.entrySet()) {
                            String macAddress = childEntry.getKey();
                            Log.i("macAdd", "-->" + macAddress);
                            DeviceChild deviceChild2 = childEntry.getValue();
                            if (macAddress.equals(deviceChild.getMacAddress())) {
                                childern.get(i).set(j, deviceChild2);
                                break;
                            }
                        }
                        if (j == deviceChildren.size() - 1) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    SharedPreferences preferences;

    class LoadMqttAsync3 extends AsyncTask<List<DeviceChild>, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (NetWorkUtil.isConn(getActivity())) {
                List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();

                if (progressDialog != null && deviceChildren.size() > 0) {
                    progressDialog.setMessage("正在加载数据...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            }
        }

        @Override
        protected String doInBackground(List<DeviceChild>... lists) {
            List<DeviceChild> deviceChildren = lists[0];
            String result = null;
            try {
                boolean running = DeviceFragment.running;
                Log.i("running", "-->" + running);
                if (NetWorkUtil.isConn(getActivity())) {
                    for (int i = 0; i < deviceChildren.size(); i++) {
                        DeviceChild deviceChild = deviceChildren.get(i);
                        if (i == deviceChildren.size() - 1) {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                        }
                        if (mqService != null) {
                            try {
                                String mac = deviceChild.getMacAddress();
                                String topic = "rango/" + mac + "/set";
                                Log.i("macAddress2", "-->" + mac);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("loadDate", "1");
                                String s = jsonObject.toString();
                                boolean success = false;
                                success = mqService.publish(topic, 1, s);
                                if (!success) {
                                    success = mqService.publish(topic, 1, s);
                                    Log.i("macAddress", "-->" + mac);
                                }
                                if (success) {
                                    Log.i("macAddress3", "-->" + mac);
//                                    Thread.sleep(200);
                                    Thread.currentThread().sleep(300);
                                    if (!Utils.isEmpty(load) && i == deviceChildren.size() - 1) {
                                        result = "result";
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!Utils.isEmpty(s)) {
                CountTimer countTimer = new CountTimer(1000, 1000);
                countTimer.start();
            }
        }
    }

    String load = null;

    @Override
    public void onStart() {
        super.onStart();

        //初始化定位
//        initLocation();
        strings = new ArrayList<>();
        strings.add("选择城市");
//        strings.add("帮我定位");
//        cityAdapter=new CityAdapter(getActivity(),strings);

//        startLocation();//开始定位
        preferences = getActivity().getSharedPreferences("my", Context.MODE_PRIVATE);
        adapter.setOnHeaderClickListener(new GroupedRecyclerViewAdapter.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(GroupedRecyclerViewAdapter adapter, BaseViewHolder holder, int groupPosition) {

                updateGroupPosition = groupPosition;
                updateDeviceGroup = deviceGroups.get(groupPosition);

                if (groupPosition == deviceGroups.size() - 1) {
                    Utils.showToast(getActivity(), "该设备组不能更改");
                } else {
                    createOrUpdate = "update";
                    showPopwindow();
                }
            }
        });
        running = true;

    }

    boolean isBind = false;


    @Override
    public void onResume() {
        super.onResume();
        running2 = true;
        initJsonData();


//        Intent intent = new Intent(getActivity(), MQService.class);
//        isBind = getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
//
//        IntentFilter intentFilter = new IntentFilter("DeviceFragment");
//        receiver=new MessageReceiver();
//        getActivity().registerReceiver(receiver, intentFilter);
    }


    /**
     * 这里popupWindow用的是showAtLocation而不是showAsDropDown
     * popupWindow.isShowing会一直返回false，所以要重新定义一个变量
     * 要注意setOutsideTouchable的干扰
     */
    /**
     * 弹出一个底部窗口
     */
    private void showPopwindow() {

        View popView = View.inflate(getActivity(), R.layout.house_pop, null);

        Button btn_edit_house = (Button) popView.findViewById(R.id.btn_edit_house);
        Button btn_delete_house = (Button) popView.findViewById(R.id.btn_delete_house);
        Button btn_cancel_house = (Button) popView.findViewById(R.id.btn_cancel_house);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        final PopupWindow popWindow = new PopupWindow(popView, width, height);
        popWindow.setFocusable(true);
        popWindow.setAnimationStyle(R.style.anim_menu_bottombar);
//        popWindow.setOutsideTouchable(false);// 设置同意在外点击消失

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_edit_house:
                        updateDeviceGroup = deviceGroups.get(updateGroupPosition);

                        showPickerView();

                        popWindow.dismiss();
                        break;
                    case R.id.btn_delete_house:
                        if (deviceGroups.size() > 2) {
                            DeviceGroup deleteHouse = deviceGroups.get(updateGroupPosition);
                            if (deleteHouse != null) {
                                try {
                                    String userId = preferences.getString("userId", "");
                                    String url = "http://47.98.131.11:8082/warmer/v1.0/house/deleteHouse?userId=" + URLEncoder.encode(userId, "utf-8") + "&houseId=" + deleteHouse.getId();
                                    new DeletHouseAsync().execute(url);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (deviceGroups.size() <= 2) {
                            Utils.showToast(getActivity(), "你确定要无家可归!");
                        }
                        popWindow.dismiss();
                        break;
                    case R.id.btn_cancel_house:
                        popWindow.dismiss();
                        break;
                }
            }
        };

        btn_edit_house.setOnClickListener(listener);
        btn_delete_house.setOnClickListener(listener);
        btn_cancel_house.setOnClickListener(listener);

        ColorDrawable dw = new ColorDrawable(0x30000000);
        popWindow.setBackgroundDrawable(dw);
        popWindow.showAtLocation(view.findViewById(R.id.layout_body), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    class DeletHouseAsync extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
            int code = 0;
            try {
                String url = urls[0];
                String result = HttpUtils.getOkHpptRequest(url);
                if (!Utils.isEmpty(result)) {
                    JSONObject object = new JSONObject(result);
                    code = object.getInt("code");
                    if (code == 2000) {
                        DeviceGroup deviceGroup2 = deviceGroups.get(updateGroupPosition);
                        if (deviceGroup2 != null) {
                            List<DeviceChild> deviceChildren3 = childern.get(updateGroupPosition);
                            if (!deviceChildren3.isEmpty()) {
                                deviceChildDao.deleteGroupDevice(deviceChildren3);
                            }
                            deviceGroupDao.delete(deviceGroup2);
                        }
                        deviceGroups.remove(updateGroupPosition);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    Utils.showToast(getActivity(), "删除住所成功");
                    List<DeviceChild> children = deviceChildDao.findAllDevice();
                    if (children == null || children.isEmpty()) {
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case -3003:
                    Utils.showToast(getActivity(), "删除住所失败");
                    break;
            }
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    /***
     * 创建房间
     */
    private void buildCreateHomeDialog() {
        final DeviceHomeDialog dialog = new DeviceHomeDialog(getActivity());
        dialog.setOnNegativeClickListener(new DeviceHomeDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnPositiveClickListener(new DeviceHomeDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String name = dialog.getName();
                if (Utils.isEmpty(name)) {
                    Utils.showToast(getActivity(), "住所名称不能为空");
                } else {
                    Map<String, Object> params = new HashMap<>();
                    String userId = preferences.getString("userId", "");
                    params.put("houseName", name);
                    params.put("location", location);
                    params.put("userId", userId);
                    new AddHomeAsync().execute(params);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private String houseName;

    /***
     * 修改房间名称
     */
    private void buildUpdateHomeDialog() {
        final DeviceUpdateHomeDialog dialog = new DeviceUpdateHomeDialog(getActivity());
        dialog.setOnNegativeClickListener(new DeviceUpdateHomeDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DeviceUpdateHomeDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                houseName = dialog.getName();
                if (Utils.isEmpty(houseName)) {
                    Utils.showToast(getActivity(), "住所名称不能为空");
                } else {
                    if (updateDeviceGroup != null) {
                        updateDeviceGroup.setHouseName(houseName);
                        new UpdateHomeNameAsync().execute(updateDeviceGroup);
                        dialog.dismiss();
                    }
                }
            }
        });
        dialog.show();
    }

    class AddHomeAsync extends AsyncTask<Map<String, Object>, Void, Integer> {

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];

            String result = HttpUtils.postOkHpptRequest(homeUrl, params);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    JSONObject content = jsonObject.getJSONObject("content");
                    String houseName = content.getString("houseName");
                    String location = content.getString("location");
                    int houseId = content.getInt("id");
                    int masterControllerDeviceId = content.getInt("masterControllerDeviceId");
                    if (code == 2001) {
                        DeviceGroup deviceGroup = new DeviceGroup();
                        deviceGroup.setHeader(houseName + "." + location);
                        deviceGroup.setId((long) houseId);
                        deviceGroup.setLocation(location);
                        deviceGroup.setHouseName(houseName);
                        deviceGroup.setMasterControllerDeviceId(masterControllerDeviceId);
                        DeviceGroup shareDeviceGroup = deviceGroups.get(deviceGroups.size() - 1);
                        childern.remove(deviceGroups.size() - 1);
                        deviceGroups.remove(deviceGroups.size() - 1);

                        deviceGroups.add(deviceGroup);/**添加新住所，但是没有向里面插入子设备*/
                        childern.add(deviceChildDao.findGroupIdAllDevice(deviceGroup.getId()));
                        deviceGroups.add(shareDeviceGroup);/**将分享设备组添加到列表的最后*/
                        childern.add(deviceChildDao.findGroupIdAllDevice(shareDeviceGroup.getId()));
                        deviceGroupDao.insert(deviceGroup);/**添加设备组*/
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
            switch (code) {
                case 2001:
                    Utils.showToast(getActivity(), "创建成功");
                    adapter.notifyDataSetChanged();
                    break;
                case -3001:
                    Utils.showToast(getActivity(), "新建住所失败");
                    break;
            }
        }
    }

    class UpdateHomeNameAsync extends AsyncTask<DeviceGroup, Void, Integer> {

        @Override
        protected Integer doInBackground(DeviceGroup... deviceGroups) {
            int code = 0;
            updateDeviceGroup = deviceGroups[0];
            try {
                String updateHomeUrl = "http://47.98.131.11:8082/warmer/v1.0/house/changeHouseName?houseId=" +
                        URLEncoder.encode(updateDeviceGroup.getId() + "", "UTF-8") + "&houseName=" + URLEncoder.encode(updateDeviceGroup.getHouseName(), "UTF-8");
                String result = HttpUtils.getOkHpptRequest(updateHomeUrl);
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {

                        updateDeviceGroup.setHeader(updateDeviceGroup.getHouseName() + "." + updateDeviceGroup.getLocation());
                        deviceGroupDao.update(updateDeviceGroup);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    Utils.showToast(getActivity(), "修改成功");
                    deviceGroups.set(updateGroupPosition, updateDeviceGroup);
                    adapter.changeHeader(updateGroupPosition);
                    break;
            }
        }
    }

    private void showPickerView() {

        OptionsPickerView pvOptions = new OptionsPickerView.Builder(getActivity(), new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String text = options2Items.get(options1).get(options2);
                location = text;
                if (!Utils.isEmpty(location)) {
                    if ("create".equals(createOrUpdate)) {
                        buildCreateHomeDialog();
                    } else if ("update".equals(createOrUpdate)) {
                        updateDeviceGroup.setLocation(location);
                        new UpdateHomeLocationAsync().execute(updateDeviceGroup);
                    }
                }
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

    @Override
    public void onStop() {
        super.onStop();
        Log.i("DeviceFragment", "-->" + "onStop");
        running = false;
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

    class UpdateHomeLocationAsync extends AsyncTask<DeviceGroup, Void, Integer> {

        @Override
        protected Integer doInBackground(DeviceGroup... deviceGroups) {
            int code = 0;
            updateDeviceGroup = deviceGroups[0];
            try {
                String updateHomeUrl = "http://47.98.131.11:8082/warmer/v1.0/house/changeHouseLocation?houseId=" +
                        URLEncoder.encode(updateDeviceGroup.getId() + "", "UTF-8") + "&houseLocation=" + URLEncoder.encode(updateDeviceGroup.getLocation(), "UTF-8");
                String result = HttpUtils.getOkHpptRequest(updateHomeUrl);
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        updateDeviceGroup.setHeader(updateDeviceGroup.getHouseName() + "." + updateDeviceGroup.getLocation());
                        deviceGroupDao.update(updateDeviceGroup);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    buildUpdateHomeDialog();/**成功，就开始修改住所名称*/
                    break;
                case -3002:
                    Utils.showToast(getActivity(), "修改住所信息失败");
                    break;
            }
        }
    }

    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
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
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
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
                if (location.getErrorCode() == 0) {
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
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("\n");
                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + com.xinrui.location.Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                //解析定位结果，
                String result = sb.toString();
                city = location.getCity();

                String s = location.getProvince();
                if (first && !Utils.isEmpty(s)) {
                    province = s;
                    first = false;
                }
            } else {

            }
        }
    };

    /**
     * 获取GPS状态的字符串
     *
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode) {
        String str = "";
        switch (statusCode) {
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
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
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
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyLocation();

        if (isBound) {
            if (connection != null) {
                getActivity().unbindService(connection);
            }
        }
        VibratorUtil.StopVibrate(getActivity());

        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
        Log.i("onDestroy", "---->" + "onDestroy");
        deviceChildDao.closeDaoSession();
        deviceGroupDao.closeDaoSession();
        deviceGroups.clear();
        childern.clear();
        allListData.clear();
        running = false;
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
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


    /**
     * Created by win7 on 2018/3/12.
     */
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch, R.mipmap.image_switch2};

    private boolean keySwitch = false;
    private static final int MIN_CLICK_DELAY_TIME = 10000;
    private static long lastClickTime = 0;


    class LoadMqttAsync extends AsyncTask<List<DeviceChild>, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(List<DeviceChild>... lists) {
            int code = 0;
            try {
                List<DeviceChild> deviceChildren = lists[0];
                for (int i = 0; i < deviceChildren.size(); i++) {
                    DeviceChild childEntry = deviceChildren.get(i);
                    send(childEntry);
                    Thread.sleep(100);
                    if (i == deviceChildren.size() - 1) {
                        code = 2000;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code == 2000) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private TimeDaoImpl timeDao;

    public class DeviceAdapter extends GroupedRecyclerViewAdapter {

        private Context context;
        private List<DeviceGroup> groups;
        private ImageView image_switch;
        ArrayList<DeviceChild> list;

        //        private DeviceChildDaoImpl deviceChildDao;
        private List<List<DeviceChild>> childern;
        TextView tv_device_child;
        private boolean isPublish = false;
        DeviceChild commonDevice;

        MyRecyclerViewItem myRecyclerViewItem;

        int[] colors = {R.color.color_white, R.color.color_orange};
        private int groupPosition = 0;
        private int childPosition = 0;
        private int mGroupPosition = 0;
        private int mChildPosition = 0;
        Runnable timerRunnable;
        String deviceId;


        class CountTimer extends CountDownTimer {
            public CountTimer(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);
            }

            /**
             * 倒计时过程中调用
             *
             * @param millisUntilFinished
             */
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("Tag", "倒计时=" + (millisUntilFinished / 1000));

                if (progressDialog != null) {
                    progressDialog.setMessage("正在发送数据...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }

//            btn_get_code.setBackgroundColor(Color.parseColor("#c7c7c7"));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
//            btn_get_code.setTextSize(16);
            }

            /**
             * 倒计时完成后调用
             */
            @Override
            public void onFinish() {
                Log.e("Tagsssssss", "倒计时完成");
                //设置倒计时结束之后的按钮样式
//            btn_get_code.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_blue_light));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
//            btn_get_code.setTextSize(18);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        }

        public DeviceAdapter(Context context, List<DeviceGroup> groups, List<List<DeviceChild>> childern) {
            super(context);

            this.context = context;
            this.groups = groups;
            this.childern = childern;
//            deviceChildDao = new DeviceChildDaoImpl(context);
        }

        /**
         * 返回组的项目数
         *
         * @return
         */
        @Override
        public int getGroupCount() {
            return groups == null ? 0 : groups.size();
        }

        /**
         * 返回某一组子条目的数目
         *
         * @param groupPosition
         * @return
         */
        @Override
        public int getChildrenCount(int groupPosition) {
//        ArrayList<DeviceChild> childern=groups.get(groupPosition).getChildern();
//        return childern==null?0:childern.size();

            try {
                return childern.get(groupPosition).size();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        /**
         * 是否有组头
         *
         * @param groupPosition
         * @return
         */
        @Override
        public boolean hasHeader(int groupPosition) {
            return true;
        }

        /**
         * 是否有组尾
         *
         * @param groupPosition
         * @return
         */
        @Override
        public boolean hasFooter(int groupPosition) {
            return true;
        }

        /**
         * 组头布局
         *
         * @param viewType
         * @return
         */
        @Override
        public int getHeaderLayout(int viewType) {
            return R.layout.device_adapter_header;
        }

        /**
         * 组尾布局
         *
         * @param viewType
         * @return
         */
        @Override
        public int getFooterLayout(int viewType) {
            return R.layout.device_adapter_footer;
        }

        /**
         * 某一组中子条目的布局
         *
         * @param viewType
         * @return
         */
        @Override
        public int getChildLayout(int viewType) {
            return R.layout.device_adapter_child;
        }

        /**
         * 绑定组头数据
         *
         * @param holder
         * @param groupPosition
         */


        @Override
        public void onBindHeaderViewHolder(final BaseViewHolder holder, final int groupPosition) {
            final DeviceGroup entry = groups.get(groupPosition);

            if (groupPosition == groups.size() - 1) {
                holder.itemView.setBackgroundResource(R.drawable.shape_header_blue);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.shape_header);
            }
            if (holder != null) {
                holder.setText(R.id.tv_header, entry.getHeader());
                TextView tv_open = (TextView) holder.itemView.findViewById(R.id.tv_open);
                TextView tv_close = (TextView) holder.itemView.findViewById(R.id.tv_close);
                tv_open.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isConn = NetWorkUtil.isConn(MyApplication.getContext());
                        if (isConn) {
                            if (NoFastClickUtils.isFastClick()) {
                                offlineDevices.clear();
                                mDeviceChild = null;
                                int count = 0;
                                isKeySwitch = true;
                                try {
                                    holder.setTextColor(R.id.tv_close, context.getResources().getColor(colors[0]));
                                    holder.setTextColor(R.id.tv_open, context.getResources().getColor(colors[1]));
                                    List<DeviceChild> list = childern.get(groupPosition);
                                    if (list != null && list.size() > 0) {
                                        long millisInFuture = 0;
                                        if (list.size() >= 20) {
                                            millisInFuture = 5000;
                                        }
                                        if (list.size() > 15 && list.size() < 20) {
                                            millisInFuture = 3000;
                                        } else if (list.size() >= 4 && list.size() <= 15) {
                                            millisInFuture = 2000;
                                        } else if (list.size() <= 4) {
                                            millisInFuture = 1000;
                                        }
                                        CountTimer countTimer = new CountTimer(millisInFuture, 1000);
                                        countTimer.start();

                                        List<DeviceChild> deviceChildList = new ArrayList<>();
                                        for (int i = 0; i < list.size(); i++) {
                                            DeviceChild childEntry = list.get(i);
                                            if (childEntry.getType()==1 && childEntry.getOnLint() && childEntry.getDeviceState().equals("close")) {
                                                keySwitch = true;
                                                if (childEntry.getType() == 1) {
                                                    if (childEntry.getControlled() == 2 || childEntry.getControlled() == 0) {
                                                        childEntry.setImg(imgs[1]);
                                                        childEntry.setDeviceState("open");
                                                        deviceChildDao.update(childEntry);
                                                        deviceChildList.add(childEntry);
                                                    }
                                                }
                                            } else {
                                                childEntry.setImg(imgs[0]);
                                            }
                                            if (i == list.size() - 1) {
                                                keySwitch = false;
                                                new LoadMqttAsync().execute(deviceChildList);
//                                            progressDialog.dismiss();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Utils.showToast(context, "请检查你的网络");
                        }
                    }
                });
                tv_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isConn = NetWorkUtil.isConn(MyApplication.getContext());
                        if (isConn) {
                            if (NoFastClickUtils.isFastClick()) {
                                mDeviceChild = null;
                                isKeySwitch = true;
                                offlineDevices.clear();
                                int count = 0;
                                try {
                                    holder.setTextColor(R.id.tv_close, context.getResources().getColor(colors[1]));
                                    holder.setTextColor(R.id.tv_open, context.getResources().getColor(colors[0]));
                                    List<DeviceChild> list = childern.get(groupPosition);
                                    if (list != null && list.size() > 0) {
                                        long millisInFuture = 0;
                                        if (list.size() >= 20) {
                                            millisInFuture = 5000;
                                        }
                                        if (list.size() > 15 && list.size() < 20) {
                                            millisInFuture = 3000;
                                        } else if (list.size() >= 4 && list.size() <= 15) {
                                            millisInFuture = 2000;
                                        } else if (list.size() <= 4) {
                                            millisInFuture = 1000;
                                        }
                                        CountTimer countTimer = new CountTimer(millisInFuture, 1000);
                                        countTimer.start();

                                        List<DeviceChild> deviceChildList = new ArrayList<>();
                                        for (int i = 0; i < list.size(); i++) {
//                                        CountTimer countTimer = new CountTimer(2000, 1000);
//                                        countTimer.start();
                                            DeviceChild childEntry = list.get(i);

                                            if (childEntry.getType()==1 && childEntry.getOnLint() && childEntry.getDeviceState().equals("open")) {
                                                if (childEntry.getType() == 1) {
                                                    if (childEntry.getControlled() == 2 || childEntry.getControlled() == 0) {
                                                        childEntry.setImg(imgs[0]);
                                                        childEntry.setDeviceState("close");
                                                        deviceChildDao.update(childEntry);
                                                        deviceChildList.add(childEntry);
                                                    }
                                                }
                                            }
                                            if (i == list.size() - 1) {
                                                new LoadMqttAsync().execute(deviceChildList);

//                                            adapter.notifyDataSetChanged();
//                                            progressDialog.dismiss();
                                            }

                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Utils.showToast(context, "请检查你的网络");
                        }
                    }
                });
            }


//        tv_open.setTextColor(colors[0]);
        }

        /**
         * 绑定组尾数据
         *
         * @param holder
         * @param groupPosition
         */
        @Override
        public void onBindFooterViewHolder(BaseViewHolder holder, final int groupPosition) {
            ImageView image_footer = (ImageView) holder.itemView.findViewById(R.id.image_footer);
            if (image_footer == null) {
                return;
            }
            image_footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AddDeviceActivity.class);
                    DeviceGroup deviceGroup = groups.get(groupPosition);
                    intent.putExtra("houseId", deviceGroup.getId() + "");

                    if (groupPosition == groups.size() - 1) {
                        intent.putExtra("wifi", "share");
                    } else {
                        intent.putExtra("wifi", "wifi");
                    }
                    startActivityForResult(intent, 6000);
//                    context.startActivity(intent);
                }
            });
        }

        /**
         * 绑定某一组中子条目的数据
         * @param holder
         * @param groupPosition
         * @param childPosition
         */
        /**
         * 绑定某一组中子条目的数据
         *
         * @param holder
         * @param groupPosition
         * @param childPosition
         */

        @Override
        public void onBindChildViewHolder(final BaseViewHolder holder, final int groupPosition, final int childPosition) {

            final DeviceChild entry = childern.get(groupPosition).get(childPosition);
            holder.setText(R.id.tv_device_child, entry.getDeviceName());
            tv_device_child = (TextView) holder.itemView.findViewById(R.id.tv_device_child);
            TextView tv_state = (TextView) holder.itemView.findViewById(R.id.tv_state);
            myRecyclerViewItem= (MyRecyclerViewItem) holder.itemView.findViewById(R.id.scroll_item);
            myRecyclerViewItem.reset();
            if (entry.getOnLint()) {
                if (entry.getType() == 1) {
                    if (entry.getControlled() == 2 || entry.getControlled() == 0) {
//                    tv_state.setText(entry.getRatedPower() + "w");
                        if ("fall".equals(entry.getMachineFall())) {

                            tv_state.setText("设备已倾倒");
                            VibratorUtil.Vibrate(getActivity(), new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}, false);   //震动10s
                        } else {
                            tv_state.setText(entry.getRatedPower() + "w");
                            VibratorUtil.StopVibrate(getActivity());
                        }
                    } else if (entry.getControlled() == 1) {
                        if ("fall".equals(entry.getMachineFall())) {
                            VibratorUtil.Vibrate(getActivity(), new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}, false);   //震动10s
                            tv_state.setText("设备已倾倒");
                        } else {
                            tv_state.setText("受控机模式");
                            VibratorUtil.StopVibrate(getActivity());
                        }
                    }
                    if ("open".equals(entry.getDeviceState())) {
                        if ("fall".equals(entry.getMachineFall())) {
                            holder.setImageResource(R.id.image_switch, imgs[2]);
                        } else {
                            holder.setImageResource(R.id.image_switch, imgs[1]);
                        }
                    } else if ("close".equals(entry.getDeviceState())) {
                        holder.setImageResource(R.id.image_switch, imgs[0]);
                    }
                } else if (entry.getType() == 2) {
                    if ("fall".equals(entry.getMachineFall())) {
                        tv_state.setText("设备已倾倒");
                        VibratorUtil.Vibrate(getActivity(), new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}, true);   //震动10s
                    } else {
                        tv_state.setText("温度：" + entry.getSensorSimpleTemp() + "℃");
                        VibratorUtil.StopVibrate(getActivity());
                    }
                }
            } else {
                tv_state.setText("离线");
                if ("open".equals(entry.getDeviceState())) {
                    holder.setImageResource(R.id.image_switch, imgs[2]);
                } else {
                    holder.setImageResource(R.id.image_switch, imgs[0]);
                }
            }

            if (entry.getType() == 1) {
                if (entry.getControlled() == 2) {
                    holder.setImageResource(R.id.image_device_child, R.mipmap.master);
                    holder.setVisible(R.id.image_switch, View.VISIBLE);
                } else if (entry.getControlled() == 1) {
                    holder.setImageResource(R.id.image_device_child, R.mipmap.controlled);
                    holder.setVisible(R.id.image_switch, View.GONE);
                } else if (entry.getControlled() == 0) {
                    holder.setImageResource(R.id.image_device_child, R.mipmap.heater2);
                    holder.setVisible(R.id.image_switch, View.VISIBLE);
                }
            } else if (entry.getType() == 2) {
                holder.setImageResource(R.id.image_device_child, R.mipmap.estsensor);
                holder.setVisible(R.id.image_switch, View.GONE);
            }

            tv_device_child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int type = entry.getType();
                    if (type == 2) {
                        Intent intent = new Intent(getActivity(), SmartTerminalActivity.class);
                        long deviceId = entry.getId();
                        intent.putExtra("deviceId", deviceId);
                        startActivityForResult(intent, 6000);
                    } else {
                        if (entry.getOnLint()) {
                            if (entry.getType() == 1) {
                                if (entry.getControlled() == 2 || entry.getControlled() == 0) {
                                    DeviceChild deviceChild = childern.get(groupPosition).get(childPosition);
                                    long id = deviceChild.getId();
                                    Intent intent = new Intent(context, DeviceListActivity.class);
                                    intent.putExtra("content", deviceChild.getDeviceName());
                                    intent.putExtra("childPosition", id + "");
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("loadDate", "7");
                                        String s = jsonObject.toString();
                                        String mac = deviceChild.getMacAddress();
                                        String topic = "rango/" + mac + "/set";
                                        int count = timeDao.findAll(deviceChild.getId()).size();
                                        if (mqService != null && count != 168) {
                                            boolean success = false;
                                            Log.i("ggggggggg", "-->" + "ggggggggggggggggg");
                                            success = mqService.publish(topic, 1, s);
                                            if (!success) {
                                                success = mqService.publish(topic, 1, s);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
//                                    context.startActivity(intent);
                                    startActivityForResult(intent, 6000);
                                } else if (entry.getControlled() == 1) {
                                    Utils.showToast(context, "受控机不能操作");
                                }
                            } else if (entry.getType() == 2) {
                                Utils.showToast(context, "外置传感器不能操作");
                            }
                        } else {
                            try {
                                String mac = entry.getMacAddress();
                                String topic = "rango/" + mac + "/set";
                                Log.i("macAddress2", "-->" + mac);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("loadDate", "1");
                                String s = jsonObject.toString();
                                boolean success = false;
                                success = mqService.publish(topic, 1, s);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Utils.showToast(context, "该设备离线");
                        }
                    }

                }
            });
            String mac = entry.getMacAddress();
            image_switch = (ImageView) holder.itemView.findViewById(R.id.image_switch);
            image_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NoFastClickUtils.isFastClick()) {
                        if (entry.getOnLint()) {
                            String mac = entry.getMacAddress();
                            if (entry.getImg() == imgs[0]) {
                                if (bound) {
                                    try {
                                        entry.setImg(imgs[1]);
                                        entry.setDeviceState("open");
                                        deviceChildDao.update(entry);
                                        mDeviceChild = entry;
                                        if (entry.getType() == 1) {
                                            if (entry.getControlled() == 2 || entry.getControlled() == 0) {
                                                send(entry);
                                            }
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if (entry.getImg() == imgs[1]) {
                                if (bound) {
                                    try {
                                        entry.setImg(imgs[0]);
                                        entry.setDeviceState("close");
                                        deviceChildDao.update(entry);
                                        mDeviceChild = entry;
                                        if (entry.getType() == 1) {
                                            if (entry.getControlled() == 2 || entry.getControlled() == 0) {
                                                send(entry);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
//                holder.setImageResource(R.id.image_switch,img);
//                            changeChild(groupPosition, childPosition);
                            notifyDataSetChanged();
                        } else {
                            Utils.showToast(context, "该设备离线");
                        }
                    } else {
                        Utils.showToast(context, "主人，请对我温柔点!");
                    }
                }
            });
            Button btn_editor = (Button) holder.itemView.findViewById(R.id.btn_editor);
            btn_editor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buildDialog(groupPosition, childPosition);
                }
            });
            Button btn_delete = (Button) holder.itemView.findViewById(R.id.btn_delete);
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DeviceAdapter.this.groupPosition = groupPosition;
                    DeviceAdapter.this.childPosition = childPosition;
                    new DeviceAdapter.DeleteDeviceAsync().execute(entry);
                }
            });

//            MyRecyclerViewItem myRecyclerViewItem = (MyRecyclerViewItem) holder.itemView.findViewById(R.id.scroll_item);
//            myRecyclerViewItem.reset();
        }

        private void buildDialog(final int groupPosition, final int childPosition) {
            final DeviceChildDialog dialog = new DeviceChildDialog(context);
            dialog.setOnPositiveClickListener(new DeviceChildDialog.OnPositiveClickListener() {
                @Override
                public void onPositiveClick() {
                    String child = dialog.getName();
                    if (!Utils.isEmpty(child)) {
                        DeviceChild deviceChild = childern.get(groupPosition).get(childPosition);
                        deviceChild.setDeviceName(child);
                        DeviceAdapter.this.groupPosition = groupPosition;
                        DeviceAdapter.this.childPosition = childPosition;
                        new DeviceAdapter.UpdateDeviceNameAsync().execute(deviceChild);
                        dialog.dismiss();
                    } else {
                        Utils.showToast(context, "设备名称不能为空");
                    }
                }
            });
            dialog.setOnNegativeClickListener(new DeviceChildDialog.OnNegativeClickListener() {
                @Override
                public void onNegativeClick() {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        DeviceChild deviceChild=null;
        class UpdateDeviceNameAsync extends AsyncTask<DeviceChild, Void, Integer> {

            @Override
            protected Integer doInBackground(DeviceChild... deviceChildren) {
                int code = 0;
                deviceChild = deviceChildren[0];
                try {
                    String updateDeviceNameUrl = "http://47.98.131.11:8082/warmer/v1.0/device/changeDeviceName?deviceId=" +
                            URLEncoder.encode(deviceChild.getId() + "", "UTF-8") + "&newName=" + URLEncoder.encode(deviceChild.getDeviceName(), "UTF-8");
                    String result = HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        deviceChildDao.update(deviceChild);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return code;
            }

            @Override
            protected void onPostExecute(Integer code) {
                super.onPostExecute(code);
                switch (code) {
                    case 2000:
                        DeviceChild deviceChild2=childern.get(groupPosition).get(childPosition);
                        if (deviceChild2!=null && deviceChild!=null){
                            childern.get(groupPosition).set(childPosition,deviceChild);
                        }
                        Utils.showToast(context, "修改成功");
                        adapter.notifyDataSetChanged();
                        break;
                    case -3009:
                        Utils.showToast(context, "修改失败");
                        break;
                }
            }
        }

        class DeleteDeviceAsync extends AsyncTask<DeviceChild, Void, Integer> {
            @Override
            protected Integer doInBackground(DeviceChild... deviceChildren) {
                int code = 0;
                DeviceChild deviceChild = deviceChildren[0];
                try {
                    String houseId = null;
                    if (Long.MAX_VALUE == deviceChild.getHouseId()) {
                        houseId = deviceChild.getShareHouseId() + "";
                    } else {
                        houseId = deviceChild.getHouseId() + "";
                    }

                    SharedPreferences preferences = context.getSharedPreferences("my", Context.MODE_PRIVATE);
                    String userId = preferences.getString("userId", "");
                    String updateDeviceNameUrl = "http://47.98.131.11:8082/warmer/v1.0/device/deleteDevice?deviceId=" +
                            URLEncoder.encode(deviceChild.getId() + "", "UTF-8") + "&userId=" + URLEncoder.encode(userId, "UTF-8")
                            + "&houseId=" + URLEncoder.encode(houseId, "UTF-8");
//                String updateDeviceNameUrl="http://192.168.168.3:8082/warmer/v1.0/device/deleteDevice?deviceId=6&userId=1&houseId=1000";
//                String updateDeviceNameUrl="http://192.168.168.10:8082/warmer/v1.0/device/deleteDevice?deviceId=1004&userId=1&&houseId=1001";
                    String result = HttpUtils.getOkHpptRequest(updateDeviceNameUrl);
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        if (mDeviceChild != null && mDeviceChild.getMacAddress().equals(deviceChild.getMacAddress())) {
                            mDeviceChild = null;
                        }
//                        TimeTaskDaoImpl timeTaskDao = new TimeTaskDaoImpl(context);
//                        TimeDaoImpl timeDao = new TimeDaoImpl(context);
//                        List<TimeTask> timeTasks = timeTaskDao.findTimeTasks(deviceChild.getId());
//                        for (TimeTask timeTask : timeTasks) {
//                            timeTaskDao.delete(timeTask);
//                        }
//                        List<Timer> timers = timeDao.findAll(deviceChild.getId());
//                        for (Timer timer : timers) {
//                            timeDao.delete(timer);
//                        }

                        deviceChildDao.delete(deviceChild);
                        String macAddress = deviceChild.getMacAddress();
                        String topicName = "rango/" + macAddress + "/transfer";
                        boolean success = mqService.publish(topicName, 1, "reSet");
                        String topicOffline = "rango/" + macAddress + "/lwt";
                        String topicShare = "rango/" + macAddress + "/refresh";
                        mqService.unsubscribe(topicName);
                        mqService.unsubscribe(topicOffline);
                        mqService.unsubscribe(topicShare);
                        Log.i("delete", "-->" + success);
                        childern.get(groupPosition).remove(childPosition);
                        if (deviceChild.getType() == 1 && deviceChild.getControlled() == 2) {
                            deviceChild.setCtrlMode("normal");
                            send(deviceChild);
                            List<DeviceChild> deviceChildren2 = deviceChildDao.findGroupIdAllDevice(deviceChild.getHouseId());
                            for (DeviceChild deviceChild2 : deviceChildren2) {
                                if (deviceChild2.getType() == 1 && deviceChild2.getControlled() == 1) {
                                    deviceChild2.setControlled(0);
                                    deviceChild2.setCtrlMode("normal");
                                    deviceChildDao.update(deviceChild2);
                                    Log.i("controlled22222222", "-->" + deviceChild.getType() + "," + deviceChild.getControlled());
                                    send(deviceChild2);
                                }
                            }
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return code;
            }

            @Override
            protected void onPostExecute(Integer code) {
                super.onPostExecute(code);
                switch (code) {
                    case 2000:
                        Utils.showToast(context, "解除设备成功");
                        List<DeviceChild> children = deviceChildDao.findAllDevice();
                        if (children != null && children.isEmpty() && groups.size() == 2) {
                            context.startActivity(new Intent(context, MainActivity.class));
                        } else {
                            notifyDataSetChanged();
                        }
                        break;
                    case -3009:
//                        progressDialog.dismiss();
                        preferences.edit().remove("login").commit();
                        Utils.showToast(context, "解除设备失败");
                        preferences.edit().remove("login").commit();
//                        context.startActivity(new Intent(context, MainActivity.class));
                        break;
                }
            }
        }
    }

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
                maser.put("grade", deviceChild.getGrade());
                maser.put("timerShutDown",deviceChild.getTimerShutdown());
                String s = maser.toString();
                boolean success = false;
                String topicName;

                String mac = deviceChild.getMacAddress();
                topicName = "rango/" + mac + "/set";
                Log.i("mac", "-->" + mac);
                if (bound) {
                    success = mqService.publish(topicName, 1, s);
                    Log.i("suss", "-->" + success);
                    Log.i("deviceFragment", "-->" + s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean bound = false;
    MQService mqService;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
            Log.i("load", "-->" + load);
            if (!Utils.isEmpty(load)) {
                List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();
                new LoadMqttAsync3().execute(deviceChildren);
            }
            if (!Utils.isEmpty(deviceId)) {
                try {
                    if (!Utils.isEmpty(deviceId)) {
                        long id = Long.parseLong(deviceId);
                        DeviceChild deviceChild2 = deviceChildDao.findDeviceById(id);
                        if (deviceChild2 != null) {
                            String mac = deviceChild2.getMacAddress();
                            String topicName2 = "rango/" + mac + "/transfer";
                            String topicOffline = "rango/" + mac + "/lwt";
                            boolean succ = mqService.subscribe(topicName2, 1);
                            succ = mqService.subscribe(topicOffline, 1);
                            if (succ) {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("loadDate", "1");
                                String s = jsonObject.toString();
                                String topicName = "rango/" + mac + "/set";

                                boolean success = mqService.publish(topicName, 1, s);
                                if (success)
                                    if (!success) {
                                        success = mqService.publish(topicName, 1, s);
                                    }
                                Log.i("sss", "-->" + success);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                DeviceChild child = null;
                int groupPostion = intent.getIntExtra("groupPostion", 0);
                Log.i("group", "-->" + groupPostion);
                int childPosition = intent.getIntExtra("childPosition", 0);
                Log.i("childPosition", "-->" + childPosition);
                String macAddress = intent.getStringExtra("macAddress");
                String deviceState = intent.getStringExtra("deviceState");
                String noNet = intent.getStringExtra("noNet");
                Log.i("noNet", "-->:" + noNet);
//                String Net = intent.getStringExtra("Net");
                String refresh = intent.getStringExtra("refresh");
                String macAddress2 = intent.getStringExtra("macAddress2");
                DeviceChild deviceChild20 = (DeviceChild) intent.getSerializableExtra("deviceChild2");
                if (!Utils.isEmpty(refresh) && !Utils.isEmpty(macAddress2) && deviceChild20 != null) {
                    int groupPostion2 = intent.getIntExtra("groupPostion", 0);
                    List<DeviceChild> childList = childern.get(groupPostion2);
                    for (int i = 0; i < childList.size(); i++) {
                        DeviceChild deviceChild2 = childList.get(i);
                        if (macAddress2.equals(deviceChild2.getMacAddress())) {
                            childern.get(groupPostion).set(i, deviceChild20);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    if (!Utils.isEmpty(noNet)) {
                        for (int i = 0; i < deviceGroups.size(); i++) {
                            List<DeviceChild> deviceChildren = childern.get(i);
                            for (int j = 0; j < deviceChildren.size(); j++) {
                                DeviceChild deviceChild = deviceChildren.get(j);
                                deviceChild.setOnLint(false);
//                                if ("open".equals(deviceChild.getDeviceState())) {
//                                    deviceChild.setImg(imgs[2]);
//                                } else if ("close".equals(deviceChild.getDeviceState())) {
//                                    deviceChild.setImg(imgs[0]);
//                                }
                                childern.get(i).set(j, deviceChild);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else if (Utils.isEmpty(noNet)) {
                        DeviceChild deviceChild = (DeviceChild) intent.getSerializableExtra("deviceChild");
                        if (!Utils.isEmpty(macAddress) && deviceChild == null) {
                            if (groupPostion < deviceGroups.size()) {
                                List<DeviceChild> deviceChildren = childern.get(groupPostion);
                                String remove = "";
                                for (int i = 0; i < deviceChildren.size(); i++) {
                                    DeviceChild deviceChild2 = deviceChildren.get(i);
                                    if (deviceChild2 != null && macAddress.equals(deviceChild2.getMacAddress())) {
                                        if (mDeviceChild != null && mDeviceChild.getMacAddress().equals(deviceChild2.getMacAddress())) {
                                            mDeviceChild = null;
                                        }
                                        childern.get(groupPostion).remove(deviceChild2);

                                        Utils.showToast(context, "该设备已重置");
                                        List<DeviceChild> children = deviceChildDao.findAllDevice();
                                        if (children != null && children.isEmpty() && deviceGroups.size() == 2) {
                                            context.startActivity(new Intent(context, MainActivity.class));
                                        } else {
                                            adapter.notifyDataSetChanged();
                                        }
                                        break;
                                    }
                                }

                            }
                        }
                        if (deviceChild != null) {
                            if (groupPostion < deviceGroups.size()) {
                                List<DeviceChild> childList = childern.get(groupPostion);
//                                Collections.sort(childList, new Comparator<DeviceChild>() {
//                                    @Override
//                                    public int compare(DeviceChild o1, DeviceChild o2) {
//                                        if (o1.getId() > o2.getId())
//                                            return 1;
//                                        if (o1.getId() == o2.getId())
//                                            return 0;
//                                        return -1;
//                                    }
//                                });
                                for (int i = 0; i < childList.size(); i++) {
                                    DeviceChild deviceChild2 = childList.get(i);
                                    if (deviceChild.getMacAddress().equals(deviceChild2.getMacAddress())) {
                                        childern.get(groupPostion).set(i, deviceChild);
                                        adapter.notifyDataSetChanged();
//                                        adapter.changeChild(groupPostion,i);
                                        break;
                                    }
                                }
//
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.i("Exception", "-->" + "jghuiop");
                e.printStackTrace();
            }
        }
    }

    class LoadMqttAsync2 extends AsyncTask<List<String>, Void, Void> {


        @Override
        protected Void doInBackground(List<String>... lists) {
            List<String> deviceChildren = lists[0];
            try {
                if (NetWorkUtil.isConn(getActivity())) {
                    for (int i = 0; i < deviceChildren.size(); i++) {
                        String mac = deviceChildren.get(i);
                        if (mqService != null) {
                            try {

                                String topic = "rango/" + mac + "/set";
                                Log.i("macAddress2", "-->" + mac);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("loadDate", "1");
                                String s = jsonObject.toString();
                                boolean success = false;
                                success = mqService.publish(topic, 1, s);
                                if (!success) {
                                    success = mqService.publish(topic, 1, s);
                                }
                                if (success) {
                                    Log.i("macAddress3", "-->" + mac);
                                    Thread.sleep(200);
//                                Thread.currentThread().sleep(300);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    boolean isRight=false;
}