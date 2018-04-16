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
    private Long id;
    private int start;
    private int end;
    private int temp;//温度
    private  long deviceId;/**对应设备的Id*/
    private String week;/**一周的星期几*/

    public TaskTime(){}
    public TaskTime(int start, int end, int temp) {
        this.start = start;
        this.end = end;
        this.temp = temp;
    }

    public TaskTime(int start, int end, int temp, String week) {
        this.start = start;
        this.end = end;
        this.temp = temp;
        this.week = week;
    }
    public String getWeek() {
        return this.week;
    }
    public void setWeek(String week) {
        this.week = week;
    }
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
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 750696548)
    public TaskTime(Long id, int start, int end, int temp, long deviceId, String week) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.temp = temp;
        this.deviceId = deviceId;
        this.week = week;
    }
    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (!(obj instanceof TaskTime))
            return false;
        TaskTime taskTime= (TaskTime) obj;
        if (this.start==taskTime.start && this.end==taskTime.getEnd() && this.temp==taskTime.getTemp() && this.week.equals(taskTime.getWeek()))
            return true;
        else
            return false;
    }
    public long getDeviceId() {
        return this.deviceId;
    }
    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }
}
