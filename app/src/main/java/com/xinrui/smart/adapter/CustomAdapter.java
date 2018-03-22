package com.xinrui.smart.adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.xinrui.smart.R;
import com.xinrui.smart.pojo.Room;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by win7 on 2018/3/12.
 */

public class CustomAdapter extends MyBaseAdapter {
    final int itemLength = 32;

    List<Room> list = new ArrayList<>();

    private LayoutInflater layoutInflater;

    private Context context;

    private int clickTemp = -1;

    private int[] clickedList=new int[itemLength];


    public CustomAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }
    public CustomAdapter(Context context,List<Room> list) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
    }
    @Override
    public int getCount() {
        return itemLength;
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolde viewHolde;
        View view;
        if(null == convertView){
            viewHolde = new ViewHolde();
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customroom_item,null);
            viewHolde.customroom_text = (TextView) view.findViewById(R.id.cusromroom_text);

            view.setTag(viewHolde);
        }else{
            view = convertView;
            viewHolde = (ViewHolde) convertView.getTag();
        }
        viewHolde.customroom_text.setText("");
        int width = parent.getWidth()/4;
        int height = width;
        viewHolde.customroom_text.setWidth(width);
        viewHolde.customroom_text.setHeight(height);
        return view;
    }

    class ViewHolde{
        private TextView customroom_text;

        public ViewHolde() {
        }

        public ViewHolde(TextView customroom_text) {
            this.customroom_text = customroom_text;
        }

        public TextView getCustomroom_text() {
            return customroom_text;
        }

        public void setCustomroom_text(TextView customroom_text) {
            this.customroom_text = customroom_text;
        }
    }



}
