package com.xinrui.secen.scene_fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.RoomEntryDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_activity.AddEquipmentActivity;
import com.xinrui.secen.scene_activity.RoomContentActivity;
import com.xinrui.secen.scene_activity.RoomTypesActivity;
import com.xinrui.secen.scene_adapter.Scene_deviceAdapter;
import com.xinrui.secen.scene_pojo.Equipment;
import com.xinrui.secen.scene_pojo.Room;
import com.xinrui.secen.scene_pojo.RoomEntry;
import com.xinrui.secen.scene_util.GetUrl;
import com.xinrui.secen.scene_util.ItemDecoration.GridSpacingItemDecoration;
import com.xinrui.secen.scene_view_custom.RoomViewGroup;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;

import org.json.JSONArray;
import org.json.JSONException;
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

public class Btn3_fragment extends Fragment{
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
    public static int running=0;
    private ProgressDialog progressDialog;
    DeviceChildDaoImpl deviceChildDao;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences("data",0);
        house_id = sharedPreferences.getLong("house_id",0);
        view_background = (RoomViewGroup) inflater.inflate(R.layout.rooms_background3, container, false);
        roomViewGroup = (FrameLayout) view_background.findViewById(R.id.f3);
        progressDialog = new ProgressDialog(getActivity());
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
//            progressDialog.setMessage("请稍后...");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected List<Room> doInBackground(Void... voids) {

            deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
            deviceChildDao=new DeviceChildDaoImpl(getActivity());
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
                    JSONArray array = content.getJSONArray(3 + "");
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
                        room = new Room(roomId,roomName,startPoint,points,houseId,devices,layer,x_min,y_min,width_room,height_room,false);
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
            progressDialog.dismiss();
            try{
                if(roomList != null){
                    for (int i = 0; i < roomList.size(); i++) {
                        roomEntry = new RoomEntry(roomList.get(i).getX(),roomList.get(i).getY(),roomList.get(i).getWidth(),roomList.get(i).getHeight());
                        View view = setLayout(roomList.get(i).getDevices(),roomList.get(i).getRoomName(),roomEntry.getX(), roomEntry.getY(), roomEntry.getWidth(), roomEntry.getHeight(),roomList.get(i));
                        Room room1 = new Room(view,roomList.get(i).getRoomId(),roomList.get(i).getRoomName(),roomList.get(i).getStartPoint(),roomList.get(i).getPoints(),roomList.get(i).getHouseId(),roomList.get(i).getDevices(),roomList.get(i).getLayer(),roomList.get(i).getX(),roomList.get(i).getY(),roomList.get(i).getWidth(),roomList.get(i).getHeight(),roomList.get(i).isSelected());
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
        ImageView imageView = (ImageView) roomViewGroup.findViewById(R.id.empty_room_iv3);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(width,width*2));
        imageView.setMinimumHeight(view_background.getHeight());

    }
    String topicName;
    boolean open = false;
    String mac;

    //绘制房间view
    @SuppressLint("ClickableViewAccessibility")
    public View setLayout(JSONArray devices, String roomName, int x, int y, int width, int height ,Room room) {
        List<Equipment> device_list = new ArrayList<>();//房间设备的list

        WindowManager wm = (WindowManager) getActivity()//获取屏幕宽高
                .getSystemService(Context.WINDOW_SERVICE);
        int width1 = wm.getDefaultDisplay().getWidth();
        int item_width = width1/4;

        int line;//列

        //根据绘制view的宽度选择布局
        if(width == item_width || width < item_width){
            line = 2;
        }else if(width == 2*item_width ||( 3*item_width > width&& width > item_width )){
            line = 4;
        }else if( width == 3*item_width || (4*item_width > width && width >3*item_width)){
            line = 6;
        }else {
            line = 8;
        }
        final View childView;
        if(line == 2){
            childView = LayoutInflater.from(getActivity()).inflate(R.layout.scene_room_content, null);

        }else {
            childView = LayoutInflater.from(getActivity()).inflate(R.layout.scene_room_content1, null);
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
                Equipment equipment = new Equipment(id,deviceName,device_drawable,macAddress,controlled,type);
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
        //获取RecyclerView控件
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), line , GridLayoutManager.VERTICAL ,false);
        RecyclerView rv = (RecyclerView) childView.findViewById(R.id.scene_device_recyclerView);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(new GridSpacingItemDecoration(
                line, getResources().getDimensionPixelSize(R.dimen.dp_6),true
        ));
        rv.setHasFixedSize(true);
        Scene_deviceAdapter scene_deviceAdapter = new Scene_deviceAdapter(device_list);


        //RecyclerView的本身的点击事件,使用手势处理点击，长按
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            //            boolean isSelected = is;//房间选中状态
            @Override
            public boolean onDown(MotionEvent e) {

                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            //点击
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
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
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            //长按
            @Override
            public void onLongPress(MotionEvent e) {
//                for (int i = 0; i < room_list.size(); i++) {
//                    if(room_list.get(i).getView() == childView){
                if(childView.isSelected()){
                    childView.setBackgroundResource(R.drawable.mergeroom_background);
                    childView_list.remove(childView);
                    childView.setSelected(false);
                }else {
                    childView.setBackgroundResource(R.drawable.select_mergeroom_background);
                    childView_list.add(childView);
                    childView.setSelected(true);
                }
//                    }
//                }

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        //触摸RecyclerView返回手势处理的响应
        rv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        saveViewInstance(roomName,childView,device_list);

        childView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        //RecyclerView的item的点击事件
        scene_deviceAdapter.setOnItemClickListener(new Scene_deviceAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int data) {
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
        //RecyclerView的item的长按事件

        scene_deviceAdapter.setOnItemLongClickListener(new Scene_deviceAdapter.OnRecyclerItemLongListener() {
//            final boolean isSelected = false;//房间选中状态

            @Override
            public void onItemLongClick(View view, int position) {
//                for (int i = 0; i < room_list.size(); i++) {
//                    if(room_list.get(i).getView().getTag() == view.getTag()){
                if(childView.isSelected()){
                    childView.setBackgroundResource(R.drawable.mergeroom_background);
                    childView_list.remove(childView);
                    childView.setSelected(false);
                }else {
                    childView.setBackgroundResource(R.drawable.select_mergeroom_background);
                    childView_list.add(childView);
                    childView.setSelected(true);
                }
//                    }
//                }

            }
        });
        rv.setAdapter(scene_deviceAdapter);

        return childView;
    }

    String houseName;
    //每个房间里面的空间
    public void saveViewInstance(String roomName,final View childView,List<Equipment> device_list){
        houseName=roomName;
        Log.i("houseName",houseName);
        for (int i = 0; i < device_list.size(); i++) {
            if(device_list.get(i).getDevice_type() == 2){

                Equipment equipment=device_list.get(i);
                long deviceId=equipment.getId();


                DeviceChild deviceChild2=deviceChildDao.findDeviceById(deviceId);
                if (deviceChild2.getTemp()==0 && deviceChild2.getHum()==0){
                    break;
                }
                TextView extTemp = (TextView) childView.findViewById(R.id.extTemp);
                TextView extHut = (TextView) childView.findViewById(R.id.extHut);
                if (deviceChild2!=null){
                    String et=deviceChild2.getTemp()+"";
                    String eh=deviceChild2.getHum()+"";
                    extTemp.setText(et+"℃");
                    extHut.setText(eh+"%");
                }


            }
        }
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

                List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(house_id);
                if (deviceChildren.isEmpty()) {
                    Utils.showToast(getActivity(), "这个家还没有设备");
                } else {
                    Intent intent = new Intent(getActivity(), AddEquipmentActivity.class);
                    intent.putExtra("houseName",houseName);
                    startActivity(intent);
                }
            }
        });

//        childView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor sp = getActivity().getSharedPreferences("roomId", Activity.MODE_PRIVATE).edit();
//
//                for (int i = 0; i < room_list.size(); i++) {
//                    if (childView.getTag() == room_list.get(i).getView().getTag()) {
//                        int roomId = room_list.get(i).getRoomId();
//                        sp.putInt("roomId",roomId);
//                        sp.commit();
//                        break;
//                    }
//                }
//
//                Intent intent = new Intent(getActivity(), RoomContentActivity.class);
//                startActivity(intent);
//            }
//        });
//        childView.setOnLongClickListener(new View.OnLongClickListener() {
//
//            boolean isSelected = false;//房间选中状态
//
//            @SuppressLint("ResourceAsColor")
//            @Override
//            public boolean onLongClick(View v) {
//                int i = (int) v.getTag();
//                if(isSelected){
//                    v.setBackgroundResource(R.drawable.mergeroom_background);
//                    childView_list.remove(v);
//                    isSelected = false;
//                }else {
//                    v.setBackgroundResource(R.drawable.select_mergeroom_background);
//                    childView_list.add(v);
//                    isSelected = true;
//                }
//                return true;
//            }
//        });
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
    public void onStart() {
        super.onStart();
        sharedPreferences =  getActivity().getSharedPreferences("roomType",Context.MODE_PRIVATE);


    }

    MessageReceiver receiver = new MessageReceiver();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        running=2;
        Intent intent=new Intent(getActivity(),MQService.class);
        getActivity().bindService(intent,connection,Context.BIND_AUTO_CREATE);
        IntentFilter intentFilter=new IntentFilter("Btn1_fragment");
        getActivity().registerReceiver(receiver,intentFilter);
        Intent intent1 = new Intent();
        intent1.setAction("mqttmessage");
        getActivity().sendBroadcast(intent1);
    }


