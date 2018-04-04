package com.xinrui.smart.fragment;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.xinrui.smart.R;

import java.util.Locale;

/**
 * Created by win7 on 2018/3/13.
 */

public class Copy_fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.btn1_fragment,container,false);
//        ImageView imageView = view.findViewById(R.id.iv_btn1);
//        imageView.setImageResource(R.drawable.dining_room);

        return view;
    }

}
