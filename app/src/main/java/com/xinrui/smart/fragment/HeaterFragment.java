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
    @BindView(R.id.tv_set_temp) TextView tv_set_temp;
    @BindView(R.id.image_mode2) ImageView model_protect;/**保护模式*/
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
        semicBar.setOnSeekBarChangeListener(new SemicircleBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(SemicircleBar seekbar, double curValue) {
                String module=semicBar.getModule();
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
            }

        }
    };
    boolean flag=true;
    @OnClick({R.id.image_switch,R.id.image_mode2})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.image_switch:
                img_circle.setImageResource(R.drawable.lottery_animlist);
                AnimationDrawable animationDrawable = (AnimationDrawable) img_circle.getDrawable();

//                Animation circle_anim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_round_rotate);
//                LinearInterpolator interpolator = new LinearInterpolator();  //设置匀速旋转，在xml文件中设置会出现卡顿
//                circle_anim.setInterpolator(interpolator);
                if (flag){
                    if (img_circle!=null){
                        animationDrawable.start();
                        flag=false;
                    }
                }else {
                    if (img_circle!=null){
                        animationDrawable.stop();
                        flag=true;
                    }
                }
                break;
            case R.id.image_mode2:
                semicBar.setModule("2");
                mCurrent=48;
                tv_set_temp.setText(mCurrent+"℃");
                Message msg=handler.obtainMessage();
                msg.arg1=2;
                handler.sendMessage(msg);
                break;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
