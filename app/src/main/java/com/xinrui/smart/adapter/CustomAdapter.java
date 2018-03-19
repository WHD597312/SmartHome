package com.xinrui.smart.adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
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
    final int itemLength = 20;

    Room room = new Room();

    private LayoutInflater layoutInflater;

    private Context context;

    private int clickTemp = -1;

    private int[] clickedList=new int[itemLength];


    public CustomAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        for (int i = 0; i < itemLength; i++) {
            clickedList[i] = 0;
        }
    }
    Room r1 = new Room();
    Room r2 = new Room();
    Room r3 = new Room();

    public CustomAdapter(List<Room> list) {
        super(list);
        for (int i = 0; i < 20; i++) {
            list.add(0,r1);
        }
    }
    @Override
    public int getCount() {
        return 20;
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


     View view;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolde viewHolde;

        if(convertView == null){
            viewHolde = new ViewHolde();
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customroom_item,null);
            viewHolde.customroom_text = (TextView) view.findViewById(R.id.cusromroom_text);

            view.setTag(viewHolde);
        }else{
            view = convertView;
            viewHolde = (ViewHolde) convertView.getTag();
        }


//        final int colors[] = {Color.BLUE,Color.TRANSPARENT};
//        int color=colors[0];
//        colors[0]=colors[1];
//        colors[1]=color;
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                view.setBackgroundColor(colors[0]);
//            }
//        });


//        if(clickTemp == position){
//            if(clickedList[position]==0 ) {
//                view.setBackgroundColor(Color.BLUE);
//                clickedList[position] = 1;
//            }else{
//                view.setBackgroundColor(Color.TRANSPARENT);
//                clickedList[position]=0;
//
//            }
//        }

        viewHolde.customroom_text.setText("卧室");
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
