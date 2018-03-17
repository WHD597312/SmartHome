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


    private CircleSeekBar mSeekbar;

    private TextView mTextView;

    private int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        ButterKnife.bind(this);
        mSeekbar = (CircleSeekBar) findViewById(R.id.seekbar);
        mTextView = (TextView) findViewById(R.id.textview);

        mSeekbar.setOnSeekBarChangeListener(new CircleSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(CircleSeekBar seekbar, double curValue) {
                if (curValue>0.0){
                    String str=curValue+"";
                    String[] args=str.split(".");
                }

//                int minuts=Integer.parseInt(args[1]);
                mTextView.setText("value:" + curValue);
            }
        });
        mSeekbar.setCurProcess(0);
    }
    @OnClick(R.id.button)
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button:
                count++;
                if (count==24){
                    count=0;
                }
                mSeekbar.setCurProcess(count);
                break;
        }
    }
}
