package com.xinrui.smart.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.Equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by win7 on 2018/3/29.
 */

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {
    //这个是checkbox的Hashmap集合
    private final HashMap<Integer,Boolean> map;
    private Context mContext;
    private List<Equipment> equipment_list;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_equipment_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Equipment equipment1 = equipment_list.get(position);
        holder.equipment_Name.setText(equipment1.getEquipment_Name());
        holder.equipment_image.setImageResource(equipment1.getEquipment_image());
        holder.CheckBox.setChecked(map.get(position));
        holder.CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.put(position,!map.get(position));
                //刷新适配器
                notifyDataSetChanged();
                //全选
//                All();
            }
        });

    }

    @Override
    public int getItemCount() {
        return equipment_list.size();
    }


    public EquipmentAdapter(List<Equipment> equipment) {
        equipment_list = new ArrayList<>();
        map = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            Equipment equipment1 = new Equipment("智能" + i, R.drawable.equipment, false);
            //添加30条数据
            equipment_list.add(equipment1);
            map.put(i, false);
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView equipment_image;
        TextView equipment_Name;
        CheckBox CheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            equipment_image = (ImageView) itemView.findViewById(R.id.equipment_image);
            equipment_Name = (TextView) itemView.findViewById(R.id.equipment_tv);
            CheckBox = (CheckBox) itemView.findViewById(R.id.equipment_ischeked);
        }
    }

    /**
     * 单选
     *
     * @param postion
     */
    public void singlesel(int postion) {
        Set<Map.Entry<Integer, Boolean>> entries = map.entrySet();
        for (Map.Entry<Integer, Boolean> entry : entries) {
            entry.setValue(false);
        }
        map.put(postion, true);
        notifyDataSetChanged();
    }
    /**
     * 全选
     */
    public void All() {
        Set<Map.Entry<Integer, Boolean>> entries = map.entrySet();        boolean shouldall = false;
        for (Map.Entry<Integer, Boolean> entry : entries) {
            Boolean value = entry.getValue();
            if (!value) {
                shouldall = true;
                break;
            }
        }
        for (Map.Entry<Integer, Boolean> entry : entries) {
            entry.setValue(shouldall);
        }
        notifyDataSetChanged();
    }

    /**
     * 反选
     */
    public void neverall() {
        Set<Map.Entry<Integer, Boolean>> entries = map.entrySet();
        for (Map.Entry<Integer, Boolean> entry : entries) {
            entry.setValue(!entry.getValue());
        }
        notifyDataSetChanged();
    }

}
