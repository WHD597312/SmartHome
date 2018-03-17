package com.xinrui.smart.pojo;

import java.util.ArrayList;

/**
 * Created by win7 on 2018/3/12.
 */

public class GroupEntry {
    private String header;
    private String footer;
    private ArrayList<ChildEntry> childern;

    public GroupEntry() {
    }

    public GroupEntry(String header, String footer, ArrayList<ChildEntry> childern) {
        this.header = header;
        this.footer = footer;
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

    public ArrayList<ChildEntry> getChildern() {
        return childern;
    }

    public void setChildern(ArrayList<ChildEntry> childern) {
        this.childern = childern;
    }
}
