package com.xinrui.smart.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by win7 on 2018/3/24.
 */

@Entity
public class TaskTime {
    @Id(autoincrement = true)
    private long id;
    private  long deviceId;/**对应设备的Id*/
    private int week;/**一周的星期几*/
    private int start;
    private int end;
    private int temp;//温度
    public int getTemp() {
        return this.temp;
    }
    public void setTemp(int temp) {
        this.temp = temp;
    }
    public int getEnd() {
        return this.end;
    }
    public void setEnd(int end) {
        this.end = end;
    }
    public int getStart() {
        return this.start;
    }
    public void setStart(int start) {
        this.start = start;
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
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    @Generated(hash = 446432775)
    public TaskTime(long id, long deviceId, int week, int start, int end, int temp) {
        this.id = id;
        this.deviceId = deviceId;
        this.week = week;
        this.start = start;
        this.end = end;
        this.temp = temp;
    }
    public TaskTime(long deviceId, int week, int start, int end, int temp){
        this.deviceId = deviceId;
        this.week = week;
        this.start = start;
        this.end = end;
        this.temp = temp;
    }
    @Generated(hash = 1763742976)
    public TaskTime() {
    }



    
}
