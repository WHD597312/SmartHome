package com.xinrui.smart.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xinrui.database.dao.daoimpl.RoomEntryDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.CustomAdapter;
import com.xinrui.smart.fragment.LiveFragment;
import com.xinrui.smart.pojo.MergeRoom;
import com.xinrui.smart.pojo.Room;
import com.xinrui.smart.pojo.RoomEntry;
import com.xinrui.smart.util.ListDataSave;
import com.xinrui.smart.util.MessageEvent;
import com.xinrui.smart.view_custom.MyGridView;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

/**
 * Created by win7 on 2018/3/12.
 */

public class CustomRoomActivity extends AppCompatActivity {
    @BindView(R.id.return_button)
    ImageButton returnButton;
    @BindView(R.id.title_text)
    TextView titleText;
    @BindView(R.id.customRooms)
    MyGridView customRooms;
    @BindView(R.id.merge)
    Button merge;
    @BindView(R.id.resolution)
    Button resolution;
    @BindView(R.id.sure)
    Button sure;

    RoomEntryDaoImpl roomEntryDao;

    CustomAdapter customAdapter;


    int clickTemp = 0; //点击item

    int longTemp = 0;//长按item

    int blinkTemp = 0;//闪烁item

    boolean ismerge = true; //是否合并

    boolean isblinked = false; //是否闪烁

    int rooms = 32;//初始房间数

    List<RoomEntry> roomEntries_list = new ArrayList<>();

    boolean isresolution = false;

    Vibrator vibrator;

    int clickedList[] = new int[rooms];//这个数组用来存放item的点击状态

    int blinkList[] = new int[rooms];//这个数组用来存放item的闪烁状态

    List<Room> roomlist = new ArrayList();

    Room room = new Room();

    List<Integer> list_color = new ArrayList();//点击变色的item

    List<Integer> list_state = new ArrayList();//合并后的item

    List<List<Integer>> list_resolution = new ArrayList<>();//合并的房间

    List<Integer> list_blink = new ArrayList<>();//闪烁的item

    List<Integer> list_no_blink = new ArrayList<>();//未闪烁的item

    List<Integer> list_colors_remove = new ArrayList();

    List<Integer> list_colors_add = new ArrayList();

    List<RoomEntry> roomEntries = new ArrayList<>();

    List<List<Double>> list_resolution1;

    int current_key;

    ListDataSave listDataSave;
     Context mContext;

    List<Integer> list1;

    List<Integer> list;
    int colors[] = {R.drawable.merge_room,R.drawable.merge_room1,R.drawable.merge_room2,R.drawable.merge_room3,R.drawable.merge_room4,
                    R.drawable.merge_room5,R.drawable.merge_room6,R.drawable.merge_room7};

    /**
     * 点击gridview一个item变色，再次点击还原
     * @param position
     */
    @OnItemClick(R.id.customRooms) void onItemClick(View view,int position) {
        setSeclection(position);
        if(list_state.contains(position)){
//            room_blink(position);
        }else{
                click_items(position,view);
            }
        customAdapter.notifyDataSetChanged();
    }

    /**
     * 合并之后长按闪烁，再次长按取消闪烁，闪烁中点击拆分删除房间
     * @param position
     * @return
     */
    @OnItemLongClick(R.id.customRooms)
        boolean gridviewItemLongClick(int position) {
        setLongSeclection(position);
        if(list_state.contains(position)){
            //震动300毫秒
            vibrator.vibrate(300);
            room_blink(position);
        }
        customAdapter.notifyDataSetChanged();
        return true;
    }

    /**
     * 识别当前被点击的item
     * @param postion
     */
    public void setSeclection(int postion) {
        clickTemp = postion;
    }

    /**
     * 识别当前被点击的item
     * @param postion
     */
    public void setLongSeclection(int postion) {
        longTemp = postion;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_room);
        ButterKnife.bind(this);


        Intent intent = getIntent();
        current_key = intent.getIntExtra("current_key",0);
        Bundle bundle = this.getIntent().getExtras();
         list1 = bundle.getIntegerArrayList("list1");



