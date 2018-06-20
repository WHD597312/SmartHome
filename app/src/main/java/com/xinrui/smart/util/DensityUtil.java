package com.xinrui.smart.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {
    private static final String TAG = "DensityUtil";

              /**
        * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
        */
              public static int dip2px(Context context, float dpValue) {
                 final float scale = context.getResources().getDisplayMetrics().density;
                 return (int) (dpValue * scale + 0.5f);
             }

             /**
       * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
       */
             public static int px2dip(Context context, float pxValue) {
                 final float scale = context.getResources().getDisplayMetrics().density;
                 return (int) (pxValue / scale + 0.5f);
             }

             /**
       * 获取手机的分辨率px，返回一个数组，为宽度和长度。
       */
             public static int[] getDevicePx(Activity activity) {
                 DisplayMetrics metrics = new DisplayMetrics();
                 activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                 int width = metrics.widthPixels;
                 int heigth = metrics.heightPixels;
                 return new int[]{width, heigth};
             }

}
