package com.xinrui.smart.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.RoomEntryDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddEquipmentActivity;
import com.xinrui.smart.activity.RoomContentActivity;
import com.xinrui.smart.activity.RoomTypesActivity;
import com.xinrui.smart.adapter.Scene_deviceAdapter;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.Equipment;
import com.xinrui.smart.pojo.Room;
import com.xinrui.smart.pojo.RoomEntry;
import com.xinrui.smart.util.GetUrl;
import com.xinrui.smart.util.ItemDecoration.GridSpacingItemDecoration;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.RoomViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Unbinder;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by win7 on 2018/3/13.
 */

public class Btn2_fragment extends Fragment{
    int x;
    int y;
    int width;
    int height;
    private int roomId = 0;
    Unbinder unbinder;
    ImageView emptyRoom;
    private Context mContext;
    private RoomEntryDaoImpl roomEntryDao;

    RoomViewGroup view_background;
    FrameLayout roomViewGroup;

     int item_width;

    List<Room> room_list = new ArrayList<>();
    List<RoomEntry> roomEntry_list = new ArrayList<>();
    public List<View> childView_list  = new ArrayList<>();
    SharedPreferences sharedPreferences;

    private List<Integer> startPoint_list = new ArrayList<>();
    private List<Integer> roomId_list = new ArrayList<>();
    private Long house_id;
    DeviceGroupDaoImpl deviceGroupDao;
    List<DeviceGroup> DeviceGroup;
    GetUrl getUrl = new GetUrl();
    Room room;
    RoomEntry roomEntry;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences("data",0);
        house_id = sharedPreferences.getLong("house_id",0);
        view_background = (RoomViewGroup) inflater.inflate(R.layout.rooms_background2, container, false);
        roomViewGroup = (FrameLayout) view_background.findViewById(R.id.f2);
        initView();
        ModificationAsyncTask();
        sendRequestForListData();
        return view_background;
    }


    //从QueryAllRoomAsyncTask获取list_room
    public void sendRequestForListData(){
        QueryAllRoomAsyncTask queryAllRoomAsyncTask = new QueryAllRoomAsyncTask();
        queryAllRoomAsyncTask.execute();
        queryAllRoomAsyncTask.setOnAsyncResponse(new AsyncResponse() {
            //通过自定义的接口回调获取AsyncTask中onPostExecute返回的结果变量
            @Override
            public void onDataReceivedSuccess(List<Room> listData) {
                room_list = listData;
            }

            @Override
            public void onDataReceivedFailed() {
            }
        });
    }

    //从服务器获取数据创建房间异步请求
    class QueryAllRoomAsyncTask extends AsyncTask<Void,Void,List<Room>>{
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        final int width = wm.getDefaultDisplay().getWidth();
        final int item_width = width/4;
        public AsyncResponse asyncResponse;

        public void setOnAsyncResponse(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Room> doInBackground(Void... voids) {
            deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
            DeviceGroup = deviceGroupDao.findAllDevices();
            List<Room> roomList = new ArrayList<>();

            Map<String, Object> params = new HashMap<>();
            params.put("houseId", house_id);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/findAllRoom", params);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                if(!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject content = jsonObject.getJSONObject("content");
                    JSONArray array = content.getJSONArray(2 + "");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        int roomId = object.getInt("roomId");
                        String roomName = object.getString("roomName");
                        int startPoint = object.getInt("startPoint");
                        JSONArray points = object.getJSONArray("points");
                        int houseId = object.getInt("houseId");
                        JSONArray devices = object.getJSONArray("devices");
                        int layer = object.getInt("layer");

                        for (int j = 0; j < points.length(); j++) {
                            String point_string = points.getString(j);
                            int point_int = Integer.parseInt(point_string) - 100;
                        }
                        int devices_length = devices.length();
                        for (int j = 0; j < devices.length(); j++) {
                            String device = devices.getString(j);
                            JSONObject devices_object = devices.getJSONObject(j);
                            int id = devices_object.getInt("id");
                            String deviceName = devices_object.getString("deviceName");
                            int type = devices_object.getInt("type");
                            String macAddress = devices_object.getString("macAddress");
                            int controlled = devices_object.getInt("controlled");
                        }


                        if (startPoint_list.contains(startPoint - 100)) {
                            Log.i("startPoint_list", startPoint_list.get(0) + "");
                            roomId_list.add(roomId);
                        }
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
                        room = new Room(roomId,roomName,startPoint,points,houseId,devices,layer,x_min,y_min,width_room,height_room);
                        roomList.add(room);
                    }
                    return roomList;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<Room> roomList) {
            try{
                if(roomList != null){
                for (int i = 0; i < roomList.size(); i++) {
                    roomEntry = new RoomEntry(roomList.get(i).getX(),roomList.get(i).getY(),roomList.get(i).getWidth(),roomList.get(i).getHeight());
                    View view = setLayout(roomList.get(i).getDevices(),roomList.get(i).getRoomName(),roomEntry.getX(), roomEntry.getY(), roomEntry.getWidth(), roomEntry.getHeight());
                    Room room1 = new Room(view,roomList.get(i).getRoomId(),roomList.get(i).getRoomName(),roomList.get(i).getStartPoint(),roomList.get(i).getPoints(),roomList.get(i).getHouseId(),roomList.get(i).getDevices(),roomList.get(i).getLayer(),roomList.get(i).getX(),roomList.get(i).getY(),roomList.get(i).getWidth(),roomList.get(i).getHeight());
                    room_list.add(room1);
                    roomEntry_list.add(roomEntry);
                    view.setTag(room1.getRoomId());
                }
                    asyncResponse.onDataReceivedSuccess(room_list);
                        if (roomEntry_list.size() ==0) {
                            view_background.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return true;
                                }
                            });
                            view_background.setVerticalScrollBarEnabled(false);
                        }else{
                            view_background.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return false;
                                }
                            });
                            view_background.setVerticalScrollBarEnabled(true);
                        }
                    }else {
                    asyncResponse.onDataReceivedFailed();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //回调接口，用于异步返回数据
    public interface AsyncResponse {
        void onDataReceivedSuccess(List<Room> listData);
        void onDataReceivedFailed();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void initView(){
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        int width =  wm.getDefaultDisplay().getWidth();
         item_width = width/4;
         height = wm.getDefaultDisplay().getHeight();
        ImageView imageView = (ImageView) roomViewGroup.findViewById(R.id.empty_room_iv2);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(width,width*2));
        imageView.setMinimumHeight(view_background.getHeight());

    }

    //绘制房间view
    @SuppressLint("ClickableViewAccessibility")
    public View setLayout(JSONArray devices, String roomName, int x, int y, int width, int height) {
        List<Equipment> device_list = new ArrayList<>();

        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
         int width1 = wm.getDefaultDisplay().getWidth();
         int item_width = width1/4;

        int line;//列
        View childView = LayoutInflater.from(getActivity()).inflate(R.layout.scene_room_content, null);

        if(width == item_width || width < item_width){
            line = 2;
        }else if(width == 2*item_width ||( 3*item_width > width&& width > item_width )){
            line = 4;
        }else if( width == 3*item_width || (4*item_width > width && width >3*item_width)){
            line = 6;
        }else {
            line = 8;
        }

        try {
            for (int j = 0; j < devices.length(); j++) {
                String device = devices.getString(j);
                JSONObject devices_object = devices.getJSONObject(j);
                int id = devices_object.getInt("id");
                String deviceName = devices_object.getString("deviceName");
                int type = devices_object.getInt("type");
                int house = devices_object.getInt("houseId");
                String macAddress = devices_object.getString("macAddress");
                int isUnlock = devices_object.getInt("isUnlock");
                int controlled = devices_object.getInt("controlled");
                int masterControllerUserId = devices_object.getInt("masterControllerUserId");
                int device_drawable = 0;
                if(type == 1){
                    device_drawable = R.drawable.equipment_warmer;
                }else if(type == 2){
                    device_drawable = R.drawable.equipment_external_sensor;
                }
                Equipment equipment = new Equipment(id,deviceName,type,macAddress,controlled,device_drawable);
                device_list.add(equipment);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        childView.setTranslationX(x);
        childView.setTranslationY(y);
        childView.setLayoutParams(params);
        roomViewGroup.addView(childView);
        //获取rv控件
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), line , GridLayoutManager.VERTICAL ,false);
        RecyclerView rv = (RecyclerView) childView.findViewById(R.id.scene_device_recyclerView);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(new GridSpacingItemDecoration(
                line, getResources().getDimensionPixelSize(R.dimen.dp_6),true
        ));
        rv.setHasFixedSize(true);
        Scene_deviceAdapter scene_deviceAdapter = new Scene_deviceAdapter(device_list);
        rv.setAdapter(scene_deviceAdapter);
        saveViewInstance(roomName,childView);
        return childView;
    }

    //每个房间里面的空间
    public void saveViewInstance(String roomName,final View childView){
        TextView room_Name = (TextView) childView.findViewById(R.id.room_name);
        ImageView add_equipment = (ImageView) childView.findViewById(R.id.add_equipment);
        room_Name.setText(roomName);

        //注册监听事件
            room_Name.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View v) {
                for (int i = 0; i < room_list.size(); i++) {
                    if(childView.getTag() == room_list.get(i).getView().getTag()){
                        SharedPreferences sp = getActivity().getSharedPreferences("room_postion", MODE_PRIVATE);
                        sp.edit().putInt("room_postion", i);
                        Intent intent = new Intent(getActivity(),RoomTypesActivity.class);
                        int roomId = room_list.get(i).getRoomId();
                        sp.edit().putInt("roomId",roomId);
                        sp.edit().commit();
                        Bundle bundle = new Bundle();
                        bundle.putInt("roomId",roomId);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,1);
                    }
                }


            }
        });
        add_equipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor sp = getActivity().getSharedPreferences("roomId", Activity.MODE_PRIVATE).edit();

                for (int i = 0; i < room_list.size(); i++) {
                    if (childView.getTag() == room_list.get(i).getView().getTag()) {
                        int roomId = room_list.get(i).getRoomId();
                        sp.putInt("roomId",roomId);
                        sp.commit();
                        break;
                    }
                }

                Intent intent = new Intent(getActivity(), AddEquipmentActivity.class);
                startActivity(intent);
            }
        });

        childView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor sp = getActivity().getSharedPreferences("roomId", Activity.MODE_PRIVATE).edit();

                for (int i = 0; i < room_list.size(); i++) {
                    if (childView.getTag() == room_list.get(i).getView().getTag()) {
                        int roomId = room_list.get(i).getRoomId();
                        sp.putInt("roomId",roomId);
                        sp.commit();
                        break;
                    }
                }

                Intent intent = new Intent(getActivity(), RoomContentActivity.class);
                startActivity(intent);
            }
        });
        childView.setOnLongClickListener(new View.OnLongClickListener() {

            boolean isSelected = false;//房间选中状态

            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onLongClick(View v) {
                int i = (int) v.getTag();
                if(isSelected){
                    v.setBackgroundResource(R.drawable.mergeroom_background);
                    childView_list.remove(v);
                    isSelected = false;
                }else {
                    v.setBackgroundResource(R.drawable.select_mergeroom_background);
                    childView_list.add(v);
                    isSelected = true;
                }
                return true;
            }
        });
    }

    public void getUnboundDevice() {
        Log.i("getUnboundDevice","getUnboundDevice");
        GetUnboundDeviceAsyncTask getUnboundDeviceAsyncTask = new GetUnboundDeviceAsyncTask();
        getUnboundDeviceAsyncTask.execute();
    }
        class GetUnboundDeviceAsyncTask extends AsyncTask<Void,Void,Integer>{
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data",MODE_PRIVATE);
            @Override
            protected Integer doInBackground(Void... voids) {
                int code = 0;
                int houseId = (int) sharedPreferences.getLong("house_id",0);
                Map<String,Object> map = new HashMap<>();
                map.put("houseId",houseId);
                String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/getUnboundDevice",map);
                String result = HttpUtils.getOkHpptRequest(url);
                try {
                    if(!Utils.isEmpty(result)){
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getInt("code");
                        if(code == 2000){
                            JSONArray content=jsonObject.getJSONArray("content");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return code;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                int code = integer;
                super.onPostExecute(integer);
            }
        }


    //修改房间类型网络请求方法
    public void ModificationAsyncTask(){
        ModificationAsyncTask modificationAsyncTask = new ModificationAsyncTask();
        modificationAsyncTask.execute();
    }
    //修改房间类型异步请求
    @SuppressLint("StaticFieldLeak")
    class ModificationAsyncTask extends AsyncTask<Void,Void,Integer>{
        SharedPreferences sp = getActivity().getSharedPreferences("room_postion", MODE_PRIVATE);

        @Override
        protected Integer doInBackground(Void... voids) {
            int code = 0;
            int roomId = sp.getInt("returnRoomId",0);
            String roomName = sp.getString("returnRoomName","卧室");
            Map<String,Object> params = new HashMap<>();
            params.put("roomName",roomName);
            params.put("roomId",roomId);
            try {
                String url="http://120.77.36.206:8082/warmer/v1.0/room/changeRoomType?roomId="+roomId+"&roomName="
                        + URLEncoder.encode(roomName);
                String result=HttpUtils.getOkHpptRequest(url);
                if(!Utils.isEmpty(result)){
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if(code == 2000){
                        JSONArray content=jsonObject.getJSONArray("content");
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return code;
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2){
            Bundle b = data.getExtras();
            String returnName = b.getString("returnName");
            int returnRoomId = b.getInt("returnRoomId");
            SharedPreferences sp = getActivity().getSharedPreferences("room_postion", MODE_PRIVATE);
            sp.edit().putString("returnName",returnName);
            sp.edit().putInt("returnRoomId",returnRoomId);
            sp.edit().commit();
            int room_postion = sp.getInt("room_postion", 0);
        }
    }

    public List<View> getListViews(){
        Log.i("childView_list1",childView_list.size()+"");
        return childView_list;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPreferences =  getActivity().getSharedPreferences("roomType",Context.MODE_PRIVATE);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
