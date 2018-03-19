package com.xinrui.smart.fragment;

import android.content.Context;
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

public class Btn1_fragment extends Fragment {
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        DragGridView dragGridView = new DragGridView(mContext);
        View view = inflater.inflate(R.layout.btn1_fragment,container,false);
//        ImageView imageView = view.findViewById(R.id.iv_btn1);
//        imageView.setImageResource(R.drawable.drawing_room);
        return view;
    }
}
