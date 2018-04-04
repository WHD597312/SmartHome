package com.xinrui.smart.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
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

import com.xinrui.database.dao.daoimpl.RoomEntryDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddEquipmentActivity;
import com.xinrui.smart.activity.RoomContentActivity;
import com.xinrui.smart.activity.RoomTypesActivity;
import com.xinrui.smart.pojo.Room;
import com.xinrui.smart.pojo.RoomEntry;
import com.xinrui.smart.view_custom.RoomViewGroup;

import java.util.ArrayList;
import java.util.List;

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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


           view_background = (RoomViewGroup) inflater.inflate(R.layout.rooms_background1, container, false);
           roomViewGroup = (FrameLayout) view_background.findViewById(R.id.fl);

        initView();

        roomEntryDao = new RoomEntryDaoImpl(getActivity());

        list=roomEntryDao.findAllByGroup(group1);

        for (int i = 0; i < list.size(); i++) {
            RoomEntry roomEntry = list.get(i);
            setLayout(roomEntry.getX(), roomEntry.getY(), roomEntry.getWidth(), roomEntry.getHeight());
        }
        return view_background;
    }

    public int getlist(int i){
        return i;
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

    private void saveViewInstance(View childView){
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
