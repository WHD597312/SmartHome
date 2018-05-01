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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaopengfei on 15/11/15.
 */
public class SemicircleBar extends View {

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
    private String module;
    /**
     * 模式
     */
    private boolean slide;

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
    private int mCurrentAngle;//当前角度

    public SemicircleBar(Context context) {
        this(context, null);
    }

    public SemicircleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SemicircleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(attrs, defStyleAttr);
        initPadding();
        initPaints();
        mPaint = new Paint();
        deviceChildDao = new DeviceChildDaoImpl(context);

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
//        if (isHasWheelShadow) {
//            mWheelPaint.setShadowLayer(mWheelShadowRadius, mDefShadowOffset, mDefShadowOffset, Color.DKGRAY);
//        }
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

    private boolean first;

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isFirst() {
        return first;
    }

    DeviceChildDaoImpl deviceChildDao;

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleSeekBar, defStyle, 0);
        mMaxProcess = a.getInt(R.styleable.CircleSeekBar_wheel_max_process, 100);

        mCurProcess = a.getInt(R.styleable.CircleSeekBar_wheel_cur_process, 0);
        if (mCurProcess > mMaxProcess) mCurProcess = mMaxProcess;
        mReachedColor = a.getColor(R.styleable.CircleSeekBar_wheel_reached_color, getColor(R.color.color_blank2));

        mUnreachedWidth = a.getDimension(R.styleable.CircleSeekBar_wheel_unreached_width,
                getDimen(R.dimen.def_wheel_width));

        isHasReachedCornerRound = a.getBoolean(R.styleable.CircleSeekBar_wheel_reached_has_corner_round, true);
        mReachedWidth = a.getDimension(R.styleable.CircleSeekBar_wheel_reached_width, mUnreachedWidth);
        mPointerColor = a.getColor(R.styleable.CircleSeekBar_wheel_pointer_color, getColor(R.color.def_pointer_color));
        mPointerRadius = a.getDimension(R.styleable.CircleSeekBar_wheel_pointer_radius, mUnreachedWidth / 2);
        isHasWheelShadow = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_wheel_shadow, false);
        mPointColor = a.getColor(R.styleable.CircleSeekBar_pointcolor, Color.WHITE);
        module = a.getString(R.styleable.CircleSeekBar_module);
        mCurrentAngle = a.getInt(R.styleable.CircleSeekBar_mCurrentAngle, 0);
        mNumColor = a.getColor(R.styleable.CircleSeekBar_numcolor, Color.BLACK);
        mNumSize = a.getInt(R.styleable.CircleSeekBar_numsize, 14);
        first = a.getBoolean(R.styleable.CircleSeekBar_first, false);
        mCurAngle = a.getInt(R.styleable.CircleSeekBar_mCurAngle, 0);
        deviceId = a.getString(R.styleable.CircleSeekBar_deviceId);
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

    private int cur = 0;

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
            mWheelPaint.setColor(getResources().getColor(R.color.color_black3));
            canvas.drawCircle(centerX, centerY, wheelRadius, mWheelPaint);
//            canvas.drawArc(new RectF(left, top, right, bottom), -90, (float) 315, false, mWheelPaint);
        }

        //画选中区域
        canvas.drawArc(new RectF(left, top, right, bottom), -90, (float) 272, false, mReachedPaint);

        //画锚点
//        mPointerPaint.setColor(getResources().getColor(R.color.color_black3));

        mPaint.setAntiAlias(true);//去除边缘锯齿，优化绘制效果
        mPaint.setColor(getResources().getColor(R.color.color_white));
        canvas.save();//保存当前的状态

        for (int i = 0; i < 45; i++) {//总共45个点  所以绘制45次  //绘制一圈的小黑点
            if (i > 37) {
                mPaint.setColor(getResources().getColor(R.color.color_black3));
            }
//            if (i % 7 == 0) {
//                canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
//                        getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
//                        centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
//
//                        getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()), mPaint);
//            } else {
            canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                    getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                    centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                    getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), mPaint);
