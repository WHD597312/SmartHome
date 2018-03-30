package com.xinrui.smart.fragment;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.RoomEntryDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddEquipmentActivity;
import com.xinrui.smart.activity.RoomTypesActivity;
import com.xinrui.smart.pojo.Room;
import com.xinrui.smart.pojo.RoomEntry;
import com.xinrui.smart.view_custom.RoomViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by win7 on 2018/3/13.
 */

public class Btn1_fragment extends Fragment {
//    Unbinder unbinder;

    //    @BindView(R.id.roomViewGroup)
//    RoomViewGroup roomViewGroup;
//    Unbinder unbinder1;
    int x;
    int y;
    int width;
    int height;
    private int roomId = 0;
    Unbinder unbinder;
//    @BindView(R.id.fl)
//    FrameLayout fl;
//    @BindView(R.id.empty_room_tv)
//    TextView tv;
    ImageView emptyRoom;
    private Context mContext;
    private RoomEntryDaoImpl roomEntryDao;

    RoomViewGroup view_background;
    FrameLayout roomViewGroup;

    List<RoomEntry> list ;
    List<Room> list_room;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//       view_background  = (LinearLayout) inflater.inflate(R.layout.rooms_background, container, false);


        Log.i("view2", "x=" + x + ";" + "y=" + y + ";" + "width=" + width + ";" + "height=" + height);
//        View view = LayoutInflater.from(getActivity()).inflate(R.layout.room_content,null);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        view_background.addView(view, layoutParams);
//        unbinder1 = ButterKnife.bind(this, view_background);

//        if (null != view_background) {
//            ViewGroup parent = (ViewGroup) view_background.getParent();
//                if (null != parent) {
//                    parent.removeView(view_background);
//            }
//        } else {
////            view_background = inflater.inflate(R.layout.rooms_background, null);
//            view_background= (LinearLayout) inflater.inflate(R.layout.rooms_background,null);
//        }

           view_background = (RoomViewGroup) inflater.inflate(R.layout.rooms_background, container, false);
           roomViewGroup = (FrameLayout) view_background.findViewById(R.id.fl);
//        emptyRoom = (ImageView) getActivity().findViewById(R.id.empty_room_iv);
//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tv.getLayoutParams();
//        layoutParams.height = 1500;


        initView();
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600, 900);
//        view_background.findViewById(R.id.tv).setLayoutParams(params);
        roomEntryDao = new RoomEntryDaoImpl(getActivity());

        list = roomEntryDao.findAll();
        for (int i = 0; i < list.size(); i++) {
            RoomEntry roomEntry = list.get(i);
//            setLayout(roomEntry.getX(),roomEntry.getY(),roomEntry.getWidth(),roomEntry.getHeight());
            Log.i("list1", "fragment---------------------" + roomEntry.getX() + "tv" + width + ";" + height);
            setLayout(roomEntry.getX(), roomEntry.getY(), roomEntry.getWidth(), roomEntry.getHeight());

        }

//        setLayout(0,0,600,100);
//        setLayout(30,40,100,100);
//        setLayout(60,40,100,100);
//        setLayout(270,540,600,400);
//        unbinder = ButterKnife.bind(this, view_background);
        return view_background;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        ImageView imageView = (ImageView) getView().findViewById(R.id.empty_room_iv);
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
//        unbinder.unbind();
    }

    public void initView(){
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        int width =  wm.getDefaultDisplay().getWidth();
         height = wm.getDefaultDisplay().getHeight();
//         view_background.findViewById(R.id.fl).setMinimumHeight((int) (width*2));
        ImageView imageView = (ImageView) roomViewGroup.findViewById(R.id.empty_room_iv);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(width,width*2));
        imageView.setMinimumHeight(view_background.getHeight());

        list_room = new ArrayList<>();
    }

    public View setLayout(int x, int y, int width, int height) {
//          setLayout(30,40,80,90);
        View childView = LayoutInflater.from(getActivity()).inflate(R.layout.room_content, null);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        childView.setTranslationX(x);
        childView.setTranslationY(y);
        childView.setLayoutParams(params);
        roomViewGroup.addView(childView);
        saveViewInstance(childView);
            /*定义LayoutParams 为了获得当前View的属性*/
//          设置View的高度，也可以设置其他属性
//           view.setLayoutParams(lpLayoutParams);
        return childView;
    }

    private void saveViewInstance(View childView){
        Room room = new Room();
        room.setId(roomId);
        TextView roomName = (TextView) childView.findViewById(R.id.room_name);
        ImageButton add_equipment = (ImageButton) childView.findViewById(R.id.add_equipment);

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


//        for (RoomEntry roomEntry:list){
//
//            setLayout(roomEntry.getX(),roomEntry.getY(),roomEntry.getWidth(),roomEntry.getHeight());
//        }

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
