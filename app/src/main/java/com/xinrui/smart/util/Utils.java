package com.xinrui.smart.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by win7 on 2018/3/22.
 */

public class Utils {
    public static void showToast(Context context,String content){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }
}