//            }
//            if (i == 37) {
//                mPaint.setColor(getResources().getColor(R.color.red_normal));
//                canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
//                        getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
//                        centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
//
//                        getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()), mPaint);
//            }
            canvas.rotate(8, centerX, centerY);//360度  绘制72次   每次旋转5度
        }
        if (mNumColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(mNumSize);
        if (mNumSize == 0) {
            mPaint.setTextSize((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        }
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        String[] strs = null;
        String[] strs2 = null;
        if ("1".equals(module)) {//定时和手动
            strs = new String[]{"05", "12", "19", "26", "33", "40"};//绘制数字1-12  (数字角度不对  可以进行相关的处理)
            strs2 = new String[]{"", "", "", "", "", "", "42"};//绘制数字1-12  (数字角度不对  可以进行相关的处理)
        } else if ("2".equals(module)) {//保护
            strs = new String[]{"48", "50", "53", "55", "57", ""};
            strs2 = new String[]{"", "", "", "", "", "", "60"};//绘制数字1-12  (数字角度不对  可以进行相关的处理)
        }

        Rect rect = new Rect();
        canvas.save();
        for (int i = 0; i < 6; i++) {//绘制6次  每次旋转56度

            mPaint.getTextBounds(strs[i], 0, strs[i].length(), rect);
            canvas.drawText(strs[i], centerX - rect.width() / 2,
                    getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()) + rect.height(), mPaint);
            canvas.rotate(56, centerX, centerY);
        }
        canvas.save();


        for (int i = 0; i < 7; i++) {//绘制6次  每次旋转30度
            if (i == 6) {
                mPaint.setColor(getResources().getColor(R.color.white));
            } else {
                mPaint.setColor(getResources().getColor(R.color.color_black3));
            }

            mPaint.getTextBounds(strs2[i], 0, strs2[i].length(), rect);
            canvas.drawText(strs2[i], centerX - rect.width() / 2,
                    getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()) + rect.height(), mPaint);

            canvas.rotate(53.5f, centerX, centerY);
        }
        canvas.save();


        mCurAngle = mCurProcess;
        if (!Utils.isEmpty(deviceId)) {
            deviceChild = deviceChildDao.findDeviceById(Long.parseLong(deviceId));
            init();
            connect();
        }

        if (mCurAngle >= 0) {
            if (mCurAngle >= 272) {
                if (mCurAngle > 272 && mCurAngle <= 310) {
                    mCurAngle = 272;
                } else if (mCurAngle > 310 && mCurAngle <= 360) {
                    mCurAngle = 0;
                    return;
                }
            }
            for (int i = 0; i < (mCurAngle) / 7; i++) {
                if ((i - 1) % 7 == 0) {

                    mPaint.setColor(getResources().getColor(R.color.color_orange));

                    canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()),
                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                            centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),

                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), mPaint);
                } else {
                    if (i == 0) {
                        mPaint.setColor(getResources().getColor(R.color.color_black3));
                    } else {
                        mPaint.setColor(getResources().getColor(R.color.color_orange));
                    }
                    canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()),
                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                            centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),
                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), mPaint);
                }
                canvas.rotate(8, centerX, centerY);//360度  绘制60次   每次旋转6度
            }
            canvas.save();


        }

//        if (mCurrentAngle>0){
//
//
//            if (mCurrentAngle >= 0) {
//                if (mCurrentAngle>=272){
//                    if (mCurrentAngle>272 && mCurrentAngle<=310){
//                        mCurAngle=272;
//                    }else if (mCurrentAngle> 310 && mCurrentAngle<=360){
//                        mCurrentAngle=0;
//                        return;
//                    }
//                }
//                for (int i = 0; i < (mCurrentAngle) / 7; i++) {
//                    if ((i - 1) % 7 == 0) {
//
//                        mPaint.setColor(getResources().getColor(R.color.color_orange));
//
//                        canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()),
//                                getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
//                                centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),
//
//                                getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), mPaint);
//                    } else {
//                        if (i == 0) {
//                            mPaint.setColor(getResources().getColor(R.color.color_black3));
//                        } else {
//                            mPaint.setColor(getResources().getColor(R.color.color_orange));
//                        }
//                        canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()),
//                                getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
//                                centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),
//                                getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), mPaint);
//                    }
//                    canvas.rotate(8, centerX, centerY);//360度  绘制60次   每次旋转6度
//                }
//                canvas.save();
//            }

