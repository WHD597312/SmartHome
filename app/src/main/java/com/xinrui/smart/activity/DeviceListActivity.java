package com.xinrui.smart.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.device.ShareDeviceActivity;
import com.xinrui.smart.adapter.DeviceListAdapter;
import com.xinrui.smart.fragment.ClockSetFragment;
import com.xinrui.smart.fragment.ClockSettingFragment;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.fragment.ShareDeviceFragment;
import com.xinrui.smart.pojo.DeviceChild;
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
    @BindView(R.id.linearout) LinearLayout linearout;
    @BindView(R.id.tv_offline) TextView tv_offline;
    MyApplication application;
    private String childPosition;
    private DeviceChildDaoImpl deviceChildDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        unbinder = ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
    }

    @OnClick({R.id.img_back, R.id.image_home,R.id.btn_cancle,R.id.btn_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra("deviceList","deviceList");
                startActivity(intent);
                break;
            case R.id.image_home:
                Intent intent2=new Intent(this,MainActivity.class);
                intent2.putExtra("deviceList","deviceList");
                startActivity(intent2);
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
    SharedPreferences preferences;
    DeviceChild deviceChild;

    @Override
    protected void onStart() {
        super.onStart();
        deviceChildDao=new DeviceChildDaoImpl(this);
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        childPosition=intent.getStringExtra("childPosition");

        deviceChild=deviceChildDao.findDeviceById(Long.parseLong(childPosition));


        tv_name.setText(content);
        fragmentManager =getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HeaterFragment heaterFragment=new HeaterFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable("deviceChild",deviceChild);
        heaterFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.linearout, heaterFragment);
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

    MessageReceiver receiver;
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("DeviceListActivity");
        receiver = new MessageReceiver();
        registerReceiver(receiver, intentFilter);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            application.removeActivity(this);
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("deviceList","deviceList");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            //解绑界面元素
            unbinder.unbind();
        }
        if (receiver!=null){
           unregisterReceiver(receiver);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPoistion=position;
        adapter.setSelectedPosition(mPoistion);
        adapter.notifyDataSetInvalidated();
        switch (position){
            case 0:
                Intent intent=new Intent(DeviceListActivity.this, ShareDeviceActivity.class);
                intent.putExtra("deviceId",childPosition);
                startActivity(intent);
                break;
            case 1:
                linearout2.setVisibility(View.VISIBLE);
                gradView.setVisibility(View.GONE);
                break;
            case 2:
                Intent timeTask=new Intent(this,TimeTaskActivity.class);
                timeTask.putExtra("deviceId",childPosition);
                startActivity(timeTask);

//                Toast.makeText(this,"我的订阅",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Intent intent3=new Intent(this,TempChartActivity.class);
                intent3.putExtra("deviceId",childPosition);
                startActivity(intent3);
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
    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceChild deviceChild2= (DeviceChild) intent.getSerializableExtra("deviceChild");
            String online=intent.getStringExtra("online");
            if (deviceChild.getMacAddress().equals(deviceChild2.getMacAddress())){
                if ("online".equals(online)){
                    linearout.setVisibility(View.VISIBLE);
                    tv_offline.setVisibility(View.GONE);
                    gradView.setVisibility(View.VISIBLE);
                }else if ("offline".equals(online)){
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    gradView.setVisibility(View.GONE);
                }
            }
        }
    }


}