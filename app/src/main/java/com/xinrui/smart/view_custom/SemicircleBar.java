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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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
    private String online;/**设备开关机*/
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
    private int end;
    private String workMode;
    private String output;
    private int rangRadus=0;

    DeviceChild deviceChild;
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
        deviceChildDao=new DeviceChildDaoImpl(context);
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
        mReachedColor = a.getColor(R.styleable.CircleSeekBar_wheel_reached_color, getColor(R.color.color_blank4));

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
        end=a.getInt(R.styleable.CircleSeekBar_end,0);
        workMode=a.getString(R.styleable.CircleSeekBar_workMode);
        output=a.getString(R.styleable.CircleSeekBar_output);
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

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getOnline() {
        return online;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        rangRadus=width/2;

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
            mWheelPaint.setColor(getResources().getColor(R.color.color_blank4));
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
                mPaint.setColor(getResources().getColor(R.color.color_blank4));
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
                mPaint.setColor(getResources().getColor(R.color.color_blank4));
            }

            mPaint.getTextBounds(strs2[i], 0, strs2[i].length(), rect);
            canvas.drawText(strs2[i], centerX - rect.width() / 2,
                    getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()) + rect.height(), mPaint);

            canvas.rotate(53.5f, centerX, centerY);
        }
        canvas.save();


        mCurAngle = mCurProcess;


        if (mCurAngle >= 0) {
            if (mCurAngle >= 272) {
                if (mCurAngle > 272 && mCurAngle <= 330) {
                    mCurAngle = 272;
                } else if (mCurAngle > 330 && mCurAngle <= 360) {
                    mCurAngle=0;
                    return;
                }
            }
            for (int i = 0; i < (mCurAngle) / 7; i++) {
                if ((i - 1) % 7 == 0) {

                    mPaint.setColor(getResources().getColor(R.color.color_orange));

                    canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2, getResources().getDisplayMetrics()),
                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                            centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()),

                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()), mPaint);
                } else {
                    if (i == 0) {
                        mPaint.setColor(getResources().getColor(R.color.color_blank4));
                    } else {
                        mPaint.setColor(getResources().getColor(R.color.color_orange));
                    }
                    canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2, getResources().getDisplayMetrics()),
                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                            centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()),
                            getPaddingTop() + mUnreachedWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()), mPaint);
                }
                canvas.rotate(8, centerX, centerY);//360度  绘制60次   每次旋转6度
            }
            canvas.save();


        }
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

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public boolean outside=false;

    public boolean isOutside() {
        return outside;
    }

    public void setOutside(boolean outside) {
        this.outside = outside;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        end=0;
        float x = event.getX();
        float y = event.getY();




        Log.i("yyyy","-->"+y);
        int height=getHeight();
        int width=getWidth();
        Log.i("height","-->"+height);
        Log.i("width","-->"+width);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if ("open".equals(online) && "timer".equals(workMode) && !"childProtect".equals(output) && event.getAction()==MotionEvent.ACTION_DOWN){
                    Toast toast=Toast.makeText(getContext(),"定时模式下不能滑动",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                end=0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isCanTouch && (event.getAction() == MotionEvent.ACTION_MOVE || isTouch(x, y)) && isInCiecle(x,y)) {
                    // 通过当前触摸点搞到cos角度值
                    outside = false;
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
                    end = 0;
                    if (mChangListener != null && (event.getAction() & (MotionEvent.ACTION_MOVE | MotionEvent.ACTION_UP)) > 0) {

//                        if (event.getAction() == MotionEvent.ACTION_UP) {
//                            end = 1;
//                        } else {
//                            end = 0;
//                        }
                        end=0;
                        Log.i("mmm", "-->" + mUnreachedWidth);
                        Log.i("xxx", "-->" + x);
                        Log.i("yyy", "-->" + y);
                        mChangListener.onChanged(this, mCurProcess);
                        Log.i("out", "-->" + outside);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                end=0;
                if (onWheelCheckListener!=null){
                    onWheelCheckListener.onWheelCheckListener(this, mCurProcess);
                }
                break;
        }

//        if (isCanTouch && (event.getAction() == MotionEvent.ACTION_MOVE || isTouch(x, y)) && isInCiecle(x,y)){
//            end=1;
//            mChangListener.onChanged(this, mCurProcess);
//            invalidate();
//            return true;
//        }


//        } else if (isCanTouch && (event.getAction() == MotionEvent.ACTION_UP)){
//
//        }else {
//            end=0;
//            Log.i("out","-->"+outside);
//
//
//        }

        return true;
    }

    /**判断落点是否在圆环上*/
    public boolean isInCiecle(float x,float y){
        Log.i("x","-->"+x);
        Log.i("y","-->"+y);
        float distance = (float) Math.sqrt((x-rangRadus)*(x-rangRadus)+(y-rangRadus)*(y-rangRadus));
        Log.i("distance","-->"+distance);
        int smallCircleRadus=rangRadus/2+50;
        Log.i("smallCircleRadus","-->"+smallCircleRadus);
        if (distance>=smallCircleRadus && distance<=rangRadus)
            return true;
        else
            return false;
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
        try {
            if (mChangListener != null) {
                mChangListener.onChanged(this, curProcess);
            }
            refershPosition();
            invalidate();
        }catch (Exception e){
            e.printStackTrace();
        }
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
    private OnWheelCheckListener onWheelCheckListener;
    public interface OnWheelCheckListener{
        void onWheelCheckListener(SemicircleBar semicircleBar,double curValue);
    }

    public void setOnWheelCheckListener(OnWheelCheckListener onWheelCheckListener) {
        this.onWheelCheckListener = onWheelCheckListener;
    }
}