//        }
    }

    private String deviceId;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
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
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (deviceChild != null) {
                        send(deviceChild);
                    }
                }
                mChangListener.onChanged(this, mCurProcess);
            }
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public boolean isCanTouch() {
        return isCanTouch;
    }

    public void setCanTouch(boolean canTouch) {
        isCanTouch = canTouch;
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

    public void setmCurrentAngle(int mCurrentAngle) {
        this.mCurrentAngle = mCurrentAngle;
    }

    public int getmCurrentAngle() {
        return mCurrentAngle;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }

    public void setSlide(boolean slide) {
        this.slide = slide;
    }

    public boolean isSlide() {
        return slide;
    }

    private double getSelectedValue() {
        return (mCurAngle / 360) * mMaxProcess;

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

    public void setmCurAngle(double mCurAngle) {
        this.mCurAngle = mCurAngle;
    }

    public double getmCurAngle() {
        return mCurAngle;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mChangListener = listener;
    }

    public interface OnSeekBarChangeListener {
        void onChanged(SemicircleBar seekbar, double curValue);
    }

    private String TAG = "MQService";
    private String host = "tcp://120.77.36.206:1883";
    private String userName = "admin";
    private String passWord = "Xr7891122";

    private MqttClient client;

    public String myTopic = "rango/dc4f220aa96e/transfer";


    DeviceChild deviceChild;
    private MqttConnectOptions options;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void connect() {
        new ConAsync().execute();
    }

    class ConAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                client.connect(options);
                if (deviceChild != null) {
                    String macAddress = deviceChild.getMacAddress();
                    String topicName = "rango/" + macAddress + "/transfer";
                    client.subscribe(topicName, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, "",
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);


            //设置回调
            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    startReconnect();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {

//
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void startReconnect() {

        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    public boolean publish(String topicName, int qos, String payload) {
        boolean flag = false;
        if (client != null && client.isConnected()) {

            try {
                MqttMessage message = new MqttMessage(payload.getBytes("utf-8"));
                message.setQos(qos);
                client.publish(topicName, message);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public void send(DeviceChild deviceChild) {
        try {
            if (deviceChild != null) {
                JSONObject maser = new JSONObject();

//                maser.put("wifiVersion", deviceChild.getWifiVersion());
//                maser.put("MCUVerion", deviceChild.getMCUVerion());
                maser.put("ctrlMode", deviceChild.getCtrlMode());
                maser.put("workMode", deviceChild.getWorkMode());
                maser.put("MatTemp", deviceChild.getMatTemp());
                maser.put("LockScreen", deviceChild.getLockScreen());
                maser.put("BackGroundLED", deviceChild.getBackGroundLED());
                maser.put("deviceState", deviceChild.getDeviceState());
                maser.put("tempState", deviceChild.getTempState());
                maser.put("outputMode", deviceChild.getOutputMod());
//                maser.put("curTemp", deviceChild.getCurTemp());
//                maser.put("ratedPower", deviceChild.getRatedPower());
//                maser.put("protectEnable", deviceChild.getProtectEnable());

//                maser.put("voltageValue", deviceChild.getVoltageValue());
//                maser.put("currentValue", deviceChild.getCurrentValue());
//                maser.put("machineFall", deviceChild.getMachineFall());
                maser.put("protectProTemp", deviceChild.getProtectProTemp());
                maser.put("protectSetTemp", deviceChild.getProtectSetTemp());


                String s = maser.toString();
                boolean success = false;
                String topicName;
                String mac = deviceChild.getMacAddress();
                if (deviceChild.getType() == 1 && deviceChild.getControlled() == 2) {
                    topicName = "rango/" + mac + "/masterController/set";
                    success=publish(topicName, 2, s);

                } else {
                    topicName = "rango/" + mac + "/set";
                    success=publish(topicName, 2, s);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}