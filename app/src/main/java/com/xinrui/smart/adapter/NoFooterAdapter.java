package com.xinrui.smart.adapter;

import android.content.Context;

import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.GroupEntry;

import java.util.ArrayList;

/**
 * Created by win7 on 2018/3/12.
 */

public class NoFooterAdapter extends GroupedListAdapter {
    public NoFooterAdapter(Context context, ArrayList<GroupEntry> groups) {
        super(context, groups);
    }


}
