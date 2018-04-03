package com.xinrui.smart.pojo;

import java.util.ArrayList;

/**
 * 可展开收起的组数据的实体类 它比GroupEntity只是多了一个boolean类型的isExpand，用来表示展开和收起的状态。
 */
public class ExpandableGroupEntity {

    private String header;
    private String footer;
    private ArrayList<DeviceChild> children;
    private boolean isExpand;

    public ExpandableGroupEntity(String header, String footer, boolean isExpand,
                                 ArrayList<DeviceChild> children) {
        this.header = header;
        this.footer = footer;
        this.isExpand = isExpand;
        this.children = children;
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

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public ArrayList<DeviceChild> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<DeviceChild> children) {
        this.children = children;
    }
}
