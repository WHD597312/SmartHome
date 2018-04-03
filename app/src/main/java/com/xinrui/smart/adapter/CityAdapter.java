package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.util.Utils;

import java.util.List;
import java.util.Properties;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CityAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;

    public CityAdapter(Context context, List<String> list) {
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
            convertView=View.inflate(context,R.layout.item_city,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        String s=getItem(position);
        if (position==0){
            viewHolder.tv_city.setGravity(Gravity.CENTER);
        }else {
            viewHolder.tv_city.setGravity(Gravity.CENTER_VERTICAL);
        }
        if (!Utils.isEmpty(s)){
            viewHolder.tv_city.setText(s);
        }
        return convertView;
    }
    class  ViewHolder{
        @BindView(R.id.tv_city) TextView tv_city;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
