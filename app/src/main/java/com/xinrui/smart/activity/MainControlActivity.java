package com.xinrui.smart.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.fragment.ControlledFragment;
import com.xinrui.smart.fragment.ETSControlFragment;
import com.xinrui.smart.fragment.MainControlFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainControlActivity extends AppCompatActivity {

    @BindView(R.id.tv_main_device)
    TextView tv_main_device;

    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_control);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=getIntent();
        String content=intent.getStringExtra("content");

        tv_main_device.setText(content);
        fragmentManager=getFragmentManager();

        if ("主控制设置".equals(content)){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.linearout,new MainControlFragment());
            fragmentTransaction.commit();
        }else if("受控机设置".equals(content)){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.linearout,new ControlledFragment());
            fragmentTransaction.commit();

        }else if("外置温度传感设置".equals(content)){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.linearout,new ETSControlFragment());
            fragmentTransaction.commit();

        }
    }
    @OnClick({R.id.img_back, R.id.btn_ensure})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_ensure:
                finish();
                break;
        }
    }

}
