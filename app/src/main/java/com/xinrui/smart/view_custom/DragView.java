package com.xinrui.smart.view_custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.xinrui.smart.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2017/8/24.
 */

@SuppressLint("AppCompatCustomView")
public class DragView extends ImageView {
    private Boolean falg = false;   //用于判断 是滑动还是点击事件 做弹出菜单处理
    private int mScreenWidth;       //屏幕宽度
    private int mScreenHeight;      //屏幕高度
    private Context context;
    private final int radius1 = 100;    //没用到
    private List<View> mData = new ArrayList<>();
    private int left;               //控件位于屏幕的 上下左右的距离
    private int top;
    private int right;
    private int bottom;
//    private ShowPopup showPopup;        //显示菜单的popup

    public DragView(Context context) {
        this(context, null);
    }

    public DragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        //获取屏幕宽高，用于控制控件在屏幕内移动
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels - 220;//这里减去的100是下边的back键和menu键那一栏的高度，看情况而定
//        showPopup = new ShowPopup(context); //初始化popup
    }

    int startDownX = 0; //手指按下 记录坐标(X,Y)
    int startDownY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int lastMoveX = 0; // 用于记录手指滑动的 距离  (x,y)
        int lastMoveY = 0;
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                falg = true;
                startDownX = (int) event.getRawX(); //记录手指按下坐标
                startDownY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
//                if (showPopup.isShowing()) {  //判断popup 是否打开 如果打在并且处于滑动 自动关闭popup
//                    showPopup.dissPopup();
//                }
                lastMoveX = (int) event.getRawX();  //滑动之后的坐标
                lastMoveY = (int) event.getRawY();

                int dx = lastMoveX - startDownX;  //计算滑动的距离  dx  dy
                int dy = lastMoveY - startDownY;
                if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {  // 用时候手指想点击 但是会有抖动的情况  误以为是滑动这样popup不会弹出来
                                                            //  判断如果移动距离大于 2  则认为用户是移动 否则是点击
                    falg = false;
                }

                left = getLeft() + dx;      //计算 控件的位置
                top = getTop() + dy;
                right = getRight() + dx;
                bottom = getBottom() + dy;
                if (left < 0) {         //判断是否越出屏幕边界
                    left = 0;
                    right = left + getWidth();
                }
                if (right > mScreenWidth) { //判断是否越出屏幕边界
                    right = mScreenWidth;
                    left = right - getWidth();
                }
//                if (top < 0) { //判断是否越出屏幕边界
//                    top = 0;
//                    bottom = top + getHeight();
//                }
//                if (bottom > mScreenHeight) { //判断是否越出屏幕边界
//                    bottom = mScreenHeight;
//                    top = bottom - getHeight();
//                }
//                layout(left, top, right, bottom); //控件重新绘制
                Log.i("____________________", left + "___" + top + "___" + right + "___" + bottom);
                invalidate();       //刷新页面
                startDownX = (int) event.getRawX();     //最后移动的位置  当做初始位置
                startDownY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                int lastMoveDx = Math.abs((int) event.getRawX() - startDownX);
                int lastMoveDy = Math.abs((int) event.getRawY() - startDownY);
//                if (falg) {     //手指离开 判断是点击还是滑动  弹出popup
//                    ToastUtils.showToast(context, "我点击了");
//                    showContent();  //弹出popup
//                }
                falg = false;  //重置弹出popup的标志位
                break;
        }
        return true;
    }

//    private void showContent() {
//        if (showPopup.isShowing()) {
//            showPopup.dissPopup();
//        } else {
//            int excursionX = -(getWidth() / 2) - 5;  //多次调试发现popup 并没有出现在控件的正下方 手动设置默认值
//            int excursionY = 0;
//            if (left == 0 && right == 0 && top == 0 && bottom == 0) { //如果控件默认在屏幕中间 不移动的情况下 默认 上下左右的值是为0
//                                                                        // 用于判断控件有没有移动 没有移动 出现在菜单的正下方
//                excursionX = -(getWidth() / 2)-5;
//                excursionY = 0;
//            } else if (left < 50 && top < 50) {     //判断控件是否是左上角 如果是左上角 则菜单出现在右下角
//                excursionX = getWidth();
//                excursionY = 0;
//            } else if (mScreenWidth - right < 50 && top < 50) { //判断控件是否在右上角  如果在右上角 则菜单出现在左下角
//                excursionX = -getWidth() * 2 - 20;
//                excursionY = 0;
//            } else if (mScreenWidth - right < 50 && mScreenHeight - bottom < getHeight()*2) {  //判断控件是否在右下角 如果在右下角 则菜单出现在左上角
//                excursionX = -getWidth() * 2 - 20;
//                excursionY = -getHeight() * 3 - 20;
//            } else if (mScreenHeight - bottom < getHeight()*2 && left < 50) {  //判断控件是否在左下角 如果在左下角 则控件出现在右上角
//                excursionX = getWidth();
//                excursionY = -getHeight() * 3 - 20;
//            } else if (left < 50) {  //判断控件是否在屏幕左边界  如果在左边界 则菜单出现在右侧
//                excursionX = getWidth();
//                excursionY = -getHeight() - getHeight() / 2;
//            } else if (top < 50) {      //判断控件是否在屏幕上边界 如果在上边界 则菜单出现在下侧(默认就是下侧)
//                excursionX = -(getWidth() / 2)-10;
//                excursionY = -10;
//            } else if (mScreenWidth - right < 50) {     //判断控件是否在屏幕右侧  如果在右侧 则菜单出现在左侧
//                excursionX = -getWidth() * 2 - 10;
//                excursionY = -getHeight() - getHeight() / 2 - 10;
//            } else if (mScreenHeight - bottom < getHeight() * 2.5) {        //判断控件是否在屏幕下边界 并且留有空间是否可以弹出popup  如果可以则在下侧显示 不可以则在上侧显示
//                excursionX = -getWidth() / 2 -10;
//                excursionY = -getHeight() * 3 - 10;
//            } else {
//
//            }
//
////            showPopup.showPopup(this, excursionX, excursionY);  //调用popup 方法 实现popup在控件某个位置显示
//        }
//    }


    public void setData(List<View> mData) {//没用到
        this.mData = mData;
    }
}