    MQService mqService;
    boolean bound = false;
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
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (connection!=null){
            getActivity().unbindService(connection);
        }
        if (receiver!=null){
            getActivity().unregisterReceiver(receiver);
        }
        running = 0;
        super.onDestroy();
    }
    String extTemp ;
    String extHut ;

    public class MessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceChild deviceChild2 = (DeviceChild) intent.getSerializableExtra("deviceChild");
            extTemp = String.valueOf(intent.getIntExtra("extTemp",0));
            extHut = String.valueOf(intent.getIntExtra("extHum",0));
            String et = extTemp;
            String eh = extHut;
//            getData(et,eh);

            for (int i = 0; i < room_list.size(); i++) {
                JSONArray jsonArray = room_list.get(i).getDevices();
                for (int j = 0; j < jsonArray.length(); j++) {
                    try {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(j);
                        String macAddress = jsonObject1.getString("macAddress");

                        int type = (int) jsonObject1.get("type");
                        if(type == 2){
                            if (deviceChild2!=null){
                                String macAddress2 = deviceChild2.getMacAddress();
                                if(macAddress.equals(macAddress2)){
                                    TextView extTemp1 = (TextView) room_list.get(i).getView().findViewById(R.id.extTemp);
                                    TextView extHut1 = (TextView) room_list.get(i).getView().findViewById(R.id.extHut);
                                    extTemp1.setText(deviceChild2.getTemp()+"℃");
                                    extHut1.setText(deviceChild2.getHum()+"％");
                                }

                            }
//                            SharedPreferences sp = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
//                            String extTemp=sp.getString("extTemp1","");
//                            String extHut=sp.getString("extHut1","");

//                            if(extTemp1 == null|| extHut1 == null){
//
//                            }else {
//
////                                String temp=d
//                                extTemp1.setText(extTemp+"℃");
//                                extHut1.setText(extHut+"％");
//                            }


                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

//    public void getData(String extTemp,String extHut){
//        this.extTemp = extTemp;
//        this.extHut = extHut;
//        SharedPreferences.Editor sp = getActivity().getSharedPreferences("data", 0).edit();
//        sp.putString("extTemp1", extTemp);
//        sp.putString("extHut1", extHut);
//        sp.commit();
//    }

}
