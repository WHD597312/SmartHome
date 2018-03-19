package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinrui.smart.R;
import com.xinrui.smart.view_custom.CircleSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class TimeTaskFragment extends Fragment {
    View view;
    Unbinder unbinder;
    TimeTaskCall timeTaskCall;
    @BindView(R.id.seekbar) CircleSeekBar seekbar;
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
        timeTaskCall.isTimeTask(true);
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

    }
    public interface TimeTaskCall{
        public void isTimeTask(boolean timeTask);
    }
}
