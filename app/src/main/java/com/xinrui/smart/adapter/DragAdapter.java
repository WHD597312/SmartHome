package com.xinrui.smart.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.xinrui.smart.R;
import com.xinrui.smart.adapter.scene.DragGridBaseAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by win7 on 2018/3/9.
 */

public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter {
    private List<HashMap<String,Object>> list;
    private LayoutInflater mInflater;
    private int mHindePosition = -1;




    public DragAdapter(Context context, List<HashMap<String, Object>> list) {
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     *由于复用convertView导致某些item消失了，所以这里不服用item
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.addroom_item,null);
        ImageView mImageView = (ImageView) convertView.findViewById(R.id.item_image);
        TextView mTextView = (TextView) convertView.findViewById(R.id.item_text);

        mImageView.setImageResource((Integer) list.get(position).get("item_image"));
        mTextView.setText((CharSequence) list.get(position).get("item_text"));

        if(position == mHindePosition){
            convertView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    @Override
    public void reorderItems(int oldPosition, int newPosition) {
        HashMap<String, Object> temp = list.get(oldPosition);
        if(oldPosition < newPosition){
            for(int i=oldPosition; i<newPosition; i++){
                Collections.swap(list, i, i+1);
            }
        }else if(oldPosition > newPosition){
            for(int i=oldPosition; i>newPosition; i--){
                Collections.swap(list, i, i-1);
            }
        }

        list.set(newPosition, temp);
    }

    @Override
    public void setHideItem(int hidePosition) {
        this.mHindePosition = hidePosition;
        notifyDataSetChanged();
    }
}
