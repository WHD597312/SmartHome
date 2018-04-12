package com.xinrui.smart.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.xinrui.smart.R;

/**
 * Created by win7 on 2018/4/10.
 */

public class ChoiceItemLayout extends LinearLayout implements Checkable {
    private boolean mChecked;
    public ChoiceItemLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        setBackgroundResource(checked? R.color.colorAccent : android.R.color.transparent);
    }

    @Override
    public boolean isChecked() {
        return true;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
