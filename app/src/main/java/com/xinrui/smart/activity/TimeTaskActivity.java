package com.xinrui.smart.activity;

import android.content.Intent;
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

import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.TimeTaskAdapter;
import com.xinrui.smart.adapter.WeekAdapter;
import com.xinrui.smart.fragment.ClockSetFragment;
import com.xinrui.smart.pojo.TaskTime;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.CircleSeekBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**定时任务
*/
public class TimeTaskActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
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

    @BindView(R.id.week) GridView week;
    private List<String> mWeekList;
    private int hour;/**开始设定时间与结束设定时间*/
    private TimeTaskDaoImpl timeTaskDao;/**定时任务的数据库操作*/
    private int temperature;/**温度*/
    private TimeTaskAdapter timeTaskAdapter;/**定时任务适配器*/
    private WeekAdapter weekAdapter;/***/
    private List<TaskTime> list;
    int mPoistion=0;;// 选中的位置
    private String mWeek;/**一周的星期几*/
    private String mSelectedWeek;
    private String copy;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_task);
        ButterKnife.bind(this);
    }
    @Override
    public void onStart() {
        super.onStart();

        listview.setOnItemClickListener(this);
        timeTaskDao=new TimeTaskDaoImpl(this);



        week.setOnItemClickListener(this);


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
        seekbar.setWeek(mWeek);

        /**初始化时间适配器*/
        list=timeTaskDao.findWeekAll(mWeek);/**查询一周某一天的定时数据*/
        timeTaskAdapter=new TimeTaskAdapter(this,list,myClickListener);
        listview.setAdapter(timeTaskAdapter);


        if (!list.isEmpty()){
            listview.setVisibility(View.VISIBLE);
        }

        /**初始化周天适配器*/
        mWeekList=new ArrayList<>();
        mWeekList.add("一");
        mWeekList.add("二");
        mWeekList.add("三");
        mWeekList.add("四");
        mWeekList.add("五");
        mWeekList.add("六");
        mWeekList.add("日");

        weekAdapter=new WeekAdapter(this,mWeekList);
        week.setAdapter(weekAdapter);
        for (int i=0;i<mWeekList.size();i++){
            String s=mWeekList.get(i);
            if (mWeek.equals(s)){
                mPoistion=i;
                weekAdapter.setSelectedPosition(mPoistion);
                weekAdapter.notifyDataSetInvalidated();
                break;
            }
        }
    }
    @OnClick({R.id.btn_add,R.id.open_time,R.id.btn_cancle2,R.id.btn_ensure2,R.id.close_time,R.id.tv_temp_num,R.id.img_back,R.id.btn_copy})
    public void onClick(View view){
        switch (view.getId()){
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
                   msg.obj=copy;
                   handler.sendMessage(msg);
                }

                break;
            case R.id.btn_add:/**添加开始设定时间和结束设定时间，温度*/
                String open=open_time.getText().toString();
                String close=close_time.getText().toString();
                if(!Utils.isEmpty(open) && !Utils.isEmpty(close)){
                    int openTime=Integer.parseInt(open.substring(0,open.indexOf(":")));
                    int closeTime=Integer.parseInt(close.substring(0,close.indexOf(":")));
                    TaskTime taskTime=new TaskTime(openTime,closeTime,temperature,mWeek);

                    if(timeTaskDao.insert(taskTime)){
                        Utils.showToast(this,"添加成功");
                        list.add(taskTime);
                        timeTaskAdapter.notifyDataSetChanged();
                        listview.setVisibility(View.VISIBLE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekbar.invalidate();
                            }
                        });
                    }else {
                        Utils.showToast(this,"添加失败");
                    }
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
                timeTaskDao.delete(taskTime);
                Utils.showToast(TimeTaskActivity.this,"删除成功");
                Message msg=handler.obtainMessage();
                msg.obj=taskTime.getWeek();
                msg.what=4;
                handler.sendMessage(msg);
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
                        list=timeTaskDao.findWeekAll(mSelectedWeek);
                        if (list.isEmpty()){
                            seekbar.setWeek(mSelectedWeek);
                            seekbar.invalidate();
                            listview.setVisibility(View.GONE);
                        }else {
                            seekbar.setWeek(mSelectedWeek);
                            seekbar.invalidate();
                            listview.setVisibility(View.VISIBLE);
                            timeTaskAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case 2:
                    copyWeek= (String) msg.obj;
                    break;
                case 3:
                   pasteWeek= (String) msg.obj;
                    List<TaskTime> mCopyList=timeTaskDao.findWeekAll(copyWeek);
                    if (mCopyList!=null && !mCopyList.isEmpty()){
                        if (pasteWeek.equals(copyWeek)){
                            return;
                        }
                        List<TaskTime> pasteList=new ArrayList<>();
                        for (TaskTime taskTime : mCopyList){
                            TaskTime taskTime2=new TaskTime(taskTime.getStart(),taskTime.getEnd(),taskTime.getTemp(),pasteWeek);
                            pasteList.add(taskTime2);
                        }
                        timeTaskDao.insertTaskTimeList(pasteList);
                        seekbar.setWeek(pasteWeek);
                        seekbar.invalidate();
                        listview.setVisibility(View.VISIBLE);
                        list=pasteList;
                        timeTaskAdapter.notifyDataSetChanged();
                    }
                    break;
                case 4:
                    String deleteWeek= (String) msg.obj;
                    list=timeTaskDao.findWeekAll(deleteWeek);
                    if (list.isEmpty()){
                        seekbar.setWeek(deleteWeek);
                        seekbar.invalidate();
                        listview.setVisibility(View.GONE);
                    }else {
                        seekbar.setWeek(deleteWeek);
                        seekbar.invalidate();
                        listview.setVisibility(View.VISIBLE);
                        timeTaskAdapter.notifyDataSetChanged();
                    }
                    break;
            }

        }
    };
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPoistion=position;

        mSelectedWeek=mWeekList.get(position);
        copy=mSelectedWeek;

        Message msg=handler.obtainMessage();
        msg.obj=mSelectedWeek;
        msg.what=1;
        handler.sendMessage(msg);
        Log.d("ss",mWeek);
        weekAdapter.setSelectedPosition(mPoistion);
        weekAdapter.notifyDataSetInvalidated();
        switch (position){
            case 0:

                break;
            case 1:

                break;
            case 2:

                break;
            case 3:

                break;
            case 4:

                break;
            case 5:

                break;
            case 6:

                break;
        }
    }
}
