package com.xinrui.smart.pojo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinrui.smart.R;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by win7 on 2018/3/8.
 */

public class Room implements Serializable{
    private View view;
    private int roomId;
    private String roomName;
    private int startPoint;
    private JSONArray points;
    private int houseId;
    private JSONArray devices;
    private int layer;


    public Room(View view,int roomId, String roomName, int startPoint, JSONArray points, int houseId, JSONArray devices, int layer) {
        this.view = view;
        this.roomId = roomId;
        this.roomName = roomName;
        this.startPoint = startPoint;
        this.points = points;
        this.houseId = houseId;
        this.devices = devices;
        this.layer = layer;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public JSONArray getPoints() {
        return points;
    }

    public void setPoints(JSONArray points) {
        this.points = points;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public JSONArray getDevices() {
        return devices;
    }

    public void setDevices(JSONArray devices) {
        this.devices = devices;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}
