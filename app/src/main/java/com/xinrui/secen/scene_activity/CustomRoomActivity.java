package com.xinrui.secen.scene_activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.RoomEntryDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_adapter.CustomAdapter;
import com.xinrui.secen.scene_pojo.Room;
import com.xinrui.secen.scene_pojo.RoomEntry;
import com.xinrui.secen.scene_util.GetUrl;
import com.xinrui.secen.scene_view_custom.CustomDialog;
import com.xinrui.secen.scene_view_custom.MyGridView;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    Long houseId;
    int clickTemp = 0; //点击item

    int longTemp = 0;//长按item

    int blinkTemp = 0;//闪烁item

    boolean ismerge = true; //是否合并

    boolean isblinked = false; //是否闪烁

    int rooms = 32;//初始房间数

    List<RoomEntry> roomEntries_list = new ArrayList<>();

    GetUrl getUrl = new GetUrl();
    Vibrator vibrator;
    DeviceGroupDaoImpl deviceGroupDao;
    @BindView(R.id.homepage)
    ImageButton homepage;

    int clickedList[] = new int[rooms];//这个数组用来存放item的点击状态

    int blinkList[] = new int[rooms];//这个数组用来存放item的闪烁状态

    List<Room> roomlist = new ArrayList();

    List<Integer> list_color = new ArrayList();//点击变色的item

    List<Integer> list_state = new ArrayList();//合并后的item

    List<List<Integer>> list_resolution = new ArrayList<>();//合并的房间

    List<Integer> list_blink = new ArrayList<>();//闪烁的item

    List<Integer> list_no_blink = new ArrayList<>();//未闪烁的item

    List<Integer> list_colors_remove = new ArrayList();

    int current_key;
    long house_id;

    private CustomDialog.Builder builder1;
    private CustomDialog.Builder builder2;
    private CustomDialog mDialog;
    private CustomDialog mDialog1;
    String url = "http://47.98.131.11:8082/warmer/v1.0/room/registerRoom";

    List<List<Integer>> list_all = new ArrayList<>();
    int colors[] = {R.drawable.merge_room, R.drawable.merge_room1, R.drawable.merge_room2, R.drawable.merge_room3,
            R.drawable.merge_room5, R.drawable.merge_room6, R.drawable.merge_room7,
            R.drawable.merge_room8, R.drawable.merge_room9, R.drawable.merge_room10, R.drawable.merge_room11,
            R.drawable.merge_room12, R.drawable.merge_room13, R.drawable.merge_room14, R.drawable.merge_room15,
            R.drawable.merge_room16, R.drawable.merge_room17, R.drawable.merge_room18};



    /**
     * 点击gridview一个item变色，再次点击还原
     *
     * @param position
     */
    @OnItemClick(R.id.customRooms)
    void onItemClick(View view, int position) {
        setSeclection(position);
        if (list_state.contains(position)) {
//            room_blink(position);
        } else {
            click_items(position, view);
        }
        customAdapter.notifyDataSetChanged();
    }

    /**
     * 合并之后长按闪烁，再次长按取消闪烁，闪烁中点击拆分删除房间
     *
     * @param position
     * @return
     */
    @OnItemLongClick(R.id.customRooms)
    boolean gridviewItemLongClick(int position) {
        setLongSeclection(position);
        if (list_state.contains(position)) {
            //震动300毫秒
            vibrator.vibrate(300);
            room_blink(position);
        }
        customAdapter.notifyDataSetChanged();
        return true;
    }

    /**
     * 识别当前被点击的item
     *
     * @param postion
     */
    public void setSeclection(int postion) {
        clickTemp = postion;
    }

    /**
     * 识别当前被点击的item
     *
     * @param postion
     */
    public void setLongSeclection(int postion) {
        longTemp = postion;
    }

    MyApplication application;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_room);
        ButterKnife.bind(this);
        builder1 = new CustomDialog.Builder(this);
        builder2 = new CustomDialog.Builder(this);
        Intent intent = getIntent();
        current_key = intent.getIntExtra("current_key", 0);//是第几层传过来的房间
        house_id = intent.getLongExtra("house_Id",0);//传递过来的houseId
        Bundle bundle = this.getIntent().getExtras();
        int list_all_size = bundle.getInt("list_all_size");//获取到所有房间个数
        for (int i = 0; i < list_all_size; i++) {
            list_all.add(bundle.getIntegerArrayList("list_" + i));//所有房间放到list集合里
        }
        //初始化数据
        initRooms();
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        roomEntryDao = new RoomEntryDaoImpl(this);

    }

    public void initcolors() {
        list_colors_remove.add(R.drawable.merge_room);
        list_colors_remove.add(R.drawable.merge_room1);
        list_colors_remove.add(R.drawable.merge_room2);
        list_colors_remove.add(R.drawable.merge_room3);
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

        deviceGroupDao = new DeviceGroupDaoImpl(this);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        titleText.setText("自定义户型");
        reset_list_no_blink();
        customAdapter = new CustomAdapter(this, roomlist);
        customRooms.setAdapter(customAdapter);

        customAdapter.notifyDataSetChanged();
        initcolors();

        if (list_all.size() == 0) {

        } else {
            setHandle();
        }


    }


    public void setHandle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 * 延时执行的代码
                 */
                for (int i = 0; i < list_all.size(); i++) {
                    for (int j = 0; j < list_all.get(i).size(); j++) {
                        View view4 = customRooms.getChildAt((list_all.get(i).get(j)) - customRooms.getFirstVisiblePosition());
                        view4.findViewById(R.id.cusromroom_text).setBackgroundResource(list_colors_remove.get(a));
                    }
                    a = random.nextInt(list_colors_remove.size());

                    View view = customRooms.getChildAt(Collections.min(list_all.get(i)));
                    View view1 = customRooms.getChildAt(Collections.max(list_all.get(i)));
                    int x = view.getLeft();
                    int y = view.getTop();
                    int width = view1.getRight() - view.getLeft();
                    int height = view1.getBottom() - view.getTop();
                    RoomEntry roomEntry = new RoomEntry(x, y, width, height);
                    roomEntry.setGroup(current_key);
                    roomEntries_list.add(roomEntry);
                }
                customAdapter.notifyDataSetChanged();
            }
        }, 100); // 延时0.1秒

        //保存list_resolution,list_state集合
        list_resolution.addAll(list_all);
        for (int i = 0; i < list_all.size(); i++) {
            for (int j = 0; j < list_all.get(i).size(); j++) {
                list_state.add(list_all.get(i).get(j));
            }
        }
    }

    @OnClick({R.id.return_button, R.id.merge, R.id.resolution, R.id.sure,R.id.homepage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.homepage:
                Intent intent1 = new Intent(this,MainActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("return_homepage", "return_homepage");
                intent1.putExtras(bundle1);
                startActivity(intent1);
                break;
            case R.id.return_button:
                Intent intent = new Intent(this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Activity_return", "Activity_return");
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.merge:
                final List<Integer> list_color1 = new ArrayList<>();//由于list_color对象固定，所以用list_color1替代list_color，每次合并new一个新的list_color1对象

                if (list_color.size() == 0) {

                } else {
                    showDoubleButtonDialog("确定合并吗!", "确定", "取消",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    merge(list_color1);
                                    mDialog.dismiss();
                                    list_color.clear();
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for (int k = 0; k < list_color.size(); k++) {
                                        View view4 = customRooms.getChildAt(list_color.get(k));
                                        view4.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
                                    }
                                    list_state.removeAll(list_color);
                                    for (int l = 0; l < list_color.size(); l++) {
                                        clickedList[list_color.get(l)] = 0;
                                    }
                                    list_color.clear();
                                    ismerge = false;
//                                    list_resolution.remove(list_color1);
                                    list_color.clear();
                                    mDialog.dismiss();
                                }
                            });
                }
                break;
            case R.id.resolution:
                if (list_state.size() == 0) {

                } else if (isblinked && blinkList[longTemp] == 1) {
                    showDoubleButtonDialog("确定拆分吗!", "确定", "取消",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    resolution(longTemp);
                                    //数据库中移除删除房间
                                    View view = customRooms.getChildAt(longTemp);
                                    int x = view.getLeft() + view.getWidth() / 2;
                                    int y = view.getTop() + view.getHeight() / 2;
                                    for (int j = 0; j < roomEntries_list.size(); j++) {
                                        if (roomEntries_list.get(j).getX() < x && x < (roomEntries_list.get(j).getX() + roomEntries_list.get(j).getWidth()) && roomEntries_list.get(j).getY() < y && y < (roomEntries_list.get(j).getY() + roomEntries_list.get(j).getHeight())) {
                                            roomEntries_list.remove(j);
                                        }
                                    }
                                    mDialog.dismiss();
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDialog.dismiss();
                                }
                            });
                }
                break;
            case R.id.sure:
                //获取item宽
                WindowManager wm = (WindowManager) this
                        .getSystemService(Context.WINDOW_SERVICE);
                int width = wm.getDefaultDisplay().getWidth() / 4;

                if (list_resolution != null) {
                    JSONArray jsonArray = new JSONArray();
                    SharedPreferences sharedPreferences = this.getSharedPreferences("data", 0);
                    long house_id = sharedPreferences.getLong("house_id", 0);
                    for (int i = 0; i < list_resolution.size(); i++) {
                        int startPoint = Collections.min(list_resolution.get(i)) + 100;

                        try {
                            JSONObject jsonObject = new JSONObject();

                            jsonObject.put("houseId", house_id);
                            jsonObject.put("layer", current_key);
                            jsonObject.put("color", getRandomColor());
                            jsonObject.put("startPoint", startPoint);


                            JSONArray array = new JSONArray();
                            for (int j = 0; j < list_resolution.get(i).size(); j++) {
                                array.put(list_resolution.get(i).get(j) + 100);
                            }
                            jsonObject.put("points", array);
                            //获取行列

                            View view_min = customRooms.getChildAt(Collections.min(list_resolution.get(i)));
                            View view_max = customRooms.getChildAt(Collections.max(list_resolution.get(i)));
                            int x = view_min.getLeft();
                            int y = view_min.getTop();
                            int columns = (view_max.getRight() - view_min.getLeft()) / width;
                            int rows = (view_max.getBottom() - view_min.getTop()) / width;


                            jsonObject.put("rows", rows);
                            jsonObject.put("columns", columns);

                            jsonArray.put(jsonObject);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(jsonArray == null||jsonArray.length() == 0){
                        new DeleteRoomAsyncTask().execute();
                    }else {
                        new CustomRoomAsyncTask().execute(jsonArray);
                    }

                    //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
                    Intent intent2 = new Intent(this, MainActivity.class);
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("Activity_return", "Activity_return");
                    intent2.putExtras(bundle2);
                    startActivity(intent2);
                }
                break;
        }
    }

    class DeleteRoomAsyncTask extends AsyncTask<JSONArray, Void, Integer> {
        @Override
        protected Integer doInBackground(JSONArray... jsonArrays) {
            int code = 0;
            Map<String, Object> params = new HashMap<>();
            long houseId = house_id;
            int layer = current_key;
            params.put("houseId", houseId);
            params.put("layer", layer);
            String url = getUrl.getRqstUrl("http://47.98.131.11:8082/warmer/v1.0/room/cleanRoom", params);
            String result = HttpUtils.getOkHpptRequest(url);

            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    String message = jsonObject.getString("message");
                    if (code == 2000) {
                        JSONObject content = jsonObject.getJSONObject("content");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }
    }
    //新建房间并提交服务器
    class CustomRoomAsyncTask extends AsyncTask<JSONArray, Void, Integer> {
        @Override
        protected Integer doInBackground(JSONArray... s) {
            int code = 0;
            JSONArray params = s[0];
            String result = HttpUtils.postOkHpptRequest2(url, params);

                if (!Utils.isEmpty(result)) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getInt("code");
                        String message = jsonObject.getString("message");
                        if (code == 2000) {
                            JSONObject content = jsonObject.getJSONObject("content");
                        } else if (code == 4001) {
                            String error = jsonObject.getString("error");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            return code;
        }
    }

    public static String getRandomColor() {
        Random random = new Random();
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < 2; i++) {
            //       result=result*10+random.nextInt(10);
            int temp = random.nextInt(16);
            r = r * 16 + temp;
            temp = random.nextInt(16);
            g = g * 16 + temp;
            temp = random.nextInt(16);
            b = b * 16 + temp;
        }
        return r + "," + g + "," + b;
    }

    //拆分
    public void resolution(int postion) {
        for (int i = 0; i < rooms; i++) {
            View view = customRooms.getChildAt(i);
            view.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < list_resolution.size(); i++) {
            for (int l = 0; l < list_resolution.get(i).size(); l++) {
                if (list_resolution.get(i).get(l).equals(postion)) {
                    for (int j = 0; j < list_resolution.get(i).size(); j++) {
                        View view3 = customRooms.getChildAt(list_resolution.get(i).get(j));
                        customRooms.stopFlick(view3);
                        view3.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
                    }

                    for (int k = 0; k < list_resolution.get(i).size(); k++) {//拆分以后将每个clickedList[postion]置为0
                        clickedList[list_resolution.get(i).get(k)] = 0;
                    }

                    for (int j = 0; j < list_resolution.get(i).size(); j++) {
                        list_state.remove(list_resolution.get(i).get(j));
                    }
                    for (int k = 0; k < list_blink.size(); k++) {//list_bink集合里的所有binkList[postion]=0
                        blinkList[list_blink.get(k)] = 0;
                    }

                    for (int j = 0; j < list_resolution.get(i).size(); j++) {
                        RoomEntry roomEntry = roomEntryDao.findById(j);
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

    public void merge(List<Integer> list_color1) {

        for (int i = 0; i < list_color.size(); i++) {
            list_color1.add(list_color.get(i));
        }


        for (int i = 0; i < list_color.size(); i++) {
            list_state.add(list_color.get(i));
        }

        isrectangle(list_color,list_color1);


        if (ismerge) {
            for (int i = 0; i < list_color.size(); i++) {
                View view4 = customRooms.getChildAt(list_color.get(i));
                view4.findViewById(R.id.cusromroom_text).setBackgroundResource(colors[a]);
            }
            a = random.nextInt(list_colors_remove.size());
            merge_house();
            ismerge = false;
            list_resolution.add(list_color1);
        }
        customAdapter.notifyDataSetChanged();
    }

    /**
     * List集合去重
     *
     * @param list
     */
    public static void removeDuplicate(List list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
    }

    /**
     * 房间闪烁
     *
     * @param postion
     */
    public void room_blink(int postion) {
        tag:
        for (int i = 0; i < list_resolution.size(); i++) {
            for (int j = 0; j < list_resolution.get(i).size(); j++) {
                if (list_resolution.get(i).contains(postion)) {
                    if (blinkList[postion] == 0) {
                        for (int k = 0; k < list_resolution.get(i).size(); k++) {//选中的房间闪烁
                            list_blink.add(list_resolution.get(i).get(k));//list_bink集合添加数据
                            list_no_blink.removeAll(list_blink);
                            removeDuplicate(list_blink);
                            removeDuplicate(list_no_blink);
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
                    } else if (blinkList[postion] == 1) {
                        for (int k = 0; k < list_blink.size(); k++) {
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
     *
     * @param postion
     * @param view
     */
    public void click_items(int postion, View view) {
        if (roomlist != null) {
            if (clickedList[postion] == 0) {
                list_color.add(postion);
                view.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_sure);
                clickedList[postion] = 1;
            } else if (clickedList[postion] == 1) {
                for (int i = 0; i < list_color.size(); i++) {
                    if (list_color.get(i) == postion)
                        list_color.remove(i);
                }
                view.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
                clickedList[postion] = 0;
            }
        }
    }

    //重置list_no_blink集合
    public void reset_list_no_blink() {
        for (int o = 0; o < rooms; o++) {
            list_no_blink.add(o);
        }
    }

    //判断是否矩形
    public void isrectangle(List<Integer> list_color,List<Integer> list_color1) {
        ismerge = true;
        int max = Collections.max(list_color);
        int min = Collections.min(list_color);
        int y_max = (max % 4) + 1;
        int x_max = (max / 4) + 1;

        int x_min = (min / 4) + 1;
        int y_min = (min % 4) + 1;

        int x = x_max - x_min + 1;
        int y = y_max - y_min + 1;

        int sum = Math.abs(x) * Math.abs(y);

        if (sum == list_color.size() && list_color.size() > 1) {
            for (int i = 0; i < list_color.size(); i++) {
                if (list_color.contains(list_color.get(i) + 1) | list_color.contains(list_color.get(i) - 1) |
                        list_color.contains(list_color.get(i) + 4) | list_color.contains(list_color.get(i) - 4)) {
                } else {
                    dialog(list_color,list_color1);
                    for (int k = 0; k < list_color.size(); k++) {
                        View view4 = customRooms.getChildAt(list_color.get(k));
                        view4.findViewById(R.id.cusromroom_text).setBackgroundResource(R.drawable.addroom_no_sure);
                    }
                    list_state.removeAll(list_color);
                    for (int l = 0; l < list_color.size(); l++) {
                        clickedList[list_color.get(l)] = 0;
                    }
                    list_color.clear();
                    ismerge = false;
                }
            }
        } else if (list_color.size() < 2) {
            dialog(list_color,list_color1);
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
        } else {
            dialog(list_color,list_color1);
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
        }
    }

    public void dialog(final List list_color,final List<Integer> list_color1) {
        showSingleButtonDialog("无法合并!", "确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog1.dismiss();
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //把合并房间的对象放到数组
    public void merge_house() {
        View view = customRooms.getChildAt(Collections.min(list_color));
        View view1 = customRooms.getChildAt(Collections.max(list_color));
        int x = view.getLeft();
        int y = view.getTop();
        int width = view1.getRight() - view.getLeft();
        int height = view1.getBottom() - view.getTop();
        RoomEntry roomEntry = new RoomEntry(x, y, width, height);
        roomEntry.setGroup(current_key);
        roomEntries_list.add(roomEntry);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showDoubleButtonDialog(String alertText, String okText, String noText, View.OnClickListener okClickListener, View.OnClickListener cancelClickListener) {
        mDialog = builder1.setMessage(alertText)
                .setPositiveButton(okText, okClickListener)
                .setNegativeButton(noText,cancelClickListener)
                .createTwoButtonDialog();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private void showSingleButtonDialog(String alertText, String btnText, View.OnClickListener onClickListener) {
        mDialog1 = builder2.setMessage(alertText)
                .setSingleButton(btnText, onClickListener)
                .createSingleButtonDialog();
        mDialog1.show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
        Intent intent = new Intent(CustomRoomActivity.this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Activity_return", "Activity_return");
        intent.putExtras(bundle);
        startActivity(intent);
        return super.onKeyDown(keyCode, event);
    }
}
