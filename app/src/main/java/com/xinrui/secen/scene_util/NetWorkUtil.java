package com.xinrui.secen.scene_util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xinrui.secen.scene_pojo.MessageEvent;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;

import org.greenrobot.eventbus.EventBus;

public class NetWorkUtil {
    private volatile static  NetWorkUtil instance;

    public NetWorkUtil(){
        instance=this;
    }
    public synchronized static NetWorkUtil getInstance() {
        if (instance == null) {

            synchronized (NetWorkUtil.class) {
                instance = instance == null ? new NetWorkUtil() : instance;
            }
        }
        return instance;
    }
    /**
     * 这个方法是判断网络状态是否可用的
     * @param context
     * @return
     */
    public static boolean isConn(Context context){
        boolean bisConnFlag=false;
        //1.获取网络连接的管理对象
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //2.通过管理者对象拿到网络的信息
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network!=null){
            //3.网络状态是否可用的返回值
            bisConnFlag=network.isAvailable();
        }
        return bisConnFlag;
    }


    /**
     * 如果没有网络 弹出dialog对话框,,,是否进入设置网络的页面
     * @param context
     */
    public static void showNoNetWorkDlg(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_launcher)         //
                .setTitle("警告")            //
                .setMessage("当前无网络,是否去设置?").setPositiveButton("设置", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 跳转到系统的网络设置界面
                Intent intent = null;
                // 先判断当前系统版本
                if(android.os.Build.VERSION.SDK_INT > 10){  // 3.0以上
                    intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }else{
                    intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.WirelessSettings");
                }
                context.startActivity(intent);


            }
        }).setNegativeButton("取消", null).show();
    }

    public static boolean isWifiConnected() {
        boolean isConnected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            isConnected = info.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    public static boolean isMobileConnected() {
        boolean isConnected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            isConnected = info.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    /*
     * 打开设置网络界面
     * */
    AlertDialog.Builder builder=null;
    public void setNetworkMethod(final Context context){
        if (builder!=null){
            builder.setCancelable(true);
        }
        //提示对话框
        builder=new AlertDialog.Builder(context);
        builder.setTitle("网络设置提示").setMessage("网络连接不可用,是否进行设置?").setPositiveButton("设置", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent intent=null;
                //判断手机系统的版本  即API大于10 就是3.0或以上版本
                if(android.os.Build.VERSION.SDK_INT>10){
                    intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                }else{
                    intent=new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }
                context.startActivity(intent);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        }).show();
    }
/**
 * 网络异常提示界面
 */
public  void setNetworkAnomaly(final Context context){
    //提示对话框
    AlertDialog.Builder builder=new AlertDialog.Builder(context);
    builder.setTitle("网络异常提示").setMessage("网络连接异常，请再试一次?").setPositiveButton("是", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            EventBus.getDefault().post(new MessageEvent("0"));
        }
    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            dialog.dismiss();
        }
    }).show();
}
 }
