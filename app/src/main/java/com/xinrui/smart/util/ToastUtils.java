package com.xinrui.smart.util;

import android.content.Context;
import android.widget.Toast;


/**
 * author zwq on 2017/07/20.
 */
public class ToastUtils {
    public static Toast mToast;

    /**
     * 显示吐司
     * @param message
     */
    public static void showToast(Context context,final String message){
        if (mToast == null){
            mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }else{
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }



    /**
     * 显示吐司
     * @param context
     * @param messageResId
     */
    public static void showToast(final Context context, final int messageResId){
        if (mToast == null){
            mToast = Toast.makeText(context, messageResId, Toast.LENGTH_SHORT);
        }else{
            mToast.setText(messageResId);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

}
