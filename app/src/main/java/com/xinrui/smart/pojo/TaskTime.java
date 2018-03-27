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
    private String week;/**一周的星期几*/

    public TaskTime(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Generated(hash = 886541981)
    public TaskTime(Long id, int start, int end, int temp, String week) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.temp = temp;
        this.week = week;
    }

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

    @Generated(hash = 1763742976)
    public TaskTime() {
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }
}
