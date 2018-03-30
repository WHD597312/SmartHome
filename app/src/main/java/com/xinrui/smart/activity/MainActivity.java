package com.xinrui.smart.activity;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.LiveFragment;
import com.xinrui.smart.fragment.SmartFragmentManager;
import com.xinrui.smart.pojo.Function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    MyApplication application;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;

    @BindView(R.id.tv_exit) TextView tv_exit;//退出
    @BindView(R.id.tv_device) TextView tv_device;//设备
    @BindView(R.id.tv_smart) TextView tv_smart;//智能
    @BindView(R.id.tv_live) TextView tv_live;//实景
    @BindView(R.id.listview) ListView listview;
    private FragmentManager fragmentManager;//碎片管理者
    private DeviceFragment deviceFragment;//设备碎片
    @BindView(R.id.device_view) View device_view;//设备页
    @BindView(R.id.smart_view) View smart_view;//智能页
    @BindView(R.id.live_view) View live_view;//实景页
    private int[] colors={R.color.color_blue,R.color.color_dark_gray};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //设置左上角的图标响应
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);


        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);//修改DrawerLayout侧滑菜单图标
        //这样修改了图标，但是这个图标的点击事件会消失，点击图标不能打开侧边栏
        //所以还要加上如下代码
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               drawer.openDrawer(GravityCompat.START);
            }
        });

        fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();//开启碎片事务
        deviceFragment=new DeviceFragment();
        fragmentTransaction.replace(R.id.layout_body,deviceFragment);
        fragmentTransaction.commit();
        device_view.setVisibility(View.VISIBLE);
        function();

    }

    public void goLiveFragment(){
         FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LiveFragment liveFragment = new LiveFragment();
        fragmentTransaction.replace(R.id.layout_body, liveFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private LocationManager locationManager;
    private String provider;
    @Override
    protected void onResume() {
        super.onResume();




    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * 设置功能菜单
     */
    private void function(){
        int[] imgs={R.drawable.example,R.drawable.example,R.drawable.example,R.drawable.example};
        String[] strings={"主页","常见问题","通用设置","关于应用"};
        List<Function> functions=new ArrayList<>();
        for(int i=0;i<imgs.length;i++){
            Function function=new Function(imgs[i],strings[i]);
            functions.add(function);
        }
        FunctionAdapter adapter=new FunctionAdapter(this,functions);
        listview.setAdapter(adapter);
    }
    @OnClick({R.id.tv_exit,R.id.tv_device,R.id.tv_smart,R.id.tv_live})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_exit:
                application.removeAllActivity();/**退出应用程序*/
                break;
            case R.id.tv_device:
                FragmentTransaction fragmentTransaction3=fragmentManager.beginTransaction();//开启碎片事务
                fragmentTransaction3.replace(R.id.layout_body,new DeviceFragment());
                fragmentTransaction3.commit();
                device_view.setVisibility(View.VISIBLE);
                smart_view.setVisibility(View.GONE);
                live_view.setVisibility(View.GONE);
                break;
            case R.id.tv_smart:
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.layout_body,new SmartFragmentManager());
                fragmentTransaction.commit();
                device_view.setVisibility(View.GONE);
                smart_view.setVisibility(View.VISIBLE);
                live_view.setVisibility(View.GONE);
                break;
            case R.id.tv_live:
                FragmentTransaction transaction=fragmentManager.beginTransaction();
                transaction.replace(R.id.layout_body,new LiveFragment());
                transaction.commit();
                device_view.setVisibility(View.GONE);
                smart_view.setVisibility(View.GONE);
                live_view.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}