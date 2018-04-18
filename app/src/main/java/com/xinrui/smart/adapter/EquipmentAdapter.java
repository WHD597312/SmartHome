package com.xinrui.smart.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddEquipmentActivity;
import com.xinrui.smart.pojo.Equipment;
import com.xinrui.smart.util.GetUrl;
import com.xinrui.smart.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by win7 on 2018/3/29.
 */

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {
    private List<Equipment> equipment_list;
    private Context mContext;
    private CheckItemListener mCheckListener;

    public EquipmentAdapter(Context mContext,List<Equipment> equipment,CheckItemListener mCheckListener) {
        this.mContext = mContext;
        this.equipment_list = equipment;
        this.mCheckListener = mCheckListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_equipment_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //根据设备type显示不同的图片
        int device_image = 0;
//        for (int i = 0; i < equipment_list.size(); i++) {
//            if (equipment_list.get(i).getType() == 1) {//取暖器
//                device_image = R.drawable.equipment_warmer;
//                break;
//            } else if (equipment_list.get(i).getType() == 2) {//外置传感器
//                device_image = R.drawable.equipment_external_sensor;
//                break;
//            }
//        }
            final Equipment equipment = equipment_list.get(position);
            holder.equipment_Name.setText(equipment.getDeviceName());
            holder.equipment_image.setImageResource(equipment.getType());
            holder.CheckBox.setChecked(equipment.isChecked());
            holder.CheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击某个CheckBox就将当前设备的isChecked属性设为相反
                    equipment.setChecked(!equipment.isChecked());
                    //设置点击的CheckBox状态为当前设备的isChecked属性所显示的状态
                    holder.CheckBox.setChecked(equipment.isChecked());
                    if (null != mCheckListener) {
                        mCheckListener.itemChecked(equipment, holder.CheckBox.isChecked());
                    }
                    //刷新适配器
                    notifyDataSetChanged();
                }
            });

    }

    @Override
    public int getItemCount() {
        return equipment_list.size();
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
    public interface CheckItemListener {

        void itemChecked(Equipment equipment, boolean isChecked);
    }
}
