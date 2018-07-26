package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.SmartSet;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/10.
 */

public class SmartSetAdapter extends BaseAdapter {

    private Context context;


    public SmartSetAdapter(Context context) {
        this.context = context;

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public SmartSet getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        int[] imgs={R.mipmap.master, R.mipmap.controlled,R.mipmap.estsensor};
        String[] str={"主控机设置","受控机设置","外置传感器"};
        switch (position){
            case 0:
                convertView= View.inflate(context, R.layout.smartset,null);
                viewHolder=new ViewHolder(convertView);
                viewHolder.image_smart.setImageResource(imgs[0]);
                viewHolder.tv_smart.setText(str[0]);
                break;
            case 1:
                convertView= View.inflate(context, R.layout.smartset2,null);
                viewHolder=new ViewHolder(convertView);
                viewHolder.image_smart.setImageResource(imgs[1]);
                viewHolder.tv_smart.setText(str[1]);
                break;
            case 2:
                convertView=View.inflate(context,R.layout.layout_view,null);
                convertView.setMinimumHeight(50);
                break;
            case 3:
                convertView= View.inflate(context, R.layout.smartset3,null);
                ViewHolder viewHolder2=new ViewHolder(convertView);
                viewHolder2.tv_smart.setText(str[2]);
                viewHolder2.image_smart.setImageResource(imgs[2]);
                break;
        }
        return convertView;
    }
    class ViewHolder{
        @BindView(R.id.image_smart)
        ImageView image_smart;
        @BindView(R.id.tv_smart)
        TextView tv_smart;
        @BindView(R.id.image_right)
        ImageView image_right;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
    class ViewHolder2{
        @BindView(R.id.tv_smart)
        TextView tv_smart;
        @BindView(R.id.image_right)
        ImageView image_right;
        public ViewHolder2(View view){
            ButterKnife.bind(this,view);
        }
    }
}
