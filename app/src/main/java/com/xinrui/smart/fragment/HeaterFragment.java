package com.xinrui.smart.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinrui.smart.R;

/**
 * Created by win7 on 2018/3/20.
 */

public class HeaterFragment extends Fragment {
    View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_heater,container,false);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
