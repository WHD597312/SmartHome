package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.TimeTask;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/26.
 */

public class TimeTaskAdapter extends BaseAdapter {
    private Context context;
    private List<TimeTask> list;
    private MyClickListener clickListener;
    public TimeTaskAdapter(Context context, List<TimeTask> list, MyClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener=clickListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public TimeTask getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView=View.inflate(context,R.layout.item_time_task,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        TimeTask timeTask =getItem(position);
        if (timeTask !=null){
            viewHolder.tv_open_time.setText(timeTask.getStart()+":00");
            viewHolder.tv_close_time.setText(timeTask.getEnd()+":00");
            viewHolder.tv_temp_num.setText(timeTask.getTemp()+"");
        }
        viewHolder.btn_delete.setOnClickListener(clickListener);
        viewHolder.btn_delete.setTag(position);
        return convertView;
    }
    class ViewHolder{
        @BindView(R.id.tv_open_time) TextView tv_open_time;
        @BindView(R.id.tv_close_time) TextView tv_close_time;
        @BindView(R.id.tv_temp_num) TextView tv_temp_num;
        @BindView(R.id.btn_delete) Button btn_delete;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
    public static abstract class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            myOnClick((Integer) v.getTag(),v);
        }
        public abstract void myOnClick(int position,View view);
    }
}
