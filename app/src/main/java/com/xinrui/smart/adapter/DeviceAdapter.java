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
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddDeviceActivity;
import com.xinrui.smart.activity.DeviceListActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.GroupEntry;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DeviceChildDialog;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.MyRecyclerViewItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by win7 on 2018/3/12.
 */

public class DeviceAdapter extends GroupedRecyclerViewAdapter{

    private Context context;
    private List<DeviceGroup> groups;
    private ImageView image_switch;
    ArrayList<DeviceChild> list;

    private DeviceChildDaoImpl deviceChildDao;
    private List<List<DeviceChild>> childern;
    TextView tv_device_child;

    int[] imgs={R.mipmap.image_unswitch, R.mipmap.image_switch};


    int []colors={R.color.color_white,R.color.color_orange};
    public DeviceAdapter(Context context, List<DeviceGroup> groups,List<List<DeviceChild>> childern) {
        super(context);
        this.context=context;
        this.groups = groups;
        this.childern=childern;
        deviceChildDao=new DeviceChildDaoImpl(context);
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
//        ArrayList<DeviceChild> childern=groups.get(groupPosition).getChildern();
//        return childern==null?0:childern.size();

        try {
            return childern.get(groupPosition).size();
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
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
        return R.layout.device_adapter_header;
    }

    /**
     * 组尾布局
     * @param viewType
     * @return
     */
    @Override
    public int getFooterLayout(int viewType) {
        return R.layout.device_adapter_footer;
    }

    /**
     * 某一组中子条目的布局
     * @param viewType
     * @return
     */
    @Override
    public int getChildLayout(int viewType) {
        return R.layout.device_adapter_child;
    }

    /**
     * 绑定组头数据
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindHeaderViewHolder(final BaseViewHolder holder, final int groupPosition) {
        DeviceGroup entry=groups.get(groupPosition);

        holder.itemView.setBackgroundResource(R.drawable.shape_header);
        holder.setText(R.id.tv_header,entry.getHeader());
        TextView tv_open=(TextView) holder.itemView.findViewById(R.id.tv_open);
        TextView tv_close= (TextView) holder.itemView.findViewById(R.id.tv_close);


        tv_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<DeviceChild> list=childern.get(groupPosition);
                    if (list!=null && list.size()>0){
                        for (int i=0;i<list.size();i++){
                            DeviceChild childEntry=list.get(i);
                            childEntry.setImg(imgs[1]);
                        }
                    }
                    holder.setTextColor(R.id.tv_close,context.getResources().getColor(colors[0]));
                    holder.setTextColor(R.id.tv_open,context.getResources().getColor(colors[1]));
                    changeChildren(groupPosition);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    List<DeviceChild> list=childern.get(groupPosition);
                    if (list!=null && list.size()>0){
                        for (int i=0;i<list.size();i++){
                            DeviceChild childEntry=list.get(i);
                            childEntry.setImg(imgs[0]);
                        }
                    }
                    holder.setTextColor(R.id.tv_close,context.getResources().getColor(colors[1]));
                    holder.setTextColor(R.id.tv_open,context.getResources().getColor(colors[0]));

                    changeChildren(groupPosition);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
//        tv_open.setTextColor(colors[0]);
    }

    /**
     * 绑定组尾数据
     * @param holder
     * @param groupPosition
     */
    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, final int groupPosition) {
        ImageView image_footer= (ImageView) holder.itemView.findViewById(R.id.image_footer);
        if (image_footer==null){
            return;
        }
        image_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, AddDeviceActivity.class);
                DeviceGroup deviceGroup=groups.get(groupPosition);
                intent.putExtra("houseId",deviceGroup.getId()+"");
                intent.putExtra("wifi","wifi");
                context.startActivity(intent);
            }
        });
    }

    /**
     * 绑定某一组中子条目的数据
     * @param holder
     * @param groupPosition
     * @param childPosition
     */
    /**
     * 绑定某一组中子条目的数据
     * @param holder
     * @param groupPosition
     * @param childPosition
     */

    @Override
    public void onBindChildViewHolder(final BaseViewHolder holder, final int groupPosition, final int childPosition) {
        final DeviceChild entry=childern.get(groupPosition).get(childPosition);
        holder.setText(R.id.tv_device_child,entry.getChild());
        holder.setImageResource(R.id.image_switch,entry.getImg());
        tv_device_child= (TextView) holder.itemView.findViewById(R.id.tv_device_child);

        tv_device_child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a=groupPosition;
                int b=childPosition;
                DeviceChild deviceChild =childern.get(groupPosition).get(childPosition);
                String content= deviceChild.getChild();
                Intent intent=new Intent(context, DeviceListActivity.class);
                intent.putExtra("content",content);
                context.startActivity(intent);
            }
        });

        image_switch= (ImageView) holder.itemView.findViewById(R.id.image_switch);
        image_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (entry.getImg()==imgs[0]){
                    entry.setImg(imgs[1]);
                    deviceChildDao.update(entry);
                }else if(entry.getImg()==imgs[1]){
                    entry.setImg(imgs[0]);
                    deviceChildDao.update(entry);
                }
//                holder.setImageResource(R.id.image_switch,img);
                changeChild(groupPosition,childPosition);
            }
        });
        Button btn_editor= (Button) holder.itemView.findViewById(R.id.btn_editor);
        btn_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialog(groupPosition,childPosition);
            }
        });
        Button btn_delete= (Button) holder.itemView.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceChildDao.delete(entry);
                childern.get(groupPosition).remove(childPosition);
                changeDataSet();
            }
        });

        MyRecyclerViewItem myRecyclerViewItem= (MyRecyclerViewItem) holder.itemView.findViewById(R.id.scroll_item);
        myRecyclerViewItem.reset();
    }
    private void buildDialog(final int groupPosition, final int childPosition){
        final DeviceChildDialog dialog=new  DeviceChildDialog(context);
        dialog.setOnPositiveClickListener(new DeviceChildDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String child=dialog.getName();
                if (!Utils.isEmpty(child)){
                    DeviceChild deviceChild=childern.get(groupPosition).get(childPosition);
                    deviceChild.setChild(child);
                    deviceChildDao.update(deviceChild);
                    changeChild(groupPosition,childPosition);
                }
                dialog.dismiss();
            }
        });
        dialog.setOnNegativeClickListener(new DeviceChildDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
