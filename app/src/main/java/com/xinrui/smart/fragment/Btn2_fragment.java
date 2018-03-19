package com.xinrui.smart.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xinrui.smart.R;


/**
 * Created by win7 on 2018/3/13.
 */

public class Btn2_fragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.btn2_fragment,container,false);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_btn2);
        imageView.setImageResource(R.drawable.bedroom);
        return view;
    }
}
