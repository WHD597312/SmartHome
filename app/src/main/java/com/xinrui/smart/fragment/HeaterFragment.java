package com.xinrui.smart.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeTaskDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.util.ChineseNumber;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.SemicircleBar;

import org.json.JSONObject;

import java.sql.Time;
import java.util.Calendar;
import java.util.List;

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
    TextView tv_set_temp;
    /**
     * 设定温度
     */
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

    @BindView(R.id.tv_outmode)
    TextView tv_outmode;
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
     * 模式文本
     */
    @BindView(R.id.image_temp)
    ImageView image_temp;
    /**
     * 模式图标
     */
    @BindView(R.id.relative4) RelativeLayout relative4;/**开机*/
    @BindView(R.id.relative5) RelativeLayout relative5;/**关机*/
    private int mCurrent = 5;
    public static boolean running = false;

    private TimeTaskDaoImpl timeTaskDao;


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
        semicBar.setCanTouch(false);


        semicBar.setOnSeekBarChangeListener(new SemicircleBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(SemicircleBar seekbar, double curValue) {

                String handTask = (String) image_hand_task.getTag();

                String open = (String) image_switch.getTag();
                String module = semicBar.getModule();

                Log.i(TAG, "-->" + seekbar.getmCurAngle());
                double curAngle = semicBar.getmCurAngle();
                if ("1".equals(module)) {
                    if (curAngle > 272 && curAngle <= 310) {
                        mCurrent = 42;
                    } else if (curAngle >= 310 && curAngle <= 360) {
                        mCurrent = 5;
                    } else {
                        mCurrent = (int) curAngle / 7 + 3;
                    }
                    String deviceState=deviceChild.getDeviceState();
                    if ("close".equals(deviceState)){
                        mCurrent=0;
                        Message msg=handler.obtainMessage();
                        msg.arg1=2;
                        msg.what=mCurrent;
                        handler.sendMessage(msg);
                        tv_set_temp.setText("--" + "℃");
                        tv_outmode.setText("");

                    }else if ("open".equals(deviceState)){
                        String workMode=deviceChild.getWorkMode();
                        if ("manual".equals(workMode)){
                            deviceChild.setManualMatTemp(mCurrent);
                            deviceChildDao.update(deviceChild);
                        }else {
                            deviceChild.setTimerTemp(mCurrent);
                            deviceChildDao.update(deviceChild);
                        }

                        tv_set_temp.setText(mCurrent + "℃");
                        int curTemp = deviceChild.getCurTemp();
                        if (mCurrent >= (curTemp + 3)) {
                            tv_outmode.setText("速热模式");
                        } else if (curTemp >= (mCurrent + 3)) {
                            tv_outmode.setText("保温模式");
                        } else {
                            tv_outmode.setText("节能模式");
                        }


                    }

                } else if ("2".equals(module)) {
                    String deviceState=deviceChild.getDeviceState();
                    if ("close".equals(deviceState)){
                        mCurrent=0;
                        Message msg=handler.obtainMessage();
                        msg.arg1=2;
                        msg.what=mCurrent;
                        handler.sendMessage(msg);
                        tv_set_temp.setText("--" + "℃");
                        tv_outmode.setText("");
                    }else if ("open".equals(deviceState)){
                        if (curAngle > 272 && curAngle <= 310) {
                            mCurrent = 60;
                        } else if ((curAngle >= 310 && curAngle <= 360)) {
                            mCurrent = 48;
                        } else {
                            if (curAngle == 0) {
                                mCurrent = 48;
                            } else if (curAngle >= 0 && curAngle <= 35) {
                                mCurrent = 49;
                            } else if (curAngle > 35 && curAngle <= 60) {
                                mCurrent = 50;
                            } else if (curAngle > 60 && curAngle <= 80) {
                                mCurrent = 51;
                            } else if (curAngle > 80 && curAngle <= 90) {
                                mCurrent = 52;
                            } else if (curAngle > 90 && curAngle <= 112) {
                                mCurrent = 53;
                            } else if (curAngle > 112 && curAngle <= 128) {
                                mCurrent = 54;
                            } else if (curAngle > 128 && curAngle <= 160) {
                                mCurrent = 55;
                            } else if (curAngle > 160 && curAngle <= 176) {
                                mCurrent = 56;
                            } else if (curAngle > 176 && curAngle <= 208) {
                                mCurrent = 57;
                            } else if (curAngle > 208 && curAngle <= 224) {
                                mCurrent = 58;
                            } else if (curAngle > 224 && curAngle <= 240) {
                                mCurrent = 59;
                            } else if (curAngle > 260 && curAngle <= 272) {
                                mCurrent = 60;
                            }
                            deviceChild.setProtectSetTemp(mCurrent);
                            deviceChildDao.update(deviceChild);
                            tv_set_temp.setText(mCurrent + "℃");
                        }
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
    private int sum = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 2:
                    semicBar.setmCurAngle(0);
                    semicBar.setCurProcess(0);
                    semicBar.invalidate();
                    break;
                case 3:
                    semicBar.setmCurAngle(0);
                    semicBar.invalidate();
                    break;
                case 4:
                    semicBar.setmCurAngle(0);
                    semicBar.invalidate();
                    break;
                case 5:
                    int temp2 = msg.what;
                    semicBar.setmCurAngle(temp2);
                    break;
                case 6:
                    int mCurrent6 = msg.what;
                    int mCurrentAngle = 0;
                    String module6 = semicBar.getModule();

                    if ("1".equals(module6)) {
                        mCurrentAngle = (mCurrent6 - 3) * 7;
                    } else if ("2".equals(module6)) {
                        if (mCurrent6 == 48) {
                            mCurrentAngle = 0;
                        } else if (mCurrent6 == 49) {
                            mCurrentAngle = 35;
                        } else if (mCurrent6 == 50) {
                            mCurrentAngle = 60;
                        } else if (mCurrent6 == 51) {
                            mCurrentAngle = 80;
                        } else if (mCurrent6 == 52) {
                            mCurrentAngle = 90;
                        } else if (mCurrent6 == 53) {
                            mCurrentAngle = 112;
                        } else if (mCurrent6 == 54) {
                            mCurrentAngle = 128;
                        } else if (mCurrent6 == 55) {
                            mCurrentAngle = 160;
                        } else if (mCurrent6 == 56) {
                            mCurrentAngle = 176;
                        } else if (mCurrent6 == 57) {
                            mCurrentAngle = 208;
                        } else if (mCurrent6 == 58) {
                            mCurrentAngle = 224;
                        } else if (mCurrent6 == 59) {
                            mCurrentAngle = 240;
                        } else if (mCurrent6 == 60) {
                            mCurrentAngle = 272;
                        }
                    }
                    semicBar.setmCurAngle(mCurrentAngle);
                    semicBar.setCurProcess(mCurrentAngle);
                    semicBar.invalidate();
                    break;
            }
        }
    };

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceChild deviceChild2 = (DeviceChild) intent.getSerializableExtra("deviceChild");
            if (deviceChild2.getMacAddress().equals(deviceChild.getMacAddress())) {
                deviceChild = deviceChild2;
                deviceChildDao.update(deviceChild);
                if (deviceChild != null) {
                    setMode(deviceChild);
                }
            }
        }
    }

    private MessageReceiver receiver;
    private DeviceChild deviceChild;
    private DeviceChildDaoImpl deviceChildDao;

    private int mCurWeek=0;
    Calendar calendar;
    @Override
    public void onResume() {
        super.onResume();
        deviceChildDao = new DeviceChildDaoImpl(getActivity());
        timeTaskDao = new TimeTaskDaoImpl(getActivity());
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week2 = calendar.get(Calendar.DAY_OF_WEEK);
        String mWeek = Utils.getWeek(year, month, day, week2).substring(2);
        if (!Utils.isEmpty(mWeek)){
            if ("日".equals(mWeek)){
                mWeek="七";
            }
            mCurWeek=ChineseNumber.chineseNumber2Int(mWeek);
        }
        Bundle bundle = getArguments();
        deviceChild = (DeviceChild) bundle.get("deviceChild");

//        String childPosiotn = bundle.getString("deviceId");


        Intent intent = new Intent(getActivity(), MQService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter("HeaterFragment");
        receiver = new MessageReceiver();
        getActivity().registerReceiver(receiver, intentFilter);


        running = true;
        image_switch.setTag("关");
        image_hand_task.setTag("定时");
        model_protect.setTag("不保护");
        image_lock.setTag("解锁");
        image_srceen.setTag("屏保关");
        semicBar.setDeviceId(deviceChild.getId() + "");


        if (deviceChild != null) {
            setMode(deviceChild);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        running = false;
    }

    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};

    @OnClick({R.id.image_switch, R.id.image_mode2, R.id.image_mode, R.id.image_mode3, R.id.image_mode4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_switch:
                String deviceState = deviceChild.getDeviceState();
                if ("open".equals(deviceState)) {
                    image_switch.setTag("关");
                    deviceChild.setDeviceState("close");
                    deviceChild.setImg(imgs[0]);

                } else {
                    image_switch.setTag("开");
                    deviceChild.setDeviceState("open");
                    deviceChild.setImg(imgs[1]);
                }
                deviceChildDao.update(deviceChild);
                setMode(deviceChild);
//                semicBar.invalidate();

                break;
            case R.id.image_mode:
                if ("open".equals(deviceChild.getDeviceState())){
                    String handTask = (String) image_hand_task.getTag();
                    if (!"childProtect".equals(deviceChild.getOutputMod())) {
                        if ("手动".equals(handTask)) {
                            image_hand_task.setTag("定时");
                            deviceChild.setWorkMode("timer");
                            int timerTemp = deviceChild.getTimerTemp();
                            int curTemp = deviceChild.getCurTemp();

                            if (timerTemp >= (curTemp + 3)) {
                                deviceChild.setOutputMod("fastHeat");//速热模式

                            } else if (curTemp >= (timerTemp + 3)) {
                                deviceChild.setOutputMod("saveTemp");//保温模式
                            } else {
                                deviceChild.setOutputMod("savePwr");//节能模式
                            }
                        } else if ("定时".equals(handTask)) {
                            image_hand_task.setTag("手动");
                            deviceChild.setWorkMode("manual");
                            int manualMatTemp = deviceChild.getManualMatTemp();
                            int curTemp = deviceChild.getCurTemp();

                            if (manualMatTemp >= (curTemp + 3)) {
                                deviceChild.setOutputMod("fastHeat");//速热模式
                            } else if (curTemp >= (manualMatTemp + 3)) {
                                deviceChild.setOutputMod("saveTemp");//保温模式
                            } else {
                                deviceChild.setOutputMod("savePwr");//节能模式
                            }
                        }else {
                            String workMode=deviceChild.getWorkMode();
                            if ("manual".equals(workMode)){
                                image_hand_task.setImageResource(R.mipmap.module_handle);
                            }else if ("timer".equals(workMode)){
                                image_hand_task.setImageResource(R.mipmap.module_task);
                            }
                        }
//                    deviceChildDao.update(deviceChild);
                        setMode(deviceChild);
                    }
                }

                break;
            case R.id.image_mode2:
                if ("open".equals(deviceChild.getDeviceState())){
                    String outputMode = deviceChild.getOutputMod();
                    if ("childProtect".equals(outputMode)) {
                        model_protect.setTag("不保护");
                        model_protect.setBackgroundResource(0);
                        String workMode = deviceChild.getWorkMode();
                        if ("manual".equals(workMode)) {
                            int manualMatTemp = deviceChild.getManualMatTemp();
                            int curTemp = deviceChild.getCurTemp();
                            if (manualMatTemp >= (curTemp + 3)) {
                                deviceChild.setOutputMod("fastHeat");//速热模式
                                tv_outmode.setText("速热模式");
                            } else if (curTemp >= (manualMatTemp + 3)) {
                                deviceChild.setOutputMod("saveTemp");//保温模式
                                tv_outmode.setText("保温模式");
                            } else {
                                deviceChild.setOutputMod("savePwr");//节能模式
                                tv_outmode.setText("节能模式");
                            }
                        } else if ("timer".equals(workMode)) {
                            int timerTemp = deviceChild.getTimerTemp();
                            int curTemp = deviceChild.getCurTemp();

                            if (timerTemp >= (curTemp + 3)) {
                                deviceChild.setOutputMod("fastHeat");//速热模式
                                tv_outmode.setText("速热模式");
                            } else if (curTemp >= (timerTemp + 3)) {
                                deviceChild.setOutputMod("saveTemp");//保温模式
                                tv_outmode.setText("保温模式");
                            } else {
                                deviceChild.setOutputMod("savePwr");//节能模式
                                tv_outmode.setText("节能模式");
                            }
                        }
                    } else {
                        deviceChild.setOutputMod("childProtect");
                    }
//                deviceChildDao.update(deviceChild);
                    setMode(deviceChild);
                }

                break;
            case R.id.image_mode3:
                if ("open".equals(deviceChild.getDeviceState())){
                    String lock = (String) image_lock.getTag();
                    if ("上锁".equals(lock)) {
                        image_lock.setTag("解锁");
                        deviceChild.setLockScreen("close");
                    } else if ("解锁".equals(lock)) {
                        image_lock.setTag("上锁");
                        deviceChild.setLockScreen("open");
                    }
//                deviceChildDao.update(deviceChild);
                    setMode(deviceChild);
                }

                break;
            case R.id.image_mode4:
                if ("open".equals(deviceChild.getDeviceState())){
                    String srceen = (String) image_srceen.getTag();
                    if ("屏保关".equals(srceen)) {
                        image_srceen.setTag("屏保开");
                        deviceChild.setBackGroundLED("open");
                    } else {
                        image_srceen.setTag("屏保关");
                        deviceChild.setBackGroundLED("close");
                    }
//                deviceChildDao.update(deviceChild);
                    setMode(deviceChild);
                }
                break;
        }
    }

    private void setMode(DeviceChild deviceChild) {

        String wifiVersion = deviceChild.getWifiVersion();
        String MCUVerion = deviceChild.getMCUVerion();

        int MatTemp = deviceChild.getMatTemp();
        int manualMatTemp = deviceChild.getManualMatTemp();
        int timerTemp = deviceChild.getTimerTemp();

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


        if ("childProtect".equals(outputMode) && "enable".equals(protectEnable)) {
            model_protect.setEnabled(true);
            if ("open".equals(deviceState)) {
                semicBar.setCanTouch(true);
            } else {
                semicBar.setCanTouch(false);
            }

            model_protect.setTag("保护");
            model_protect.setBackgroundResource(R.mipmap.img_temp_circle);

            image_temp.setImageResource(R.mipmap.img_protect_open);

            tv_outmode.setText("保护模式");
            tv_set_temp.setText(protectSetTemp + "℃");
            tv_cur_temp.setText(protectProTemp + "℃");
            Message msg = handler.obtainMessage();
            msg.arg1 = 6;
            msg.what = protectSetTemp;
            handler.sendMessage(msg);
            semicBar.setModule("2");
        }
        if ("disable".equals(protectEnable)) {
            semicBar.setCanTouch(false);
            model_protect.setTag("不保护");
            model_protect.setBackgroundResource(0);
            model_protect.setEnabled(false);
        }

        /**开机，关机状态*/
        if ("open".equals(deviceState)) {
            animationDrawable.start();
            image_switch.setTag("开");
            image_switch.setImageResource(R.mipmap.img_switch);
            relative4.setVisibility(View.VISIBLE);
            relative5.setVisibility(View.GONE);
        } else if ("close".equals(deviceState)) {
            animationDrawable.stop();
            image_switch.setTag("关");

            image_switch.setImageResource(R.mipmap.img_close);
            semicBar.setCanTouch(false);
            relative4.setVisibility(View.GONE);
            relative5.setVisibility(View.VISIBLE);
        }

        if (!"childProtect".equals(outputMode)) {
            model_protect.setBackgroundResource(0);
            if ("manual".equals(workMode)) {
                if ("open".equals(deviceState)) {
                    semicBar.setCanTouch(true);
                } else {
                    semicBar.setCanTouch(false);
                }
                image_hand_task.setImageResource(R.mipmap.module_handle);
                image_hand_task.setTag("手动");

                image_temp.setImageResource(R.mipmap.module_handle);
//                tv_set_temp.setText(manualMatTemp + "℃");
                tv_cur_temp.setText(curTemp + "℃");

                semicBar.setModule("1");
                Message msg = handler.obtainMessage();
                msg.arg1 = 6;
                msg.what = manualMatTemp;
                handler.sendMessage(msg);
                if (manualMatTemp >= (curTemp + 3)) {
                    deviceChild.setOutputMod("fastHeat");//速热模式
                    tv_outmode.setText("速热模式");
                } else if (curTemp >= (manualMatTemp + 3)) {
                    deviceChild.setOutputMod("saveTemp");//保温模式
                    tv_outmode.setText("保温模式");
                    animationDrawable.stop();
                } else {
                    deviceChild.setOutputMod("savePwr");//节能模式
                    tv_outmode.setText("节能模式");
                }
            } else if ("timer".equals(workMode)) {


                image_hand_task.setImageResource(R.mipmap.module_task);

                image_hand_task.setTag("定时");
                semicBar.setCanTouch(false);
                image_temp.setImageResource(R.mipmap.module_task);
                tv_cur_temp.setText(curTemp + "℃");
                semicBar.setModule("1");
                Message msg = handler.obtainMessage();
                msg.arg1 = 6;
                msg.what = timerTemp;
                handler.sendMessage(msg);

                if ("timerShutdown".equals(outputMode)){
                    animationDrawable.stop();
                    deviceChild.setOutputMod("timerShutdown");
                }else {
                    if (timerTemp >= (curTemp + 3)) {
                        deviceChild.setOutputMod("fastHeat");//速热模式
                        tv_outmode.setText("速热模式");
                    } else if (curTemp >= (timerTemp + 3)) {
                        deviceChild.setOutputMod("saveTemp");//保温模式
                        tv_outmode.setText("保温模式");
                        animationDrawable.stop();
                    } else {
                        deviceChild.setOutputMod("savePwr");//节能模式
                        tv_outmode.setText("节能模式");
                    }
                }
            }
        }



        if ("open".equals(BackGroundLED)) {
            image_srceen.setTag("屏保开");
            image_srceen.setBackgroundResource(R.mipmap.img_temp_circle);
        } else if ("close".equals(BackGroundLED)) {
            image_srceen.setTag("屏保关");
            image_srceen.setBackgroundResource(0);
        }

        if ("open".equals(LockScreen)) {
            image_lock.setTag("上锁");
            image_lock.setBackgroundResource(R.mipmap.img_temp_circle);
        } else if ("close".equals(LockScreen)) {
            image_lock.setTag("解锁");
            image_lock.setBackgroundResource(0);
        }
        if ("err".equals(tempState)) {
            tv_cur_temp.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
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
