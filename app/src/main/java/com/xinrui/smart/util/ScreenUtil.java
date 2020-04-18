package com.xinrui.smart.util;

import android.content.Context;
import android.content.res.Resources;

/**
 * @author：byd666 on 2017/12/2 15:39
 */

public class ScreenUtil {


    public final static String WIDTH = "width";

    public final static String HEIGHT = "height";


    public static int dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }
}
