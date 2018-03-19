package com.xinrui.smart.adapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.xinrui.smart.R;
import com.xinrui.smart.pojo.Room;

import java.util.List;


/**
 * Created by win7 on 2018/3/10.
 */

public class RoomAdapter extends MyBaseAdapter {


    private LayoutInflater layoutInflater;

    public RoomAdapter(LayoutInflater layoutInflater) {
        super();
        this.layoutInflater = layoutInflater;
    }

    public RoomAdapter(List<Room> list) {
        super(list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolde viewHolde;
        View view;
        if(convertView == null){
            viewHolde = new ViewHolde();
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_type,null);
            viewHolde.roomImage = (ImageView) view.findViewById(R.id.room_image);
            viewHolde.roomName = (TextView) view.findViewById(R.id.room_name);

            view.setTag(viewHolde);
        }else{
            view = convertView;
            viewHolde = (ViewHolde) convertView.getTag();
        }
        viewHolde.getRoomImage().setImageResource(list.get(position).getRoom_image());
        viewHolde.getRoomName().setText(list.get(position).getRoom_name());
        return view;
    }

    class ViewHolde {
        private ImageView roomImage;
        private TextView roomName;

        public ViewHolde() {
        }

        public ViewHolde(ImageView roomImage, TextView roomName) {
            this.roomImage = roomImage;
            this.roomName = roomName;
        }

        public ImageView getRoomImage() {
            return roomImage;
        }

        public TextView getRoomName() {
            return roomName;
        }

        public void setRoomImage(ImageView roomImage) {
            this.roomImage = roomImage;
        }

        public void setRoomName(TextView roomName) {
            this.roomName = roomName;
        }
    }


}
