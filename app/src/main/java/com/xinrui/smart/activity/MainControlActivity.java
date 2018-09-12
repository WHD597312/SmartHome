package com.xinrui.smart.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.fragment.ControlledFragment;
import com.xinrui.smart.fragment.ETSControlFragment;
import com.xinrui.smart.fragment.MainControlFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.Function;
import com.xinrui.smart.pojo.MainControl;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.GlideCircleTransform;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainControlActivity extends AppCompatActivity{


    @BindView(R.id.tv_main_device)
    TextView tv_main_device;//主控制

    @BindView(R.id.img_cancel) ImageView img_cancel;//返回键
    private FragmentManager fragmentManager;
    MyApplication application;
    DeviceChildDaoImpl deviceChildDao;


    public final static int MAINCONTROL=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_control);
        ButterKnife.bind(this);

        if (application==null){
            application= (MyApplication) getApplication();
        }
        deviceChildDao=new DeviceChildDaoImpl(this);
        application.addActivity(this);
        Intent intent=getIntent();
        content=intent.getStringExtra("content");
        houseId=intent.getStringExtra("houseId");
        List<DeviceChild> deviceChildren=deviceChildDao.findGroupIdAllDevice(Long.parseLong(houseId));


        tv_main_device.setText(content);
        fragmentManager=getFragmentManager();
    }


    String houseId;
    String content;
    @Override
    protected void onStart() {
        super.onStart();

        if ("主控机设置".equals(content)){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            MainControlFragment mainControlFragment=new MainControlFragment();
            fragmentTransaction.replace(R.id.layout_body, mainControlFragment);

            if (!Utils.isEmpty(houseId)){
                Bundle bundle=new Bundle();
                bundle.putString("houseId",houseId);
                mainControlFragment.setArguments(bundle);
                fragmentTransaction.commit();
            }
        }else if("受控机设置".equals(content)){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            ControlledFragment controlledFragment=new ControlledFragment();
            fragmentTransaction.replace(R.id.layout_body,controlledFragment);
            if (!Utils.isEmpty(houseId)){
                Bundle bundle=new Bundle();
                bundle.putString("houseId",houseId);
                controlledFragment.setArguments(bundle);
                fragmentTransaction.commit();
            }
        }else if("外置传感器".equals(content)){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            ETSControlFragment etsControlFragment= new ETSControlFragment();
            fragmentTransaction.replace(R.id.layout_body,etsControlFragment);
            if (!Utils.isEmpty(houseId)){
                Bundle bundle=new Bundle();
                bundle.putString("houseId",houseId);
                etsControlFragment.setArguments(bundle);
                fragmentTransaction.commit();
            }
        }
    }
    @OnClick({R.id.img_cancel,R.id.img_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_back:
//                List<DeviceChild> deviceChildren=deviceChildDao.findGroupIdAllDevice(Long.parseLong(houseId));
//                for (int i = 0; i < deviceChildren.size(); i++) {
//                    DeviceChild deviceChildr2=deviceChildren.get(i);
//                    Log.i("deviceChildAddress",deviceChildr2.getMacAddress()+","+deviceChildr2.getControlled());
//                }
                setResult(7000);
                finish();
//                startActivity(intent2);
                break;
            case R.id.img_cancel:
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra("mainControl","mainControl");
                startActivity(intent);
                break;
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(7000);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
