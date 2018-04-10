package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.Controlled;
import com.xinrui.smart.pojo.DeviceChild;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/14.
 */

public class ControlledAdapter extends BaseAdapter {
    private Context context;
    private List<DeviceChild> list;



    public ControlledAdapter(Context context, List<DeviceChild> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public DeviceChild getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       ViewHolder viewHolder=null;
        if (convertView==null){
            convertView= View.inflate(context, R.layout.item_main_control,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        viewHolder.img_main.setImageResource(R.mipmap.controlled);
        final DeviceChild controlled=getItem(position);
        CheckBox check=viewHolder.check;
        if (controlled!=null){
            viewHolder.tv_main.setText(controlled.getDeviceName());
            if (controlled.getControlled()==1){
                check.setChecked(true);
            }
        }
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    selected.add(controlled);
                }else {
                    selected.remove(controlled);
                }
            }
        });
        return convertView;
    }

    List<DeviceChild> selected=new ArrayList<>();


    public List<DeviceChild> getSelected() {
        return selected;
    }

    class ViewHolder{
        @BindView(R.id.img_main)
        ImageView img_main;
        @BindView(R.id.tv_main)
        TextView tv_main;
        @BindView(R.id.check)
        CheckBox check;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
