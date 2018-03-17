package com.xinrui.smart.pojo;

/**
 * Created by win7 on 2018/3/12.
 */

/**
 * 主控机设置
 */
public class MainControl {
    private String name;
    private int img;
    boolean isChecked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
