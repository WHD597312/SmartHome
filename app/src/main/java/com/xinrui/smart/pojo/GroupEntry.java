package com.xinrui.smart.pojo;

import java.util.ArrayList;

/**
 * Created by win7 on 2018/3/12.
 */
public class GroupEntry {
    private Long id;
    private String header;
    private String footer;
    private int color;
    private ArrayList<DeviceChild> childern;

    public GroupEntry() {
    }

    public GroupEntry(String header, int color, ArrayList<DeviceChild> childern) {
        this.header = header;
        this.color = color;
        this.childern = childern;
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

    public ArrayList<DeviceChild> getChildern() {
        return childern;
    }

    public void setChildern(ArrayList<DeviceChild> childern) {
        this.childern = childern;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
