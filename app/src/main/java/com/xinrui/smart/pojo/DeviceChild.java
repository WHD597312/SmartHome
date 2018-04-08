package com.xinrui.smart.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by win7 on 2018/3/12.
 */

@Entity
public class DeviceChild {
    @Id(autoincrement = false)
    private Long id;
    private String child;
    private int img;
    private int direction;
    private Long groupId;
    private int masterControllerUserId;
    private int controlled;
    int type;
    int isUnlock;


    public DeviceChild() {
    }

    public DeviceChild(String child) {
        this.child = child;
    }

    public DeviceChild(String child, int img) {
        this.child = child;
        this.img = img;
    }

    public DeviceChild(Long id, String child, int img, int direction, Long groupId,
                       int masterControllerUserId, int type, int isUnlock) {
        this.id = id;
        this.child = child;
        this.img = img;
        this.direction = direction;
        this.groupId = groupId;
        this.masterControllerUserId = masterControllerUserId;
        this.type = type;
        this.isUnlock = isUnlock;
    }
    @Generated(hash = 1516157361)
    public DeviceChild(Long id, String child, int img, int direction, Long groupId,
            int masterControllerUserId, int controlled, int type, int isUnlock) {
        this.id = id;
        this.child = child;
        this.img = img;
        this.direction = direction;
        this.groupId = groupId;
        this.masterControllerUserId = masterControllerUserId;
        this.controlled = controlled;
        this.type = type;
        this.isUnlock = isUnlock;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public int getControlled() {
        return controlled;
    }

    public void setControlled(int controlled) {
        this.controlled = controlled;
    }
}
