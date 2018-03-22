package com.xinrui.smart.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.view_custom.CircleSeekBar;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClockActivity extends AppCompatActivity {

    @BindView(R.id.time) TimePicker time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        ButterKnife.bind(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        time.setIs24HourView(true);

        time.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Toast.makeText(ClockActivity.this,hourOfDay+"小时"+minute+"分",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.button)
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button:
                time.setVisibility(View.VISIBLE);
                break;
        }
    }


}
