package com.xinrui.smart.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
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

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.FunctionAdapter;
import com.xinrui.smart.fragment.ControlledFragment;
import com.xinrui.smart.fragment.ETSControlFragment;
import com.xinrui.smart.fragment.MainControlFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.Function;
import com.xinrui.smart.pojo.MainControl;
import com.xinrui.smart.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainControlActivity extends AppCompatActivity{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.listview)
    ListView listview;
    @BindView(R.id.tv_main_device)
    TextView tv_main_device;//主控制

    @BindView(R.id.img_cancel) ImageView img_cancel;//返回键
    private FragmentManager fragmentManager;
    MyApplication application;
    public final static int MAINCONTROL=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_control);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //设置左上角的图标响应
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
    }

    private DeviceGroupDaoImpl deviceGroupDao;
    private DeviceChildDaoImpl deviceChildDao;
    String houseId;

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent=getIntent();
        String content=intent.getStringExtra("content");
        houseId=intent.getStringExtra("houseId");


        tv_main_device.setText(content);
        fragmentManager=getFragmentManager();

        function();
        if ("主控制设置".equals(content)){
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
            fragmentTransaction.replace(R.id.layout_body,new ETSControlFragment());

            fragmentTransaction.commit();
        }
    }
    @OnClick({R.id.img_cancel})
    public void onClick(View view){
        switch (view.getId()){
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("mainControl","mainControl");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
