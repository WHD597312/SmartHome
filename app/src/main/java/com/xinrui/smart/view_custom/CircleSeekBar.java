package com.xinrui.smart.view_custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.TaskTime;

import java.util.List;

/**
 * Created by gaopengfei on 15/11/15.
 */
public class CircleSeekBar extends View {

    private static final double RADIAN = 180 / Math.PI;

    private static final String INATANCE_STATE = "state";
    private static final String INSTANCE_MAX_PROCESS = "max_process";
    private static final String INSTANCE_CUR_PROCESS = "cur_process";
    private static final String INSTANCE_REACHED_COLOR = "reached_color";
    private static final String INSTANCE_REACHED_WIDTH = "reached_width";
    private static final String INSTANCE_REACHED_CORNER_ROUND = "reached_corner_round";
    private static final String INSTANCE_UNREACHED_COLOR = "unreached_color";
    private static final String INSTANCE_UNREACHED_WIDTH = "unreached_width";
    private static final String INSTANCE_POINTER_COLOR = "pointer_color";
    private static final String INSTANCE_POINTER_RADIUS = "pointer_radius";
    private static final String INSTANCE_POINTER_SHADOW = "pointer_shadow";
    private static final String INSTANCE_POINTER_SHADOW_RADIUS = "pointer_shadow_radius";
    private static final String INSTANCE_WHEEL_SHADOW = "wheel_shadow";
    private static final String INSTANCE_WHEEL_SHADOW_RADIUS = "wheel_shadow_radius";
    private static final String INSTANCE_WHEEL_HAS_CACHE = "wheel_has_cache";
    private static final String INSTANCE_WHEEL_CAN_TOUCH = "wheel_can_touch";
    private static final String INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE = "wheel_scroll_only_one_circle";

    private Paint mWheelPaint;

    private Paint mReachedPaint;

    private Paint mReachedEdgePaint;

    private Paint mPointerPaint;

    private int mMaxProcess;
    private double mCurProcess;
    private float mUnreachedRadius;
    private int mReachedColor, mUnreachedColor;
    private float mReachedWidth, mUnreachedWidth;
    private boolean isHasReachedCornerRound;
    private int mPointerColor;
    private float mPointerRadius;
    private float start_angle;
    private float cur_angle;
    private String week;

    private double mCurAngle;
    private float mWheelCurX, mWheelCurY;

    private boolean isHasWheelShadow, isHasPointerShadow;
    private float mWheelShadowRadius, mPointerShadowRadius;

    private boolean isHasCache;
    private Canvas mCacheCanvas;
    private Bitmap mCacheBitmap;

    private boolean isCanTouch;

    private boolean isScrollOneCircle;

    private float mDefShadowOffset;
    private int mPointColor;
    private int mNumColor;
    private int mNumSize;

    private Paint mPaint;
    private OnSeekBarChangeListener mChangListener;
    public double[] angles={-90, -75, -60, -45,-30, -15, 0, 15,30,45,60,75,90,105,120,135,150,165,180,195,210,225,250,265,270,285,300,315,330,345,360};
    public double[] angles2={270,  285, 300, 315, 330,345,0,15,30,45,60,75,90,105,120,135,150,165,180,195,210,225,250,265};
    private Context context;
    private TimeTaskDaoImpl timeTaskDao;
    public CircleSeekBar(Context context) {
        this(context, null);
        this.context=context;

    }

