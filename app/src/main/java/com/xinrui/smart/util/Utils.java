package com.xinrui.smart.util;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by win7 on 2018/3/22.
 */

public class Utils {
    public static void showToast(Context context,String content){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }

    /**
     * 判断字符串是空的包括空对象和内容为空
     * @param s
     * @return
     */
    public static boolean isEmpty(String s){
        boolean flag=false;
        if (s==null || "".equals(s)){
            flag=true;
        }
        return flag;
    }

    /**
     * 得到某一天的星期几
     * @param year
     * @param month
     * @param day
     * @param week
     * @return
     */
    public static String getWeek(int year,int month,int day,int week){
        String mWeek=null;
        switch (week) {
            case (1):
                mWeek="星期日";
                break;
            case (2):
                mWeek="星期一";
                break;
            case 3:
                mWeek="星期二";
                break;
            case 4:
                mWeek="星期三";
                break;
            case 5:
                mWeek="星期四";
                break;
            case 6:
                mWeek="星期五";
                break;
            case 7:
                mWeek="星期六";
                break;
        }
        return mWeek;
    }
    /***
     * 计算一年中的第几天
     * @param year
     * @param month
     * @param day
     * @return
     */
    public  static  int cal(int year, int month, int day) {
        int sum = 0;
        for (int i = 1; i < month; i++) {
            switch (i) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    sum += 31;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    sum += 30;
                    break;
                case 2:
                    if (((year % 4 == 0) & (year % 100 != 0)) | (year % 400 == 0))
                        sum += 29;
                    else
                        sum += 28;
            }
        }
        return sum = sum + day;
    }
}
