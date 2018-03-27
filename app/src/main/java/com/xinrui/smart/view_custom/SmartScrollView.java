package com.xinrui.smart.view_custom;

/**
 * Created by win7 on 2018/3/23.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 监听ScrollView滚动到顶部或者底部做相关事件拦截
 */
public class SmartScrollView extends ScrollView {

    private boolean isScrolledToTop = true;// 初始化的时候设置一下值
    private boolean isScrolledToBottom = false;
    public SmartScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private ISmartScrollChangedListener mSmartScrollChangedListener;

    /** 定义监听接口 */
    public interface ISmartScrollChangedListener {
        void onScrolledToBottom();
        void onScrolledToTop();
    }

    public void setScanScrollChangedListener(ISmartScrollChangedListener smartScrollChangedListener) {
        mSmartScrollChangedListener = smartScrollChangedListener;
    }
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (scrollY == 0) {
            isScrolledToTop = clampedY;
            isScrolledToBottom = false;
            System.out.println("onOverScrolled isScrolledToTop:" + isScrolledToTop);
        } else {
            isScrolledToTop = false;
            isScrolledToBottom = clampedY;
            System.out.println("onOverScrolled isScrolledToBottom:" + isScrolledToBottom);
        }
        notifyScrollChangedListeners();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 这个log可以研究ScrollView的上下padding对结果的影响
        System.out.println("onScrollChanged getScrollY():" + getScrollY() + " t: " + t + " paddingTop: " + getPaddingTop());
        if (getScrollY() == 0) {
            isScrolledToTop = true;
            isScrolledToBottom = false;
            System.out.println("onScrollChanged isScrolledToTop:" + isScrolledToTop);
        } else if (getScrollY() + getHeight() - getPaddingTop() - getPaddingBottom() == getChildAt(0).getHeight()) {
            isScrolledToBottom = true;
            System.out.println("onScrollChanged isScrolledToBottom:" + isScrolledToBottom);
            isScrolledToTop = false;
        } else {
            isScrolledToTop = false;
            isScrolledToBottom = false;
        }
        notifyScrollChangedListeners();
    }
    private void notifyScrollChangedListeners() {
        if (isScrolledToTop) {
            if (mSmartScrollChangedListener != null) {
                mSmartScrollChangedListener.onScrolledToTop();
            }
        } else if (isScrolledToBottom) {
            if (mSmartScrollChangedListener != null) {
                mSmartScrollChangedListener.onScrolledToBottom();
            }
        }
    }

    public boolean isScrolledToTop() {
        return isScrolledToTop;
    }

    public boolean isScrolledToBottom() {
        return isScrolledToBottom;
    }
}
