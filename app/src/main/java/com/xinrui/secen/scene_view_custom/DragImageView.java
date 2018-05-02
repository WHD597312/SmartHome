package com.xinrui.secen.scene_view_custom;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.xinrui.secen.scene_pojo.Equipment;

import java.util.List;

/**
 * Created by win7 on 2018/4/16.
 */

public class DragImageView extends RecyclerView {
    private static final String TAG = "TestViewGroup";

    private ViewDragHelper mDragHelper;


    private float mDragOriLeft;
    private float mDragOriTop;

    public DragImageView(Context context) {
        this(context,null);
    }

    public DragImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DragImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
                mDragOriLeft = capturedChild.getLeft();
                mDragOriTop = capturedChild.getTop();
                Log.d(TAG, "onViewCaptured: left:"+mDragOriLeft
                        +" top:"+mDragOriTop);
            }

//            @Override
//            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
//                super.onEdgeDragStarted(edgeFlags, pointerId);
//                Log.d(TAG, "onEdgeDragStarted: "+edgeFlags);
//                mDragHelper.captureChildView(getChildAt(getChildCount()-1),pointerId);
//            }
//
//            @Override
//            public void onEdgeTouched(int edgeFlags, int pointerId) {
//                super.onEdgeTouched(edgeFlags, pointerId);
//                Log.d(TAG, "onEdgeTouched: "+edgeFlags);
//            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }

            //拖拽后回弹
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);

                View child = getChildAt(0);
                if ( child != null) {
                    mDragHelper.flingCapturedView(getPaddingLeft(),getPaddingTop(),
                            getWidth()-getPaddingRight()-child.getWidth(),
                            getHeight()-getPaddingBottom()-child.getHeight());
                } else {

                    mDragHelper.settleCapturedViewAt((int)mDragOriLeft,(int)mDragOriTop);

                }
                invalidate();
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return 1000;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return 1000;
            }
        });

        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);

    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper != null && mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }


    public void testSmoothSlide(List<Equipment> equipment, boolean isReverse) {
        if ( mDragHelper != null ) {
            for (int i = 0; i < equipment.size(); i++) {
                View child = getChildAt(i);
                if ( child != null ) {
                    if ( isReverse ) {
                        mDragHelper.smoothSlideViewTo(child,
                                getLeft(),getTop());
                    } else {
                        mDragHelper.smoothSlideViewTo(child,
                                getRight()-child.getWidth(),
                                getBottom()-child.getHeight());
                    }
                    invalidate();
                }
            }

        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

}
