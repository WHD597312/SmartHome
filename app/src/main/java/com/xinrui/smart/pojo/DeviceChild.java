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

    private String deviceName;
    private String macAddress;
    private int img;
    private int direction;
    private Long houseId;
    private int masterControllerUserId;
    private int controlled;
    private int type;
    private int isUnlock;
    private int version;


    public DeviceChild() {
    }

    public DeviceChild(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceChild(String deviceName, int img) {
        this.deviceName = deviceName;
        this.img = img;
    }

    public DeviceChild(Long id, String deviceName, int img, int direction, Long houseId,
                       int masterControllerUserId, int type, int isUnlock) {
        this.id = id;
        this.deviceName = deviceName;
        this.img = img;
        this.direction = direction;
        this.houseId = houseId;
        this.masterControllerUserId = masterControllerUserId;
        this.type = type;
        this.isUnlock = isUnlock;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getIsUnlock() {
        return this.isUnlock;
    }

    public void setIsUnlock(int isUnlock) {
        this.isUnlock = isUnlock;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getControlled() {
        return this.controlled;
    }

    public void setControlled(int controlled) {
        this.controlled = controlled;
    }

    public int getMasterControllerUserId() {
        return this.masterControllerUserId;
    }

    public void setMasterControllerUserId(int masterControllerUserId) {
        this.masterControllerUserId = masterControllerUserId;
    }

    public Long getHouseId() {
        return this.houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public int getDirection() {
        return this.direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getImg() {
        return this.img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Generated(hash = 426386247)
    public DeviceChild(Long id, String deviceName, String macAddress, int img, int direction,
            Long houseId, int masterControllerUserId, int controlled, int type, int isUnlock,
            int version) {
        this.id = id;
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.img = img;
        this.direction = direction;
        this.houseId = houseId;
        this.masterControllerUserId = masterControllerUserId;
        this.controlled = controlled;
        this.type = type;
        this.isUnlock = isUnlock;
        this.version = version;
    }

   


}
