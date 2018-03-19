package com.xinrui.smart.pojo;

/**
 * Created by win7 on 2018/3/8.
 */

public class Room {
    private int room_image;
    private String room_name;

    public Room() {
    }

    public Room(int room_image, String room_name) {
        this.room_image = room_image;
        this.room_name = room_name;
    }


    public int getRoom_image() {
        return room_image;
    }

    public void setRoom_image(int room_image) {
        this.room_image = room_image;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }
}
