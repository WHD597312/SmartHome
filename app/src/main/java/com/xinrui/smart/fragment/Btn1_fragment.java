package com.xinrui.smart.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.Room;
import com.xinrui.smart.pojo.RoomEntry;
import com.xinrui.smart.util.GetUrl;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.RoomViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class Btn1_fragment extends Fragment{
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
    int group1 = 1;

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
        view_background = (RoomViewGroup) inflater.inflate(R.layout.rooms_background1, container, false);
        roomViewGroup = (FrameLayout) view_background.findViewById(R.id.fl);
//        roomViewGroup.setOnClickListener(listec);
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
                for (int i = 0; i < room_list.size(); i++) {//如此，我们便把onPostExecute中的变量赋给了成员变量list_room
                    Toast.makeText(getActivity(), room_list.size(),Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onDataReceivedFailed() {
            }
        });
    }

    //从服务器获取数据创建房间异步请求
    class QueryAllRoomAsyncTask extends AsyncTask<Void,Void,String>{
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
            Toast.makeText(getActivity(),"开始执行",Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
            DeviceGroup = deviceGroupDao.findAllDevices();
            Map<String, Object> params = new HashMap<>();
            params.put("houseId", house_id);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/findAllRoom", params);
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                if (code == 2000) {
                    return result;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            try{
                if(!Utils.isEmpty(result)){
                    JSONObject jsonObject = new JSONObject(result);
                        JSONObject content = jsonObject.getJSONObject("content");
                        JSONArray array = content.getJSONArray(1 + "");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            int roomId = object.getInt("roomId");
                            String roomName = object.getString("roomName");
                            int startPoint = object.getInt("startPoint");
                            JSONArray points = object.getJSONArray("points");
                            int houseId = object.getInt("houseId");
                            JSONArray devices = object.getJSONArray("devices");
                            int layer = object.getInt("layer");

                            for (int j = 0; j <  points.length(); j++) {
                                String point_string = points.getString(j);
                                int point_int = Integer.parseInt(point_string)-100;
                            }

                            if (startPoint_list.contains(startPoint - 100)) {
                                Log.i("startPoint_list", startPoint_list.get(0) + "");
                                roomId_list.add(roomId);
                            }
                            JSONArray jsonArray = object.getJSONArray("points");
                            List<Integer> list_point = new ArrayList<>();
                            for (int j = 0; j <jsonArray.length() ; j++) {
                                String s=jsonArray.getString(j);
                                list_point.add(Integer.parseInt(s));
                            }
                            int point_min = Collections.min(list_point)-100;
                            int point_max = Collections.max(list_point)-100;

                            int yu_min = point_min%4;
                            int shang_min = point_min/4;
                            int x_min = item_width *yu_min;
                            int y_min = item_width *shang_min;

                            int yu_max = point_max%4;
                            int shang_max = point_max/4;
                            int x_max = item_width *yu_max;
                            int y_max = item_width *shang_max;

                            int width_room = ((x_max-x_min)/ item_width +1)*(width/4);
                            int height_room = ((y_max-y_min)/ item_width +1)*(width/4);
                            roomEntry = new RoomEntry(x_min,y_min,width_room,height_room);

                            View view = setLayout(roomEntry.getX(), roomEntry.getY(), roomEntry.getWidth(), roomEntry.getHeight());
                            roomEntry_list.add(roomEntry);
                            view.setTag(roomId);
                             room = new Room(view,roomId,roomName,startPoint,points,houseId,devices,layer);
                             room_list.add(room);
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
         height = wm.getDefaultDisplay().getHeight();
        ImageView imageView = (ImageView) roomViewGroup.findViewById(R.id.empty_room_iv1);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(width,width*2));
        imageView.setMinimumHeight(view_background.getHeight());

    }

    View childView;
    public View setLayout(int x, int y, int width, int height) {
        View childView1 = LayoutInflater.from(getActivity()).inflate(R.layout.room_content_x1, null);
        View childView2 = LayoutInflater.from(getActivity()).inflate(R.layout.room_content_x2, null);
        View childView3 = LayoutInflater.from(getActivity()).inflate(R.layout.room_content_x3, null);
        View childView4 = LayoutInflater.from(getActivity()).inflate(R.layout.room_content_x4, null);

        if(width>270&&width<810){
            if(height<540){
                childView1 = childView4;
            }else{
                childView1 = childView2;

            }
        }
        if(width>810||width==810){
            if(height<540){
                childView1 = childView4;
            }else{
                childView1 = childView2;
            }
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        childView1.setTranslationX(x);
        childView1.setTranslationY(y);
        childView1.setLayoutParams(params);
        roomViewGroup.addView(childView1);
        saveViewInstance(childView1);
        TextView textView = (TextView) childView1.findViewById(R.id.roomName);
//        textView.setOnClickListener(onClickListener);
        childView=childView1;
        return childView1;
    }


     TextView roomName;
    ImageView add_equipment;
    public void saveViewInstance(final View childView){
         roomName = (TextView) childView.findViewById(R.id.room_name);
         add_equipment = (ImageView) childView.findViewById(R.id.add_equipment);

         roomName.setOnClickListener(onClickListener);
        //注册监听事件
            roomName.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View v) {
                for (int i = 0; i < room_list.size(); i++) {
                    if(childView.getTag() == room_list.get(i).getView().getTag()){
                        SharedPreferences sp = getActivity().getSharedPreferences("room_postion", MODE_PRIVATE);
                        sp.edit().putInt("room_postion", i);
                        Toast.makeText(getActivity(),room_list.get(i).getRoomId()+"",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(),RoomTypesActivity.class);
                        int roomId = room_list.get(i).getRoomId();
                        sp.edit().putInt("roomId",roomId);
                        sp.edit().apply();
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
                Intent intent = new Intent(getActivity(), AddEquipmentActivity.class);
                startActivity(intent);
            }
        });

        childView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            String returnName = sp.getString("returnRoomName","卧室");
            Map<String,Object> params = new HashMap<>();
            params.put("returnName",returnName);
            params.put("roomId",roomId);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/changeRoomType",params);
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
