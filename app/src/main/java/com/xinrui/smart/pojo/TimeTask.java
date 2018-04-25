package com.xinrui.smart.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by win7 on 2018/3/24.
 */

@Entity
public class TimeTask {
    @Id(autoincrement = true)
    private Long id;
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
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 957618815)
    public TimeTask(Long id, long deviceId, int week, int start, int end, int temp) {
        this.id = id;
        this.deviceId = deviceId;
        this.week = week;
        this.start = start;
        this.end = end;
        this.temp = temp;
    }
    public TimeTask(long deviceId, int week, int start, int end, int temp){
        this.deviceId = deviceId;
        this.week = week;
        this.start = start;
        this.end = end;
        this.temp = temp;
    }
    @Generated(hash = 434847027)
    public TimeTask() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (!(obj instanceof TimeTask))
           return false;
        else {
            TimeTask timeTask= (TimeTask) obj;
            if (this.deviceId==timeTask.deviceId && this.week==timeTask.getWeek() && this.start==timeTask.start && this.end==timeTask.end)
                return true;
            else
                return false;
        }
    }
}
