package com.xinrui.smart.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.view_custom.CircleSeekBar;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClockActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        ButterKnife.bind(this);

    }

}
