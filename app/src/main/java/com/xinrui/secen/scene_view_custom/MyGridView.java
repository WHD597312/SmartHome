package com.xinrui.secen.scene_view_custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.GridView;

/**
 * Created by win7 on 2018/3/21.
 */

public class MyGridView extends GridView {

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 开启View闪烁效果
     *
     * */
    public void startFlick( View view ){
        if( null == view ){
            return;
        }
        Animation alphaAnimation = new AlphaAnimation( 1, 0.4f );
        alphaAnimation.setDuration( 600 );
        alphaAnimation.setInterpolator( new LinearInterpolator( ) );
        alphaAnimation.setRepeatCount( Animation.INFINITE );
        alphaAnimation.setRepeatMode( Animation.REVERSE );
        view.startAnimation( alphaAnimation );
    }

    /**
     * 取消View闪烁效果
     *
     * */
    public void stopFlick( View view ){
        if( null == view ){
            return;
        }
        view.clearAnimation( );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
//            return true; // 禁止GridView滑动
//        }
//        return super.dispatchTouchEvent(ev);
//    }
}
