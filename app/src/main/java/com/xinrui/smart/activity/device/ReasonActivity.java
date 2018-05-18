package com.xinrui.smart.activity.device;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ReasonActivity extends AppCompatActivity {

    Unbinder unbinder;
    @BindView(R.id.tv_reason) TextView tv_reason;
    @BindView(R.id.tv_cs) TextView tv_cs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reason);
        unbinder=ButterKnife.bind(this);
        Intent intent=getIntent();
        String reason=intent.getStringExtra("reason");
        String cs=intent.getStringExtra("cs");
        if (!Utils.isEmpty(reason)){
            tv_reason.setText(reason);
        }
        if (!Utils.isEmpty(cs)){
            tv_cs.setText(cs);
        }
    }

    @OnClick({R.id.image_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_back:
                finish();
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
