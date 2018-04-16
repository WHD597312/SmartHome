package com.xinrui.smart.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.TimeTaskAdapter;
import com.xinrui.smart.adapter.WeekAdapter;
import com.xinrui.smart.fragment.ClockSetFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.MainControl;
import com.xinrui.smart.pojo.TaskTime;
import com.xinrui.smart.util.ChineseNumber;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.CircleSeekBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**定时任务
 */
public class TimeTaskActivity extends AppCompatActivity{
    public String OPEN_CLOSE="";
    @BindView(R.id.img_back) ImageView img_back;//返回按钮
    Unbinder unbinder;
    @BindView(R.id.seekbar)
    CircleSeekBar seekbar;
    @BindView(R.id.tv_open)
    TextView tv_open;//开始时间
    @BindView(R.id.open_time)
    TextView open_time;//设定开始时间
    @BindView(R.id.close_time)
    TextView close_time;//结束时间

    @BindView(R.id.tv_temp) TextView tv_temp;//温度
    @BindView(R.id.tv_temp_num) TextView tv_temp_num;//温度计数
    @BindView(R.id.listview) ListView listview;/**开始时间，结束时间，温度列表*/
    @BindView(R.id.linearout) LinearLayout linearout;
    @BindView(R.id.tv_clock) TextView tv_clock;
    @BindView(R.id.timePicker) TimePicker timePicker;/**时间选择器*/
    @BindView(R.id.numberPicker) NumberPicker numberPicker;/**数字选择器*/
    @BindView(R.id.btn_copy) Button btn_copy;/**复制按钮*/
    @BindView(R.id.btn_add) Button btn_add;/**添加时间*/

    @BindView(R.id.tv_mon) TextView tv_mon;
    @BindView(R.id.tv_tue) TextView tv_tue;
    @BindView(R.id.tv_wen) TextView tv_wen;
    @BindView(R.id.tv_thu) TextView tv_thu;
    @BindView(R.id.tv_fri) TextView tv_fri;
    @BindView(R.id.tv_sta) TextView tv_sta;
    @BindView(R.id.tv_sun) TextView tv_sun;

    private int hour;/**开始设定时间与结束设定时间*/
    private TimeTaskDaoImpl timeTaskDao;/**定时任务的数据库操作*/
    private DeviceChildDaoImpl deviceChildDao;/**单个设备数据库操作*/
    private int temperature;/**温度*/
    private TimeTaskAdapter timeTaskAdapter;/**定时任务适配器*/
    private WeekAdapter weekAdapter;/***/
    private List<TaskTime> list;
    private String mWeek;/**一周的星期几*/

