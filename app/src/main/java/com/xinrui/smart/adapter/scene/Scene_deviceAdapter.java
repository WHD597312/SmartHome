package com.xinrui.smart.adapter.scene;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.scene.Equipment;
import com.xinrui.smart.util.mqtt.MQService;

import java.util.List;

/**
 * Created by win7 on 2018/4/12.
 */

public class Scene_deviceAdapter extends RecyclerView.Adapter<Scene_deviceAdapter.MyHolder>{
    private final List<Equipment> list;
    private Context mContext;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnRecyclerItemLongListener mOnItemLong = null;

    public Scene_deviceAdapter(List<Equipment> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
    }

    public Scene_deviceAdapter(List<Equipment> list){
        this.list = list;
    }
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //参数3：判断条件 true  1.是打气 2.添加到parent
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_device_item,parent,false);
        MyHolder holder = new MyHolder(view ,mOnItemClickListener,mOnItemLong);
        return holder;
    }


    @Override
    public void onBindViewHolder(MyHolder holder, int postion) {
                final Equipment equipment = list.get(postion);
        for (int i = 0; i < list.size(); i++) {
            int r = list.get(i).getType();
        }
                holder.device_image.setImageResource(equipment.getType());

    }
    public static class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        public int device_id;
        public int type;
        private ImageView device_image;
        public String macAddress;
        public int controlled;
        private OnRecyclerViewItemClickListener mOnItemClickListener = null;
        private OnRecyclerItemLongListener mOnItemLong = null;
        private MyHolder(View itemView,OnRecyclerViewItemClickListener mListener,OnRecyclerItemLongListener longListener) {
            super(itemView);
            this.mOnItemClickListener = mListener;
            this.mOnItemLong = longListener;
            device_image = (ImageView) itemView.findViewById(R.id.scren_device);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                mOnItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mOnItemLong != null){
                mOnItemLong.onItemLongClick(v,getPosition());
            }
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return list==null ? 0 : list.size();
    }



    MQService mqService;
    boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int data);

    }
    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
    public void setOnItemLongClickListener(OnRecyclerItemLongListener listener){
        this.mOnItemLong =  listener;
    }

}
