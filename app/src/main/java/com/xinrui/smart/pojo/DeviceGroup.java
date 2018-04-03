package com.xinrui.smart.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DeviceGroup {
    @Id(autoincrement = false)
    private Long id;
    private String header;
    private String footer;
    private int color;
    private String houseName;
    private String location;
    private int masterControllerDeviceId;

    public DeviceGroup(){}

    @Generated(hash = 2145919706)
    public DeviceGroup(Long id, String header, String footer, int color,
            String houseName, String location, int masterControllerDeviceId) {
        this.id = id;
        this.header = header;
        this.footer = footer;
        this.color = color;
        this.houseName = houseName;
        this.location = location;
        this.masterControllerDeviceId = masterControllerDeviceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getMasterControllerDeviceId() {
        return masterControllerDeviceId;
    }

    public void setMasterControllerDeviceId(int masterControllerDeviceId) {
        this.masterControllerDeviceId = masterControllerDeviceId;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHouseName() {
        return this.houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }
}
