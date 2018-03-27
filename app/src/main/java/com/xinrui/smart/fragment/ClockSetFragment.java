package com.xinrui.smart.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.xinrui.smart.R;
import com.xinrui.smart.util.Utils;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/26.
 */

public class ClockSetFragment extends Fragment {
    View view;
    Unbinder unbinder;
    @BindView(R.id.datePicker) DatePicker datePicker;
    @BindView(R.id.timePicker) TimePicker timePicker;
    @BindView(R.id.tv_clock) TextView tv_clock;
    private int year;
    private int month;
    private int day;
    private int week;
    private String hour;
    private String mWeek;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_clock_set,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }
    Calendar calendar;
    @Override
    public void onStart() {
        super.onStart();
        timePicker.setIs24HourView(true);
        calendar=Calendar.getInstance();
        year=calendar.get(Calendar.YEAR);
        month=calendar.get(Calendar.MONTH)+1;
        day=calendar.get(Calendar.DAY_OF_MONTH);
        week=calendar.get(Calendar.DAY_OF_WEEK);
        mWeek=Utils.getWeek(year,month,day,week);

        tv_clock.setText(year+"年"+month+"月"+day+"日"+mWeek);
        datePicker.init(year, month-1, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                int Y=Integer.parseInt(String.valueOf(year).substring(2));
                int sum=Utils.cal(year,monthOfYear+1,dayOfMonth);//一年中的第几天
                int W = (Y-1) + ((Y-1)/4) - ((Y-1)/100) + ((Y-1)/400) + sum;
                int week=W % 7;
                switch (week){
                    case (1):
                        mWeek="星期一";
                        break;
                    case (2):
                        mWeek="星期二";
                        break;
                    case 3:
                        mWeek="星期三";
                        break;
                    case 4:
                        mWeek="星期四";
                        break;
                    case 5:
                        mWeek="星期五";
                        break;
                    case 6:
                        mWeek="星期六";
                        break;
                    case 7:
                        mWeek="星期日";
                        break;
                }
                Log.d("ss",year+"年"+monthOfYear+"月"+dayOfMonth+"日");
                tv_clock.setText(year+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日"+mWeek);
            }
        });

    }

}
