package com.xinrui.smart.pojo;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by win7 on 2018/4/3.
 */

public class MergeRoom implements Serializable{
    private static final long serialVersionUID = 2421263553592651152L;

    private List<Integer> postions;
    private int drawable;

    public MergeRoom(List<Integer> postions, int drawable) {
        this.postions = postions;
        this.drawable = drawable;
    }

    public List<Integer> getPostions() {
        return postions;
    }

    public void setPostions(List<Integer> postions) {
        this.postions = postions;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }
}