        //初始化数据
        initRooms();

    }

    @Override
    protected void onStart() {
        super.onStart();
        roomEntryDao=new RoomEntryDaoImpl(this);

    }

    public void initcolors(){
        list_colors_remove.add(R.drawable.merge_room);
        list_colors_remove.add(R.drawable.merge_room1);
        list_colors_remove.add(R.drawable.merge_room2);
        list_colors_remove.add(R.drawable.merge_room3);
        list_colors_remove.add(R.drawable.merge_room4);
        list_colors_remove.add(R.drawable.merge_room5);
        list_colors_remove.add(R.drawable.merge_room6);
        list_colors_remove.add(R.drawable.merge_room7);
        list_colors_remove.add(R.drawable.merge_room8);
        list_colors_remove.add(R.drawable.merge_room9);
        list_colors_remove.add(R.drawable.merge_room10);
        list_colors_remove.add(R.drawable.merge_room11);
        list_colors_remove.add(R.drawable.merge_room12);
        list_colors_remove.add(R.drawable.merge_room13);
        list_colors_remove.add(R.drawable.merge_room14);
        list_colors_remove.add(R.drawable.merge_room15);
        list_colors_remove.add(R.drawable.merge_room16);
        list_colors_remove.add(R.drawable.merge_room17);
        list_colors_remove.add(R.drawable.merge_room18);
    }
    private void initRooms() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        titleText.setText("自定义户型");
        reset_list_no_blink();
        customAdapter = new CustomAdapter(this,roomlist);
        customRooms.setAdapter(customAdapter);

        customAdapter.notifyDataSetChanged();
        initcolors();

        setHandle();


    }

    public void setHandle(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                /**
                 * 延时执行的代码
                 */
                for (int i = 0; i < list1.size(); i++) {
                    View view4 = customRooms.getChildAt(list1.get(i)-customRooms.getFirstVisiblePosition());
                    view4.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.merge_room3);
                }

                customAdapter.notifyDataSetChanged();
            }
        },100); // 延时0.1秒
    }

    @OnClick({R.id.return_button,R.id.merge, R.id.resolution, R.id.sure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.return_button:
                finish();
            break;
            case R.id.merge:

                if(list_color.size() == 0){

                }else {
                    AlertDialog.Builder dialog2 = new AlertDialog.Builder(this);
                    dialog2.setMessage("确认合并!");
                    dialog2.setCancelable(false);
                    dialog2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            merge();
                        }
                    });
                    dialog2.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog2.show();
                }

                break;
            case R.id.resolution:
                Toast.makeText(this,list_color+";"+list_state,Toast.LENGTH_LONG).show();

                if(list_state.size()==0 || list_color.size()!=0){

                }else if(isblinked&&blinkList[longTemp]==1){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("确认拆分!");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resolution(longTemp);
                            //数据库中移除删除房间
                            View view = customRooms.getChildAt(longTemp);
                            int x = view.getLeft()+view.getWidth()/2;
                            int y = view.getTop()+view.getHeight()/2;
                            Log.i("roomEntries_list1",roomEntries_list.size()+"");
                            for (int j = 0; j < roomEntries_list.size(); j++) {

                                if(roomEntries_list.get(j).getX()<x&& x<(roomEntries_list.get(j).getX()+roomEntries_list.get(j).getWidth())&&roomEntries_list.get(j).getY()<y&&y<(roomEntries_list.get(j).getY()+roomEntries_list.get(j).getHeight())){
                                    roomEntries_list.remove(j);
                                }
                            }
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }
                break;
            case R.id.sure:


                SharedPreferences sharedPreferences = this.getSharedPreferences("data",0);
                int group1 = sharedPreferences.getInt("btn1_group",0);
                int group2 = sharedPreferences.getInt("btn2_group",0);
                int group3 = sharedPreferences.getInt("btn3_group",0);
                int group4 = sharedPreferences.getInt("btn4_group",0);

                List<RoomEntry> list=roomEntryDao.findAllByGroup(current_key);
                roomEntryDao.deleteAll(list);

                roomEntryDao.findAllByGroup(current_key);

                if (!roomEntries_list.isEmpty()){
                    roomEntryDao.insertAll(roomEntries_list,current_key);
                    finish();
                }
                break;
        }
    }

    //拆分
    public void resolution(int postion){
            for (int i = 0; i < rooms; i++) {
                View view = customRooms.getChildAt(i);
                view.setVisibility(View.VISIBLE);
            }


            for (int i = 0; i < list_resolution.size(); i++) {
                for (int l = 0; l < list_resolution.get(i).size(); l++) {
                    if (list_resolution.get(i).get(l).equals(postion)){
                        for (int j = 0; j < list_resolution.get(i).size(); j++) {
                            View view3 = customRooms.getChildAt(list_resolution.get(i).get(j));
                            customRooms.stopFlick(view3);
                            view3.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
                        }

                        for (int k = 0; k < list_resolution.get(i).size(); k++) {//拆分以后将每个clickedList[postion]置为0
                            clickedList[list_resolution.get(i).get(k)]=0;
                        }

                        for (int j = 0; j < list_resolution.get(i).size(); j++) {
                            list_state.remove(list_resolution.get(i).get(j));
                        }
                        for (int k = 0; k < list_blink.size(); k++) {//list_bink集合里的所有binkList[postion]=0
                            blinkList[list_blink.get(k)] = 0;
                        }

                        for (int j = 0; j < list_resolution.get(i).size(); j++) {
                            RoomEntry roomEntry=roomEntryDao.findById(j);
                        }

                        list_resolution.remove(list_resolution.get(i));
                        list_blink.clear();
                        reset_list_no_blink();
                        break;
                    }
                }
            }
    }
    //合并
    Random random = new Random();
    int a = random.nextInt(colors.length);

    public void merge(){
         List<Integer> list_color1 = new ArrayList<>();//由于list_color对象固定，所以用list_color1替代list_color，每次合并new一个新的list_color1对象

        Log.i("jjy",list_resolution+";"+list_color);
        for (int i = 0; i < list_color.size(); i++) {
            list_color1.add(list_color.get(i));
        }
        list_resolution.add(list_color1);
        Log.i("jjy",list_resolution+";"+list_color);


        for (int i = 0; i < list_color.size(); i++) {
            list_state.add(list_color.get(i));
        }

        isrectangle(list_color,list_color1);


        if(ismerge){
            Log.i("a3","sdaf"+ismerge);
            for (int i = 0; i < list_color.size(); i++) {
                View view4 = customRooms.getChildAt(list_color.get(i));
                view4.findViewById(R.id.cusromroom_text).setBackgroundResource(list_colors_remove.get(a));
            }
            a = random.nextInt(list_colors_remove.size());
            merge_house();
            ismerge = false;
        }
        customAdapter.notifyDataSetChanged();
        list_color.clear();
    }

    /**
     * List集合去重
     * @param list
     */
    public  static   void  removeDuplicate(List list)   {
        HashSet h  =   new  HashSet(list);
        list.clear();
        list.addAll(h);
        System.out.println(list);
    }

    /**
     * 房间闪烁
     * @param postion
     */
    public void room_blink(int postion){
        tag:for (int i = 0; i < list_resolution.size(); i++) {
            for (int j = 0; j < list_resolution.get(i).size(); j++) {
                if(list_resolution.get(i).contains(postion)){
                    if (blinkList[postion] == 0) {
                        for (int k = 0; k < list_resolution.get(i).size(); k++) {//选中的房间闪烁
                            list_blink.add(list_resolution.get(i).get(k));//list_bink集合添加数据
                            list_no_blink.removeAll(list_blink);
                            removeDuplicate(list_blink);
                            removeDuplicate(list_no_blink);
                            Log.i("this",list_blink+""+list_no_blink);
                            View view_start_blink = customRooms.getChildAt(list_resolution.get(i).get(k));
                            customRooms.startFlick(view_start_blink);
                        }
                        for (int k = 0; k < list_no_blink.size(); k++) {//list_no_blink集合的item隐藏
                            View view5 = customRooms.getChildAt(list_no_blink.get(k));
                            view5.setVisibility(View.GONE);
                        }
                        isblinked = true;
                        for (int k = 0; k < list_blink.size(); k++) {//list_bink集合里的所有binkList[postion]=1
                            blinkList[list_blink.get(k)] = 1;
                        }
                        break tag;
                    }else if(blinkList[postion] == 1){
                        for (int k = 0; k < list_blink.size(); k++) {
                            Log.i("this1",list_blink+""+list_no_blink);
                            View view_stop_blink = customRooms.getChildAt(list_resolution.get(i).get(k));
                            customRooms.stopFlick(view_stop_blink);
                        }
                        for (int l = 0; l < list_no_blink.size(); l++) {
                            View view5 = customRooms.getChildAt(list_no_blink.get(l));
                            view5.setVisibility(View.VISIBLE);
                        }
                        for (int k = 0; k < list_blink.size(); k++) {
                            blinkList[list_blink.get(k)] = 0;
                        }
                        list_blink.clear();
                        isblinked = false;
                        reset_list_no_blink();
                        break tag;
                    }
                }
            }
        }
    }

    /**
     * 点击item变色
     * @param postion
     * @param view
     */
    public void click_items(int postion, View view){
        if(roomlist != null) {
            if (clickedList[postion] == 0) {
                list_color.add(postion);
                view.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_sure);
                clickedList[postion] = 1;
            } else if(clickedList[postion] == 1){
                for (int i = 0; i < list_color.size(); i++) {
                    if(list_color.get(i) == postion)
                        list_color.remove(i);
                }
                view.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
                clickedList[postion] = 0;
            }
        }
    }

    //重置list_no_blink集合
    public void reset_list_no_blink(){
        for (int o = 0; o < rooms; o++) {
            list_no_blink.add(o);
        }
    }

    //判断是否矩形
    public void isrectangle(List<Integer> list_color, List<Integer> list_color1){
        ismerge = true;
        int max = Collections.max(list_color);
        int min = Collections.min(list_color);
        int y_max = (max%4)+1;
        int x_max = (max/4)+1;

        int x_min = (min/4)+1;
        int y_min = (min%4)+1;

        int x = x_max-x_min+1;
        int y = y_max-y_min+1;

        int sum = Math.abs(x)*Math.abs(y);

        if(sum == list_color.size() && list_color.size()>1){
            Log.i("a","sdf");
            for (int i = 0; i < list_color.size(); i++) {
                    if(list_color.contains(list_color.get(i)+1)|list_color.contains(list_color.get(i)-1)|
                            list_color.contains(list_color.get(i)+4)|list_color.contains(list_color.get(i)-4)){
                        Log.i("tt","sd");
                    }else{
                        dialog(list_color1);
                        for (int k = 0; k < list_color.size(); k++) {
                            View view4 = customRooms.getChildAt(list_color.get(k));
                            view4.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
                        }
                        list_state.removeAll(list_color);
                        for (int l = 0; l < list_color.size(); l++) {
                            clickedList[list_color.get(l)] = 0;
                        }
                        Log.i("tt","sd");
                        list_color.clear();
                        ismerge = false;
                    }
            }
        }else if(list_color.size() < 2){
            Log.i("a1","sdf");
            dialog(list_color1);
            for (int i = 0; i < list_color.size(); i++) {
                View view4 = customRooms.getChildAt(list_color.get(i));
                view4.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
            }
            list_state.removeAll(list_color);
            for (int i = 0; i < list_color.size(); i++) {
                clickedList[list_color.get(i)] = 0;
            }
            list_color.clear();
            ismerge = false;
        }else {
            Log.i("a2","sdf");
            dialog(list_color1);
            for (int i = 0; i < list_color.size(); i++) {
                View view4 = customRooms.getChildAt(list_color.get(i));
                view4.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
            }
            list_state.removeAll(list_color);
            for (int i = 0; i < list_color.size(); i++) {
                clickedList[list_color.get(i)] = 0;
            }
            list_color.clear();
            ismerge = false;
            Log.i("us",list_color+""+ismerge);
            Log.i("us",list_color+"");
        }
    }
    public void dialog(final List list_color1){
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle("error");
    dialog.setMessage("无法合并");
    dialog.setCancelable(false);
    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            list_resolution.remove(list_color1);
        }
    });
    dialog.show();

}


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //把合并房间的对象放到数组
    public void merge_house(){
        View view = customRooms.getChildAt(Collections.min(list_color));
        View view1 = customRooms.getChildAt(Collections.max(list_color));
        int x =  view.getLeft();
        int y =  view.getTop();
        int width = view1.getRight()-view.getLeft();
        int height = view1.getBottom()-view.getTop();
        RoomEntry roomEntry = new RoomEntry(x,y,width,height);
        roomEntry.setGroup(current_key);
        roomEntries_list.add(roomEntry);
        Log.i("roomEntries_list=",roomEntries_list.size()+"");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



}
