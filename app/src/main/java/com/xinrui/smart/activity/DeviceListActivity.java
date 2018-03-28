package com.xinrui.smart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.DeviceListAdapter;
import com.xinrui.smart.fragment.ClockSetFragment;
import com.xinrui.smart.fragment.ClockSettingFragment;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.fragment.ShareDeviceFragment;
import com.xinrui.smart.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    protected static final float FLIP_DISTANCE = 50;
    GestureDetector mDetector;
    private Unbinder unbinder;
    @BindView(R.id.img_back)
    ImageView img_back;//返回键
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.gradView)
    GridView gradView;
    private int[] colors = {R.color.color_black, R.color.holo_orange_dark};
    private List<String> list;
    DeviceListAdapter adapter;
    private FragmentManager fragmentManager;
    @BindView(R.id.linearout2) LinearLayout linearout2;
    @BindView(R.id.timePicker) TimePicker timePicker;
    @BindView(R.id.datePicker) DatePicker datePicker;
    @BindView(R.id.tv_clock) TextView tv_clock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        unbinder = ButterKnife.bind(this);
    }

    @OnClick({R.id.img_back, R.id.image_home,R.id.btn_cancle,R.id.btn_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.image_home:
                finish();
                break;
            case R.id.btn_cancle:
                linearout2.setVisibility(View.GONE);
                gradView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_ensure:
                linearout2.setVisibility(View.GONE);
                gradView.setVisibility(View.VISIBLE);
                break;
        }
    }

    int mPoistion=0;;// 选中的位置
    private int hour=0;
    int year=0;
    int month;
    int day=0;
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        tv_name.setText(content);
        fragmentManager =getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.linearout, new HeaterFragment());
        fragmentTransaction.commit();

       list = new ArrayList<>();
        String[] titles = {"分享设备", "时钟设置", "定时任务", "设备状态", "常见问题", "关乎我们"};
        for (int i = 0; i < titles.length; i++) {
            list.add(titles[i]);
        }

       adapter=new DeviceListAdapter(this,list);
        gradView.setAdapter(adapter);
       gradView.setOnItemClickListener(this);

        timePicker.setIs24HourView(true);
        Calendar calendar= Calendar.getInstance();
        year=calendar.get(Calendar.YEAR);
        month=calendar.get(Calendar.MONTH)+1;
        day=calendar.get(Calendar.DAY_OF_MONTH);
        hour=calendar.get(Calendar.HOUR_OF_DAY);

        tv_clock.setText(year+"年"+month+"月"+day+"日"+hour+"时");
        timePicker.setIs24HourView(true);
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int mYear, int monthOfYear, int dayOfMonth) {
                year=mYear;
                month=monthOfYear;
                day=dayOfMonth;
                tv_clock.setText(year+"年"+month+"月"+day+"日"+hour+"时");
            }
        });
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour=hourOfDay;
                tv_clock.setText(year+"年"+month+"月"+day+"日"+hour+"时");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            //解绑界面元素
            unbinder.unbind();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPoistion=position;
        adapter.setSelectedPosition(mPoistion);
        adapter.notifyDataSetInvalidated();
        switch (position){
            case 0:
                Toast.makeText(this,"我的委托",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                linearout2.setVisibility(View.VISIBLE);
                gradView.setVisibility(View.GONE);
                break;
            case 2:
                startActivity(new Intent(this,TimeTaskActivity.class));
//                Toast.makeText(this,"我的订阅",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                startActivity(new Intent(this,TempChartActivity.class));
                break;
            case 4:
                Toast.makeText(this,"我的推荐",Toast.LENGTH_SHORT).show();
                break;
            case 5:
                Toast.makeText(this,"设置",Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this,"1",Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
