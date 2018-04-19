package com.xinrui.smart.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.SemicircleBar;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/20.
 */

public class HeaterFragment extends Fragment {
    public String TAG = "HeaterFragment";
    View view;
    Unbinder unbinder;
    @BindView(R.id.img_circle)
    ImageView img_circle;
    @BindView(R.id.image_switch)
    ImageView image_switch;
    @BindView(R.id.semicBar)
    SemicircleBar semicBar;
    @BindView(R.id.tv_set_temp)
    TextView tv_set_temp;/**设定温度*/
    @BindView(R.id.tv_cur_temp)
    TextView tv_cur_temp;/**当前温度*/
    /**
     * 设定温度
     */
    @BindView(R.id.image_mode2)
    ImageView model_protect;
    /**
     * 保护模式
     */
    @BindView(R.id.image_mode)
    ImageView image_hand_task;
    /**
     * 手动，定时模式
     */
    @BindView(R.id.image_mode3)
    ImageView image_lock;
    /**
     * 锁定模式
     */
    @BindView(R.id.image_mode4)
    ImageView image_srceen;
    /**
     * 屏幕模式
     */
    @BindView(R.id.tv_mode)
    TextView tv_mode;
    /**
     * 模式文本
     */
    @BindView(R.id.image_temp)
    ImageView image_temp;
    /**
     * 模式图标
     */
    private int mCurrent = 5;
    public static boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_heater, container, false);
        unbinder = ButterKnife.bind(this, view);
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
                String handTask = (String) image_hand_task.getTag();
                if ("定时".equals(handTask)) {
                    mCurrent = 0;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 4;
                    msg.what = mCurrent;
                    handler.sendMessage(msg);
                } else {
                    String open = (String) image_switch.getTag();
                    String module = semicBar.getModule();
                    if ("开".equals(open)) {
                        Log.i(TAG, "-->" + seekbar.getmCurAngle());
                        double curAngle = semicBar.getmCurAngle();
                        if ("1".equals(module)){
                            if (curAngle > 272 && curAngle <= 310){
                                mCurrent = 42;
                            }else if (curAngle >= 310 && curAngle <= 360){
                                mCurrent = 5;
                            }else {
                                mCurrent = (int) curAngle / 8 + 8;
                            }
                        }else if ("2".equals(module)){
                            if (curAngle > 272 && curAngle <= 310){
                                mCurrent = 60;
                            }else if ((curAngle >= 310 && curAngle <= 360)){
                                mCurrent = 48;
                            }else {
                                int mcurAngle= (int) curAngle;
                                if (curAngle==0){
                                    mCurrent=48;
                                }else if (curAngle>=0 && curAngle<=35){
                                    mCurrent=49;
                                }else if (curAngle>35 && curAngle<=60){
                                    mCurrent=50;
                                }else if (curAngle>60 && curAngle<=80){
                                    mCurrent=51;
                                }else if (curAngle>80 && curAngle<=90){
                                    mCurrent=52;
                                }else if (curAngle>90 && curAngle<=112){
                                    mCurrent=53;
                                }else if (curAngle>112 && curAngle<=128){
                                    mCurrent=54;
                                } else if (curAngle>128 && curAngle<=160){
                                    mCurrent=55;
                                }else if (curAngle>160 && curAngle<=176){
                                    mCurrent=56;
                                }else if (curAngle>176 && curAngle<=208){
                                    mCurrent=57;
                                }else if (curAngle>208 && curAngle<=224){
                                    mCurrent=58;
                                }else if (curAngle>224 && curAngle<=240){
                                    mCurrent=59;
                                }else if (curAngle>260 && curAngle<=272){
                                    mCurrent=60;
                                }
                                tv_set_temp.setText(mCurrent + "℃");
                            }
                        }

                        Message msg = handler.obtainMessage();
                        msg.arg1 = 1;
                        msg.what = mCurrent;
                        handler.sendMessage(msg);
                        if (deviceChild != null) {
                            deviceChild.setMatTemp(mCurrent);
                            if ("2".equals(module)) {
                                deviceChild.setProtectSetTemp(mCurrent);
                            }
                            send(deviceChild);
                        }
                    } else {
                        return;
                    }
                }

            }
        });


    }
    public void send(DeviceChild deviceChild) {
        try {
            if (deviceChild != null) {
                JSONObject maser = new JSONObject();
                int type = deviceChild.getType();
                String ctrlMode = "normal";
                String workMode = "manual";
                String LockScreen = "open";
                String deviceState = "close";
                String BackGroundLED = "open";
                String outputMode = "fastHeat";


                int controlled = deviceChild.getControlled();
                if (type == 1 && controlled == 2) {
                    ctrlMode = "master";
                } else if (type == 1 && controlled == 1) {
                    ctrlMode = "slave";
                } else {
                    ctrlMode = "normal";
                }

                String tag = (String) image_hand_task.getTag();
                if ("定时".equals(tag)) {
                    workMode = "timer";
                } else {
                    workMode = "manual";
                }


                String tag2 = (String) model_protect.getTag();
                if ("保护".equals(tag2)) {
                    outputMode = "childProtect";
                } else {
                    outputMode = "fastHeat";
                }

                String tag3 = (String) image_lock.getTag();
                if ("上锁".equals(tag3)) {
                    LockScreen = "open";
                } else {
                    LockScreen = "close";
                }
                String open = (String) image_switch.getTag();
                if ("关".equals(open)) {
                    deviceState = "close";
                } else {
                    deviceState = "open";
                }
                String tag4 = (String) image_srceen.getTag();
                if ("屏保关".equals(tag4)) {
                    BackGroundLED = "close";
                } else {
                    BackGroundLED = "open";
                }

                deviceChild.setCtrlMode(ctrlMode);
                deviceChild.setWorkMode(workMode);
                deviceChild.setLockScreen(LockScreen);
                deviceChild.setDeviceState(deviceState);
                deviceChild.setBackGroundLED(BackGroundLED);
                deviceChild.setOutputMod(outputMode);

                maser.put("ctrlMode", deviceChild.getCtrlMode());
                maser.put("workMode", deviceChild.getWorkMode());
                maser.put("MatTemp", deviceChild.getMatTemp());
                maser.put("LockScreen", deviceChild.getLockScreen());
                maser.put("BackGroundLED", deviceChild.getBackGroundLED());
                maser.put("deviceState", deviceChild.getDeviceState());
                maser.put("tempState", deviceChild.getTempState());
                maser.put("outputMode", deviceChild.getOutputMod());
                maser.put("MatTemp", deviceChild.getMatTemp());
                maser.put("protectProTemp", deviceChild.getProtectProTemp());
                maser.put("protectSetTemp", deviceChild.getProtectSetTemp());

                deviceChildDao.update(deviceChild);
                String s = maser.toString();
                boolean success = false;
                String topicName;
                String mac = deviceChild.getMacAddress();
                if (deviceChild.getType() == 1 && deviceChild.getControlled() == 2) {
                    topicName = "rango/" + mac + "/masterController/set";
                    if (bound) {
                        success = mqService.publish(topicName, 2, s);
                    }

                } else {
                    topicName = "rango/" + mac + "/set";
                    if (bound) {
                        success = mqService.publish(topicName, 2, s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MQService mqService;
    private boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
    private int sum=0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 1:
                    int mCurrent=msg.what;
                    int mCurrentAngle2=0;
                    String module2 = semicBar.getModule();

                    if ("1".equals(module2)){
                        mCurrentAngle2=(mCurrent-3)*7;
                    } else if ("2".equals(module2)){
                        if (mCurrent==48){
                            mCurrentAngle2=0;
                        }else if (mCurrent==49){
                            mCurrentAngle2=35;
                        }else if (mCurrent==50){
                            mCurrentAngle2=60;
                        }else if (mCurrent==51){
                            mCurrentAngle2=80;
                        }else if (mCurrent==52){
                            mCurrentAngle2=90;
                        }else if (mCurrent==53){
                            mCurrentAngle2=112;
                        }else if (mCurrent==54){
                            mCurrentAngle2=128;
                        } else if (mCurrent==55){
                            mCurrentAngle2=160;
                        }else if (mCurrent==56){
                            mCurrentAngle2=176;
                        }else if (mCurrent==57){
                            mCurrentAngle2=208;
                        }else if (mCurrent==58){
                            mCurrentAngle2=224;
                        }else if (mCurrent==59){
                            mCurrentAngle2=240;
                        }else if (mCurrent==60){
                            mCurrentAngle2=272;
                        }

                    }
                    semicBar.setmCurAngle(mCurrentAngle2);
                    semicBar.invalidate();
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
                case 5:
                    int temp2 = msg.what;
                    semicBar.setmCurAngle(temp2);
                    semicBar.invalidate();
                    break;
                case 6:
                    int mCurrent6=msg.what;
                    int mCurrentAngle=0;
                    String module6 = semicBar.getModule();

                    if ("1".equals(module6)){
                        mCurrentAngle=(mCurrent6-3)*7;
                    }else if ("2".equals(module6)){
                        if (mCurrent6==48){
                            mCurrentAngle=0;
                        }else if (mCurrent6==49){
                            mCurrentAngle=35;
                        }else if (mCurrent6==50){
                            mCurrentAngle=60;
                        }else if (mCurrent6==51){
                            mCurrentAngle=80;
                        }else if (mCurrent6==52){
                            mCurrentAngle=90;
                        }else if (mCurrent6==53){
                            mCurrentAngle=112;
                        }else if (mCurrent6==54){
                            mCurrentAngle=128;
                        } else if (mCurrent6==55){
                            mCurrentAngle=160;
                        }else if (mCurrent6==56){
                            mCurrentAngle=176;
                        }else if (mCurrent6==57){
                            mCurrentAngle=208;
                        }else if (mCurrent6==58){
                            mCurrentAngle=224;
                        }else if (mCurrent6==59){
                            mCurrentAngle=240;
                        }else if (mCurrent6==60){
                            mCurrentAngle=272;
                        }
                    }
                    semicBar.setmCurAngle(mCurrentAngle);
                    semicBar.invalidate();
                    break;
                case 8:
                    int mCurrent8=msg.what;
                    int mCurrentAngle8=0;
                    String module8 = semicBar.getModule();
                    if ("1".equals(module8)){
                        mCurrentAngle8=(mCurrent8-3)*7;
                    }else if ("2".equals(module8)){
                        if (mCurrent8==48){
                            mCurrentAngle8=0;
                        }else if (mCurrent8==49){
                            mCurrentAngle8=35;
                        }else if (mCurrent8==50){
                            mCurrentAngle8=60;
                        }else if (mCurrent8==51){
                            mCurrentAngle8=80;
                        }else if (mCurrent8==52){
                            mCurrentAngle8=90;
                        }else if (mCurrent8==53){
                            mCurrentAngle8=112;
                        }else if (mCurrent8==54){
                            mCurrentAngle8=128;
                        } else if (mCurrent8==55){
                            mCurrentAngle8=160;
                        }else if (mCurrent8==56){
                            mCurrentAngle8=176;
                        }else if (mCurrent8==57){
                            mCurrentAngle8=208;
                        }else if (mCurrent8==58){
                            mCurrentAngle8=224;
                        }else if (mCurrent8==59){
                            mCurrentAngle8=240;
                        }else if (mCurrent8==60){
                            mCurrentAngle8=272;
                        }
                    }
                    semicBar.setmCurrentAngle(mCurrentAngle8);
                    semicBar.invalidate();
                    break;

            }
        }
    };

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            long deviceId=intent.getLongExtra("deviceId",0);
//            DeviceChild deviceChild = deviceChildDao.findDeviceChild(deviceId);
            DeviceChild deviceChild= (DeviceChild) intent.getSerializableExtra("deviceChild");
            if (deviceChild != null) {
                String wifiVersion = deviceChild.getWifiVersion();
                String MCUVerion = deviceChild.getMCUVerion();

                int MatTemp = deviceChild.getMatTemp();


                String workMode = deviceChild.getWorkMode();
                String LockScreen = deviceChild.getLockScreen();
                String BackGroundLED = deviceChild.getBackGroundLED();
                String deviceState = deviceChild.getDeviceState();
                String tempState = deviceChild.getTempState();

                String outputMode = deviceChild.getOutputMod();
                int curTemp = deviceChild.getCurTemp();
                int ratedPower = deviceChild.getRatedPower();
                String protectEnable = deviceChild.getProtectEnable();
                String ctrlMode = deviceChild.getCtrlMode();
                int powerValue = deviceChild.getPowerValue();
                int voltageValue = deviceChild.getVoltageValue();
                int currentValue = deviceChild.getCurrentValue();
                String machineFall = deviceChild.getMachineFall();
                int protectSetTemp = deviceChild.getProtectSetTemp();
                int protectProTemp = deviceChild.getProtectProTemp();
                img_circle.setImageResource(R.drawable.lottery_animlist);
                AnimationDrawable animationDrawable = (AnimationDrawable) img_circle.getDrawable();


                if ("open".equals(deviceState)) {
                    image_switch.setTag("开");
                    animationDrawable.start();
                    deviceChild.setDeviceState("open");
                    send(deviceChild);
                    semicBar.setSlide(true);
                } else if ("close".equals(deviceState)) {
                    image_switch.setTag("关");
                    animationDrawable.stop();
                    semicBar.setSlide(false);

                    mCurrent = deviceChild.getMatTemp();
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 5;
                    msg.what = mCurrent;
                    handler.sendMessage(msg);

                    deviceChild = deviceChildDao.findDeviceById(deviceChild.getId());
                    deviceChild.setDeviceState("close");
                    send(deviceChild);
                }
                if ("manual".equals(workMode)) {
                    image_hand_task.setImageResource(R.mipmap.module_task);
                    image_hand_task.setTag("手动");
                    tv_mode.setText("手动");
                } else if ("timer".equals(workMode)) {
                    image_hand_task.setImageResource(R.mipmap.module_handle);
                    image_hand_task.setTag("定时");
                    tv_mode.setText("定时");
                }
                if (!Utils.isEmpty(workMode)){
                    setModuleBack(image_hand_task);
                    String s = (String) model_protect.getTag();
                    if ("保护".equals(s)) {
                        image_temp.setImageResource(R.mipmap.img_protect_open);
                    } else {
                        image_temp.setImageResource(R.mipmap.img_cur_temp);
                    }
                }

                if ("open".equals(LockScreen)) {
                    image_lock.setTag("解锁");
                    tv_mode.setText("解锁");
                    image_lock.setBackgroundResource(R.mipmap.img_temp_circle);
                } else if ("close".equals(LockScreen)) {
                    image_lock.setTag("上锁");
                    tv_mode.setText("上锁");
                    image_lock.setBackgroundResource(0);
                }
                setModuleBack(image_lock);
                String s3 = (String) model_protect.getTag();
                if ("保护".equals(s3)) {
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                } else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }

                if ("open".equals(BackGroundLED)) {
                    image_srceen.setTag("屏保开");
                    tv_mode.setText("屏保开");
                    image_srceen.setBackgroundResource(R.mipmap.img_temp_circle);
                } else if ("close".equals(BackGroundLED)) {
                    image_srceen.setTag("屏保关");
                    tv_mode.setText("屏保关");
                    image_srceen.setBackgroundResource(0);
                }
                setModuleBack(image_srceen);
                String s4 = (String) model_protect.getTag();
                if ("保护".equals(s4)) {
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                } else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                if ("childProtect".equals(outputMode)) {
                    model_protect.setTag("保护");
                    tv_mode.setText("保护");
                    model_protect.setBackgroundResource(R.mipmap.img_temp_circle);
                } else {
                    model_protect.setTag("不保护");
                    tv_mode.setText("定时");
                    model_protect.setBackgroundResource(0);
                }
                setModuleBack(model_protect);
                String s2 = (String) model_protect.getTag();
                if ("保护".equals(s2)) {
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                } else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }

                if (MatTemp>0){
                    tv_set_temp.setText(MatTemp + "℃");
                    Message msg=handler.obtainMessage();
                    msg.arg1=6;
                    msg.what=MatTemp;
                    handler.sendMessage(msg);
                }
                if ("err".equals(tempState)){
                    tv_cur_temp.setText(MatTemp + "℃");
                    tv_cur_temp.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
                }
            }

        }
    }

    private MessageReceiver receiver;
    private DeviceChild deviceChild;
    private DeviceChildDaoImpl deviceChildDao;

    @Override
    public void onResume() {
        super.onResume();
        deviceChildDao = new DeviceChildDaoImpl(getActivity());
        Bundle bundle = getArguments();
        String childPosiotn = bundle.getString("deviceId");
        if (!Utils.isEmpty(childPosiotn)) {
            int deviceId = Integer.parseInt(childPosiotn);
            deviceChild = deviceChildDao.findDeviceById(deviceId);
        }

        Intent intent = new Intent(getActivity(), MQService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter("HeaterFragment");
        receiver = new MessageReceiver();
        getActivity().registerReceiver(receiver, intentFilter);


        running = true;
        image_switch.setTag("关");
        image_hand_task.setTag("手动");
        model_protect.setTag("不保护");
        image_lock.setTag("上锁");
        image_srceen.setTag("屏保关");

        if (deviceChild != null) {
            String wifiVersion = deviceChild.getWifiVersion();
            String MCUVerion = deviceChild.getMCUVerion();

            int MatTemp = deviceChild.getMatTemp();


            String workMode = deviceChild.getWorkMode();
            String LockScreen = deviceChild.getLockScreen();
            String BackGroundLED = deviceChild.getBackGroundLED();
            String deviceState = deviceChild.getDeviceState();
            String tempState = deviceChild.getTempState();

            String outputMode = deviceChild.getOutputMod();
            int curTemp = deviceChild.getCurTemp();
            int ratedPower = deviceChild.getRatedPower();
            String protectEnable = deviceChild.getProtectEnable();
            String ctrlMode = deviceChild.getCtrlMode();
            int powerValue = deviceChild.getPowerValue();
            int voltageValue = deviceChild.getVoltageValue();
            int currentValue = deviceChild.getCurrentValue();
            String machineFall = deviceChild.getMachineFall();
            int protectSetTemp = deviceChild.getProtectSetTemp();
            int protectProTemp = deviceChild.getProtectProTemp();
            img_circle.setImageResource(R.drawable.lottery_animlist);
            AnimationDrawable animationDrawable = (AnimationDrawable) img_circle.getDrawable();


            if ("open".equals(deviceState)) {
                image_switch.setTag("开");
                animationDrawable.start();
                semicBar.setSlide(true);
            } else if ("close".equals(deviceState)) {
                image_switch.setTag("关");
                animationDrawable.stop();
                semicBar.setSlide(false);
            }
            if ("manual".equals(workMode)) {
                image_hand_task.setImageResource(R.mipmap.module_task);
                image_hand_task.setTag("手动");
                tv_mode.setText("手动");
            } else if ("timer".equals(workMode)) {
                image_hand_task.setImageResource(R.mipmap.module_handle);
                image_hand_task.setTag("定时");
                tv_mode.setText("定时");
            }

            if ("childProtect".equals(outputMode)) {
                model_protect.setTag("保护");
                tv_mode.setText("保护");
                image_temp.setImageResource(R.mipmap.img_protect_open);
                model_protect.setBackgroundResource(R.mipmap.img_temp_circle);
                semicBar.setModule("2");
                mCurrent = 48;
                tv_set_temp.setText(mCurrent + "℃");
                Message msg = handler.obtainMessage();
                msg.arg1 = 2;
                handler.sendMessage(msg);
            } else {
                semicBar.setModule("1");
                mCurrent = 5;
                tv_set_temp.setText(mCurrent + "℃");
                Message msg = handler.obtainMessage();
                msg.arg1 = 3;
                handler.sendMessage(msg);
            }

//            if (!Utils.isEmpty(workMode)){
//                setModuleBack(image_hand_task);
//                String s = (String) model_protect.getTag();
//                if ("保护".equals(s)) {
//                    image_temp.setImageResource(R.mipmap.img_protect_open);
//                } else {
//                    image_temp.setImageResource(R.mipmap.img_cur_temp);
//                }
//            }
//
            if ("open".equals(LockScreen)) {
                image_lock.setTag("解锁");
                tv_mode.setText("解锁");
                image_lock.setBackgroundResource(R.mipmap.img_temp_circle);
            } else if ("close".equals(LockScreen)) {
                image_lock.setTag("上锁");
                tv_mode.setText("上锁");
                image_lock.setBackgroundResource(0);
            }
//            setModuleBack(image_lock);
//            String s3 = (String) model_protect.getTag();
//            if ("保护".equals(s3)) {
//                image_temp.setImageResource(R.mipmap.img_protect_open);
//            } else {
//                image_temp.setImageResource(R.mipmap.img_cur_temp);
//            }
//
            if ("open".equals(BackGroundLED)) {
                image_srceen.setTag("屏保开");
                tv_mode.setText("屏保开");
                image_srceen.setBackgroundResource(R.mipmap.img_temp_circle);
            } else if ("close".equals(BackGroundLED)) {
                image_srceen.setTag("屏保关");
                tv_mode.setText("屏保关");
                image_srceen.setBackgroundResource(0);
            }

            if (MatTemp>0){
                tv_set_temp.setText(MatTemp + "℃");
                Message msg=handler.obtainMessage();
                msg.arg1=6;
                msg.what=MatTemp;
                handler.sendMessage(msg);
            }
            if ("err".equals(tempState)){
                tv_cur_temp.setText(MatTemp + "℃");
                tv_cur_temp.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        running = false;
    }

    @OnClick({R.id.image_switch, R.id.image_mode2, R.id.image_mode, R.id.image_mode3, R.id.image_mode4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_switch:
                img_circle.setImageResource(R.drawable.lottery_animlist);
                AnimationDrawable animationDrawable = (AnimationDrawable) img_circle.getDrawable();
                String open = (String) image_switch.getTag();
                if ("关".equals(open)) {
                    image_switch.setTag("开");
                    animationDrawable.start();
                    deviceChild.setDeviceState("open");
                    send(deviceChild);
                    semicBar.setSlide(true);
                } else {
                    image_switch.setTag("关");
                    animationDrawable.stop();
                    semicBar.setSlide(false);

                    mCurrent = deviceChild.getMatTemp();
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 5;
                    msg.what = mCurrent;
                    handler.sendMessage(msg);

                    deviceChild = deviceChildDao.findDeviceById(deviceChild.getId());
                    deviceChild.setDeviceState("close");
                    send(deviceChild);
                }
                break;
            case R.id.image_mode:
                String tag = (String) image_hand_task.getTag();
                if ("定时".equals(tag)) {
                    image_hand_task.setImageResource(R.mipmap.module_task);
                    image_hand_task.setTag("手动");
                    tv_mode.setText("手动");
                } else {
                    image_hand_task.setImageResource(R.mipmap.module_handle);
                    image_hand_task.setTag("定时");
                    tv_mode.setText("定时");
                }
                setModuleBack(image_hand_task);
                String s = (String) model_protect.getTag();
                if ("保护".equals(s)) {
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                } else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                break;
            case R.id.image_mode2:
                String tag2 = (String) model_protect.getTag();
                if ("不保护".equals(tag2)) {
                    model_protect.setTag("保护");
                    tv_mode.setText("保护");
                    model_protect.setBackgroundResource(R.mipmap.img_temp_circle);
                } else {
                    model_protect.setTag("不保护");
                    tv_mode.setText("定时");
                    model_protect.setBackgroundResource(0);
                }
                setModuleBack(model_protect);
                String s2 = (String) model_protect.getTag();
                if ("保护".equals(s2)) {
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                } else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                break;
            case R.id.image_mode3:
                String tag3 = (String) image_lock.getTag();
                if ("上锁".equals(tag3)) {
                    image_lock.setTag("解锁");
                    tv_mode.setText("解锁");
                    image_lock.setBackgroundResource(R.mipmap.img_temp_circle);
                } else {
                    image_lock.setTag("上锁");
                    tv_mode.setText("上锁");
                    image_lock.setBackgroundResource(0);
                }
                setModuleBack(image_lock);
                String s3 = (String) model_protect.getTag();
                if ("保护".equals(s3)) {
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                } else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                break;
            case R.id.image_mode4:
                String tag4 = (String) image_srceen.getTag();
                if ("屏保关".equals(tag4)) {
                    image_srceen.setTag("屏保开");
                    tv_mode.setText("屏保开");
                    image_srceen.setBackgroundResource(R.mipmap.img_temp_circle);
                } else {
                    image_srceen.setTag("屏保关");
                    tv_mode.setText("屏保关");
                    image_srceen.setBackgroundResource(0);
                }
                setModuleBack(image_srceen);
                String s4 = (String) model_protect.getTag();
                if ("保护".equals(s4)) {
                    image_temp.setImageResource(R.mipmap.img_protect_open);
                } else {
                    image_temp.setImageResource(R.mipmap.img_cur_temp);
                }
                image_srceen.setTag("屏保开");
                tv_mode.setText("屏保开");
                break;
        }
    }

    /**
     * 设置背景
     */
    private void setModuleBack(ImageView view) {
        if (view.getTag().equals("保护")) {
            semicBar.setModule("2");
            mCurrent = 48;
            tv_set_temp.setText(mCurrent + "℃");
            Message msg = handler.obtainMessage();
            msg.arg1 = 2;
            handler.sendMessage(msg);
        } else {
            semicBar.setModule("1");
            mCurrent = 5;
            tv_set_temp.setText(mCurrent + "℃");
            Message msg = handler.obtainMessage();
            msg.arg1 = 3;
            handler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            getActivity().unbindService(connection);
        }
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
    }
}
