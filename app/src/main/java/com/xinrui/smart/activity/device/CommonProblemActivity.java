package com.xinrui.smart.activity.device;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xinrui.secen.scene_activity.AddEquipmentActivity;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_problem);
        unbinder=ButterKnife.bind(this);
        tv_problem.setText("以下情况将无法发现设备\n1.使用配置时必须拥有网络的WiFi;\n2.Wi-Fi名称不能使用中文;\n3.配置时Wi-Fi密码不能输错；\n4.设备只能逐一配置");
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
