package com.xinrui.smart.activity.device;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class CommonProblemActivity extends AppCompatActivity {

    @BindView(R.id.tv_problem) TextView tv_problem;
    Unbinder unbinder;
    MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_problem);
        unbinder=ButterKnife.bind(this);
        tv_problem.setText("以下情况将无法发现设备;\n1.配置时WIFI网络无信号;\n2.WIFI名称使用中文;\n3.配置时WIFI密码输错;\n4.多个设备一起配置;\n5.设备正在配置时,终止配置");
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
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
                    intent.putExtra("deviceList","deviceList");
                    startActivity(intent);
                } else if (!Utils.isEmpty(smart)){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("mainControl","mainControl");
                    startActivity(intent);
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
            intent.putExtra("deviceList","deviceList");
            startActivity(intent);
        } else if (!Utils.isEmpty(smart)){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("mainControl","mainControl");
            startActivity(intent);
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
