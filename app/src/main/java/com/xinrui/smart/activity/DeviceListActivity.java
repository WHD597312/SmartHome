package com.xinrui.smart.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.fragment.ClockSettingFragment;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.fragment.ShareDeviceFragment;
import com.xinrui.smart.fragment.TimeTaskFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DeviceListActivity extends AppCompatActivity implements
        ShareDeviceFragment.ShareDeviceBackValue,
        ClockSettingFragment.ClockCallBackValue,TimeTaskFragment.TimeTaskCall {

    private Unbinder unbinder;
    @BindView(R.id.img_back)
    ImageView img_back;//返回键
    @BindView(R.id.tv_name)
    TextView tv_name;
    private int[] colors={R.color.color_black, R.color.holo_orange_dark};
    private FragmentManager fragmentManager;
    @BindView(R.id.btn_share_device)
    Button btn_share_device;//分享设备
    @BindView(R.id.btn_clock_setting)
    Button btn_clock_setting;//s时钟设置
    @BindView(R.id.btn_time_task)
    Button btn_time_task;//定时任务
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        unbinder=ButterKnife.bind(this);
    }

    @OnClick({R.id.img_back, R.id.btn_share_device, R.id.btn_clock_setting, R.id.btn_time_task})
    public void onClick(View view){
        switch (view.getId()){

            case R.id.img_back:
                finish();
                break;
            case R.id.btn_share_device:
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.linearout,new ShareDeviceFragment());
                fragmentTransaction.commit();
                btn_clock_setting.setTextColor(getResources().getColor(R.color.color_black));
                btn_time_task.setTextColor(getResources().getColor(R.color.color_black));
                break;
            case R.id.btn_clock_setting:
                FragmentTransaction fragmentTransaction2=fragmentManager.beginTransaction();
                fragmentTransaction2.replace(R.id.linearout,new ClockSettingFragment());
                fragmentTransaction2.commit();
                btn_share_device.setTextColor(getResources().getColor(R.color.color_black));
                btn_time_task.setTextColor(getResources().getColor(R.color.color_black));
                break;
            case R.id.btn_time_task:
                FragmentTransaction fragmentTransaction3=fragmentManager.beginTransaction();
                fragmentTransaction3.replace(R.id.linearout,new TimeTaskFragment());
                fragmentTransaction3.commit();
                btn_share_device.setTextColor(getResources().getColor(R.color.color_black));
                btn_clock_setting.setTextColor(getResources().getColor(R.color.color_black));
                tv_name.setText("定时任务");
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();
        String content=intent.getStringExtra("content");
        tv_name.setText(content);

        fragmentManager=getFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.linearout,new HeaterFragment());
        fragmentTransaction.commit();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            //解绑界面元素
            unbinder.unbind();
        }
    }
    @Override
    public void isShareDevice(boolean shareDevice) {
        if (shareDevice){
            btn_share_device.setTextColor(getResources().getColor(R.color.holo_orange_dark));
        }else {
            btn_share_device.setTextColor(getResources().getColor(R.color.color_black));
        }
    }

    @Override
    public void isClock(boolean clock) {
        if (clock){
            btn_clock_setting.setTextColor(getResources().getColor(R.color.holo_orange_dark));
        }else {
           btn_clock_setting.setTextColor(getResources().getColor(R.color.color_black));
        }
    }

    @Override
    public void isTimeTask(boolean timeTask) {
        if (timeTask){
            btn_time_task.setTextColor(getResources().getColor(R.color.holo_orange_dark));
        }else
            btn_time_task.setTextColor(getResources().getColor(R.color.color_black));
    }
}
