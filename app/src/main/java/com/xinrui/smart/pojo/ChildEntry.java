package com.xinrui.smart.pojo;

/**
 * Created by win7 on 2018/3/12.
 */

public class ChildEntry {
    private String child;
    private int img;
    private int direction;

    public ChildEntry() {
    }

    public ChildEntry(String child) {
        this.child = child;
    }

    public ChildEntry(String child, int img) {
        this.child = child;
        this.img = img;
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
}
