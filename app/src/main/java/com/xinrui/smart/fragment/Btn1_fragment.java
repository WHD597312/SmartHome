package com.xinrui.smart.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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


/**
 * Created by win7 on 2018/3/13.
 */

public class Btn1_fragment extends Fragment {
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

    List<RoomEntry> list ;
    List<Room> list_room;

    public List<View> childView_list  = new ArrayList<>();
    SharedPreferences sharedPreferences;

    private List<Integer> startPoint_list = new ArrayList<>();
    private List<Integer> roomId_list = new ArrayList<>();
    private Long house_id;
    DeviceGroupDaoImpl deviceGroupDao;
    List<DeviceGroup> DeviceGroup;
    GetUrl getUrl = new GetUrl();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view_background = (RoomViewGroup) inflater.inflate(R.layout.rooms_background1, container, false);
        roomViewGroup = (FrameLayout) view_background.findViewById(R.id.fl);
        initView();
        getHttp();
//        roomEntryDao = new RoomEntryDaoImpl(getActivity());
//        list=roomEntryDao.findAllByGroup(group1);
        return view_background;
    }
    public void getHttp(){
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        final int width = wm.getDefaultDisplay().getWidth();
        final int item_width = width/4;
        sharedPreferences = getActivity().getSharedPreferences("data",0);
        house_id = sharedPreferences.getLong("house_id",0);
        Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            List<String> strings = (List<String>) msg.obj;
                            for (String result:strings) {
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    int code = jsonObject.getInt("code");
                                    if (code == 2000) {
                                        JSONObject content = jsonObject.getJSONObject("content");
                                            JSONArray array = content.getJSONArray(1 + "");
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject object = array.getJSONObject(i);
                                                int roomId = object.getInt("roomId");
                                                int startPoint = object.getInt("startPoint");
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
                                                RoomEntry roomEntry = new RoomEntry(x_min,y_min,width_room,height_room);
                                                setLayout(roomEntry.getX(),roomEntry.getY(),roomEntry.getWidth(),roomEntry.getHeight());
                                            }
                                        }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                JSONArray jsonArray = new JSONArray();
                                for (int i = 0; i < roomId_list.size(); i++) {
                                    roomId_list.get(i);
                                    Log.i("roomId_list.get(i)", roomId_list.get(i) + "");
                                    jsonArray.put(roomId_list.get(i));
                                }
                            }

                            break;

                        default:
                            break;
                    }
                }

            };

        QueryAllRoomAsyncTask queryAllRoomAsyncTask = new QueryAllRoomAsyncTask(handler);
        queryAllRoomAsyncTask.execute();
    }
    class QueryAllRoomAsyncTask extends AsyncTask<Void,Void,List<String>>{

        Handler mHandler;

        public QueryAllRoomAsyncTask(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            int code=0;
//            String result = null;
            List<String> strings=new ArrayList<>();
            deviceGroupDao = new DeviceGroupDaoImpl(getActivity());
            DeviceGroup = deviceGroupDao.findAllDevices();
//            for (int i = 0; i < DeviceGroup.size(); i++) {
//                Long houseId = DeviceGroup.get(i).getId();
            Map<String, Object> params = new HashMap<>();
            params.put("houseId", house_id);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/findAllRoom", params);
            String result = HttpUtils.getOkHpptRequest(url);
            try{
                if(!Utils.isEmpty(result)){
                    strings.add(result);
                    JSONObject jsonObject = new JSONObject();
                    if(code == 2000){
                        JSONArray content =jsonObject.getJSONArray("content");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
//            }
            return strings;
        }
        @Override
        protected void onPostExecute(List<String> strings) {
            Message msg = mHandler.obtainMessage();
            if(strings!=null && !strings.isEmpty()){
                msg.what = 1;
                msg.obj = strings;
            }else{
                msg.what = 2;
            }
            mHandler.sendMessage(msg);
        }

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (null == list || list.size() ==0) {
            Log.i("sdf","asdf");
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
        Log.i("view2","x="+x+";"+"y="+y+";"+"width="+width+";"+"height="+height);
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

        list_room = new ArrayList<>();
    }

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

        return childView1;
    }

    public void saveViewInstance(final View childView){

        Room room = new Room();
        room.setId(roomId);
        TextView roomName = (TextView) childView.findViewById(R.id.room_name);
        ImageView add_equipment = (ImageView) childView.findViewById(R.id.add_equipment);

        //注册监听事件
        roomName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RoomTypesActivity.class);
                startActivity(intent);
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
                if(isSelected){
                    v.setBackgroundResource(R.drawable.mergeroom_background);
                    childView_list.remove(v);
                    isSelected = false;
                    Log.i("childView_list",childView_list.size()+"");
                }else {
                    v.setBackgroundResource(R.drawable.select_mergeroom_background);
                    childView_list.add(v);
                    isSelected = true;
                }
                return true;
            }
        });
        Log.i("childView_list2",childView_list.size()+"");
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
        Log.i("sss1", "fragment---------------------");


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
