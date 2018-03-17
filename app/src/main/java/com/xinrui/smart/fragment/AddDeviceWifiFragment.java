package com.xinrui.smart.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinrui.smart.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AddDeviceWifiFragment extends Fragment {

    View view;
    Unbinder unbinder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_add_device_wifi,container,false);
        unbinder=ButterKnife.bind(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
