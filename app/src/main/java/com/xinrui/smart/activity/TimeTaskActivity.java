package com.xinrui.smart.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.TimeTaskAdapter;
import com.xinrui.smart.adapter.WeekAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.ChineseNumber;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.CircleSeekBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * 定时任务
 */
public class TimeTaskActivity extends AppCompatActivity {
    public String OPEN_CLOSE = "";
    @BindView(R.id.img_back)
    ImageView img_back;//返回按钮
    Unbinder unbinder;
    @BindView(R.id.seekbar)
    CircleSeekBar seekbar;
    @BindView(R.id.tv_open)
    TextView tv_open;//开始时间
    @BindView(R.id.open_time)
    TextView open_time;//设定开始时间
    @BindView(R.id.close_time)
    TextView close_time;//结束时间

    @BindView(R.id.tv_temp)
    TextView tv_temp;//温度
    @BindView(R.id.tv_temp_num)
    TextView tv_temp_num;//温度计数
    @BindView(R.id.listview)
    ListView listview;
    /**
     * 开始时间，结束时间，温度列表
     */
    @BindView(R.id.linearout)
    LinearLayout linearout;
    @BindView(R.id.tv_clock)
    TextView tv_clock;
    @BindView(R.id.timePicker)
    NumberPicker timePicker;
    /**
     * 时间选择器
     */
    @BindView(R.id.numberPicker)
    NumberPicker numberPicker;
    /**
     * 数字选择器
     */
    @BindView(R.id.btn_copy)
    Button btn_copy;
    /**
     * 复制按钮
     */
    @BindView(R.id.btn_add)
    Button btn_add;
    /**
     * 添加时间
     */

    @BindView(R.id.tv_mon)
    TextView tv_mon;
    @BindView(R.id.tv_tue)
    TextView tv_tue;
    @BindView(R.id.tv_wen)
    TextView tv_wen;
    @BindView(R.id.tv_thu)
    TextView tv_thu;
    @BindView(R.id.tv_fri)
    TextView tv_fri;
    @BindView(R.id.tv_sta)
    TextView tv_sta;
    @BindView(R.id.tv_sun)
    TextView tv_sun;

    private int hour;
    /**
     * 开始设定时间与结束设定时间
     */
    private TimeTaskDaoImpl timeTaskDao;
    /**
     * 定时任务的数据库操作
     */
    private DeviceChildDaoImpl deviceChildDao;
    /**
     * 单个设备数据库操作
     */
    private int temperature;
    /**
     * 温度
     */
    private TimeTaskAdapter timeTaskAdapter;
    /**
     * 定时任务适配器
     */
    private WeekAdapter weekAdapter;
    /***/
    private List<TimeTask> list;
    private String mWeek;
    /**
     * 一周的星期几
     */

    private String mSelectedWeek;
    private String copy;
    MyApplication application;
    String taskTimeUrl = "http://120.77.36.206:8082/warmer/v1.0/device/timeControl";
    SharedPreferences preferences;
    public static boolean running = false;

    private TimeDaoImpl timeDao;

    private boolean isBound = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_task);
        unbinder = ButterKnife.bind(this);
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
        Intent intent = new Intent(this, MQService.class);
        isBound = bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    long deviceId;
    private DeviceChild deviceChild;
    MessageReceiver receiver;

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter("TimeTaskActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);
        Intent intent = getIntent();
        String device = intent.getStringExtra("deviceId");


        if (!Utils.isEmpty(device)) {
            deviceId = Integer.parseInt(device);
        }
//        listview.setOnItemClickListener(this);
        timeTaskDao = new TimeTaskDaoImpl(getApplicationContext());
        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());
        deviceChild = deviceChildDao.findDeviceById(deviceId);
        timeDao = new TimeDaoImpl(this);

