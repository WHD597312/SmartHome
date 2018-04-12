package com.xinrui.smart.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.RoomType;
import com.xinrui.smart.util.ChoiceItemLayout;
import com.xinrui.smart.util.OnItemClickListener;

import java.util.List;

/**
 * Created by win7 on 2018/3/29.
 */

public class RoomtypeAdapter extends RecyclerView.Adapter<RoomtypeAdapter.ViewHolder> {
    private List<RoomType> mRoomTypelist;

    public RoomtypeAdapter(List<RoomType> mRoomTypelist) {
        this.mRoomTypelist = mRoomTypelist;

    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView roomImage;
        TextView roomName;

        public ViewHolder(View itemView) {
            super(itemView);
            roomImage = (ImageView) itemView.findViewById(R.id.roomImage);
            roomName = (TextView) itemView.findViewById(R.id.roomName);
        }
    }

    /**
     * 加载item 的布局  创建ViewHolder实例
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.roomtype_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * 对RecyclerView子项数据进行赋值
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        RoomType roomType = mRoomTypelist.get(position);
        holder.roomImage.setImageResource(roomType.getRoomImageview());
        holder.roomName.setText(roomType.getRoomName());
        ViewHolder viewHolde = holder;

        ChoiceItemLayout layout = (ChoiceItemLayout) viewHolde.itemView;
        layout.setChecked(roomType.isFlag());

        if(onItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v,position);
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    private OnItemClickListener onItemClickListener;
    /*
    返回子项个数
     */
    @Override
    public int getItemCount() {
        return mRoomTypelist.size();
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
