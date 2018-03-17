package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.Function;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/8.
 */

public class FunctionAdapter extends BaseAdapter {
    private Context context;
    private List<Function> functions;

    public FunctionAdapter(Context context, List<Function> functions) {
        this.context = context;
        this.functions = functions;
    }

    @Override
    public int getCount() {
        return functions.size();
    }

    @Override
    public Function getItem(int position) {
        return functions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if(convertView==null){
            convertView= View.inflate(context, R.layout.function_item,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        Function function=getItem(position);
        if (function!=null){
            viewHolder.img_function.setImageResource(function.getImg_id());
            viewHolder.tv_desc.setText(function.getTv_desc());
        }
        return convertView;
    }
    class ViewHolder{
        @BindView(R.id.img_function)
        ImageView img_function;
        @BindView(R.id.tv_desc)
        TextView tv_desc;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
