package com.xinrui.secen.scene_util;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;

/**
 * Created by mythmayor on 2017/6/2.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView houseName;
    public ImageView imageView;

    public MyViewHolder(View itemView) {
        super(itemView);
        houseName = (TextView) itemView.findViewById(R.id.houseName);
        imageView = (ImageView) itemView.findViewById(R.id.image);
    }
}
