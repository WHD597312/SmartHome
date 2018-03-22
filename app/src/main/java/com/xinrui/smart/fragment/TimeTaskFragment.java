package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.view_custom.CircleSeekBar;
import com.xinrui.smart.view_custom.TimePickerDialog;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class TimeTaskFragment extends Fragment {
    View view;
    Unbinder unbinder;
    TimeTaskCall timeTaskCall;
    @BindView(R.id.seekbar) CircleSeekBar seekbar;
    @BindView(R.id.tv_open) TextView tv_open;//开始时间
    @BindView(R.id.tv_open_time) TextView tv_open_time;//设定开始时间
    @BindView(R.id.tv_close) TextView tv_close;//结束时间
    @BindView(R.id.tv_close_time) TextView tv_close_time;//设定结束时间
    public static float OPEN_TIME=0F;
    public static float CLOSE_TIME=0F;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_time_task,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//
    }

    @OnClick({R.id.tv_open,R.id.tv_close})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_open:
                buildOpenDialog();
                break;
            case R.id.tv_close:
                buildCloseDialog();
                break;
        }
    }
    private void buildOpenDialog(){

        final TimePickerDialog dialog=new TimePickerDialog(getActivity());

        dialog.setOnNegativeClickListener(new TimePickerDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new TimePickerDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
//                Toast.makeText(getActivity(),"确定", Toast.LENGTH_LONG).show();
                String s=dialog.getTimeValue();
                if (s!=null && s!=""){
//                    Toast.makeText(getActivity(),"当前时间:"+s, Toast.LENGTH_LONG).show();
                    tv_open_time.setText(s+":00");
                    OPEN_TIME=(360/24)*Integer.parseInt(s);
                    seekbar.setStart_angle(OPEN_TIME-90);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void buildCloseDialog(){
        final TimePickerDialog dialog=new TimePickerDialog(getActivity());
        dialog.setOnNegativeClickListener(new TimePickerDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new TimePickerDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
//                Toast.makeText(getActivity(),"确定", Toast.LENGTH_LONG).show();
                String s=dialog.getTimeValue();
                if (s!=null && s!=""){
//                    Toast.makeText(getActivity(),"当前时间:"+s, Toast.LENGTH_LONG).show();
                    String time_open=tv_open_time.getText().toString();
                    String open=time_open.substring(0,time_open.indexOf(":"));
                    int openTime=Integer.parseInt(open);
                    int closeTime=Integer.parseInt(s);
                    if (closeTime>openTime){
                        tv_close_time.setText(s+":00");
                        CLOSE_TIME=(360/24)*Integer.parseInt(s);
                        seekbar.setCur_angle(CLOSE_TIME-90);
                    }else {
                        Toast.makeText(getActivity(),"结束时间不能小于开始时间",Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        timeTaskCall= (TimeTaskCall) getActivity();
        timeTaskCall.isTimeTask(true);

    }
    public interface TimeTaskCall{
        public void isTimeTask(boolean timeTask);
    }
}
