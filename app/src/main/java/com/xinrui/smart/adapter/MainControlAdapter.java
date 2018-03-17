package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.MainControl;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/12.
 */

public class MainControlAdapter extends BaseAdapter {
    private int count=0;
    private Context context;
    private List<MainControl> list;
    private  boolean checked=false;
    private  int checkedCount=0;
    public MainControlAdapter(Context context, List<MainControl> list) {
        this.context = context;
        this.list = list;
        count++;
    }

    @Override
    public int getCount() {
        return list==null?0:list.size();
    }

    @Override
    public MainControl getItem(int position) {
        return list==null?null:list.get(position);
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
        MainControl control=getItem(position);
        if (control!=null){
            viewHolder.tv_main.setText(control.getName());
            CheckBox box=viewHolder.check;

            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checked=isChecked;
                    if (checked){
                        checkedCount++;
                    }else {
                        checkedCount--;
                    }
                }
            });
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checked&&checkedCount>1){
                        Toast.makeText(context,"只能选择一个主控制", Toast.LENGTH_LONG).show();
                        ((CheckBox)v).setBackgroundResource(R.drawable.shape_normal_round);
                        return;
                    }
                }
            });
        }
        return convertView;
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
