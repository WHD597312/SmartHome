package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xinrui.smart.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/26.
 */

public class TimeAdapter extends BaseAdapter {
    private Context context;
    private List<Integer> list;

    int colors[]={R.color.color_black,R.color.color_dark_gray};
    private int selectedPosition = 0;// 选中的位置
    public TimeAdapter(Context context, List<Integer> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Integer getItem(int position) {
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
            convertView=View.inflate(context, R.layout.item_time,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        Integer integer=getItem(position);
        if (integer!=null){
            viewHolder.tv_time.setText(integer+":00");
        }
        if (selectedPosition == position) {
            viewHolder.tv_time.setTextColor(context.getResources().getColor(R.color.color_black));
        } else {
            viewHolder.tv_time.setTextColor(context.getResources().getColor(R.color.holo_gray_light));
        }
        return convertView;
    }
    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }
    class ViewHolder{
        @BindView(R.id.tv_time) TextView tv_time;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
