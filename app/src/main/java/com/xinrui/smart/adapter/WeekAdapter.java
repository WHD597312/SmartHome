package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeekAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;

    private int selectedPosition = 0;// 选中的位置
    public WeekAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
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
            convertView=View.inflate(context,R.layout.item_week,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        String week=getItem(position);
        if (!Utils.isEmpty(week)){
            viewHolder.tv_week.setText(week);
        }
        if (selectedPosition == position) {
            viewHolder.tv_week.setTextColor(context.getResources().getColor(R.color.color_black));
            viewHolder.tv_week.setBackgroundResource(R.drawable.button_normal);
        } else {
            viewHolder.tv_week.setTextColor(context.getResources().getColor(R.color.white));
            viewHolder.tv_week.setBackgroundColor(context.getResources().getColor(R.color.color_black3));
        }
        return convertView;
    }
    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }
    class ViewHolder{
        @BindView(R.id.tv_week) TextView tv_week;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
