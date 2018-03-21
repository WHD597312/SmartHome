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
import com.xinrui.smart.pojo.ETSControl;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by win7 on 2018/3/12.
 */

public class ETSControlAdapter extends BaseAdapter {
    private Context context;
    private List<ETSControl> list;
    public static boolean checked=false;
    public  int checkedCount=0;
    public ETSControlAdapter(Context context, List<ETSControl> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list==null?0:list.size();
    }

    @Override
    public ETSControl getItem(int position) {
        return list==null?null:list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public HashMap<Integer, Boolean> states = new HashMap<Integer, Boolean>();  //在这里要做判断保证只有一个RadioButton被选中
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if (convertView==null){
            convertView= View.inflate(context, R.layout.item_main_control,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        ETSControl control=getItem(position);
        if (control!=null){
            viewHolder.tv_main.setText(control.getName());
            CheckBox box=viewHolder.check;

            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i=0;i<getCount();i++){
                        states.put(i, false);
                    }
                    states.put(position, true);    //这样所有的条目中只有一个被选中！
                    notifyDataSetChanged();//刷新适配器
                }
            });
            //上面是点击后设置状态，但是也是需要设置显示样式,通过判断状态设置显示的样式
            if (states.get((Integer) position) == null || states.get((Integer) position) == false) {  //true说明没有被选中
                viewHolder.check.setChecked(false);
            } else {
                viewHolder.check.setChecked(true);
            }
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
