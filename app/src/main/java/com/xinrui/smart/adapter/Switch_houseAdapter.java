package com.xinrui.smart.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinrui.smart.R;
import com.xinrui.smart.util.MyViewHolder;
import com.xinrui.smart.util.OnItemClickListener;

import java.util.List;

/**
 * Created by win7 on 2018/4/10.
 */

public class Switch_houseAdapter extends RecyclerView.Adapter<MyViewHolder> {
private Context mContext;
private List<String> mList;
private LayoutInflater mInflater;
private OnItemClickListener mListener;

    public Switch_houseAdapter(Context mContext, OnItemClickListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.house_name, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.houseName.setText(mList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
         return mList != null ? mList.size() : 0;
    }
    public void refreshDatas(List<String> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }
}
