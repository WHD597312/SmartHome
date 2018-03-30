package com.xinrui.smart.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginActivity extends AppCompatActivity {

    Unbinder unbinder;
    MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unbinder=ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
    }

    @OnClick({R.id.btn_login})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_login:
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
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
