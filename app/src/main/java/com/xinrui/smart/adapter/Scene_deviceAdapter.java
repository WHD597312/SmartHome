package com.xinrui.smart.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.Equipment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2018/4/12.
 */

public class Scene_deviceAdapter extends RecyclerView.Adapter<Scene_deviceAdapter.MyHolder>{
    private final List<Equipment> list;

    public Scene_deviceAdapter(List<Equipment> list){
        this.list = list;
    }
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //参数3：判断条件 true  1.是打气 2.添加到parent
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_device_item,parent,false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(MyHolder holder, int postion) {
        Equipment scene_device = list.get(postion);
        int device_image = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType() == 1){//取暖器
                device_image = R.drawable.new_equipment_warmer;
            }else if(list.get(i).getType() == 2){//外置传感器
                device_image= R.drawable.equipment_external_sensor;
            }
        }
        final Equipment equipment = list.get(postion);
        holder.device_image.setImageResource(device_image);
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        public int device_id;
        public int type;
        public ImageView device_image;
        public String macAddress;
        public int controlled;
        public MyHolder(View itemView) {
            super(itemView);
            device_image = (ImageView) itemView.findViewById(R.id.scren_device);
            device_image.setClickable(false);
            device_image.setLongClickable(false);
            device_image.setEnabled(false);
            device_image.setOnClickListener(null);
            device_image.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return list==null ? 0 : list.size();
    }

}
