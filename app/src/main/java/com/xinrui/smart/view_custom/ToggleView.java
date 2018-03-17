package com.xinrui.smart.view_custom;

/**
 * Created by win7 on 2018/3/9.
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.xinrui.smart.R;

public class ToggleView extends View {
    String toggleText;
    float textSize;
    boolean toggleState;
    Paint txtPaint;
    Paint btnPaint;
    boolean isTouchMode=false;
    float currentX=0.0f;
    public ToggleView(Context context) {
        this(context,null);
    }

    public ToggleView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ToggleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.ToggleView);
        toggleText=typedArray.getString(R.styleable.ToggleView_toggle_text);
        textSize=typedArray.getDimension(R.styleable.ToggleView_toggle_txt_size,getResources().getDimension(R.dimen.sp_18));
        toggleState=typedArray.getBoolean(R.styleable.ToggleView_toggle_txt_state,false);
        typedArray.recycle();
    }

    /**
     * 初始化画笔
     */
    private void initPaint(){
        txtPaint=new Paint();
        btnPaint=new Paint();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX=getWidth()/2;
        int centerY=getHeight()/2;
        btnPaint.setColor(getResources().getColor(R.color.color_white));
        btnPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        btnPaint.setStrokeWidth(1);
        btnPaint.setAntiAlias(true);

        txtPaint.setStrokeWidth(0);
        txtPaint.setTextSize(textSize);
        txtPaint.setColor(getResources().getColor(R.color.color_white));
        txtPaint.setAntiAlias(true);

        float textWidth=txtPaint.measureText(getToggleText());

        if (isTouchMode){
            float newLeft=currentX-centerX/2;
            float maxLeft=centerX+centerX/2+10;
            if(newLeft<0){
                newLeft=centerX/3+1;
                canvas.drawText(toggleText,centerX+textWidth/5,centerY+textWidth/5,txtPaint);
            }else if(newLeft>0 && newLeft<maxLeft){
                canvas.drawText(toggleText,textWidth/5,centerY+textWidth/5,txtPaint);
            }else if(newLeft>centerX){
                canvas.drawText(toggleText,centerX+textWidth/5,centerY+textWidth/5,txtPaint);
                newLeft=maxLeft;
            }
            canvas.drawCircle(newLeft+2,centerY,centerX/2,btnPaint);
        }else{
            if (toggleState){
                canvas.drawText(toggleText,textWidth/5,centerY+textWidth/5,txtPaint);
                canvas.drawCircle(centerX+centerX/2+2,centerY,centerX/2,btnPaint);
            }else {
//                toggleText="车队";
                canvas.drawText(toggleText,centerX+textWidth/5,centerY+textWidth/5,txtPaint);
                canvas.drawCircle(centerX/2+2,centerY,centerX/2,btnPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isTouchMode=true;
                currentX=event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                isTouchMode=true;
                currentX=event.getX();
                break;
            case MotionEvent.ACTION_UP:
                isTouchMode=false;
                currentX=event.getX();
                float centeterX=getWidth()/2;
                boolean state=currentX>centeterX;
                if(state!=toggleState&&onUpdateToggleListener!=null){
                    onUpdateToggleListener.onUpdateListener(state);
                }
                toggleState=state;
        }
        invalidate();
        return true;
    }

    public void setToggleText(String toggleText) {
        this.toggleText = toggleText;
    }

    public String getToggleText() {
        return toggleText;
    }

    public interface OnUpdateToggleListener{
        public void onUpdateListener(boolean state);
    }
    private OnUpdateToggleListener onUpdateToggleListener;

    public void setOnUpdateToggleListener(OnUpdateToggleListener onUpdateToggleListener) {
        this.onUpdateToggleListener = onUpdateToggleListener;
    }
}
