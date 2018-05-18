package com.xinrui.smart.activity.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.xinrui.secen.scene_activity.AddEquipmentActivity;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.util.Utils;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AboutAppActivity extends AppCompatActivity {

    Unbinder unbinder;
    MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        unbinder= ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }
    }
    String main;
    String common;
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();
        main=intent.getStringExtra("main");
        common=intent.getStringExtra("common");
    }

    @OnClick({R.id.image_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_back:
                if (!Utils.isEmpty(main) && Utils.isEmpty(common)){
                    startActivity(new Intent(this,MainActivity.class));
                }else if (Utils.isEmpty(main) && !Utils.isEmpty(common)){
                    startActivity(new Intent(this,AddEquipmentActivity.class));
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!Utils.isEmpty(main) && Utils.isEmpty(common)){
            startActivity(new Intent(this,MainActivity.class));
        }else if (Utils.isEmpty(main) && !Utils.isEmpty(common)){
            startActivity(new Intent(this,AddEquipmentActivity.class));
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
