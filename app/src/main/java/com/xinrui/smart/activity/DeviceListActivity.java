package com.xinrui.smart.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.device.AboutUsActivity;
import com.xinrui.smart.activity.device.ShareDeviceActivity;
import com.xinrui.smart.adapter.DeviceListAdapter;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

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
//    @BindView(R.id.linearout2) LinearLayout linearout2;
//    @BindView(R.id.timePicker) TimePicker timePicker;
//    @BindView(R.id.datePicker) DatePicker datePicker;
//    @BindView(R.id.tv_clock) TextView tv_clock;
    @BindView(R.id.linearout) LinearLayout linearout;
    @BindView(R.id.tv_offline) TextView tv_offline;
    MyApplication application;
    private String childPosition;
    private DeviceChildDaoImpl deviceChildDao;
    public static boolean running=false;
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


    @OnClick({R.id.img_back, R.id.image_home})
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

        }
    }

    int mPoistion=-1;;// 选中的位置
    private int hour=0;
    int year=0;
    int month;
    int day=0;
    SharedPreferences preferences;
    DeviceChild deviceChild;

    @Override
    protected void onStart() {
        super.onStart();
        deviceChildDao=new DeviceChildDaoImpl(getApplicationContext());
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
        int commit=fragmentTransaction.commit();
        Log.i("mmmmm",commit+"");


        list = new ArrayList<>();
        String[] titles = {"分享设备", "时钟设置", "定时任务", "设备状态", "常见问题", "关于我们"};
        for (int i = 0; i < titles.length; i++) {
            list.add(titles[i]);
        }

        adapter=new DeviceListAdapter(this,list);
        gradView.setAdapter(adapter);

        gradView.setOnItemClickListener(this);
        adapter.setSelectedPosition(mPoistion);


        running=true;

        boolean online=deviceChild.getOnLint();
        if (online){
            linearout.setVisibility(View.VISIBLE);
            tv_offline.setVisibility(View.GONE);
            gradView.setVisibility(View.VISIBLE);
        }else {
            linearout.setVisibility(View.GONE);
            tv_offline.setVisibility(View.VISIBLE);
            gradView.setVisibility(View.GONE);
        }
//        linearout2.setVisibility(View.GONE);
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
    protected void onStop() {
        super.onStop();
        deviceChildDao.closeDaoSession();
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            //解绑界面元素
            unbinder.unbind();
        }

        application.removeActivity(this);
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
                popupWindow();
//                linearout2.setVisibility(View.VISIBLE);
//                gradView.setVisibility(View.GONE);
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
                Intent intent4=new Intent(this,ComProblemActivity.class);
                intent4.putExtra("deviceId",childPosition);
                startActivity(intent4);
                break;
            case 5:
                Intent intent5=new Intent(this, AboutUsActivity.class);
                startActivity(intent5);
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
            String noNet=intent.getStringExtra("noNet");
            String machineFall=intent.getStringExtra("machineFall");
            if(Utils.isEmpty(noNet)){
                if (deviceChild!=null && deviceChild.getMacAddress().equals(deviceChild2.getMacAddress())){
                    if ("online".equals(online)){
                        if ("fall".equals(machineFall)){
                            linearout.setVisibility(View.GONE);
                            tv_offline.setVisibility(View.VISIBLE);
                            tv_offline.setText("设备已倾倒");
                            gradView.setVisibility(View.GONE);
                        }else {
                            linearout.setVisibility(View.VISIBLE);
                            tv_offline.setVisibility(View.GONE);
                            gradView.setVisibility(View.VISIBLE);
                        }
                    }else if ("offline".equals(online)){
                        linearout.setVisibility(View.GONE);
                        tv_offline.setVisibility(View.VISIBLE);
                        tv_offline.setText("设备已离线");
                        gradView.setVisibility(View.GONE);
                    }
                }
            }else {
                if ("fall".equals(deviceChild.getMachineFall())){
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    tv_offline.setText("设备已倾倒");
                    gradView.setVisibility(View.GONE);
                }else {
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    gradView.setVisibility(View.GONE);
                    tv_offline.setText("设备已离线");
                }
            }

        }
    }

    private PopupWindow popupWindow;
    TextView tv_clock;

    //底部popupWindow
    public void popupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popup_clockset, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        DatePicker datePicker= (DatePicker) view.findViewById(R.id.datePicker);
        TimePicker timePicker= (TimePicker) view.findViewById(R.id.timePicker);
        tv_clock= (TextView) view.findViewById(R.id.tv_clock);
        Button btn_cancle= (Button) view.findViewById(R.id.btn_cancle);
        Button btn_ensure= (Button) view.findViewById(R.id.btn_ensure);

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);

        ColorDrawable dw = new ColorDrawable(0x30000000);
        popupWindow.setBackgroundDrawable(dw);
        popupWindow.showAtLocation(linearout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        //添加按键事件监听

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_cancle:
                        popupWindow.dismiss();
                        break;
                    case R.id.btn_ensure:
                        popupWindow.dismiss();
                        break;

                }

            }
        };
        timePicker.setIs24HourView(true);
        Calendar calendar= Calendar.getInstance();
        year=calendar.get(Calendar.YEAR);
        month=calendar.get(Calendar.MONTH);
        day=calendar.get(Calendar.DAY_OF_MONTH);
        hour=calendar.get(Calendar.HOUR_OF_DAY);


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
        month=month+1;
        tv_clock.setText(year+"年"+month+"月"+day+"日"+hour+"时");
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour=hourOfDay;
                tv_clock.setText(year+"年"+month+"月"+day+"日"+hour+"时");
            }
        });

        btn_cancle.setOnClickListener(listener);
        btn_ensure.setOnClickListener(listener);
    }


}