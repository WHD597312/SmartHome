package com.xinrui.smart.pojo;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * Created by win7 on 2018/3/29.
 */

public class Equipment implements Serializable{
    private int id;
    private String deviceName;
    private int type;
    private int houseId;
    private int masterControllerUserId;
    private int isUnlock;
    public String macAddress;
    public int controlled;
    private boolean isChecked;

    public Equipment(int type) {
        this.type = type;
    }

    public Equipment(int id, String deviceName, int type, String macAddress, int controlled) {
        this.id = id;
        this.deviceName = deviceName;
        this.type = type;
        this.macAddress = macAddress;
        this.controlled = controlled;
    }

    public Equipment(int id, String deviceName, int type, int houseId, int masterControllerUserId, int isUnlock, boolean isChecked) {
        this.id = id;
        this.deviceName = deviceName;
        this.type = type;
        this.houseId = houseId;
        this.masterControllerUserId = masterControllerUserId;
        this.isUnlock = isUnlock;
        this.isChecked = isChecked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public int getMasterControllerUserId() {
        return masterControllerUserId;
    }

    public void setMasterControllerUserId(int masterControllerUserId) {
        this.masterControllerUserId = masterControllerUserId;
    }

    public int getIsUnlock() {
        return isUnlock;
    }

    public void setIsUnlock(int isUnlock) {
        this.isUnlock = isUnlock;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
