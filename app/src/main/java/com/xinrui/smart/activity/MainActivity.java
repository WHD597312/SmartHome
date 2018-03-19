package com.xinrui.smart.activity;

import android.app.ActionBar;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.fragment.DeviceFragment;
import com.xinrui.smart.fragment.LiveFragment;
import com.xinrui.smart.fragment.SmartFragment;
import com.xinrui.smart.pojo.Function;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;

    @BindView(R.id.tv_exit) TextView tv_exit;//退出
    @BindView(R.id.tv_device) TextView tv_device;//设备
    @BindView(R.id.tv_smart) TextView tv_smart;//智能
    @BindView(R.id.tv_live) TextView tv_live;//实景
    @BindView(R.id.listview) ListView listview;
    private FragmentManager fragmentManager;//碎片管理者
    private DeviceFragment deviceFragment;//设备碎片
    private int[] colors={R.color.color_blue,R.color.color_dark_gray};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();//开启碎片事务
        deviceFragment=new DeviceFragment();
        fragmentTransaction.replace(R.id.layout_body,deviceFragment);
        fragmentTransaction.commit();

        function();
        tv_device.setTextColor(getResources().getColor(R.color.color_black));
        tv_smart.setTextColor(getResources().getColor(R.color.color_gray2));
        tv_live.setTextColor(getResources().getColor(R.color.color_gray2));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

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
                Toast.makeText(this,"dfsaf",Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_device:
                FragmentTransaction fragmentTransaction3=fragmentManager.beginTransaction();//开启碎片事务
                fragmentTransaction3.replace(R.id.layout_body,new DeviceFragment());
                fragmentTransaction3.commit();
                tv_device.setTextColor(getResources().getColor(R.color.color_black));
                tv_smart.setTextColor(getResources().getColor(R.color.color_gray2));
                tv_live.setTextColor(getResources().getColor(R.color.color_gray2));
                break;
            case R.id.tv_smart:
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.layout_body,new SmartFragment());
                fragmentTransaction.commit();

                tv_device.setTextColor(getResources().getColor(R.color.color_gray2));
                tv_smart.setTextColor(getResources().getColor(R.color.color_black));
                tv_live.setTextColor(getResources().getColor(R.color.color_gray2));
                break;
            case R.id.tv_live:

                FragmentTransaction transaction=fragmentManager.beginTransaction();
                transaction.replace(R.id.layout_body,new LiveFragment());
                transaction.commit();
                tv_device.setTextColor(getResources().getColor(R.color.color_gray2));
                tv_smart.setTextColor(getResources().getColor(R.color.color_gray2));
                tv_live.setTextColor(getResources().getColor(R.color.color_black));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}