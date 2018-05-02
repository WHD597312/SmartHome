package com.xinrui.secen.scene_view_custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.xinrui.secen.scene_util.ToastUtils;
import com.xinrui.smart.R;

/**
 * Created by pc on 2017/8/24.
 */

public class ShowPopup extends PopupWindow {
    View view;
    Context context;
    public ShowPopup(Context context) {
        this(context,null);
    }

    public ShowPopup(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ShowPopup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
this.context = context;
        initView();
        this.setContentView(view);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(false);
        this.setOutsideTouchable(false);

        this.update();

        setBackgroundDrawable(new ColorDrawable());

    }
    private void initView() {
        view = LayoutInflater.from(((Activity)context)).inflate(R.layout.popup, null);
        Button button = (Button) view.findViewById(R.id.iv1);


    }
    public class click implements View.OnClickListener{
        int c;
        public click(int i){
            c = i;
        }
        @Override
        public void onClick(View v) {
            ToastUtils.showToast(context,"我点击了菜单");
        }
    }

    public void showPopup(View view,int x,int y){
        showAsDropDown(view,x,y);
        WindowManager.LayoutParams attributes = ((Activity) context).getWindow().getAttributes();
        attributes.alpha = 0.7f;
        ((Activity) context).getWindow().setAttributes(attributes);

    }
    public void dissPopup(){
        dismiss();
        WindowManager.LayoutParams attributes = ((Activity) context).getWindow().getAttributes();
        attributes.alpha = 1.0f;
        ((Activity) context).getWindow().setAttributes(attributes);
    }
}
