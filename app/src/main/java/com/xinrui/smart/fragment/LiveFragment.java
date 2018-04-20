package com.xinrui.smart.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.RoomEntryDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.CustomRoomActivity;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.adapter.DeviceAdapter;
import com.xinrui.smart.adapter.FragmentViewPagerAdapter;
import com.xinrui.smart.adapter.Switch_houseAdapter;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.Room;
import com.xinrui.smart.pojo.RoomEntry;
import com.xinrui.smart.util.CommonUtil;
import com.xinrui.smart.util.GetUrl;
import com.xinrui.smart.util.OnItemClickListener;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/19.
 */

public class LiveFragment extends Fragment implements OnItemClickListener {
    @BindView(R.id.custom_house_type)
    Button customHouseType;
    //    @BindView(R.id.copy_and_paste)
//    Button copyAndPaste;
    @BindView(R.id.delete)
    ImageButton delete;
    @BindView(R.id.btn1)
    Button btn1;
    @BindView(R.id.btn2)
    Button btn2;
    @BindView(R.id.btn3)
    Button btn3;
    @BindView(R.id.btn4)
    Button btn4;
    @BindView(R.id.new_btn)
    Button btn5;
    @BindView(R.id.houseId)
    TextView houseId;
    Dialog dialog;
    GetUrl getUrl = new GetUrl();
    DeviceGroupDaoImpl deviceGroupDao;
    List<DeviceGroup> DeviceGroup;
    Bundle savedState;

    View view1;
    int postion_current;
    RecyclerView mRecyclerView;
    @BindView(R.id.temperature)
    TextView temperature;
    @BindView(R.id.air_quality)
    TextView airQuality;
    @BindView(R.id.humidity)
    TextView humidity;
    @BindView(R.id.city)
    TextView city;
    private ImageView drawing_room, bedroom, toilet, study;
    Long house_id;
    private ViewPager viewPager;


    private View one_pager, two_pager, three_pager, four_pager;

    private List<Fragment> fragmentslist;

    public int current_key = 1;
    private int add_key = 1;
    private boolean isestablied = false;
    private List<HashMap<String, Object>> dataSourceList = new ArrayList<>();

    private List<Room> roomList = new ArrayList<>();

    Boolean isFirstIn = false;

    private String house_Name;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    FragmentViewPagerAdapter fragmentViewPagerAdapter;

    Btn1_fragment btn1_fragment;
    Btn2_fragment btn2_fragment;
    Btn3_fragment btn3_fragment;
    Btn4_fragment btn4_fragment;