    private String mSelectedWeek;
    private String copy;
    MyApplication application;
    String taskTimeUrl="http://120.77.36.206:8082/warmer/v1.0/device/timeControl";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_task);
        ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
    }
    long deviceId;
    private DeviceChild deviceChild;
    @Override
    public void onStart() {
        super.onStart();

        Intent intent=getIntent();
        String device=intent.getStringExtra("deviceId");
        if (!Utils.isEmpty(device)){
            deviceId=Integer.parseInt(device);
        }
//        listview.setOnItemClickListener(this);
        timeTaskDao=new TimeTaskDaoImpl(this);

        deviceChildDao=new DeviceChildDaoImpl(this);
        deviceChild=deviceChildDao.findDeviceChild(deviceId);

//        week.setOnItemClickListener(this);


        timePicker.setIs24HourView(true);



        numberPicker.setMinValue(5);
        numberPicker.setMaxValue(42);
        numberPicker.setValue(5);
        temperature=5;
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                temperature=newVal;
            }
        });
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour= hourOfDay;
            }
        });
    }
    private TextView[] week=new TextView[7];
    @Override
    protected void onResume() {
        super.onResume();
        Calendar calendar= Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int week2=calendar.get(Calendar.DAY_OF_WEEK);
        mWeek=Utils.getWeek(year,month,day,week2).substring(2);

        mSelectedWeek=mWeek;
        int week3=ChineseNumber.chineseNumber2Int(mWeek);
        seekbar.setWeek(week3+"");

        /**初始化时间适配器*/
        list=timeTaskDao.findWeekAll(week3+"");/**查询一周某一天的定时数据*/
        timeTaskAdapter=new TimeTaskAdapter(this,list,myClickListener);
        listview.setAdapter(timeTaskAdapter);


        if (!list.isEmpty()){
            listview.setVisibility(View.VISIBLE);
        }

        week[0]=tv_mon;
        week[1]=tv_tue;
        week[2]=tv_wen;
        week[3]=tv_thu;
        week[4]=tv_fri;
        week[5]=tv_sta;
        week[6]=tv_sun;

        for (int i=0;i<week.length;i++){
            String s=week[i].getText().toString();
            if (mWeek.equals(s)){
                tv_copy=week[i];
                tv_copy.setTextColor(getResources().getColor(R.color.color_black));
                tv_copy.setBackgroundResource(R.drawable.button_normal);
            }
        }
    }

    TextView tv_copy;
    private void setBack(TextView tv_week){
        String copy=btn_copy.getText().toString();
        if ("复制".equals(copy)){
            for (int i=0;i<week.length;i++){
                if (tv_week==week[i]){
                    Message msg=handler.obtainMessage();
                    msg.obj=tv_week.getText().toString();
                    msg.what=1;
                    handler.sendMessage(msg);
                    tv_copy=tv_week;
                    mSelectedWeek=tv_week.getText().toString();
                    tv_week.setTextColor(getResources().getColor(R.color.color_black));
                    tv_week.setBackgroundResource(R.drawable.button_normal);
                }else {
                    week[i].setTextColor(getResources().getColor(R.color.white));
                    week[i].setBackgroundColor(getResources().getColor(R.color.color_black3));
                }
            }
        }else if ("粘贴".equals(copy)){
            for (int i=0;i<week.length;i++){
                if (tv_week==week[i]){
                    pasteWeek=tv_week.getText().toString();
                    tv_week.setTextColor(getResources().getColor(R.color.color_black));
                    tv_week.setBackgroundResource(R.drawable.shape_btn_ensure_pressed);
                }else {
                    if (tv_copy==week[i]){
                        tv_copy.setTextColor(getResources().getColor(R.color.color_black));
                        tv_copy.setBackgroundResource(R.drawable.button_normal);
                    }else {
                        week[i].setTextColor(getResources().getColor(R.color.white));
                        week[i].setBackgroundColor(getResources().getColor(R.color.color_black3));
                    }
                }
            }
        }
    }
    //    private void setPaster
    int [] tvBack={R.color.color_black,R.color.white};
    @OnClick({R.id.btn_add,R.id.open_time,R.id.btn_cancle2,R.id.btn_ensure2,R.id.close_time,R.id.tv_temp_num,R.id.img_back,R.id.btn_copy,R.id.tv_mon,R.id.tv_tue,R.id.tv_wen,R.id.tv_thu,R.id.tv_fri,R.id.tv_sta,R.id.tv_sun})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_mon:
                setBack(tv_mon);
                break;
            case R.id.tv_tue:
                setBack(tv_tue);
                break;
            case R.id.tv_wen:
                setBack(tv_wen);
                break;
            case R.id.tv_thu:
                setBack(tv_thu);
                break;
            case R.id.tv_fri:
                setBack(tv_fri);
                break;
            case R.id.tv_sta:
                setBack(tv_sta);
                break;
            case R.id.tv_sun:
                setBack(tv_sun);
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_copy:
                String s=btn_copy.getText().toString();
                if ("复制".equals(s)){
                    btn_copy.setText("粘贴");
                    Message msg=handler.obtainMessage();
                    msg.what=2;
                    msg.obj=mSelectedWeek;
                    handler.sendMessage(msg);
                }else if ("粘贴".equals(s)){
                    btn_copy.setText("复制");
                    Message msg=handler.obtainMessage();
                    msg.what=3;
                    msg.obj=pasteWeek;
                    handler.sendMessage(msg);
                }
                break;
            case R.id.btn_add:/**添加开始设定时间和结束设定时间，温度*/


                String open=open_time.getText().toString();
                String close=close_time.getText().toString();
                String temp=tv_temp_num.getText().toString();
                if(!Utils.isEmpty(open) && !Utils.isEmpty(close)&& !Utils.isEmpty(temp)){
                    open=open.substring(0,open.indexOf(":"));
                    close=close.substring(0,close.indexOf(":"));
                    temp=close.substring(0,temp.indexOf("℃"));
                    int mWeek= ChineseNumber.chineseNumber2Int(mSelectedWeek);
                    TaskTime taskTime2=new TaskTime(Integer.parseInt(open), Integer.parseInt(close), Integer.parseInt(temp), mWeek+"");
                    List<TaskTime> taskTimes=timeTaskDao.findWeekAll(mWeek+"");
                    for (TaskTime taskTime:taskTimes){
                        if (taskTime2.equals(taskTime)){
                            Utils.showToast(this,"该时间段已存在");
                            return;
                        }
                    }
                    try {
                        JSONObject device=new JSONObject();
                        device.put("deviceId",deviceId);
                        JSONArray deviceTimeControlDtos=new JSONArray();

                        JSONObject week=new JSONObject();

                        week.put("week",mWeek);
                        JSONArray deviceTimeControlList=new JSONArray();
                        JSONObject object=new JSONObject();
                        object.put("temp",temp);
                        object.put("openTime",open);
                        object.put("closeTime",close);
                        deviceTimeControlList.put(object);

                        week.put("deviceTimeControlList",deviceTimeControlList);
                        deviceTimeControlDtos.put(week);
                        device.put("deviceTimeControlDtos",deviceTimeControlDtos);
                        new TaskTimeAsync().execute(device);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

//                    int openTime=Integer.parseInt(open.substring(0,open.indexOf(":")));
//                    int closeTime=Integer.parseInt(close.substring(0,close.indexOf(":")));

                }
                break;
            case R.id.open_time:/**设定开始时间*/
                linearout.setVisibility(View.VISIBLE);
                tv_clock.setText("开始时间");
                OPEN_CLOSE="开始时间";
                timePicker.setVisibility(View.VISIBLE);
                listview.setVisibility(View.GONE);
                numberPicker.setVisibility(View.GONE);
                break;
            case R.id.close_time:/**设定结束时间*/
                linearout.setVisibility(View.VISIBLE);
                tv_clock.setText("结束时间");
                OPEN_CLOSE="结束时间";
                listview.setVisibility(View.GONE);
                timePicker.setVisibility(View.VISIBLE);
                numberPicker.setVisibility(View.GONE);
                break;
            case R.id.tv_temp_num:
                linearout.setVisibility(View.VISIBLE);
                tv_clock.setText("温度");
                listview.setVisibility(View.GONE);
                timePicker.setVisibility(View.GONE);
                numberPicker.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_cancle2:
                linearout.setVisibility(View.GONE);
                break;
            case R.id.btn_ensure2:
                linearout.setVisibility(View.GONE);
                if ("开始时间".equals(OPEN_CLOSE)){
                    open_time.setText(hour+":00");
                }else if ("结束时间".equals(OPEN_CLOSE)){
                    close_time.setText(hour+":00");
                }
                tv_temp_num.setText(temperature+"℃");
                break;
        }
    }

    private TimeTaskAdapter.MyClickListener myClickListener = new TimeTaskAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, View view) {
//            Toast.makeText(TimeTaskActivity.this,"listview的内部的按钮被点击了！，位置是-->" + position + ",内容是-->"+list.get(position),Toast.LENGTH_LONG).show();
            TaskTime taskTime=list.get(position);
            if (taskTime!=null){
                Message msg=handler.obtainMessage();
                msg.obj=taskTime.getWeek();
                timeTaskDao.delete(taskTime);
                msg.what=4;
                handler.sendMessage(msg);
                Utils.showToast(TimeTaskActivity.this,"删除成功");
            }
        }
    };
    String copyWeek;
    String pasteWeek;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String mWeek= (String) msg.obj;
            switch (msg.what){
                case 1:

                    if (!Utils.isEmpty(mSelectedWeek)){
                        int selectedWeek=ChineseNumber.chineseNumber2Int(mSelectedWeek);
                        list=timeTaskDao.findWeekAll(selectedWeek+"");
                        if (list.isEmpty()){
                            seekbar.setWeek(mSelectedWeek);
                            seekbar.invalidate();
                            listview.setVisibility(View.GONE);
                        }else {
                            seekbar.setWeek(selectedWeek+"");
                            seekbar.invalidate();
                            listview.setVisibility(View.VISIBLE);
                            timeTaskAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case 2:
                    copyWeek= (String) msg.obj;
                    int copy=ChineseNumber.chineseNumber2Int(copyWeek);
                    List<TaskTime> mCopyList=timeTaskDao.findWeekAll(copy+"");
                    if (mCopyList.isEmpty()){
                        Utils.showToast(TimeTaskActivity.this,"没有复制的数据");
                        btn_copy.setText("复制");
                    }
                    break;
                case 3:
                    pasteWeek= (String) msg.obj;
                    int copy2=ChineseNumber.chineseNumber2Int(copyWeek);
                    int paster=ChineseNumber.chineseNumber2Int(pasteWeek);
                    List<TaskTime> mCopyList2=timeTaskDao.findWeekAll(copy2+"");
                    if (mCopyList2!=null && !mCopyList2.isEmpty()){
                        if (pasteWeek.equals(copyWeek)){
                            return;
                        }
                        List<TaskTime> pasteList=new ArrayList<>();
                        for (TaskTime taskTime : mCopyList2){
                            TaskTime taskTime2=new TaskTime(taskTime.getStart(),taskTime.getEnd(),taskTime.getTemp(),paster+"");
                            pasteList.add(taskTime2);
                        }
                        timeTaskDao.insertTaskTimeList(pasteList);
                        seekbar.setWeek(paster+"");

                        seekbar.invalidate();
                        listview.setVisibility(View.VISIBLE);
                        list=pasteList;
                        timeTaskAdapter.notifyDataSetChanged();
                    }
                    break;
                case 4:
                    String deleteWeek= (String) msg.obj;
                    int delete=ChineseNumber.chineseNumber2Int(deleteWeek);
                    list=timeTaskDao.findWeekAll(delete+"");
                    if (list.isEmpty()){
                        seekbar.setWeek(delete+"");
                        seekbar.invalidate();
                        listview.setVisibility(View.GONE);
                    }else {
                        seekbar.setWeek(delete+"");
                        seekbar.invalidate();
                        listview.setVisibility(View.VISIBLE);
                        timeTaskAdapter.notifyDataSetChanged();
                    }
                    break;
            }

        }
    };

    class GetTaskTimeAsync extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            try {
                String taskTimeUrl="http://120.77.36.206:8082/warmer/v1.0/device/timeControl?deviceId="+ URLEncoder.encode(deviceId+"","utf-8");
                String result=HttpUtils.getOkHpptRequest(taskTimeUrl);
                if (!Utils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                    if (code==2000){
                        JSONObject content=jsonObject.getJSONObject("content");
                        int deviceId=content.getInt("deviceId");
                        JSONArray deviceTimeControlDtos=content.getJSONArray("deviceTimeControlDtos");
                        for (int i = 0; i < deviceTimeControlDtos.length(); i++) {
                            JSONObject week=deviceTimeControlDtos.getJSONObject(i);
                            int  mWeek=week.getInt("week");
                            JSONArray deviceTimeControlList=week.getJSONArray("deviceTimeControlList");
                            for (int j = 0; j < deviceTimeControlList.length(); j++) {
                                JSONObject object1=deviceTimeControlList.getJSONObject(j);
                                String openTime=object1.getString("openTime");
                                String closeTime=object1.getString("closeTime");
                                String temp=object1.getString("temp");
                                TaskTime taskTime=new TaskTime(Integer.parseInt(openTime), Integer.parseInt(closeTime), Integer.parseInt(temp),mWeek+"");
                                if (timeTaskDao.insert(taskTime)){
                                    list.add(taskTime);
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    class TaskTimeAsync extends AsyncTask<JSONObject,Void,Integer>{

        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {
            int code=0;
            JSONObject jsonObject=jsonObjects[0];
            String result=HttpUtils.postOkHpptRequest3(taskTimeUrl,jsonObject);
            if (!Utils.isEmpty(result)){
                try {
                    JSONObject object=new JSONObject(result);
                    code=object.getInt("code");
                    if (code==2000){
//                        long jsonObject.getLong("deviceId");
                        JSONArray deviceTimeControlDtos=jsonObject.getJSONArray("deviceTimeControlDtos");
                        for (int i = 0; i < deviceTimeControlDtos.length(); i++) {
                            JSONObject week=deviceTimeControlDtos.getJSONObject(i);
                            int  mWeek=week.getInt("week");
                            JSONArray deviceTimeControlList=week.getJSONArray("deviceTimeControlList");
                            for (int j = 0; j < deviceTimeControlList.length(); j++) {
                                JSONObject object1=deviceTimeControlList.getJSONObject(j);
                                String openTime=object1.getString("openTime");
                                String closeTime=object1.getString("closeTime");
                                String temp=object1.getString("temp");
                                TaskTime taskTime=new TaskTime(Integer.parseInt(openTime), Integer.parseInt(closeTime), Integer.parseInt(temp),mWeek+"");
                                if (timeTaskDao.insert(taskTime)){
                                    list.add(taskTime);
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code){
                case 2000:
                    Utils.showToast(TimeTaskActivity.this, "添加成功");
                    timeTaskAdapter.notifyDataSetChanged();
                    listview.setVisibility(View.VISIBLE);
                    seekbar.invalidate();
                    break;
                default:
                    Utils.showToast(TimeTaskActivity.this, "添加失败");
                    break;
            }
        }
    }

}