    public CircleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context=context;
    }

    public CircleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        initAttrs(attrs, defStyleAttr);
        initPadding();
        initPaints();
        mPaint=new Paint();
        timeTaskDao=new TimeTaskDaoImpl(context);
        Log.d("angles",angles.length+"");
    }

    private void initPaints() {
        mDefShadowOffset = getDimen(R.dimen.def_shadow_offset);
        /**
         * 圆环画笔
         */
        mWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWheelPaint.setColor(mUnreachedColor);
        mWheelPaint.setStyle(Paint.Style.STROKE);
        mWheelPaint.setStrokeWidth(mUnreachedWidth);
        if (isHasWheelShadow) {
            mWheelPaint.setShadowLayer(mWheelShadowRadius, mDefShadowOffset, mDefShadowOffset, Color.DKGRAY);
        }
        /**
         * 选中区域画笔
         */
        mReachedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedPaint.setColor(mReachedColor);
        mReachedPaint.setStyle(Paint.Style.STROKE);
        mReachedPaint.setStrokeWidth(mReachedWidth);
        if (isHasReachedCornerRound) {
            mReachedPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        /**
         * 锚点画笔
         */
        mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerPaint.setColor(mPointerColor);
        mPointerPaint.setStyle(Paint.Style.FILL);
        if (isHasPointerShadow) {
            mPointerPaint.setShadowLayer(mPointerShadowRadius, mDefShadowOffset, mDefShadowOffset, Color.DKGRAY);
        }
        /**
         * 选中区域两头的圆角画笔
         */
        mReachedEdgePaint = new Paint(mReachedPaint);
        mReachedEdgePaint.setStyle(Paint.Style.FILL);
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleSeekBar, defStyle, 0);
        mMaxProcess = a.getInt(R.styleable.CircleSeekBar_wheel_max_process, 100);
        mCurProcess = a.getInt(R.styleable.CircleSeekBar_wheel_cur_process, 0);
        if (mCurProcess > mMaxProcess) mCurProcess = mMaxProcess;
        mReachedColor = a.getColor(R.styleable.CircleSeekBar_wheel_reached_color, getColor(R.color.color_orange));
        mUnreachedColor = a.getColor(R.styleable.CircleSeekBar_wheel_unreached_color,
                getColor(R.color.color_blank2));
        mUnreachedWidth = a.getDimension(R.styleable.CircleSeekBar_wheel_unreached_width,
                getDimen(R.dimen.def_wheel_width));
        isHasReachedCornerRound = a.getBoolean(R.styleable.CircleSeekBar_wheel_reached_has_corner_round, true);
        mReachedWidth = a.getDimension(R.styleable.CircleSeekBar_wheel_reached_width, mUnreachedWidth);
        mPointerColor = a.getColor(R.styleable.CircleSeekBar_wheel_pointer_color, getColor(R.color.def_pointer_color));
        mPointerRadius = a.getDimension(R.styleable.CircleSeekBar_wheel_pointer_radius, mUnreachedWidth / 2);
        isHasWheelShadow = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_wheel_shadow, false);
        mPointColor=a.getColor(R.styleable.CircleSeekBar_pointcolor, Color.WHITE);
        start_angle=a.getFloat(R.styleable.CircleSeekBar_wheel_start_angle,0);
        cur_angle=a.getFloat(R.styleable.CircleSeekBar_wheel_current_angle,0);
        week=a.getString(R.styleable.CircleSeekBar_week);


        mNumColor=a.getColor(R.styleable.CircleSeekBar_numcolor, Color.BLACK);
        mNumSize=a.getInt(R.styleable.CircleSeekBar_numsize,14);
        if (isHasWheelShadow) {
            mWheelShadowRadius = a.getDimension(R.styleable.CircleSeekBar_wheel_shadow_radius,
                    getDimen(R.dimen.def_shadow_radius));
        }
        isHasPointerShadow = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_pointer_shadow, false);
        if (isHasPointerShadow) {
            mPointerShadowRadius = a.getDimension(R.styleable.CircleSeekBar_wheel_pointer_shadow_radius,
                    getDimen(R.dimen.def_shadow_radius));
        }
        isHasCache = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_cache, isHasWheelShadow);
        isCanTouch = a.getBoolean(R.styleable.CircleSeekBar_wheel_can_touch, true);
        isScrollOneCircle = a.getBoolean(R.styleable.CircleSeekBar_wheel_scroll_only_one_circle, false);

        if (isHasPointerShadow | isHasWheelShadow) {
            setSoftwareLayer();
        }
        a.recycle();
    }

    private void initPadding() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int paddingStart = 0, paddingEnd = 0;
        if (Build.VERSION.SDK_INT >= 17) {
            paddingStart = getPaddingStart();
            paddingEnd = getPaddingEnd();
        }
        int maxPadding = Math.max(paddingLeft, Math.max(paddingTop,
                Math.max(paddingRight, Math.max(paddingBottom, Math.max(paddingStart, paddingEnd)))));
        setPadding(maxPadding, maxPadding, maxPadding, maxPadding);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private int getColor(int colorId) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return getContext().getColor(colorId);
        } else {
            return ContextCompat.getColor(getContext(), colorId);
        }
    }

    private float getDimen(int dimenId) {
        return getResources().getDimension(dimenId);
    }

    private void setSoftwareLayer() {
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);

        refershPosition();
        refershUnreachedWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float left = getPaddingLeft() + mUnreachedWidth / 2;
        float top = getPaddingTop() + mUnreachedWidth / 2;
        float right = canvas.getWidth() - getPaddingRight() - mUnreachedWidth / 2;
        float bottom = canvas.getHeight() - getPaddingBottom() - mUnreachedWidth / 2;
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;

        float wheelRadius = (canvas.getWidth() - getPaddingLeft() - getPaddingRight()) / 2 - mUnreachedWidth / 2;

        if (isHasCache) {
            if (mCacheCanvas == null) {
                buildCache(centerX, centerY, wheelRadius);
            }
            canvas.drawBitmap(mCacheBitmap, 0, 0, null);
        } else {
            mWheelPaint.setColor(getResources().getColor(R.color.color_blank3));
            canvas.drawCircle(centerX, centerY, wheelRadius, mWheelPaint);
        }

