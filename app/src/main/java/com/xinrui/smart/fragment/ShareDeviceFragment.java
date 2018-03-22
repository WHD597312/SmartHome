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

public class ShareDeviceFragment extends Fragment {
    View view;
    private Unbinder unbinder;
    public static boolean isruning=false;
    ShareDeviceBackValue callBackValue;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_share_device,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        isruning=true;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isruning=false;
    }

    /**
     * fragmen与activity产生关联
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callBackValue= (ShareDeviceBackValue) getActivity();
        callBackValue.isShareDevice(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
            isruning=false;
        }
    }
    public interface ShareDeviceBackValue {
        public void isShareDevice(boolean shareDevice);
    }

}
