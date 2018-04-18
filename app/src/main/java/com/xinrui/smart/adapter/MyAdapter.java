package com.xinrui.smart.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.pojo.Equipment;
import com.xinrui.smart.util.MyViewHolder;
import com.xinrui.smart.util.OnItemClickListener;

import java.util.List;

/**
 * Created by win7 on 2018/4/16.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private LayoutInflater mInflater;
    private List<Equipment> mLists;
    public  OnItemLongClickListener mListener;
    public Context mContext;

    public MyAdapter(List<Equipment> mLists, OnItemLongClickListener mListener, Context mContext) {
        this.mLists = mLists;
        this.mListener = mListener;
        this.mContext = mContext;
    }

    public void setListener(OnItemLongClickListener listener){
        mListener = listener;
    }

    public MyAdapter(List<Equipment> lists) {
        this.mLists = lists;
    }
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.item_recycle,parent,false);
        MyViewHolder holder = new MyViewHolder(view,mListener);
        return holder;
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        Equipment equipment = mLists.get(position);
//        if(equipment.getType() == 1){
//            holder.imageView.setBackgroundResource(R.drawable.equipment_warmer);
//        }else if(equipment.getType() == 2){
//            holder.imageView.setBackgroundResource(R.drawable.equipment_external_sensor);
//        }
        holder.imageView.setBackgroundResource(equipment.getType());
    }

    public void addData(Equipment data,int position){
        mLists.add(data);
        notifyItemInserted(position);
    }

    public void remove(int postion){
        mLists.remove(postion);
        notifyItemRemoved(postion);
    }


    @Override
    public int getItemCount() {
        return mLists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{

        ImageView imageView;
        private OnItemLongClickListener onItemLongClickListener;

        public MyViewHolder(View itemView,OnItemLongClickListener listener) {
            super(itemView);
            onItemLongClickListener = listener;
            imageView = (ImageView) itemView.findViewById(R.id.device_image);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public boolean onLongClick(View v) {
            onItemLongClickListener.onItemLongClick(v,getPosition());
            return true;
        }
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(View v,int postion);
    }
    public void setOnItemClickListener(OnItemLongClickListener listener) {this.mListener = listener;}

}