//        //画选中区域
//        canvas.drawArc(new RectF(left, top, right, bottom), -90, (float) -60, false, mReachedPaint);
//        canvas.drawArc(new RectF(left, top, right, bottom), 0, (float) 30, false, mReachedPaint);



        List<TaskTime> list=timeTaskDao.findWeekAll(week);
        for (TaskTime taskTime :list){
            int start=taskTime.getStart();
            int end=taskTime.getEnd();
            if (start==24){
                start=0;
            }
            float startAngle= (360/24)*start-90;
            float endAngle=(360/24)*end-90;

            if (startAngle>180){
                startAngle=startAngle-360;
            }
            mCurAngle=endAngle-startAngle;

            //画选中区域
            canvas.drawArc(new RectF(left, top, right, bottom), startAngle, (float)mCurAngle, false, mReachedPaint);
            canvas.save();

//            double cos = -Math.cos(Math.toRadians((360/24)*start));
//            mWheelCurX = calcXLocationInWheel((360/24)*start, cos);
//            mWheelCurY = calcYLocationInWheel(cos);
//            //画锚点
//            canvas.drawCircle(mWheelCurX, mWheelCurY, mPointerRadius, mPointerPaint);
//            canvas.save();
//            cos = -Math.cos(Math.toRadians(360/24)*end);
//            mWheelCurX = calcXLocationInWheel((360/24)*end, cos);
//            mWheelCurY = calcYLocationInWheel(cos);
//            //画锚点
//            canvas.drawCircle(mWheelCurX, mWheelCurY, mPointerRadius, mPointerPaint);
//            canvas.save();
        }

        mPaint.setAntiAlias(true);//去除边缘锯齿，优化绘制效果
        mPaint.setColor(getResources().getColor(R.color.color_white));
        canvas.save();//保存当前的状态


        for (int i = 0; i < 24; i++) {//总共24个点  所以绘制24次  //绘制一圈的小黑点
            if (i % 2 == 0) {

                canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                        getPaddingTop() + mUnreachedWidth+ TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()),
                        centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),

                        getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), mPaint);
            } else {
                canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                        getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()),
                       centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                        getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), mPaint);
            }
            canvas.rotate(15, centerX, centerY);//360度  绘制60次   每次旋转6度
        }
        if (mNumColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
        mPaint.setTextSize(mNumSize);
        if (mNumSize == 0) {
            mPaint.setTextSize((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        }
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        String[] strs = new String[]{"24","2","4","6", "8","10", "12","14", "16","18", "20","22"};//绘制数字1-12  (数字角度不对  可以进行相关的处理)
        Rect rect = new Rect();
        canvas.save();
        for (int i = 0; i < 12; i++) {//绘制12次  每次旋转30度
            mPaint.getTextBounds(strs[i], 0, strs[i].length(), rect);
            canvas.drawText(strs[i], centerX - rect.width() / 2,
                    getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics()) + rect.height(), mPaint);
            canvas.rotate(30, centerX, centerY);
        }
        canvas.restore();
    }

    private void buildCache(float centerX, float centerY, float wheelRadius) {
        mCacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCacheCanvas = new Canvas(mCacheBitmap);

        //画环
        mCacheCanvas.drawCircle(centerX, centerY, wheelRadius, mWheelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (isCanTouch && (event.getAction() == MotionEvent.ACTION_MOVE || isTouch(x, y))) {
            // 通过当前触摸点搞到cos角度值
            float cos = computeCos(x, y);
            // 通过反三角函数获得角度值
            double angle;
            if (x < getWidth() / 2) { // 滑动超过180度
                angle = Math.PI * RADIAN + Math.acos(cos) * RADIAN;
            } else { // 没有超过180度
                angle = Math.PI * RADIAN - Math.acos(cos) * RADIAN;
            }
            if (isScrollOneCircle) {
                if (mCurAngle > 270 && angle < 90) {
                    mCurAngle = 360;
                    cos = -1;
                } else if (mCurAngle < 90 && angle > 270) {
                    mCurAngle = 0;
                    cos = -1;
                } else {
                    mCurAngle = angle;
                }
            } else {
                mCurAngle = angle;
            }
            mCurProcess = getSelectedValue();
            refershWheelCurPosition(cos);
            if (mChangListener != null && (event.getAction() & (MotionEvent.ACTION_MOVE | MotionEvent.ACTION_UP)) > 0) {
                mChangListener.onChanged(this, mCurProcess);
            }
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    private boolean isTouch(float x, float y) {
        double radius = (getWidth() - getPaddingLeft() - getPaddingRight() + getCircleWidth()) / 2;
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        return Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2) < radius * radius;
    }

    private float getCircleWidth() {
        return Math.max(mUnreachedWidth, Math.max(mReachedWidth, mPointerRadius));
    }

    private void refershUnreachedWidth() {
        mUnreachedRadius = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - mUnreachedWidth) / 2;
    }

    private void refershWheelCurPosition(double cos) {
        mWheelCurX = calcXLocationInWheel(mCurAngle, cos);
        mWheelCurY = calcYLocationInWheel(cos);
    }

    private void refershPosition() {
        mCurAngle = (double) mCurProcess / mMaxProcess * 360.0;
        double cos = -Math.cos(Math.toRadians(mCurAngle));
        refershWheelCurPosition(cos);
    }

    private float calcXLocationInWheel(double angle, double cos) {
        if (angle < 180) {
            return (float) (getMeasuredWidth() / 2 + Math.sqrt(1 - cos * cos) * mUnreachedRadius);
        } else {
            return (float) (getMeasuredWidth() / 2 - Math.sqrt(1 - cos * cos) * mUnreachedRadius);
        }
    }

    private float calcYLocationInWheel(double cos) {
        return getMeasuredWidth() / 2 + mUnreachedRadius * (float) cos;
    }

    /**
     * 拿到倾斜的cos值
     */
    private float computeCos(float x, float y) {
        float width = x - getWidth() / 2;
        float height = y - getHeight() / 2;
        float slope = (float) Math.sqrt(width * width + height * height);
        return height / slope;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INATANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_MAX_PROCESS, mMaxProcess);
        bundle.putDouble(INSTANCE_CUR_PROCESS, mCurProcess);
        bundle.putInt(INSTANCE_REACHED_COLOR, mReachedColor);
        bundle.putFloat(INSTANCE_REACHED_WIDTH, mReachedWidth);
        bundle.putBoolean(INSTANCE_REACHED_CORNER_ROUND, isHasReachedCornerRound);
        bundle.putInt(INSTANCE_UNREACHED_COLOR, mUnreachedColor);
        bundle.putFloat(INSTANCE_UNREACHED_WIDTH, mUnreachedWidth);
        bundle.putInt(INSTANCE_POINTER_COLOR, mPointerColor);
        bundle.putFloat(INSTANCE_POINTER_RADIUS, mPointerRadius);
        bundle.putBoolean(INSTANCE_POINTER_SHADOW, isHasPointerShadow);
        bundle.putFloat(INSTANCE_POINTER_SHADOW_RADIUS, mPointerShadowRadius);
        bundle.putBoolean(INSTANCE_WHEEL_SHADOW, isHasWheelShadow);
        bundle.putFloat(INSTANCE_WHEEL_SHADOW_RADIUS, mPointerShadowRadius);
        bundle.putBoolean(INSTANCE_WHEEL_HAS_CACHE, isHasCache);
        bundle.putBoolean(INSTANCE_WHEEL_CAN_TOUCH, isCanTouch);
        bundle.putBoolean(INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE, isScrollOneCircle);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(INATANCE_STATE));
            mMaxProcess = bundle.getInt(INSTANCE_MAX_PROCESS);
            mCurProcess = bundle.getInt(INSTANCE_CUR_PROCESS);
            mReachedColor = bundle.getInt(INSTANCE_REACHED_COLOR);
            mReachedWidth = bundle.getFloat(INSTANCE_REACHED_WIDTH);
            isHasReachedCornerRound = bundle.getBoolean(INSTANCE_REACHED_CORNER_ROUND);
            mUnreachedColor = bundle.getInt(INSTANCE_UNREACHED_COLOR);
            mUnreachedWidth = bundle.getFloat(INSTANCE_UNREACHED_WIDTH);
            mPointerColor = bundle.getInt(INSTANCE_POINTER_COLOR);
            mPointerRadius = bundle.getFloat(INSTANCE_POINTER_RADIUS);
            isHasPointerShadow = bundle.getBoolean(INSTANCE_POINTER_SHADOW);
            mPointerShadowRadius = bundle.getFloat(INSTANCE_POINTER_SHADOW_RADIUS);
            isHasWheelShadow = bundle.getBoolean(INSTANCE_WHEEL_SHADOW);
            mPointerShadowRadius = bundle.getFloat(INSTANCE_WHEEL_SHADOW_RADIUS);
            isHasCache = bundle.getBoolean(INSTANCE_WHEEL_HAS_CACHE);
            isCanTouch = bundle.getBoolean(INSTANCE_WHEEL_CAN_TOUCH);
            isScrollOneCircle = bundle.getBoolean(INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE);
            initPaints();
        } else {
            super.onRestoreInstanceState(state);
        }

        if (mChangListener != null) {
            mChangListener.onChanged(this, mCurProcess);
        }
    }

    private double getSelectedValue() {
        return  (mCurAngle/360)*mMaxProcess;

//        return Math.round(mMaxProcess * ((float) mCurAngle / 360));
    }

    public double getCurProcess() {
        return mCurProcess;
    }

    public void setCurProcess(int curProcess) {
        this.mCurProcess = curProcess > mMaxProcess ? mMaxProcess : curProcess;
        if (mChangListener != null) {
            mChangListener.onChanged(this, curProcess);
        }
        refershPosition();
        invalidate();
    }


    public int getMaxProcess() {
        return mMaxProcess;
    }

    public void setMaxProcess(int maxProcess) {
        mMaxProcess = maxProcess;
        refershPosition();
        invalidate();
    }

    public int getReachedColor() {
        return mReachedColor;
    }

    public void setReachedColor(int reachedColor) {
        this.mReachedColor = reachedColor;
        mReachedPaint.setColor(reachedColor);
        mReachedEdgePaint.setColor(reachedColor);
        invalidate();
    }

    public int getUnreachedColor() {
        return mUnreachedColor;
    }

    public void setUnreachedColor(int unreachedColor) {
        this.mUnreachedColor = unreachedColor;
        mWheelPaint.setColor(unreachedColor);
        invalidate();
    }

    public float getReachedWidth() {
        return mReachedWidth;
    }

    public void setReachedWidth(float reachedWidth) {
        this.mReachedWidth = reachedWidth;
        mReachedPaint.setStrokeWidth(reachedWidth);
        mReachedEdgePaint.setStrokeWidth(reachedWidth);
        invalidate();
    }

    public static double getRADIAN() {
        return RADIAN;
    }

    public float getStart_angle() {
        return start_angle;
    }

    public void setStart_angle(float start_angle) {
        this.start_angle = start_angle;
        invalidate();
    }

    public float getCur_angle() {
        return cur_angle;
    }

    public void setCur_angle(float cur_angle) {
        this.cur_angle = cur_angle;
        invalidate();
    }

    public boolean isHasReachedCornerRound() {
        return isHasReachedCornerRound;
    }

    public void setHasReachedCornerRound(boolean hasReachedCornerRound) {
        isHasReachedCornerRound = hasReachedCornerRound;
        mReachedPaint.setStrokeCap(hasReachedCornerRound ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        invalidate();
    }

    public float getUnreachedWidth() {
        return mUnreachedWidth;
    }

    public void setUnreachedWidth(float unreachedWidth) {
        this.mUnreachedWidth = unreachedWidth;
        mWheelPaint.setStrokeWidth(unreachedWidth);
        refershUnreachedWidth();
        invalidate();
    }

    public int getPointerColor() {
        return mPointerColor;
    }

    public void setPointerColor(int pointerColor) {
        this.mPointerColor = pointerColor;
        mPointerPaint.setColor(pointerColor);
    }

    public float getPointerRadius() {
        return mPointerRadius;
    }

    public void setPointerRadius(float pointerRadius) {
        this.mPointerRadius = pointerRadius;
        mPointerPaint.setStrokeWidth(pointerRadius);
        invalidate();
    }

    public boolean isHasWheelShadow() {
        return isHasWheelShadow;
    }

    public void setWheelShadow(float wheelShadow) {
        this.mWheelShadowRadius = wheelShadow;
        if (wheelShadow == 0) {
            isHasWheelShadow = false;
            mWheelPaint.clearShadowLayer();
            mCacheCanvas = null;
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        } else {
            mWheelPaint.setShadowLayer(mWheelShadowRadius, mDefShadowOffset, mDefShadowOffset, Color.DKGRAY);
            setSoftwareLayer();
        }
        invalidate();
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public float getWheelShadowRadius() {
        return mWheelShadowRadius;
    }

    public boolean isHasPointerShadow() {
        return isHasPointerShadow;
    }

    public float getPointerShadowRadius() {
        return mPointerShadowRadius;
    }

    public void setPointerShadowRadius(float pointerShadowRadius) {
        this.mPointerShadowRadius = pointerShadowRadius;
        if (mPointerShadowRadius == 0) {
            isHasPointerShadow = false;
            mPointerPaint.clearShadowLayer();
        } else {
            mPointerPaint.setShadowLayer(pointerShadowRadius, mDefShadowOffset, mDefShadowOffset, Color.DKGRAY);
            setSoftwareLayer();
        }
        invalidate();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mChangListener = listener;
    }

    public interface OnSeekBarChangeListener {
        void onChanged(CircleSeekBar seekbar, double curValue);
    }
}