//        week.setOnItemClickListener(this);


        numberPicker.setMinValue(5);
        numberPicker.setMaxValue(42);
        numberPicker.setValue(5);
        temperature = 5;
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                temperature = newVal;
            }
        });
        timePicker.setMinValue(0);
        timePicker.setMaxValue(24);
        Calendar calendar = Calendar.getInstance();

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        timePicker.setValue(hour);


        timePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                hour = newVal;
            }
        });
    }

    private TextView[] week = new TextView[7];

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week2 = calendar.get(Calendar.DAY_OF_WEEK);
        mWeek = Utils.getWeek(year, month, day, week2).substring(2);

        mSelectedWeek = mWeek;
        if ("日".equals(mWeek)) {
            mWeek = "七";
            mSelectedWeek = mWeek;
        }
        int week3 = ChineseNumber.chineseNumber2Int(mWeek);
        seekbar.setWeek(week3);
        seekbar.setDeviceId(deviceId + "");

        /**初始化时间适配器*/
        list = timeTaskDao.findWeekAll(deviceId, week3);/**查询某个设备，一周某一天的定时数据*/
        Collections.sort(list, new Comparator<TimeTask>() {
            @Override
            public int compare(TimeTask o1, TimeTask o2) {
                if (o1.getStart() > o2.getStart())
                    return 1;
                if (o1.getStart() == o2.getStart())
                    return 0;
                return -1;
            }
        });
        timeTaskAdapter = new TimeTaskAdapter(this, list, myClickListener);
        listview.setAdapter(timeTaskAdapter);


        if (!list.isEmpty()) {
            listview.setVisibility(View.VISIBLE);
        }

        week[0] = tv_mon;
        week[1] = tv_tue;
        week[2] = tv_wen;
        week[3] = tv_thu;
        week[4] = tv_fri;
        week[5] = tv_sta;
        week[6] = tv_sun;

        for (int i = 0; i < week.length; i++) {
            String s = week[i].getText().toString();
            if ("日".equals(s)) {
                s = "七";
            }
            if (mWeek.equals(s)) {
                tv_copy = week[i];
                tv_copy.setTextColor(getResources().getColor(R.color.color_black));
                tv_copy.setBackgroundResource(R.drawable.button_normal);
            }
        }
    }


    TextView tv_copy;

    private void setBack(TextView tv_week) {
        String copy = btn_copy.getText().toString();
        if ("复制".equals(copy)) {
            for (int i = 0; i < week.length; i++) {
                if (tv_week == week[i]) {
                    Message msg = handler.obtainMessage();
                    msg.obj = tv_week.getText().toString();
                    msg.what = 1;
                    handler.sendMessage(msg);
                    tv_copy = tv_week;
                    mSelectedWeek = tv_week.getText().toString();
                    if ("日".equals(mSelectedWeek)) {
                        mSelectedWeek = "七";
                    } else {
                        mSelectedWeek = tv_week.getText().toString();
                    }
                    tv_week.setTextColor(getResources().getColor(R.color.color_black));
                    tv_week.setBackgroundResource(R.drawable.button_normal);
                } else {
                    week[i].setTextColor(getResources().getColor(R.color.white));
                    week[i].setBackgroundColor(getResources().getColor(R.color.color_black3));
                }
            }
        }
    }

    //    private void setPaster
    int[] tvBack = {R.color.color_black, R.color.white};
    Map<String, TextView> pasterWeek = new HashMap<>();

    @OnClick({R.id.btn_add, R.id.open_time, R.id.btn_cancle2, R.id.btn_ensure2, R.id.close_time, R.id.tv_temp_num,
            R.id.img_back, R.id.btn_copy, R.id.tv_mon, R.id.tv_tue, R.id.tv_wen, R.id.tv_thu, R.id.tv_fri, R.id.tv_sta, R.id.tv_sun
            , R.id.btn_publish,R.id.btn_cancle})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_mon:
                String copy = btn_copy.getText().toString();
                if ("粘贴".equals(copy) && !mSelectedWeek.equals("一")) {
                    pasterWeek.put("1", tv_mon);
                    tv_mon.setBackgroundResource(R.drawable.shape_time_task);
                    tv_mon.setTextColor(getResources().getColor(R.color.white));
                }
                setBack(tv_mon);
                break;
            case R.id.tv_tue:
                setBack(tv_tue);
                String copy2 = btn_copy.getText().toString();
                if ("粘贴".equals(copy2) && !mSelectedWeek.equals("二")) {
                    pasterWeek.put("2", tv_tue);
                    tv_tue.setBackgroundResource(R.drawable.shape_time_task);
                    tv_tue.setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case R.id.tv_wen:
                setBack(tv_wen);
                String copy3 = btn_copy.getText().toString();
                if ("粘贴".equals(copy3) && !mSelectedWeek.equals("三")) {
                    pasterWeek.put("3", tv_wen);
                    tv_wen.setBackgroundResource(R.drawable.shape_time_task);
                    tv_wen.setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case R.id.tv_thu:
                setBack(tv_thu);
                String copy4 = btn_copy.getText().toString();
                if ("粘贴".equals(copy4) && !mSelectedWeek.equals("四")) {
                    pasterWeek.put("4", tv_thu);
                    tv_thu.setBackgroundResource(R.drawable.shape_time_task);
                    tv_thu.setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case R.id.tv_fri:
                setBack(tv_fri);
                String copy5 = btn_copy.getText().toString();
                if ("粘贴".equals(copy5) && !mSelectedWeek.equals("五")) {
                    pasterWeek.put("5", tv_fri);
                    tv_fri.setBackgroundResource(R.drawable.shape_time_task);
                    tv_fri.setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case R.id.tv_sta:
                setBack(tv_sta);
                String copy6 = btn_copy.getText().toString();
                if ("粘贴".equals(copy6) && !mSelectedWeek.equals("六")) {
                    pasterWeek.put("6", tv_sta);
                    tv_sta.setBackgroundResource(R.drawable.shape_time_task);
                    tv_sta.setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case R.id.tv_sun:
                setBack(tv_sun);
                String copy7 = btn_copy.getText().toString();
                if ("粘贴".equals(copy7) && !mSelectedWeek.equals("七")) {
                    pasterWeek.put("7", tv_sun);
                    tv_sun.setBackgroundResource(R.drawable.shape_time_task);
                    tv_sun.setTextColor(getResources().getColor(R.color.white));
                }
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_cancle:
                if (!pasterWeek.isEmpty()){
                    String s = btn_copy.getText().toString();
                    if ("粘贴".equals(s)){
                        for (Map.Entry<String, TextView> entry : pasterWeek.entrySet()){
                            TextView textView = entry.getValue();
                            if (textView!=null){
                                textView.setBackgroundColor(0);
                                textView.setTextColor(getResources().getColor(R.color.white));
                            }
                        }
                        btn_copy.setText("复制");
                    }
                }
                break;
            case R.id.btn_copy:
                String s = btn_copy.getText().toString();
                if ("复制".equals(s)) {
                    btn_copy.setText("粘贴");
                } else if ("粘贴".equals(s)) {
                    btn_copy.setText("复制");
                    if (pasterWeek.isEmpty()) {
                        Utils.showToast(this, "请选择要复制的星期");
                    } else {
                        for (Map.Entry<String, TextView> entry : pasterWeek.entrySet()) {
                            String weekStr = entry.getKey();
                            int pastWeek = Integer.parseInt(weekStr);
                            int copyWeek = ChineseNumber.chineseNumber2Int(mSelectedWeek);

                            try {

                                JSONObject jsonObject = new JSONObject();

                                List<Timer> timers = timeDao.findAll(deviceId, copyWeek);
                                Collections.sort(timers, new Comparator<Timer>() {
                                    @Override
                                    public int compare(Timer o1, Timer o2) {
                                        if (o1.getHour() > o2.getHour())
                                            return 1;
                                        else if (o1.getHour() < o2.getHour())
                                            return -1;
                                        return 0;
                                    }
                                });
                                if (timers.size()==24){
                                    jsonObject.put("timerTaskWeek", pastWeek);

                                    for (int i = 0; i < 24; i++) {
                                        Timer timer = timers.get(i);
                                        jsonObject.put("h" + i, timer.getOpen());
                                        jsonObject.put("t" + i, timer.getTemp());
                                    }
                                    Thread.sleep(300);
                                    String jsonData = jsonObject.toString();


                                    if (bound) {
                                        boolean success = false;
                                        String mac = deviceChild.getMacAddress();
                                        String topicName;
                                        topicName = "rango/" + mac + "/set";
                                        success = mqService.publish(topicName, 2, jsonData);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            TextView textView = entry.getValue();
                            textView.setBackgroundColor(0);
                            textView.setTextColor(getResources().getColor(R.color.white));
                        }
                    }

                }
                break;
            case R.id.btn_add:/**添加开始设定时间和结束设定时间，温度*/
                if (NoFastClickUtils.isFastClick()){
                    String open = open_time.getText().toString();
                    String close = close_time.getText().toString();
                    String temp = tv_temp_num.getText().toString();
                    if (!Utils.isEmpty(open) && !Utils.isEmpty(close) && !Utils.isEmpty(temp)) {
                        open = open.substring(0, open.indexOf(":"));
                        close = close.substring(0, close.indexOf(":"));
                        temp = temp.substring(0, temp.indexOf("℃"));
                        int start = Integer.parseInt(open);
                        int end = Integer.parseInt(close);
                        if (start > end) {
                            Utils.showToast(this, "关的时间应大于开的时间");
                            close_time.setText((start + 1) + ":00");
                            return;
                        }
                        if ("日".equals(mSelectedWeek)) {
                            mSelectedWeek = "七";
                        }
                        int week = ChineseNumber.chineseNumber2Int(mSelectedWeek);
                        TimeTask timeTask = new TimeTask(deviceId, week, start, end, Integer.parseInt(temp));
                        if (timeTask != null) {
                            List<TimeTask> timeTasks = timeTaskDao.findWeekAll(deviceId, week);
                            int i = 0;
                            //遍历所有的定时类
                            for (TimeTask t : timeTasks) {
                                //判断要添加的对象 开始结束点  是否 都小于等于开始时间 或者都大于等于结束时间
                                if ((timeTask.getStart() <= t.getStart() && timeTask.getEnd() <= t.getStart()) || (timeTask.getStart() >= t.getEnd() && timeTask.getEnd() >= t.getEnd())) {
                                    i++;
                                }
                            }
                            //如果i和list的长度相等 说明和以前添加的都不交叉 可以添加
                            if (i == timeTasks.size()) {
                                timeTaskDao.insert(timeTask);
                                list.add(timeTask);
                                List<Timer> timers = timeDao.findAll(deviceId, week);
                                for (int startTime = timeTask.getStart(); startTime < timeTask.getEnd(); startTime++) {
                                    if (startTime == timeTask.getEnd()) {
                                        Timer timer = timers.get(startTime);
                                        timer.setOpen("off");
                                        timer.setTemp(timeTask.getTemp());
                                        timeDao.update(timer);
                                    } else {
                                        Timer timer = timers.get(startTime);
                                        if (timer != null) {
                                            timer.setOpen("on");
                                            timer.setTemp(timeTask.getTemp());
                                            timeDao.update(timer);
                                        }
                                    }
                                }
                                try {
                                    String jsonData = null;
                                    JSONObject jsonObject = new JSONObject();
                                    int selectedWeek = ChineseNumber.chineseNumber2Int(mSelectedWeek);
                                    List<Timer> timers2 = timeDao.findAll(deviceId, selectedWeek);
                                    Collections.sort(timers2, new Comparator<Timer>() {
                                        @Override
                                        public int compare(Timer o1, Timer o2) {
                                            if (o1.getHour() > o2.getHour())
                                                return 1;
                                            else if (o1.getHour() < o2.getHour())
                                                return -1;
                                            return 0;
                                        }
                                    });
                                    if (timers2.size()==24){
                                        jsonObject.put("timerTaskWeek", selectedWeek);

                                        for (int ii = 0; ii < 24; ii++) {
                                            Timer timer = timers.get(ii);
                                            jsonObject.put("h" + ii, timer.getOpen());
                                            jsonObject.put("t" + ii, timer.getTemp());
                                        }
                                        jsonData = jsonObject.toString();
                                        if (bound) {
                                            boolean success = false;
                                            String mac = deviceChild.getMacAddress();
                                            String topicName;
                                            topicName = "rango/" + mac + "/set";
                                            success = mqService.publish(topicName, 2, jsonData);
                                        }
                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Utils.showToast(this, "该时间段已存在");
                            }
                            Collections.sort(list, new Comparator<TimeTask>() {
                                @Override
                                public int compare(TimeTask o1, TimeTask o2) {
                                    if (o1.getStart() > o2.getStart())
                                        return 1;
                                    if (o1.getStart() == o2.getStart())
                                        return 0;
                                    return -1;
                                }
                            });
                            timeTaskAdapter.notifyDataSetChanged();
                            listview.setVisibility(View.VISIBLE);
                            seekbar.setDeviceId(deviceId + "");
                            seekbar.setWeek(week);
                            seekbar.invalidate();
                        }
                    }
                }
                break;
            case R.id.btn_publish:
                finish();
                break;
            case R.id.open_time:/**设定开始时间*/

                linearout.setVisibility(View.VISIBLE);
                tv_clock.setText("开始时间");
                OPEN_CLOSE = "开始时间";
                timePicker.setVisibility(View.VISIBLE);
                if (list.isEmpty()) {
                    listview.setVisibility(View.GONE);
                } else {
                    listview.setVisibility(View.VISIBLE);
                }
                numberPicker.setVisibility(View.GONE);
                break;
            case R.id.close_time:/**设定结束时间*/
                linearout.setVisibility(View.VISIBLE);
                tv_clock.setText("结束时间");
                OPEN_CLOSE = "结束时间";
                if (list.isEmpty()) {
                    listview.setVisibility(View.GONE);
                } else {
                    listview.setVisibility(View.VISIBLE);
                }

                timePicker.setVisibility(View.VISIBLE);
                numberPicker.setVisibility(View.GONE);
                break;
            case R.id.tv_temp_num:/**设定温度*/
                linearout.setVisibility(View.VISIBLE);
                tv_clock.setText("温度");
                if (list.isEmpty()) {
                    listview.setVisibility(View.GONE);
                } else {
                    listview.setVisibility(View.VISIBLE);
                }
                timePicker.setVisibility(View.GONE);
                numberPicker.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_cancle2:
                linearout.setVisibility(View.GONE);
                break;
            case R.id.btn_ensure2:
                linearout.setVisibility(View.GONE);
                if ("开始时间".equals(OPEN_CLOSE)) {
                    open_time.setText(hour + ":00");
                    if (hour==24){
                        Utils.showToast(this,"24点就是0点哦!");
                        hour=0;
                        open_time.setText(hour + ":00");
                    }
                } else if ("结束时间".equals(OPEN_CLOSE)) {
                    close_time.setText(hour + ":00");
                    String openTime = open_time.getText().toString();
                    openTime = openTime.substring(0, openTime.indexOf(":"));
                    String closeTime = close_time.getText().toString();
                    closeTime = closeTime.substring(0, closeTime.indexOf(":"));

                    if (Integer.parseInt(openTime) > Integer.parseInt(closeTime)) {
                        int endTime = Integer.parseInt(openTime);
                        Utils.showToast(this, "结束时间要大于开始时间");
                        close_time.setText((endTime + 1) + ":00");
                        return;
                    }
                }
                tv_temp_num.setText(temperature + "℃");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private TimeTaskAdapter.MyClickListener myClickListener = new TimeTaskAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, View view) {
//            Toast.makeText(TimeTaskActivity.this,"listview的内部的按钮被点击了！，位置是-->" + position + ",内容是-->"+list.get(position),Toast.LENGTH_LONG).show();
            try {
                TimeTask timeTask = list.get(position);
                if (timeTask != null) {
                    List<Timer> timers = timeDao.findAll(deviceId, timeTask.getWeek());
                    if (timers.size() == 24) {
                        for (int start = timeTask.getStart(); start < timeTask.getEnd(); start++) {
                            Timer timer = timers.get(start);
                            if (timer != null) {
                                timer.setOpen("off");
                                timeDao.update(timer);
                            }
                        }
                    }
                    String jsonData = null;
                    JSONObject jsonObject = new JSONObject();
                    int selectedWeek = ChineseNumber.chineseNumber2Int(mSelectedWeek);
                    List<Timer> timers2 = timeDao.findAll(deviceId, selectedWeek);
                    Collections.sort(timers2, new Comparator<Timer>() {
                        @Override
                        public int compare(Timer o1, Timer o2) {
                            if (o1.getHour() > o2.getHour())
                                return 1;
                            else if (o1.getHour() < o2.getHour())
                                return -1;
                            return 0;
                        }
                    });
                    jsonObject.put("timerTaskWeek", selectedWeek);

                    for (int i = 0; i < 24; i++) {
                        Timer timer = timers.get(i);
                        jsonObject.put("h" + i, timer.getOpen());
                        jsonObject.put("t" + i, timer.getTemp());
                    }
                    jsonData = jsonObject.toString();
                    if (bound) {
                        boolean success = false;
                        String mac = deviceChild.getMacAddress();
                        String topicName;
                        topicName = "rango/" + mac + "/set";
                        success = mqService.publish(topicName, 2, jsonData);
                    }

                }
                timeTaskDao.delete(timeTask);
                Message msg = handler.obtainMessage();
                switch (timeTask.getWeek()) {
                    case 1:
                        mSelectedWeek = "一";
                        break;
                    case 2:
                        mSelectedWeek = "二";
                        break;
                    case 3:
                        mSelectedWeek = "三";
                        break;
                    case 4:
                        mSelectedWeek = "四";
                        break;
                    case 5:
                        mSelectedWeek = "五";
                        break;
                    case 6:
                        mSelectedWeek = "六";
                        break;
                    case 7:
                        mSelectedWeek = "七";
                        break;
                }
                msg.what = 1;
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    String copyWeek;
    String pasteWeek;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String mWeek = (String) msg.obj;
            switch (msg.what) {
                case 1:
                    if (!Utils.isEmpty(mSelectedWeek)) {
                        int selectedWeek = ChineseNumber.chineseNumber2Int(mSelectedWeek);
                        list.clear();
                        List<TimeTask> timeTasks = timeTaskDao.findWeekAll(deviceId, selectedWeek);
                        list.addAll(timeTasks);
                        Collections.sort(list, new Comparator<TimeTask>() {
                            @Override
                            public int compare(TimeTask o1, TimeTask o2) {
                                if (o1.getStart() > o2.getStart())
                                    return 1;
                                if (o1.getStart() == o2.getStart())
                                    return 0;
                                return -1;
                            }
                        });

                        if (list.isEmpty()) {
                            seekbar.setWeek(selectedWeek);
                            seekbar.invalidate();
                            listview.setVisibility(View.GONE);
                        } else {
                            seekbar.setWeek(selectedWeek);
                            seekbar.invalidate();
                            listview.setVisibility(View.VISIBLE);
                            timeTaskAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case 2:
                    copyWeek = (String) msg.obj;
                    int copy = ChineseNumber.chineseNumber2Int(copyWeek);
                    List<TimeTask> mCopyList = timeTaskDao.findWeekAll(deviceId, copy);
                    if (mCopyList.isEmpty()) {
                        Utils.showToast(TimeTaskActivity.this, "没有复制的数据");
                        btn_copy.setText("复制");
                    }
                    break;
                case 3:
                    pasteWeek = (String) msg.obj;
                    int copy2 = ChineseNumber.chineseNumber2Int(copyWeek);
                    int paster = ChineseNumber.chineseNumber2Int(pasteWeek);
                    List<TimeTask> mCopyList2 = timeTaskDao.findWeekAll(deviceId, copy2);
                    if (mCopyList2 != null && !mCopyList2.isEmpty()) {
                        if (pasteWeek.equals(copyWeek)) {
                            return;
                        }
                        List<TimeTask> pasteList = new ArrayList<>();
                        for (TimeTask timeTask : mCopyList2) {
                            TimeTask timeTask2 = new TimeTask(timeTask.getDeviceId(), paster, timeTask.getStart(), timeTask.getEnd(), timeTask.getTemp());
                            pasteList.add(timeTask2);
                        }
                        timeTaskDao.insertTaskTimeList(pasteList);
                        seekbar.setWeek(paster);
                        seekbar.setDeviceId(deviceId + "");

                        seekbar.invalidate();
                        listview.setVisibility(View.VISIBLE);
                        list = pasteList;
                        timeTaskAdapter.notifyDataSetChanged();
                    }
                    break;

            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        running = false;
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        deviceChildDao.closeDaoSession();
        timeTaskDao.closeDaoSession();
        timeDao.closeDaoSession();
        deviceChildDao.closeDaoSession();
        deviceChildDao.closeDaoSession();
    }

    MQService mqService;
    private boolean bound = false;
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

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long deviceId = intent.getLongExtra("deviceId", 0);
            int timerTaskWeek = intent.getIntExtra("timerTaskWeek", 0);
            List<TimeTask> timeTasks = (List<TimeTask>) intent.getSerializableExtra("list");
            String macAddress = intent.getStringExtra("macAddress");
            String noNet = intent.getStringExtra("noNet");
            if (!Utils.isEmpty(noNet)) {
                Utils.showToast(TimeTaskActivity.this, "网络已断开，请设置网络");
            } else {
                if (!Utils.isEmpty(macAddress)) {
                    if (deviceChild.getMacAddress().equals(macAddress)) {
                        Utils.showToast(TimeTaskActivity.this, "该设备已被重置");
                        Intent intent2 = new Intent(TimeTaskActivity.this, MainActivity.class);
                        intent2.putExtra("deviceList", "deviceList");
                        startActivity(intent2);
                    }
                } else {
                    DeviceChild deviceChild2 = deviceChildDao.findDeviceById(deviceId);
                    if (deviceChild.getMacAddress().equals(deviceChild2.getMacAddress())) {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int week2 = calendar.get(Calendar.DAY_OF_WEEK);
                        mWeek = Utils.getWeek(year, month, day, week2).substring(2);
                        int secelctWeek = ChineseNumber.chineseNumber2Int(mWeek);
                        if (!timeTasks.isEmpty() && secelctWeek == timerTaskWeek) {
                            list.clear();
                            list.addAll(timeTasks);
                            timeTaskDao.updateTaskTimeList(list);
                            Collections.sort(list, new Comparator<TimeTask>() {
                                @Override
                                public int compare(TimeTask o1, TimeTask o2) {
                                    if (o1.getStart() > o2.getStart())
                                        return 1;
                                    if (o1.getStart() == o2.getStart())
                                        return 0;
                                    return -1;
                                }
                            });

                            if (list.isEmpty()) {
                                seekbar.setWeek(timerTaskWeek);
                                seekbar.invalidate();
                                listview.setVisibility(View.GONE);
                            } else {
                                seekbar.setWeek(timerTaskWeek);
                                seekbar.invalidate();
                                listview.setVisibility(View.VISIBLE);
                                timeTaskAdapter.notifyDataSetChanged();
                            }
                            TextView tv_week = week[timerTaskWeek - 1];
                            for (int i = 0; i < week.length; i++) {
                                if (tv_week == week[i]) {
                                    tv_copy = tv_week;
                                    mSelectedWeek = tv_week.getText().toString();
                                    if ("日".equals(mSelectedWeek)) {
                                        mSelectedWeek = "七";
                                    } else {
                                        mSelectedWeek = tv_week.getText().toString();
                                    }
                                    tv_week.setTextColor(getResources().getColor(R.color.color_black));
                                    tv_week.setBackgroundResource(R.drawable.button_normal);
                                } else {
                                    week[i].setTextColor(getResources().getColor(R.color.white));
                                    week[i].setBackgroundColor(getResources().getColor(R.color.color_black3));
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (isBound) {
            if (connection != null) {
                unbindService(connection);

            }

        }

    }
}