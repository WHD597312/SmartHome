package com.xinrui.smart.activity.device;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.xinrui.secen.scene_activity.AddEquipmentActivity;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AboutAppActivity extends AppCompatActivity {

    Unbinder unbinder;
    MyApplication application;
    @BindView(R.id.textView4) TextView textView4;
    String versionName;
    int versionCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        unbinder= ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }
        PackageManager packageManager=application.getPackageManager();
        try {
            PackageInfo packageInfo=packageManager.getPackageInfo(getPackageName(),0);
            versionName=packageInfo.versionName;
            versionCode=packageInfo.versionCode;
            textView4.setText("当前版本:"+versionName);
            System.out.println("pakageInfo:"+versionName+","+versionCode);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    String main;
    String common;
    String device;
    String change;
    String smart;
    String live;
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();
        main=intent.getStringExtra("main");
        common=intent.getStringExtra("common");
        device = intent.getStringExtra("device");
        change = intent.getStringExtra("change");
        smart=intent.getStringExtra("smart");
        live=intent.getStringExtra("live");
    }

    @OnClick({R.id.image_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_back:
                if (!Utils.isEmpty(device)){
                    Intent intent=new Intent(this,MainActivity.class);
//                    intent.putExtra("deviceList","deviceList");
//                    startActivity(intent);
                    setResult(6000,intent);
                    finish();
                } else if (!Utils.isEmpty(smart)){
                    Intent intent = new Intent(this, MainActivity.class);
//                    intent.putExtra("mainControl","mainControl");
//                    startActivity(intent);
                    setResult(7000,intent);
                    finish();
                }else if (!Utils.isEmpty(live)){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("live","live");
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!Utils.isEmpty(device)){
            Intent intent=new Intent(this,MainActivity.class);
//            intent.putExtra("deviceList","deviceList");
//            startActivity(intent);
            setResult(6000,intent);
            finish();
        } else if (!Utils.isEmpty(smart)){
            Intent intent = new Intent(this, MainActivity.class);
//            intent.putExtra("mainControl","mainControl");
//            startActivity(intent);
            setResult(7000,intent);
            finish();
        }else if (!Utils.isEmpty(live)){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("live","live");
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }

    }

}
