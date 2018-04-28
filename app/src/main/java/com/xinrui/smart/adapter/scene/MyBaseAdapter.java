package com.xinrui.smart.adapter.scene;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import com.xinrui.smart.pojo.scene.Room;

import java.util.List;

/**
 * Created by win7 on 2018/3/10.
 */

public class MyBaseAdapter extends BaseAdapter {
    protected List<Room> list;

    public MyBaseAdapter(List<Room> list) {
        this.list = list;
    }

    public MyBaseAdapter() {

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }


}
