package com.xinrui.smart.activity.device;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xinrui.secen.scene_view_custom.MySeekBar;
import com.xinrui.smart.R;

public class test extends AppCompatActivity {
     MySeekBar mySeekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_clockset);
        mySeekBar = (MySeekBar) findViewById(R.id.beautySeekBar1);

        mySeekBar.invalidate();
    }
}
