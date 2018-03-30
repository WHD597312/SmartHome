package com.xinrui.smart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class RegistActivity2 extends AppCompatActivity {

    MyApplication application;
    Unbinder unbinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist2);
        unbinder=ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
    }

    @OnClick({R.id.btn_finish})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_finish:
                Intent intent=new Intent(this,LoginActivity.class);
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
