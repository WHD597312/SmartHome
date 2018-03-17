package com.xinrui.smart.pojo;

/**
 * Created by win7 on 2018/3/8.
 */

public class Function {
    private int img_id;
    private String tv_desc;

    public Function(int img_id, String tv_desc) {
        this.img_id = img_id;
        this.tv_desc = tv_desc;
    }

    public int getImg_id() {
        return img_id;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }

    public String getTv_desc() {
        return tv_desc;
    }

    public void setTv_desc(String tv_desc) {
        this.tv_desc = tv_desc;
    }
}
