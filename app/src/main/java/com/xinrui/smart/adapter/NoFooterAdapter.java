package com.xinrui.smart.adapter;

import android.content.Context;

import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.xinrui.smart.pojo.GroupEntry;

import java.util.ArrayList;

/**
 * Created by win7 on 2018/3/12.
 */

public class NoFooterAdapter extends GroupedListAdapter {
    public NoFooterAdapter(Context context, ArrayList<GroupEntry> groups) {
        super(context, groups);
    }
    @Override
    public boolean hasFooter(int groupPosition) {
        return false;
    }

    @Override
    public int getFooterLayout(int viewType) {
        return 0;
    }

    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, int groupPosition) {

    }

}
