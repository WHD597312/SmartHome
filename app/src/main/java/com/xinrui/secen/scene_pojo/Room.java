package com.xinrui.secen.scene_pojo;

import android.view.View;

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
    private int x;
    private int y;
    private int width;
    private int height;
    private int extTemp;
    private int extHut;
    private boolean isSelected;


    public int getExtTemp() {
        return extTemp;
    }

    public void setExtTemp(int extTemp) {
        this.extTemp = extTemp;
    }

    public int getExtHut() {
        return extHut;
    }

    public void setExtHut(int extHut) {
        this.extHut = extHut;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Room(int roomId, String roomName, int startPoint, JSONArray points, int houseId, JSONArray devices, int layer, int x, int y, int width, int height, boolean isSelected) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.startPoint = startPoint;
        this.points = points;
        this.houseId = houseId;
        this.devices = devices;
        this.layer = layer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isSelected = isSelected;
    }

    public Room (View view, int roomId, String roomName, int startPoint, JSONArray points, int houseId, JSONArray devices, int layer, int x, int y, int width, int height,boolean isSelected) {
        this.view = view;
        this.roomId = roomId;
        this.roomName = roomName;
        this.startPoint = startPoint;
        this.points = points;
        this.houseId = houseId;
        this.devices = devices;
        this.layer = layer;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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