    SharedPreferences.Editor editor1;
    SharedPreferences sharedPreferences;
    SharedPreferences pref;
    RoomEntryDaoImpl roomEntryDao;
    int item_width;
    View view;
    Unbinder unbinder;
    private ArrayAdapter<String> adapter;
    Switch_houseAdapter switch_houseAdapter;
    String location;//住所地址

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_live, container, false);
        unbinder = ButterKnife.bind(this, view);
        roomEntryDao = new RoomEntryDaoImpl(getActivity());
        pref = getActivity().getSharedPreferences("myActivityName", 0);
        editor1 = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        //取得相应的值，如果没有该值，说明还未写入，用true作为默认值
        isFirstIn = pref.getBoolean("isFirstIn", true);
        initData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        item_width = wm.getDefaultDisplay().getWidth() / 4;
        fragmentslist = new ArrayList<>();
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        getActivity().getSupportFragmentManager().findFragmentByTag("");
        initView();
    }

    private void initData() {
        deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
        DeviceGroup = deviceGroupDao.findAllDevices();
        if (DeviceGroup == null || DeviceGroup.isEmpty()) {

        } else {
            house_id = DeviceGroup.get(0).getId();
            house_Name = DeviceGroup.get(0).getHouseName() + "(" + house_id + ")";
            editor1.putString("house_Name", house_Name);
            editor1.putLong("house_id", house_id);
        }
        SharedPreferences pref1 = getActivity().getSharedPreferences("myActivityName", 0);
        SharedPreferences.Editor editor = pref1.edit();
        editor.putBoolean("isFirstIn", false);
        editor.commit();
    }

    @Override
    public void onPause() {
        savedState();
        super.onPause();
    }

    //初始化View
    public void initView() {
        fragmentViewPagerAdapter = new FragmentViewPagerAdapter(
                getChildFragmentManager(), fragmentslist);
        btn1_fragment = new Btn1_fragment();
        fragmentslist.add(btn1_fragment);
        viewPager = (ViewPager) view.findViewById(R.id.fragment_viewPager);
        viewPager.setAdapter(fragmentViewPagerAdapter);
        saveViewPage();
        restore_Data();
        if (isFirstIn) {
            showDialog();
        }
    }

    //初始化viewpage
    public void saveViewPage() {

        //viewPager滑动监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                Button btns[] = {btn1, btn2, btn3, btn4};
                int f = fragmentslist.size();
                for (int i = 0; i < fragmentslist.size(); i++) {
                    if (position == i) {
                        btns[i].setBackgroundResource(R.drawable.new_floor_button_colour);
                        btns[i].setTextColor(getResources().getColor(R.color.white));
                    } else {
                        btns[i].setBackgroundResource(R.drawable.floor_button_colour);
                        btns[i].setTextColor(Color.WHITE);
                    }
                }
                current_key = position + 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
    }

    /**
     * 切换房间的diaolog时监听
     *
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        house_id = DeviceGroup.get(position).getId();
        DeviceGroup deviceGroup = deviceGroupDao.findById(house_id);
        house_Name = deviceGroup.getHouseName() + "(" + house_id + ")";
        location = deviceGroup.getLocation().replace("市", "");
        houseId.setText(house_Name);
        WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask();
        weatherAsyncTask.execute();
        cut_houseId();
        viewPager.setCurrentItem(0);
        dialog.dismiss();
    }

    public void cut_houseId() {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        List<String> strings = (List<String>) msg.obj;
                        for (String result : strings) {
                            try {
                                btn1_fragment = new Btn1_fragment();
                                btn2_fragment = new Btn2_fragment();
                                btn3_fragment = new Btn3_fragment();
                                btn4_fragment = new Btn4_fragment();
                                JSONObject jsonObject = new JSONObject(result);
                                int code = jsonObject.getInt("code");
                                if (code == 2000) {
                                    fragmentslist.clear();
                                    JSONObject content = jsonObject.getJSONObject("content");
                                    int floor = content.length();
                                    if (floor == 1) {
                                        fragmentslist.add(btn1_fragment);
                                        btn1.setVisibility(View.VISIBLE);
                                        btn2.setVisibility(View.GONE);
                                        btn3.setVisibility(View.GONE);
                                        btn4.setVisibility(View.GONE);
                                    } else if (floor == 2) {
                                        fragmentslist.add(btn1_fragment);
                                        fragmentslist.add(btn2_fragment);
                                        btn1.setVisibility(View.VISIBLE);
                                        btn2.setVisibility(View.VISIBLE);
                                        btn3.setVisibility(View.GONE);
                                        btn4.setVisibility(View.GONE);
                                    } else if (floor == 3) {
                                        fragmentslist.add(btn1_fragment);
                                        fragmentslist.add(btn2_fragment);
                                        fragmentslist.add(btn3_fragment);
                                        btn1.setVisibility(View.VISIBLE);
                                        btn2.setVisibility(View.VISIBLE);
                                        btn3.
                                                setVisibility(View.VISIBLE);
                                        btn4.setVisibility(View.GONE);
                                    } else if (floor == 4) {
                                        fragmentslist.add(btn1_fragment);
                                        fragmentslist.add(btn2_fragment);
                                        fragmentslist.add(btn3_fragment);
                                        fragmentslist.add(btn4_fragment);
                                        btn1.setVisibility(View.VISIBLE);
                                        btn2.setVisibility(View.VISIBLE);
                                        btn3.setVisibility(View.VISIBLE);
                                        btn4.setVisibility(View.VISIBLE);
                                    }
                                    add_key = floor;
                                    current_key = 1;
                                    btn1.setBackgroundResource(R.drawable.new_floor_button_colour);
                                    btn2.setBackgroundResource(R.drawable.floor_button_colour);
                                    btn3.setBackgroundResource(R.drawable.floor_button_colour);
                                    btn4.setBackgroundResource(R.drawable.floor_button_colour);
//                                    viewPager.setCurrentItem(0);
                                    savedState();//切换房间后保存此时的数据,显示当前住所的信息
                                    fragmentViewPagerAdapter.notifyDataSetChanged();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            JSONArray jsonArray = new JSONArray();

                            new DeleteRoomAsyncTask().execute(jsonArray);
                        }

                        break;

                    default:
                        break;
                }
            }

        };
        QueryAllRoomAsyncTask1 queryAllRoomAsyncTask1 = new QueryAllRoomAsyncTask1(handler);

        queryAllRoomAsyncTask1.execute();
    }

    class NewRoomAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            int code = 0;
            deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
            DeviceGroup = deviceGroupDao.findAllDevices();
            Map<String, Object> params = new HashMap<>();
            params.put("houseId", house_id);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/house/createLayer", params);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        JSONArray content = jsonObject.getJSONArray("content");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
            return code;
        }
    }

    class DeleteRoomAsyncTask extends AsyncTask<JSONArray, Void, Integer> {

        @Override
        protected Integer doInBackground(JSONArray... s) {
            int code = 0;
            JSONArray request = s[0];
            String url = "http://120.77.36.206:8082/warmer/v1.0/room/deleteRoom";
            String result = HttpUtils.doDelete(url, request);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        JSONArray content = jsonObject.getJSONArray("content");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }
    }

    class QueryAllRoomAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            int code = 0;
            deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
            DeviceGroup = deviceGroupDao.findAllDevices();
            Map<String, Object> params = new HashMap<>();
            params.put("houseId", house_id);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/findAllRoom", params);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject();
                    if (code == 2000) {
                        JSONArray content = jsonObject.getJSONArray("content");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
            return code;
        }
    }

    class QueryAllRoomAsyncTask1 extends AsyncTask<Void, Void, List<String>> {
        Handler mHandler;

        public QueryAllRoomAsyncTask1(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            int code = 0;
//            String result = null;
            List<String> strings = new ArrayList<>();
            deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
            DeviceGroup = deviceGroupDao.findAllDevices();
            Map<String, Object> params = new HashMap<>();
            params.put("houseId", house_id);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/findAllRoom", params);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                if (!Utils.isEmpty(result)) {
                    strings.add(result);
                    JSONObject jsonObject = new JSONObject();
                    if (code == 2000) {
                        JSONArray content = jsonObject.getJSONArray("content");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
            return strings;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            Message msg = mHandler.obtainMessage();
            if (strings != null && !strings.isEmpty()) {
                msg.what = 1;
                msg.obj = strings;
            } else {
                msg.what = 2;
            }
            mHandler.sendMessage(msg);
        }
    }

    @OnClick({R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.new_btn, R.id.custom_house_type, R.id.delete, R.id.houseId})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                method_btn_1();
                break;
            case R.id.btn2:
                method_btn_2();
                break;
            case R.id.btn3:
                method_btn_3();
                break;
            case R.id.btn4:
                method_btn_4();
                break;
            case R.id.new_btn:
                method_new_btn();
                if (house_id == null) {

                } else {
                    new NewRoomAsyncTask().execute();
                }
                break;
            case R.id.custom_house_type:
                savedState();
//                setRoomType();
                From_server_make_room();
                break;
//            case R.id.copy_and_paste:
//                method_copy_paste_btn();
//                break;
            case R.id.delete:
                final List<Integer> startPoint_list = new ArrayList<>();
                final List<Integer> roomId_list = new ArrayList<>();
                if (current_key == 1) {
                    Btn1_fragment btn1_fragment = (Btn1_fragment) fragmentViewPagerAdapter.getmCurrentFragment();
                    if (btn1_fragment.getListViews().isEmpty() || btn1_fragment.getListViews() == null) {

                    } else {
                        for (int i = 0; i < btn1_fragment.getListViews().size(); i++) {
                            View childView = btn1_fragment.getListViews().get(i);
                            int startPoint = (int) (childView.getX() / item_width) + (int) (childView.getY() / item_width) * 4;
                            startPoint_list.add(startPoint);
                            Log.i("startPoint", "childView.getX()=" + childView.getX() + ";" + "childView.getY()=" + childView.getY() + ";" + "startPoint=" + startPoint);
                            FrameLayout roomViewGroup = (FrameLayout) btn1_fragment.getView().findViewById(R.id.fl);
                            roomViewGroup.removeView(childView);

                            RoomEntry roomEntry = new RoomEntry((int) childView.getX(), (int) childView.getY(), childView.getWidth(), childView.getHeight());
                            for (int j = 0; j < roomEntryDao.findAllByGroup(1).size(); j++) {
                                if ((roomEntry.getX() == roomEntryDao.findAllByGroup(1).get(j).getX()) && (roomEntry.getY() == roomEntryDao.findAllByGroup(1).get(j).getY()) && (roomEntry.getWidth() == roomEntryDao.findAllByGroup(1).get(j).getWidth()) && (roomEntry.getHeight() == roomEntryDao.findAllByGroup(1).get(j).getHeight())) {
                                    roomEntryDao.delete(roomEntryDao.findAllByGroup(1).get(j));
                                }
                            }
                        }
                    }
                } else if (current_key == 2) {
                    Log.i("uuu2", "dsf");
                    Btn2_fragment btn2_fragment = (Btn2_fragment) fragmentViewPagerAdapter.getmCurrentFragment();
                    for (int i = 0; i < btn2_fragment.getListViews().size(); i++) {
                        if (btn2_fragment.getListViews().isEmpty() || btn2_fragment.getListViews() == null) {

                        } else {
                            View childView = btn2_fragment.getListViews().get(i);
                            int startPoint = (int) (childView.getX() / item_width) + (int) (childView.getY() / item_width) * 4;
                            startPoint_list.add(startPoint);
                            Log.i("btn2_fragment", btn2_fragment.getListViews().size() + "");
                            FrameLayout roomViewGroup = (FrameLayout) btn2_fragment.getView().findViewById(R.id.f2);
                            roomViewGroup.removeView(childView);
//                        RoomEntry roomEntry_list = new RoomEntry((int) childView.getX(),(int) childView.getY(),childView.getWidth(),childView.getHeight());
//                        roomEntryDao.delete(roomEntry_list);

                            RoomEntry roomEntry = new RoomEntry((int) childView.getX(), (int) childView.getY(), childView.getWidth(), childView.getHeight());
                            for (int j = 0; j < roomEntryDao.findAllByGroup(2).size(); j++) {
                                if ((roomEntry.getX() == roomEntryDao.findAllByGroup(2).get(j).getX()) && (roomEntry.getY() == roomEntryDao.findAllByGroup(2).get(j).getY()) && (roomEntry.getWidth() == roomEntryDao.findAllByGroup(2).get(j).getWidth()) && (roomEntry.getHeight() == roomEntryDao.findAllByGroup(2).get(j).getHeight())) {
                                    roomEntryDao.delete(roomEntryDao.findAllByGroup(2).get(j));
                                }
                            }
                        }
                    }
                } else if (current_key == 3) {
                    Log.i("uuu3", "dsf");
                    Btn3_fragment btn3_fragment = (Btn3_fragment) fragmentViewPagerAdapter.getmCurrentFragment();
                    for (int i = 0; i < btn3_fragment.getListViews().size(); i++) {
                        if (btn3_fragment.getListViews().isEmpty() || btn3_fragment.getListViews() == null) {

                        } else {
                            View childView = btn3_fragment.getListViews().get(i);
                            int startPoint = (int) (childView.getX() / item_width) + (int) (childView.getY() / item_width) * 4;
                            startPoint_list.add(startPoint);
                            FrameLayout roomViewGroup = (FrameLayout) btn3_fragment.getView().findViewById(R.id.f3);
                            roomViewGroup.removeView(childView);
                            Log.i("uuu33", "dsf" + ";" + roomEntryDao.findAllByGroup(3).size());
                            RoomEntry roomEntry = new RoomEntry((int) childView.getX(), (int) childView.getY(), childView.getWidth(), childView.getHeight());
                            for (int j = 0; j < roomEntryDao.findAllByGroup(3).size(); j++) {
                                if ((roomEntry.getX() == roomEntryDao.findAllByGroup(3).get(j).getX()) && (roomEntry.getY() == roomEntryDao.findAllByGroup(3).get(j).getY()) && (roomEntry.getWidth() == roomEntryDao.findAllByGroup(3).get(j).getWidth()) && (roomEntry.getHeight() == roomEntryDao.findAllByGroup(3).get(j).getHeight())) {
                                    roomEntryDao.delete(roomEntryDao.findAllByGroup(3).get(j));
                                    Log.i("uuu333", "dsf");
                                }
                            }
                        }
                    }
                } else if (current_key == 4) {
                    Log.i("uuu4", "dsf");
                    Btn4_fragment btn4_fragment = (Btn4_fragment) fragmentViewPagerAdapter.getmCurrentFragment();
                    for (int i = 0; i < btn4_fragment.getListViews().size(); i++) {
                        if (btn4_fragment.getListViews().isEmpty() || btn4_fragment.getListViews() == null) {

                        } else {
                            View childView = btn4_fragment.getListViews().get(i);
                            int startPoint = (int) (childView.getX() / item_width) + (int) (childView.getY() / item_width) * 4;
                            startPoint_list.add(startPoint);
                            FrameLayout roomViewGroup = (FrameLayout) btn4_fragment.getView().findViewById(R.id.f4);
                            roomViewGroup.removeView(childView);

                            RoomEntry roomEntry = new RoomEntry((int) childView.getX(), (int) childView.getY(), childView.getWidth(), childView.getHeight());
                            for (int j = 0; j < roomEntryDao.findAllByGroup(4).size(); j++) {
                                if ((roomEntry.getX() == roomEntryDao.findAllByGroup(4).get(j).getX()) && (roomEntry.getY() == roomEntryDao.findAllByGroup(4).get(j).getY()) && (roomEntry.getWidth() == roomEntryDao.findAllByGroup(4).get(j).getWidth()) && (roomEntry.getHeight() == roomEntryDao.findAllByGroup(4).get(j).getHeight())) {
                                    roomEntryDao.delete(roomEntryDao.findAllByGroup(4).get(j));
                                }
                            }
                        }
                    }
                }
                @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 1:
                                List<String> strings = (List<String>) msg.obj;
                                for (String result : strings) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(result);
                                        int code = jsonObject.getInt("code");
                                        if (code == 2000) {
                                            JSONObject content = jsonObject.getJSONObject("content");
//                                        for (int x = 1; x <=add_key; x++) {
                                            JSONArray array = content.getJSONArray(current_key + "");
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject object = array.getJSONObject(i);
                                                int roomId = object.getInt("roomId");
                                                int startPoint = object.getInt("startPoint");
                                                if (startPoint_list.contains(startPoint - 100)) {
                                                    roomId_list.add(roomId);
                                                }
                                            }
//                                        }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    JSONArray jsonArray = new JSONArray();
                                    for (int i = 0; i < roomId_list.size(); i++) {
                                        roomId_list.get(i);
                                        jsonArray.put(roomId_list.get(i));
                                    }

                                    new DeleteRoomAsyncTask().execute(jsonArray);
                                }

                                break;

                            default:
                                break;
                        }
                    }

                };
                QueryAllRoomAsyncTask1 queryAllRoomAsyncTask1 = new QueryAllRoomAsyncTask1(handler);

                queryAllRoomAsyncTask1.execute();

                break;
            case R.id.houseId:
                new QueryAllRoomAsyncTask().execute();
                showDialog();
                break;
        }
    }

    private void showDialog() {
        List<String> houseName_list = new ArrayList<>();
        deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
        DeviceGroup = deviceGroupDao.findAllDevices();
        for (int i = 0; i < DeviceGroup.size(); i++) {
            String houseName = DeviceGroup.get(i).getHouseName();
            if (houseName == null) {
                break;
            }
            houseName_list.add(houseName);
        }
        switch_houseAdapter = new Switch_houseAdapter(getActivity(), this);
        view1 = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_list, null);
        mRecyclerView = (RecyclerView) view1.findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(switch_houseAdapter);
        dialog = new Dialog(getActivity(), R.style.position_dialog_theme);
        switch_houseAdapter.refreshDatas(houseName_list);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view1);
        dialog.setTitle("切换住所");
        dialog.show();
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
//        layoutParams.width = CommonUtil.getScreenWidth(getActivity());
//        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = CommonUtil.getScreenHeight(getActivity()) / 3;
//                layoutParams.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(layoutParams);
    }

    //    public void setRoomType(){
//
//        Intent custom_house_type = new Intent(getActivity(),CustomRoomActivity.class);
//        Bundle bundle = new Bundle();
//        List<RoomEntry> list=roomEntryDao.findAllByGroup(current_key);
//        List<List<Integer>> list_all = new ArrayList<>();
//        List<Integer> list_allRoomPostion = new ArrayList<>();//所有房间的postion个数
//        Log.i("list",list.size()+"");
//        for (int i = 0; i < list.size(); i++) {
//            List<Integer>  list_roomPostion = new ArrayList<>();//每间房间的postion
//            RoomEntry roomEntry_list = list.get(i);
//            int x = roomEntry_list.getX();
//            int y = roomEntry_list.getY();
//            int width = roomEntry_list.getWidth();
//            int height = roomEntry_list.getHeight();
//            int postion_left_top = x/270+4*(y/270);
//            int postion_right_bottom = (x+width)/270+4*((y+height)/270);
//            int postion = 0;
//            for (int k = 0; k <width/270; k++) {
//                postion = postion_left_top++;
//                for (int l = 0; l<(height/270)-1; l++) {
//                    list_roomPostion.add(postion);
//                    postion = postion+4;
//                }
//                list_roomPostion.add(postion);
//
//            }
//            list_all.add(list_roomPostion);
//            list_allRoomPostion.addAll(list_roomPostion);
//        }
//        for (int i = 0; i < list_all.size(); i++) {
//            bundle.putIntegerArrayList("list_"+i, (ArrayList<Integer>) list_all.get(i));//所有房间放到list集合里
//        }
//        bundle.putInt("list_all_size", list_all.size());//传递所有房间
//        bundle.putInt("current_key", current_key);//第几层传过来的房间
//        custom_house_type.putExtras(bundle);
//        getActivity().startActivity(custom_house_type);
//    }
    List<RoomEntry> roomEntries = new ArrayList<>();

    //从服务器获取数据创建房间的形状
    public void From_server_make_room() {
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        final int width = wm.getDefaultDisplay().getWidth();
        final int item_width = width / 4;
        sharedPreferences = getActivity().getSharedPreferences("data", 0);
        house_id = sharedPreferences.getLong("house_id", 0);
        @SuppressLint("HandlerLeak") Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        List<String> strings = (List<String>) msg.obj;
                        for (String result : strings) {
                            try {
                                Intent custom_house_type = new Intent(getActivity(), CustomRoomActivity.class);
                                Bundle bundle = new Bundle();
                                JSONObject jsonObject = new JSONObject(result);
                                int code = jsonObject.getInt("code");
                                if (code == 2000) {
                                    JSONObject content = jsonObject.getJSONObject("content");
                                    JSONArray array = content.getJSONArray(current_key + "");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject object = array.getJSONObject(i);
                                        int roomId = object.getInt("roomId");
                                        int startPoint = object.getInt("startPoint");

                                        JSONArray jsonArray = object.getJSONArray("points");
                                        List<Integer> list_point = new ArrayList<>();
                                        for (int j = 0; j < jsonArray.length(); j++) {
                                            String s = jsonArray.getString(j);
                                            list_point.add(Integer.parseInt(s));
                                        }
                                        int point_min = Collections.min(list_point) - 100;
                                        int point_max = Collections.max(list_point) - 100;

                                        int yu_min = point_min % 4;
                                        int shang_min = point_min / 4;
                                        int x_min = item_width * yu_min;
                                        int y_min = item_width * shang_min;

                                        int yu_max = point_max % 4;
                                        int shang_max = point_max / 4;
                                        int x_max = item_width * yu_max;
                                        int y_max = item_width * shang_max;

                                        int width_room = ((x_max - x_min) / item_width + 1) * (width / 4);
                                        int height_room = ((y_max - y_min) / item_width + 1) * (width / 4);
                                        RoomEntry roomEntry = new RoomEntry(x_min, y_min, width_room, height_room);
                                        roomEntries.add(roomEntry);
                                    }
                                    List<List<Integer>> list_all = new ArrayList<>();
                                    List<Integer> list_allRoomPostion = new ArrayList<>();//所有房间的postion个数
                                    Log.i("list", roomEntries.size() + "");
                                    for (int i = 0; i < roomEntries.size(); i++) {
                                        List<Integer> list_roomPostion = new ArrayList<>();//每间房间的postion
                                        RoomEntry roomEntry = roomEntries.get(i);
                                        int x = roomEntry.getX();
                                        int y = roomEntry.getY();
                                        int width = roomEntry.getWidth();
                                        int height = roomEntry.getHeight();
                                        int postion_left_top = x / 270 + 4 * (y / 270);
                                        int postion_right_bottom = (x + width) / 270 + 4 * ((y + height) / 270);
                                        int postion = 0;
                                        for (int k = 0; k < width / 270; k++) {
                                            postion = postion_left_top++;
                                            for (int l = 0; l < (height / 270) - 1; l++) {
                                                list_roomPostion.add(postion);
                                                postion = postion + 4;
                                            }
                                            list_roomPostion.add(postion);

                                        }
                                        list_all.add(list_roomPostion);
                                        list_allRoomPostion.addAll(list_roomPostion);
                                    }
                                    for (int i = 0; i < list_all.size(); i++) {
                                        bundle.putIntegerArrayList("list_" + i, (ArrayList<Integer>) list_all.get(i));//所有房间放到list集合里
                                    }
                                    bundle.putInt("list_all_size", list_all.size());//传递所有房间
                                    bundle.putInt("current_key", current_key);//第几层传过来的房间
                                    custom_house_type.putExtras(bundle);
                                    getActivity().startActivity(custom_house_type);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        break;

                    default:
                        break;
                }
            }

        };

        QueryAllRoomAsyncTask1 queryAllRoomAsyncTask = new QueryAllRoomAsyncTask1(handler);
        queryAllRoomAsyncTask.execute();
    }


    //展示，删除，添加各层
    public void method_btn_1() {
        if (current_key == 2) {
            btn1.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn1.setTextColor(getResources().getColor(R.color.white));
            btn2.setBackgroundResource(R.drawable.floor_button_colour);
            btn2.setTextColor(Color.WHITE);
            current_key = 1;
        } else if (current_key == 3) {
            btn1.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn1.setTextColor(getResources().getColor(R.color.white));
            btn3.setBackgroundResource(R.drawable.floor_button_colour);
            btn3.setTextColor(Color.WHITE);
            current_key = 1;
        } else if (current_key == 4) {
            btn1.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn1.setTextColor(getResources().getColor(R.color.white));
            btn4.setBackgroundResource(R.drawable.floor_button_colour);
            btn4.setTextColor(Color.WHITE);
            current_key = 1;
        }
        viewPager.setCurrentItem(0);
        savedState();
    }

    public void method_btn_2() {
        if (current_key == 3) {
            btn2.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn2.setTextColor(getResources().getColor(R.color.white));
            btn3.setBackgroundResource(R.drawable.floor_button_colour);
            btn3.setTextColor(Color.WHITE);
            current_key = 2;
        } else if (current_key == 4) {
            btn2.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn2.setTextColor(getResources().getColor(R.color.white));
            btn4.setBackgroundResource(R.drawable.floor_button_colour);
            btn4.setTextColor(Color.WHITE);
            current_key = 2;
        } else if (current_key == 1) {
            btn2.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn2.setTextColor(getResources().getColor(R.color.white));
            btn1.setBackgroundResource(R.drawable.floor_button_colour);
            btn1.setTextColor(Color.WHITE);
            current_key = 2;
        }
        viewPager.setCurrentItem(1);
        savedState();

    }

    public void method_btn_3() {
        if (current_key == 4) {
            btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn3.setTextColor(getResources().getColor(R.color.white));
            btn4.setBackgroundResource(R.drawable.floor_button_colour);
            btn4.setTextColor(Color.WHITE);
            current_key = 3;
        } else if (current_key == 2) {
            btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn3.setTextColor(getResources().getColor(R.color.white));
            btn2.setBackgroundResource(R.drawable.floor_button_colour);
            btn2.setTextColor(Color.WHITE);
            current_key = 3;
        } else if (current_key == 1) {
            btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn3.setTextColor(getResources().getColor(R.color.white));
            btn1.setBackgroundResource(R.drawable.floor_button_colour);
            btn1.setTextColor(Color.WHITE);
            current_key = 3;
        }
        viewPager.setCurrentItem(2);
        savedState();
    }

    public void method_btn_4() {
        if (current_key == 1) {
            btn4.setBackgroundColor(R.drawable.new_floor_button_colour);
            btn4.setTextColor(getResources().getColor(R.color.white));
            btn1.setBackgroundResource(R.drawable.floor_button_colour);
            btn1.setTextColor(Color.WHITE);
            current_key = 4;
        } else if (current_key == 2) {
            btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn4.setTextColor(getResources().getColor(R.color.white));
            btn2.setBackgroundResource(R.drawable.floor_button_colour);
            btn2.setTextColor(Color.WHITE);
            current_key = 4;
        } else if (current_key == 3) {
            btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn4.setTextColor(getResources().getColor(R.color.white));
            btn3.setBackgroundResource(R.drawable.floor_button_colour);
            btn3.setTextColor(Color.WHITE);
            current_key = 4;
        }
        viewPager.setCurrentItem(3);
        savedState();

    }

    public void method_new_btn() {
        if (!isestablied) {
            if (add_key == 0) {
                btn1.setVisibility(View.VISIBLE);
                current_key = 1;
            } else if (add_key == 1) {
                if (current_key == 1) {
                    btn2.setVisibility(View.VISIBLE);
                    btn1.setBackgroundResource(R.drawable.floor_button_colour);//背景变为白色，文字变为绿色（表示非选中按钮）
                    btn1.setTextColor(Color.WHITE);
                    btn2.setBackgroundResource(R.drawable.new_floor_button_colour);//背景变为绿色，文字变为白色（表示当前选中按钮）
                    btn2.setTextColor(getResources().getColor(R.color.white));
                    current_key = 2;
                }
            } else if (add_key == 2) {
                if (current_key == 1) {
                    btn3.setVisibility(View.VISIBLE);
                    btn2.setBackgroundResource(R.drawable.floor_button_colour);
                    btn2.setTextColor(Color.WHITE);
                    btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
                    btn3.setTextColor(getResources().getColor(R.color.white));
                    current_key = 3;
                } else if (current_key == 2) {
                    btn3.setVisibility(View.VISIBLE);
                    btn2.setBackgroundResource(R.drawable.floor_button_colour);
                    btn2.setTextColor(Color.WHITE);
                    btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
                    btn3.setTextColor(getResources().getColor(R.color.white));
                    current_key = 3;
                }
            } else if (add_key == 3) {
                if (current_key == 1) {
                    btn4.setVisibility(View.VISIBLE);
                    btn1.setBackgroundResource(R.drawable.floor_button_colour);
                    btn1.setTextColor(Color.WHITE);
                    btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
                    btn4.setTextColor(getResources().getColor(R.color.white));
                    current_key = 4;

                } else if (current_key == 2) {
                    btn4.setVisibility(View.VISIBLE);
                    btn2.setBackgroundResource(R.drawable.floor_button_colour);
                    btn2.setTextColor(Color.WHITE);
                    btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
                    btn3.setTextColor(getResources().getColor(R.color.white));
                    current_key = 4;
                } else if (current_key == 3) {
                    btn4.setVisibility(View.VISIBLE);
                    btn3.setBackgroundResource(R.drawable.floor_button_colour);
                    btn3.setTextColor(Color.WHITE);
                    btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
                    btn4.setTextColor(getResources().getColor(R.color.white));
                    current_key = 4;

                }
                isestablied = true;
            }
            addPage(add_key);
            viewPager.setCurrentItem(add_key);
            add_key++;
        }
        savedState();
    }

//    public void method_delete_btn() {
//        int postion_delete = current_key - 1; //删除页面的序号
//        if(add_key == 0){
//            Toast.makeText(getActivity(), "数据全部清除，无法再继续删除", Toast.LENGTH_LONG).show();
//        }else if (add_key == 1) {
//            btn1.setVisibility(View.GONE);
//            delPage(postion_delete);
//            add_key = 0;
//        } else if (add_key == 2) {
//            if (current_key == 1) {
//                btn2.setVisibility(View.GONE);
//                current_key = 1;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//            } else if (current_key == 2) {
//                btn2.setVisibility(View.GONE);
//                btn1.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn1.setTextColor(getResources().getColor(R.color.white));
//                current_key = 1;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//            }
//
//        } else if (add_key == 3) {
//            if (current_key == 1) {
//                btn3.setVisibility(View.GONE);
//                current_key = 1;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//            } else if (current_key == 2) {
//                btn3.setVisibility(View.GONE);
//                current_key = 2;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//
//            } else if (current_key == 3) {
//                btn3.setVisibility(View.GONE);
//                btn2.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn2.setTextColor(getResources().getColor(R.color.white));
//                current_key = 2;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//            }
//        } else if (add_key == 4) {
//            if (current_key == 1) {
//                btn4.setVisibility(View.GONE);
//                current_key = 1;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//
//            } else if (current_key == 2) {
//                btn4.setVisibility(View.GONE);
//                current_key = 2;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//
//            } else if (current_key == 3) {
//                btn4.setVisibility(View.GONE);
//                current_key = 3;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//
//            } else if (current_key == 4) {
//                btn4.setVisibility(View.GONE);
//                btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn3.setTextColor(getResources().getColor(R.color.white));
//                current_key = 3;
//                add_key--;
//                isestablied = false;
//                delPage(postion_delete);
//                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
//            }
//        }
//        savedState();
//    }

//    public void method_copy_paste_btn() {
//        postion_current = add_key;//当前页面序号
//        if(add_key == 0){
//            Toast.makeText(getActivity(),"没有数据，无法复制，请创建",Toast.LENGTH_LONG).show();
//        }else if (add_key == 1) {
//            btn2.setVisibility(View.VISIBLE);
//            btn1.setBackgroundResource(R.drawable.floor_button_colour);
//            btn1.setTextColor(Color.WHITE);
//            btn2.setBackgroundResource(R.drawable.new_floor_button_colour);
//            btn2.setTextColor(getResources().getColor(R.color.white));
////            addPage(add_key);
//            copyPage(add_key);
//            viewPager.setCurrentItem(add_key);
//
//            current_key = 2;
//            add_key++;
//        } else if (add_key == 2) {
//            if (current_key == 1) {
//                btn3.setVisibility(View.VISIBLE);
//                btn1.setBackgroundResource(R.drawable.floor_button_colour);
//                btn1.setTextColor(Color.WHITE);
//                btn2.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn2.setTextColor(getResources().getColor(R.color.white));
//                copyPage(add_key);
//                viewPager.setCurrentItem(add_key);
//                current_key = 3;
//                add_key++;
//            } else if (current_key == 2) {
//                btn3.setVisibility(View.VISIBLE);
//                btn2.setBackgroundResource(R.drawable.floor_button_colour);
//                btn2.setTextColor(Color.WHITE);
//                btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn3.setTextColor(getResources().getColor(R.color.white));
//
////                copyPage(postion_current);
//                copyPage(add_key);
//
//                viewPager.setCurrentItem(add_key);
//                current_key = 3;
//                add_key++;
//            }
//        } else if (add_key == 3) {
//            if (current_key == 1) {
//                btn4.setVisibility(View.VISIBLE);
//                btn1.setBackgroundResource(R.drawable.floor_button_colour);
//                btn1.setTextColor(Color.WHITE);
//                btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn4.setTextColor(getResources().getColor(R.color.white));
//
////                copyPage(postion_current);
//                copyPage(add_key);
//                viewPager.setCurrentItem(add_key);
//
//                current_key = 4;
//                add_key++;
//            } else if (current_key == 2) {
//                btn4.setVisibility(View.VISIBLE);
//                btn2.setBackgroundResource(R.drawable.floor_button_colour);
//                btn2.setTextColor(Color.WHITE);
//                btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn4.setTextColor(getResources().getColor(R.color.white));
//
//
////                copyPage(postion_current);
//                addPage(add_key);
//                viewPager.setCurrentItem(add_key);
//
//                current_key = 4;
//                add_key++;
//            } else if (current_key == 3) {
//                btn4.setVisibility(View.VISIBLE);
//                btn3.setBackgroundResource(R.drawable.floor_button_colour);
//                btn3.setTextColor(Color.WHITE);
//                btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
//                btn4.setTextColor(getResources().getColor(R.color.white));
//
//
//                addPage(add_key);
//                viewPager.setCurrentItem(add_key);
//
//                current_key = 4;
//                add_key++;
//            }
//            isestablied = true;
//        } else if (add_key == 4) {
//            Toast.makeText(getActivity(), "无法继续增加", Toast.LENGTH_LONG).show();
//        }
//        savedState();
//    }

    /**
     * 新增一层页面
     */
    public void addPage(int add_key) {
        btn1_fragment = new Btn1_fragment();
        btn2_fragment = new Btn2_fragment();
        btn3_fragment = new Btn3_fragment();
        btn4_fragment = new Btn4_fragment();

        if (add_key == 0) {
            fragmentslist.add(btn1_fragment);
        } else if (add_key == 1) {
            fragmentslist.add(btn2_fragment);
        } else if (add_key == 2) {
            fragmentslist.add(btn3_fragment);
        } else if (add_key == 3) {
            fragmentslist.add(btn4_fragment);
        }
        int postion = viewPager.getCurrentItem();
        fragmentViewPagerAdapter.notifyDataSetChanged();
    }


    public void copyPage(int add_key) {
        btn1_fragment = new Btn1_fragment();
        btn2_fragment = new Btn2_fragment();
        btn3_fragment = new Btn3_fragment();
        btn4_fragment = new Btn4_fragment();

        if (add_key == 0) {
        } else if (add_key == 1) {
            Bundle bundle = new Bundle();
            bundle.putInt("group1", 1);
            btn2_fragment.setArguments(bundle);
            fragmentslist.add(btn2_fragment);
        } else if (add_key == 2) {
            if (current_key == 1) {
                Bundle bundle = new Bundle();
                bundle.putInt("group1", 1);
                bundle.putInt("current_key", 1);
                btn2_fragment.setArguments(bundle);
                fragmentslist.add(btn3_fragment);
            } else if (current_key == 2) {
                Bundle bundle = new Bundle();
                bundle.putInt("group2", 2);
                bundle.putInt("current_key", 2);
                btn2_fragment.setArguments(bundle);
                fragmentslist.add(btn3_fragment);
            }
        } else if (add_key == 3) {
            if (current_key == 1) {
                Bundle bundle = new Bundle();
                bundle.putInt("group1", 1);
                bundle.putInt("current_key", 1);
                btn2_fragment.setArguments(bundle);
                fragmentslist.add(btn3_fragment);
            } else if (current_key == 2) {
                Bundle bundle = new Bundle();
                bundle.putInt("group2", 2);
                bundle.putInt("current_key", 2);
                btn2_fragment.setArguments(bundle);
                fragmentslist.add(btn3_fragment);
            } else if (current_key == 3) {
                Bundle bundle = new Bundle();
                bundle.putInt("group3", 3);
                bundle.putInt("current_key", 3);
                btn2_fragment.setArguments(bundle);
                fragmentslist.add(btn4_fragment);
            }
        }
        int postion = viewPager.getCurrentItem();
        fragmentViewPagerAdapter.notifyDataSetChanged();
    }

    /**
     * 删除当前页面
     */
    public void delPage(int postion_delete) {
        int position = viewPager.getCurrentItem();//获取当前页面位置
        fragmentslist.remove(position);//删除一项数据源中的数据
        viewPager.setCurrentItem(postion_delete);//postion_delete当前页面的序号，删除后跳转到
        fragmentViewPagerAdapter.notifyDataSetChanged();//通知UI更新

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.goLiveFragment();
            Log.i("view", "as");
        }
    }

    //保存数据
    public void savedState() {
        //保存数据
        editor1.putString("location", location);
        editor1.putInt("current_key", current_key);
        editor1.putInt("add_key", add_key);
        editor1.putInt("btn1", btn1.getVisibility());
        editor1.putInt("btn2", btn2.getVisibility());
        editor1.putInt("btn3", btn3.getVisibility());
        editor1.putInt("btn4", btn4.getVisibility());
        editor1.putLong("house_id", house_id);
        editor1.putString("house_Name", house_Name);
        editor1.putInt("fragmentlist_size", fragmentslist.size());
        editor1.apply();
    }

    //恢复数据,只能在进入页面时执行一次，切记
    public void restore_Data() {
        sharedPreferences = getActivity().getSharedPreferences("data", 0);
        add_key = sharedPreferences.getInt("add_key", 1);
        location = sharedPreferences.getString("lication", "北京");
        house_id = sharedPreferences.getLong("house_id", 0);
        house_Name = sharedPreferences.getString("house_Name", "我的家");
        current_key = sharedPreferences.getInt("current_key", 1);
        int fragmentlist_size = sharedPreferences.getInt("fragmentlist_size", 1);
        String airCondition = sharedPreferences.getString("airCondition", "良好");
        String humidity1 = sharedPreferences.getString("humidity", "60%").substring(3);
        String temperature1 = sharedPreferences.getString("temperature", "28℃");

        airQuality.setText("室外空气:" + airCondition);
        humidity.setText(humidity1);
        temperature.setText(temperature1);
        for (int i = 1; i < fragmentlist_size; i++) {
            addPage(i);
        }
        viewPager.setCurrentItem(current_key-1);
        houseId.setText(house_Name);
        int i1 = sharedPreferences.getInt("btn1", btn1.getVisibility());
        int i2 = sharedPreferences.getInt("btn2", btn2.getVisibility());
        int i3 = sharedPreferences.getInt("btn3", btn3.getVisibility());
        int i4 = sharedPreferences.getInt("btn4", btn4.getVisibility());

        btn1.setVisibility(i1);
        btn2.setVisibility(i2);
        btn3.setVisibility(i3);
        btn4.setVisibility(i4);

        if (current_key == 1) {
            btn1.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn1.setTextColor(getResources().getColor(R.color.white));
        } else if (current_key == 2) {
            btn2.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn2.setTextColor(getResources().getColor(R.color.white));
        } else if (current_key == 3) {
            btn3.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn3.setTextColor(getResources().getColor(R.color.white));
        } else if (current_key == 4) {
            btn4.setBackgroundResource(R.drawable.new_floor_button_colour);
            btn4.setTextColor(getResources().getColor(R.color.white));
        }
        fragmentViewPagerAdapter.notifyDataSetChanged();
    }

    class WeatherAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            int code = 0;
            Map<String, Object> map = new HashMap<>();
            map.put("city", location);
            map.put("key", "254835760bcca");
            String url = getUrl.getRqstUrl("http://apicloud.mob.com/v1/weather/query", map);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("retCode");
                    if (code == 200) {
                        JSONArray content = jsonObject.getJSONArray("result");
                        for (int i = 0; i < content.length(); i++) {
                            JSONObject jsonObject1 = content.getJSONObject(i);
                            if (jsonObject1 != null) {
                                String airCondition = jsonObject1.getString("airCondition");
                                String humidity = jsonObject1.getString("humidity");
                                String temperature = jsonObject1.getString("temperature");
                                String city = jsonObject1.getString("city");
                                editor1.putString("airCondition", airCondition);
                                editor1.putString("humidity", humidity);
                                editor1.putString("temperature", temperature);
                                editor1.putString("city", city);
                                editor1.apply();
                            }
                        }
                        return result;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", 0);
            String airCondition = sharedPreferences.getString("airCondition", "良好");
            String humidity1 = sharedPreferences.getString("humidity", "60%").substring(3);
            String city1 = sharedPreferences.getString("city", "北京");
            String temperature1 = sharedPreferences.getString("temperature", "28℃");
            airQuality.setText("室外空气:" + airCondition);
            humidity.setText(humidity1);
            temperature.setText(temperature1);
            city.setText(city1);
            super.onPostExecute(s);
        }


    }

}
