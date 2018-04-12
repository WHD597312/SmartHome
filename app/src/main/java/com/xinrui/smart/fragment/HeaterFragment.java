package com.xinrui.smart.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.view_custom.SemicircleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/20.
 */

public class HeaterFragment extends Fragment {
    public String TAG="HeaterFragment";
    View view;
    Unbinder unbinder;
    @BindView(R.id.img_circle) ImageView img_circle;
    @BindView(R.id.image_switch) ImageView image_switch;
    @BindView(R.id.semicBar) SemicircleBar semicBar;
    @BindView(R.id.tv_set_temp) TextView tv_set_temp;/**设定温度*/
    @BindView(R.id.image_mode2) ImageView model_protect;/**保护模式*/
    @BindView(R.id.image_mode) ImageView image_hand_task;/**手动，定时模式*/
    @BindView(R.id.image_mode3) ImageView image_lock;/**锁定模式*/
    @BindView(R.id.image_mode4) ImageView image_srceen;/**屏幕模式*/
    @BindView(R.id.tv_mode) TextView tv_mode;/**模式文本*/
    @BindView(R.id.image_temp) ImageView image_temp;/**模式图标*/
    private int mCurrent=5;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_heater,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        semicBar.setModule("1");
        semicBar.setSlide(false);
        semicBar.setOnSeekBarChangeListener(new SemicircleBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(SemicircleBar seekbar, double curValue) {
                String open= (String) image_switch.getTag();
                String module=semicBar.getModule();
                if ("开".equals(open)){
                    Log.i(TAG,"-->"+seekbar.getmCurAngle());
                    double curAngle=semicBar.getmCurAngle();
                    mCurrent=(int) curAngle/8+8;
                    if (curAngle>272 && curAngle<=310){
                        if ("1".equals(module)){
                            mCurrent=42;
                        }else if ("2".equals(module)){
                            mCurrent=60;
                        }
                    }else if (curAngle>= 310 && curAngle<=360){
                        if ("1".equals(module)){
                            mCurrent=5;
                        }else if ("2".equals(module)){
                            mCurrent=48;
                        }
                    }
                    Message msg=handler.obtainMessage();
                    msg.arg1=1;
                    msg.what=mCurrent;
                    handler.sendMessage(msg);
                }else {
                    mCurrent=0;
                    Message msg=handler.obtainMessage();
                    msg.arg1=4;
                    msg.what=mCurrent;
                    handler.sendMessage(msg);
                }

            }
        });
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1){
                case 1:
                    int temp=msg.what;
                    tv_set_temp.setText(temp+"℃");
                    break;
                case 2:
                    semicBar.setmCurAngle(0);
                    semicBar.invalidate();
                    break;
                case 3:
                    semicBar.setmCurAngle(0);
                    semicBar.invalidate();
                    break;
                case 4:
//                    semicBar.setmCurAngle(0);
//                    semicBar.invalidate();
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        image_switch.setTag("关");
        image_hand_task.setTag("定时");
        model_protect.setTag("不保护");
        image_lock.setTag("上锁");
        image_srceen.setTag("屏保关");
    }

    @OnClick({R.id.image_switch,R.id.image_mode2,R.id.image_mode,R.id.image_mode3,R.id.image_mode4})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_switch:
                img_circle.setImageResource(R.drawable.lottery_animlist);
                AnimationDrawable animationDrawable = (AnimationDrawable) img_circle.getDrawable();
                String open= (String) image_switch.getTag();
                if ("关".equals(open)){
                   image_switch.setTag("开");
                   animationDrawable.start();
                    semicBar.setSlide(true);
                }else {
                    image_switch.setTag("关");
                    animationDrawable.stop();
                    semicBar.setSlide(false);
                }

                break;
            case R.id.image_mode:
                String tag= (String) image_hand_task.getTag();
                if ("定时".equals(tag)){
                    image_hand_task.setImageResource(R.mipmap.module_task);
                    image_hand_task.setTag("手动");
                    tv_mode.setText("手动");
                }else {
                    image_hand_task.setImageResource(R.mipmap.module_handle);
                    image_hand_task.setTag("定时");
                    tv_mode.setText("定时");
                }
                setModuleBack(image_hand_task);
                String s= (String) model_protect.getTag();
                if ("保护".equals(s)){
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                }else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                break;
            case R.id.image_mode2:
                String tag2= (String) model_protect.getTag();
               if ("不保护".equals(tag2)){
                   model_protect.setTag("保护");
                   tv_mode.setText("保护");
                   model_protect.setBackgroundResource(R.mipmap.img_temp_circle);
               }else {
                   model_protect.setTag("不保护");
                   tv_mode.setText("定时");
                   model_protect.setBackgroundResource(0);
               }
                setModuleBack( model_protect);
               String s2= (String) model_protect.getTag();
                if ("保护".equals(s2)){
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                }else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                break;
            case R.id.image_mode3:
                String tag3= (String) image_lock.getTag();
                if ("上锁".equals(tag3)){
                    image_lock.setTag("解锁");
                    tv_mode.setText("解锁");
                    image_lock.setBackgroundResource(R.mipmap.img_temp_circle);
                }else {
                    image_lock.setTag("上锁");
                    tv_mode.setText("上锁");
                    image_lock.setBackgroundResource(0);
                }
                setModuleBack(image_lock);
                String s3= (String) model_protect.getTag();
                if ("保护".equals(s3)){
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                }else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                break;
            case R.id.image_mode4:
                String tag4= (String) image_srceen.getTag();
                if ("屏保关".equals(tag4)){
                    image_srceen.setTag("屏保开");
                    tv_mode.setText("屏保开");
                    image_srceen.setBackgroundResource(R.mipmap.img_temp_circle);
                }else {
                    image_srceen.setTag("屏保关");
                    tv_mode.setText("屏保关");
                    image_srceen.setBackgroundResource(0);
                }
                setModuleBack(image_srceen);
                String s4= (String) model_protect.getTag();
                if ("保护".equals(s4)){
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                }else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                break;
        }
    }
    /**设置背景*/
    private void setModuleBack(ImageView view){
                if (view.getTag().equals("保护")){

                    semicBar.setModule("2");
                    mCurrent=48;
                    tv_set_temp.setText(mCurrent+"℃");
                    Message msg=handler.obtainMessage();
                    msg.arg1=2;
                    handler.sendMessage(msg);
                }else {
                    semicBar.setModule("1");
                    mCurrent=5;
                    tv_set_temp.setText(mCurrent+"℃");
                    Message msg=handler.obtainMessage();
                    msg.arg1=3;
                    handler.sendMessage(msg);
                }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
