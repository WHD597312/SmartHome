package com.xinrui.smart.pojo;

import android.media.Image;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by win7 on 2018/3/29.
 */

public class RoomType {
    private int roomImageview;
    private String roomName;

    public RoomType(int roomImageview, String roomName) {
        this.roomImageview = roomImageview;
        this.roomName = roomName;
    }

    public RoomType() {

    }

    public int getRoomImageview() {
        return roomImageview;
    }

    public void setRoomImageview(int roomImageview) {
        this.roomImageview = roomImageview;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
