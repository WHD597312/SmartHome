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
    private int ratedPower;//额定功率
    private String MatTemp;/**手动/定时模式下的温度*/
    private String workMode;/**manual:手动模式	timer:定时模式*/
    private String LockScreen;/** open:上锁  close:解锁*/
    private String BackGroundLED;/**open:照明  close:节能*/
    private String deviceState;/**open:开机  close：关机*/
    private String tempState;/**nor:正常 err:异常*/
    private String outputMod;
    private int curTemp;
    private String protectEnable;
    private String ctrlMode;
    private int powerValue;
    private int voltageValue;
    private int currentValue;
    private String machineFall;
    private int protectSetTemp;
    private int protectProTemp;


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

    public int getRatedPower() {
        return this.ratedPower;
    }

    public void setRatedPower(int ratedPower) {
        this.ratedPower = ratedPower;
    }

    public int getProtectProTemp() {
        return this.protectProTemp;
    }

    public void setProtectProTemp(int protectProTemp) {
        this.protectProTemp = protectProTemp;
    }

    public int getProtectSetTemp() {
        return this.protectSetTemp;
    }

    public void setProtectSetTemp(int protectSetTemp) {
        this.protectSetTemp = protectSetTemp;
    }

    public String getMachineFall() {
        return this.machineFall;
    }

    public void setMachineFall(String machineFall) {
        this.machineFall = machineFall;
    }

    public int getCurrentValue() {
        return this.currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public int getVoltageValue() {
        return this.voltageValue;
    }

    public void setVoltageValue(int voltageValue) {
        this.voltageValue = voltageValue;
    }

    public int getPowerValue() {
        return this.powerValue;
    }

    public void setPowerValue(int powerValue) {
        this.powerValue = powerValue;
    }

    public String getCtrlMode() {
        return this.ctrlMode;
    }

    public void setCtrlMode(String ctrlMode) {
        this.ctrlMode = ctrlMode;
    }

    public String getProtectEnable() {
        return this.protectEnable;
    }

    public void setProtectEnable(String protectEnable) {
        this.protectEnable = protectEnable;
    }

    public int getCurTemp() {
        return this.curTemp;
    }

    public void setCurTemp(int curTemp) {
        this.curTemp = curTemp;
    }

    public String getOutputMod() {
        return this.outputMod;
    }

    public void setOutputMod(String outputMod) {
        this.outputMod = outputMod;
    }

    public String getTempState() {
        return this.tempState;
    }

    public void setTempState(String tempState) {
        this.tempState = tempState;
    }

    public String getDeviceState() {
        return this.deviceState;
    }

    public void setDeviceState(String deviceState) {
        this.deviceState = deviceState;
    }

    public String getBackGroundLED() {
        return this.BackGroundLED;
    }

    public void setBackGroundLED(String BackGroundLED) {
        this.BackGroundLED = BackGroundLED;
    }

    public String getLockScreen() {
        return this.LockScreen;
    }

    public void setLockScreen(String LockScreen) {
        this.LockScreen = LockScreen;
    }

    public String getWorkMode() {
        return this.workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getMatTemp() {
        return this.MatTemp;
    }

    public void setMatTemp(String MatTemp) {
        this.MatTemp = MatTemp;
    }

    @Generated(hash = 1714108533)
    public DeviceChild(Long id, String deviceName, String macAddress, int img, int direction,
            Long houseId, int masterControllerUserId, int controlled, int type, int isUnlock,
            int version, int ratedPower, String MatTemp, String workMode, String LockScreen,
            String BackGroundLED, String deviceState, String tempState, String outputMod,
            int curTemp, String protectEnable, String ctrlMode, int powerValue,
            int voltageValue, int currentValue, String machineFall, int protectSetTemp,
            int protectProTemp) {
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
        this.ratedPower = ratedPower;
        this.MatTemp = MatTemp;
        this.workMode = workMode;
        this.LockScreen = LockScreen;
        this.BackGroundLED = BackGroundLED;
        this.deviceState = deviceState;
        this.tempState = tempState;
        this.outputMod = outputMod;
        this.curTemp = curTemp;
        this.protectEnable = protectEnable;
        this.ctrlMode = ctrlMode;
        this.powerValue = powerValue;
        this.voltageValue = voltageValue;
        this.currentValue = currentValue;
        this.machineFall = machineFall;
        this.protectSetTemp = protectSetTemp;
        this.protectProTemp = protectProTemp;
    }
}
