package com.xinrui.smart.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Timer {
    @Id
    private Long id;
    private long deviceId;
    private int week;
    private  int temp ;
    private String open;
    private int  hour ;
    public int getHour() {
        return this.hour;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public String getOpen() {
        return this.open;
    }
    public void setOpen(String open) {
        this.open = open;
    }
    public int getTemp() {
        return this.temp;
    }
    public void setTemp(int temp) {
        this.temp = temp;
    }
    public int getWeek() {
        return this.week;
    }
    public void setWeek(int week) {
        this.week = week;
    }
    public long getDeviceId() {
        return this.deviceId;
    }
    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 1959398489)
    public Timer(Long id, long deviceId, int week, int temp, String open, int hour) {
        
        this.id = id;
        this.deviceId = deviceId;
        this.week = week;
        this.temp = temp;
        this.open = open;
        this.hour = hour;
    }
    @Generated(hash = 872717714)
    public Timer() {
    }
}
