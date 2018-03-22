package com.xinrui.smart.view_custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.xinrui.smart.R;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/9.
 */

/**
 * 创建时间选择器对话框
 */
public class TimePickerDialog extends Dialog {

    @BindView(R.id.button_cancel)
    Button button_cancel;
    @BindView(R.id.button_ensure)
    Button button_ensure;
    @BindView(R.id.time) TimePicker time;
    private String timeValue;
    public TimePickerDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_timepicker);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        time.setIs24HourView(true);

        time.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeValue=hourOfDay+"";
            }
        });
        Calendar calendar = Calendar.getInstance();
        timeValue=calendar.get(Calendar.HOUR_OF_DAY)+"";//获取系统当前时间
    }

    public String getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }

    @OnClick({R.id.button_cancel, R.id.button_ensure})
    public void onClick(View view){
        switch(view.getId()){
            case R.id.button_cancel:
                if (onNegativeClickListener!=null){
                    onNegativeClickListener.onNegativeClick();
                }
                break;
            case R.id.button_ensure:
                if (onPositiveClickListener!=null){
                    onPositiveClickListener.onPositiveClick();
                }
                break;
        }
    }
    private OnPositiveClickListener onPositiveClickListener;

    public void setOnPositiveClickListener(OnPositiveClickListener onPositiveClickListener) {


        this.onPositiveClickListener = onPositiveClickListener;
    }

    private OnNegativeClickListener onNegativeClickListener;

    public void setOnNegativeClickListener(OnNegativeClickListener onNegativeClickListener) {

        this.onNegativeClickListener = onNegativeClickListener;
    }

    public interface OnPositiveClickListener {
        void onPositiveClick();
    }

    public interface OnNegativeClickListener {
        void onNegativeClick();
    }
}
