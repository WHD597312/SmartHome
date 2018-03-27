package com.xinrui.smart.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.xinrui.smart.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/23.
 */

/**
 * 取暖器底部6个按钮,采用网络视图进行布局
 */
public class DeviceListAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;

    int colors[]={R.color.color_black,R.color.color_orange};
    private int selectedPosition = 0;// 选中的位置
    public DeviceListAdapter(Context context, List<String> list) {
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
            convertView=View.inflate(context,R.layout.item_list,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
        String title=getItem(position);
        if (title!=null && title!=""){
            viewHolder.btn_list.setText(title);
        }
        if (selectedPosition == position) {
            viewHolder.btn_list.setTextColor(context.getResources().getColor(R.color.color_orange));
        } else {
            viewHolder.btn_list.setTextColor(context.getResources().getColor(R.color.black));
        }
        return convertView;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }
    class ViewHolder{
        @BindView(R.id.btn_list) Button btn_list;
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
        }
    }
}
