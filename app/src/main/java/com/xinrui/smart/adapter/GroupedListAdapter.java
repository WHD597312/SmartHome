package com.xinrui.smart.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.DeviceListActivity;
import com.xinrui.smart.pojo.ChildEntry;
import com.xinrui.smart.pojo.GroupEntry;

import java.util.ArrayList;

/**
 * Created by win7 on 2018/3/12.
 */

public class GroupedListAdapter extends GroupedRecyclerViewAdapter{
    private Context context;
    private ArrayList<GroupEntry> groups;
    private ImageView image_switch;
    private RecyclerView mRecycler;

    int groupPosition;
    int childPosition;
    TextView tv_device_child;
    ChildEntry entry;
    int[] imgs={R.drawable.switch_close, R.drawable.switch_open};

    public GroupedListAdapter(Context context, ArrayList<GroupEntry> groups) {
        super(context);
        this.context=context;
        this.groups = groups;
    }

    /**
     * 返回组的项目数
     * @return
     */
    @Override
    public int getGroupCount() {
        return groups==null?0:groups.size();
    }

    /**
     * 返回某一组子条目的数目
     * @param groupPosition
     * @return
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<ChildEntry> childern=groups.get(groupPosition).getChildern();
        return childern==null?0:childern.size();
    }

    /**
     * 是否有组头
     * @param groupPosition
     * @return
     */
    @Override
    public boolean hasHeader(int groupPosition) {
        return true;
    }

    /**
     * 是否有组尾
     * @param groupPosition
     * @return
     */
    @Override
    public boolean hasFooter(int groupPosition) {
        return true;
    }

    /**
     * 组头布局
     * @param viewType
     * @return
     */
    @Override
    public int getHeaderLayout(int viewType) {
        return R.layout.adapter_header;
    }

    /**
     * 组尾布局
     * @param viewType
     * @return
     */
    @Override
    public int getFooterLayout(int viewType) {
        return R.layout.adapter_footer;
    }

    /**
     * 某一组中子条目的布局
     * @param viewType
     * @return
     */
    @Override
    public int getChildLayout(int viewType) {
        return R.layout.adapter_child;
    }

    /**
     * 绑定组头数据
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindHeaderViewHolder(BaseViewHolder holder, int groupPosition) {
        GroupEntry entry=groups.get(groupPosition);
        holder.setText(R.id.tv_header,entry.getHeader());
    }

    /**
     * 绑定组尾数据
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, int groupPosition) {
        GroupEntry entry=groups.get(groupPosition);
        holder.setText(R.id.tv_footer,entry.getFooter());
    }

    /**
     * 绑定某一组中子条目的数据
     * @param holder
     * @param groupPosition
     * @param childPosition
     */
    @Override
    public void onBindChildViewHolder(final BaseViewHolder holder, final int groupPosition, final int childPosition) {
        final ChildEntry entry=groups.get(groupPosition).getChildern().get(childPosition);
        holder.setText(R.id.tv_device_child,entry.getChild());
        holder.setImageResource(R.id.image_switch,entry.getImg());
        tv_device_child= (TextView) holder.itemView.findViewById(R.id.tv_device_child);
        tv_device_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_device_child!=null){
                    String content=tv_device_child.getText().toString();
                    Intent intent=new Intent(context, DeviceListActivity.class);
                    intent.putExtra("content",content);
                    context.startActivity(intent);
                }
            }
        });
        image_switch= (ImageView) holder.itemView.findViewById(R.id.image_switch);
        image_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int img=imgs[1];
                imgs[1]=imgs[0];
                imgs[0]=img;
                entry.setImg(img);
                holder.setImageResource(R.id.image_switch,entry.getImg());
                changeDataSet();
            }
        });
        Button btn_editor= (Button) holder.itemView.findViewById(R.id.btn_editor);
        btn_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groups!=null && groups.get(groupPosition).getChildern()!=null){
                    groups.get(groupPosition).getChildern().remove(childPosition);
                    changeDataSet();
                }
            }
        });
    }

}
