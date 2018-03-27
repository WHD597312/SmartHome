package com.xinrui.smart.fragment;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.TimeAdapter;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/26.
 */

public class TaskTimeFragement extends Fragment{
    View view;
    Unbinder unbinder;

    @BindView(R.id.timePicker) TimePicker timePicker;
    @BindView(R.id.tv_clock) TextView tv_clock;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_task_time_set,container,false);

        unbinder=ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        timePicker.setIs24HourView(true);
        Bundle bundle=getArguments();
        if (bundle!=null){
            String open=bundle.getString("open");
            tv_clock.setText(open);
        }
    }

    @OnClick({R.id.btn_cancle})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_cancle:
                getChildFragmentManager().beginTransaction().hide(this);
                break;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }
}
