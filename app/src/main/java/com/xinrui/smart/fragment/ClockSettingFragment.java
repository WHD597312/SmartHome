package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinrui.smart.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ClockSettingFragment extends Fragment {
    View view;
    private Unbinder unbinder;
    ClockCallBackValue callBackValue;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_clock_setting,container,false);
        unbinder= ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callBackValue= (ClockCallBackValue) getActivity();
        callBackValue.isClock(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }
    public interface ClockCallBackValue {
        public void isClock(boolean clock);
    }
}
