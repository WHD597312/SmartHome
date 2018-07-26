package com.xinrui.smart.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
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
import android.view.WindowManager;
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
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.TimeTask;
import com.xinrui.smart.util.ChineseNumber;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.DeviceChildDialog;
import com.xinrui.smart.view_custom.DeviceChildProjectDialog;
import com.xinrui.smart.view_custom.SemicircleBar;

import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/20.
 */

public class HeaterFragment extends LazyFragment {
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
    @BindView(R.id.relative4)
    RelativeLayout relative4;
    /**
     * 开机
     */
    @BindView(R.id.relative5)
    RelativeLayout relative5;
    @BindView(R.id.tv_timeShutDown) TextView tv_timeShutDown;/**定时关加热*/
    /**
     * 关机
     */
    private int mCurrent = 5;
    public static boolean running = false;


    // 标志位，标志已经初始化完成。
    private boolean isPrepared;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_heater, container, false);
        unbinder = ButterKnife.bind(this, view);
        isPrepared = true;
        return view;
    }

    @Override
    protected void lazyLoad() {
        if(!isPrepared || !isVisible) {
            return;
        }
        //填充各控件的数据
    }
    private boolean outside=true;
    @Override
    public void onStart() {
        super.onStart();
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth()-200;
        Log.w("width","width"+width);

        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(width,width);
        params.leftMargin=100;

        semicBar.setLayoutParams(params);
//        params=new RelativeLayout.LayoutParams(width/2+100,width/2+100);
//        params.leftMargin=200;
//
//        img_circle.setLayoutParams(params);
        semicBar.setModule("1");
        semicBar.setCanTouch(false);

        semicBar.setEnd(0);

        semicBar.setOnSeekBarChangeListener(new SemicircleBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(SemicircleBar seekbar, double curValue) {

                try {
                    String handTask = (String) image_hand_task.getTag();

                    String open = (String) image_switch.getTag();
                    img_circle.setImageResource(R.drawable.lottery_animlist);
                    AnimationDrawable animationDrawable = (AnimationDrawable) img_circle.getDrawable();
                    String module = semicBar.getModule();
                    String workMode = deviceChild.getWorkMode();
                    String deviceState = deviceChild.getDeviceState();
                    Log.i(TAG, "-->" + seekbar.getmCurAngle());
                    Log.i("curValue","curValue:"+curValue);
                    double curAngle = semicBar.getmCurAngle();
                    if ("1".equals(module)) {
                        if (curAngle > 272 && curAngle <= 330) {
                            mCurrent = 42;
                            if ("open".equals(deviceState)) {
                                if ("manual".equals(workMode)) {
                                    if (outside){
                                        tv_set_temp.setText(mCurrent + "℃");
                                        int curTemp = deviceChild.getCurTemp();
                                        tv_cur_temp.setText(curTemp+"℃");
                                        deviceChild.setManualMatTemp(mCurrent);
//                                        send(deviceChild);
                                        outside=false;
                                    }
                                }
                            }
                        } else if (curAngle >330 && curAngle <= 360) {

                            if ("open".equals(deviceState)) {
                                if ("manual".equals(workMode)) {
                                    outside=true;
                                    mCurrent=5;
                                    if (outside){
                                        tv_set_temp.setText(mCurrent + "℃");
                                        int curTemp = deviceChild.getCurTemp();
                                        tv_cur_temp.setText(curTemp+"℃");
                                        deviceChild.setManualMatTemp(mCurrent);
//                                        send(deviceChild);
                                        outside=false;
                                    }
                                }
                            }

                        } else {
                            mCurrent = (int) curAngle / 7 + 3;
                            if (mCurrent<5){
                                mCurrent=5;
                            }
                        }

                        if ("close".equals(deviceState)) {
//                            Message msg = handler.obtainMessage();
//                            msg.arg1 = 2;
//                            msg.what = mCurrent;
//                            handler.sendMessage(msg);
                            tv_set_temp.setText("--" + "℃");
                            int curTemp = deviceChild.getCurTemp();
                            tv_cur_temp.setText(curTemp+"℃");
                            tv_outmode.setText("");
                        } else if ("open".equals(deviceState)) {
                            if ("manual".equals(workMode)) {
                                deviceChild.setManualMatTemp(mCurrent);
                                deviceChildDao.update(deviceChild);
//                                tv_timeShutDown.setVisibility(View.GONE);
//                                tv_outmode.setText("定时关加热");
//                                tv_timeShutDown.setVisibility(View.GONE);
                                String tempState=deviceChild.getTempState();
                                String outputMode=deviceChild.getOutputMod();
                                if ("err".equals(tempState)){
                                    if ("saveTemp".equals(outputMode)){
                                        tv_outmode.setText("保温模式");
                                        animationDrawable.stop();
                                    }else if ("savePwr".equals(outputMode)){
                                        tv_outmode.setText("节能模式");
                                        animationDrawable.start();
                                    }
                                }else {
                                    int manualMatTemp=deviceChild.getManualMatTemp();
                                    int curTemp=deviceChild.getCurTemp();
                                    if (manualMatTemp >= (curTemp + 3)) {
                                        deviceChild.setOutputMod("fastHeat");//速热模式
                                        tv_outmode.setText("速热模式");
                                        animationDrawable.start();
                                    } else if (curTemp >= (manualMatTemp + 3)) {
                                        deviceChild.setOutputMod("saveTemp");//保温模式
                                        tv_outmode.setText("保温模式");
                                        animationDrawable.stop();
                                    } else {
                                        deviceChild.setOutputMod("savePwr");//节能模式
                                        tv_outmode.setText("节能模式");
                                        animationDrawable.start();
                                    }
                                }

                                if (seekbar.getEnd() == 1) {
                                    send(deviceChild);
                                    seekbar.setEnd(0);
                                    Message msg=handler.obtainMessage();
                                    msg.arg1=7;
                                    handler.sendMessage(msg);
                                }
                            } else if ("timer".equals(workMode)){
                                deviceChild.setTimerTemp(mCurrent);
                                deviceChildDao.update(deviceChild);
                                if ("timer".equals(deviceChild.getWorkMode()) && "enable".equals(deviceChild.getTimerShutdown())){
//                                tv_timeShutDown.setVisibility(View.VISIBLE);
                                    tv_outmode.setText("定时关加热");
                                }else {
                                    if ("err".equals(deviceChild.getTempState())){
                                        if (mCurrent>=40){
                                            tv_outmode.setText("速热模式");
                                            animationDrawable.start();
                                        }else if (mCurrent>20 && mCurrent<40){
                                            tv_outmode.setText("节能模式");
                                            animationDrawable.start();
                                        }else{
                                            tv_outmode.setText("保温模式");
                                            animationDrawable.stop();
                                        }
                                    }else {
                                        int curTemp=deviceChild.getCurTemp();
                                        if ((mCurrent-3) >curTemp) {
                                            tv_outmode.setText("速热模式");
                                            animationDrawable.start();
                                        } else if ((mCurrent+2)< curTemp) {
                                            tv_outmode.setText("保温模式");
                                            animationDrawable.stop();
                                        } else if ((mCurrent-3) > curTemp){
                                            tv_outmode.setText("节能模式");
                                            animationDrawable.start();
                                        }
                                    }

                                }
                            }


                            tv_set_temp.setText(mCurrent + "℃");
                            int curTemp = deviceChild.getCurTemp();
                            tv_cur_temp.setText(curTemp+"℃");

                        }
                    } else if ("2".equals(module)) {
                        if ("close".equals(deviceState)) {
//                            Message msg = handler.obtainMessage();
//                            msg.arg1 = 2;
//                            msg.what = mCurrent;
//                            handler.sendMessageDelayed(msg,1000);
                            tv_set_temp.setText("--" + "℃");
                            int curTemp = deviceChild.getProtectProTemp();
                            tv_cur_temp.setText(curTemp+"℃");
                            tv_outmode.setText("");
                            animationDrawable.stop();
                        } else if ("open".equals(deviceState)) {
                            if ("enable".equals(deviceChild.getTimerShutdown()) && "timer".equals(deviceChild.getWorkMode())) {
//                                    tv_timeShutDown.setVisibility(View.VISIBLE);
//                                    tv_outmode.setVisibility(View.GONE);
                                tv_outmode.setText("定时关加热");
                                animationDrawable.stop();
                            }else {
                                animationDrawable.start();
                            }
                            if (curAngle > 272 && curAngle <= 330) {
                                outside=true;
                                if (outside){
                                    mCurrent = 60;
                                    deviceChild.setProtectSetTemp(mCurrent);
                                    deviceChildDao.update(deviceChild);
                                    tv_set_temp.setText(mCurrent + "℃");
                                    int curTemp = deviceChild.getProtectProTemp();
                                    tv_cur_temp.setText(curTemp+"℃");
//                                    send(deviceChild);
                                    outside=false;
                                }

                            } else if ((curAngle > 330 && curAngle <= 360)) {
                                outside=true;
                                if (outside){
                                    mCurrent = 48;

                                    deviceChild.setProtectSetTemp(mCurrent);
                                    deviceChildDao.update(deviceChild);
                                    tv_set_temp.setText(mCurrent + "℃");
                                    int curTemp = deviceChild.getProtectProTemp();
                                    tv_cur_temp.setText(curTemp+"℃");
//                                    send(deviceChild);
                                    outside=false;
                                }

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
                                } else if (curAngle > 260 && curAngle <= 280) {
                                    mCurrent = 60;

                                }

                                deviceChild.setProtectSetTemp(mCurrent);
                                deviceChildDao.update(deviceChild);
                                tv_set_temp.setText(mCurrent + "℃");
                                int curTemp = deviceChild.getProtectProTemp();
                                tv_cur_temp.setText(curTemp+"℃");

                                if (seekbar.getEnd() == 1) {

                                    send(deviceChild);
                                    seekbar.setEnd(0);
                                    Message msg=handler.obtainMessage();
                                    msg.arg1=7;
                                    handler.sendMessage(msg);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void send(DeviceChild deviceChild) {
        try {
            if (deviceChild != null) {
                JSONObject maser = new JSONObject();
                maser.put("ctrlMode", deviceChild.getCtrlMode());
                maser.put("workMode", deviceChild.getWorkMode());
                maser.put("MatTemp", deviceChild.getManualMatTemp());
                maser.put("TimerTemp", deviceChild.getTimerTemp());
                maser.put("LockScreen", deviceChild.getLockScreen());
                maser.put("BackGroundLED", deviceChild.getBackGroundLED());
                maser.put("deviceState", deviceChild.getDeviceState());
                maser.put("tempState", deviceChild.getTempState());
                maser.put("outputMode", deviceChild.getOutputMod());
                maser.put("protectProTemp", deviceChild.getProtectProTemp());
                maser.put("protectSetTemp", deviceChild.getProtectSetTemp());

                String s = maser.toString();
                boolean success = false;
                String topicName;
                String mac = deviceChild.getMacAddress();
                if (deviceChild.getType() == 1 && deviceChild.getControlled() == 2) {
                    String houseId=deviceChild.getHouseId()+"";
//                    topicName = "rango/masterController/"+houseId+"/"+mac+"/set";
                    topicName = "rango/" + mac + "/set";
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
            String workMode=deviceChild.getWorkMode();
            String output=deviceChild.getOutputMod();
            String online=deviceChild.getDeviceState();
            try {
                switch (msg.arg1) {
                    case 2:
                        int current=msg.what;
                        semicBar.setmCurAngle(current);
                        semicBar.setCurProcess(current);
                        semicBar.setWorkMode(workMode);
                        semicBar.setOutput(output);
                        semicBar.setOnline(online);
                        break;
                    case 3:
                        semicBar.setmCurAngle(0);
                        semicBar.setWorkMode(workMode);
                        semicBar.setOutput(output);
                        semicBar.setOnline(online);
                        break;
                    case 4:
                        semicBar.setmCurAngle(0);
                        semicBar.setWorkMode(workMode);
                        semicBar.setOutput(output);
                        semicBar.setOnline(online);
                        break;
                    case 5:
                        int temp2 = msg.what;
                        semicBar.setmCurAngle(temp2);
                        semicBar.setWorkMode(workMode);
                        semicBar.setOutput(output);
                        semicBar.setOnline(online);
                        break;
                    case 6:
                        int mCurrent6 = msg.what;
                        int mCurrentAngle = 0;
                        if (semicBar!=null){
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
                            semicBar.setWorkMode(workMode);
                            semicBar.setOutput(output);
                            semicBar.setOnline(online);
//                        semicBar.invalidate();
                        }

                        break;
                    case 7:
                        semicBar.setEnd(0);
                        semicBar.setWorkMode(workMode);
                        semicBar.setOutput(output);
                        semicBar.setOnline(online);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            DeviceChild deviceChild2 = (DeviceChild) intent.getSerializableExtra("deviceChild");
            String macAddress=intent.getStringExtra("macAddress");
            String noNet=intent.getStringExtra("noNet");
            if (!Utils.isEmpty(noNet)){
                Utils.showToast(getActivity(),"网络已断开，请设置网络");
            }
            else {
                if (!Utils.isEmpty(macAddress)){
                    if (deviceChild.getMacAddress().equals(macAddress)){
                        Utils.showToast(getActivity(),"该设备已被重置");
                        Intent intent2=new Intent(getActivity(),MainActivity.class);
                        intent2.putExtra("deviceList","deviceList");
                        startActivity(intent2);
                    }
                }else {
                    if (deviceChild2.getMacAddress().equals(deviceChild.getMacAddress())) {
                        deviceChild = deviceChild2;
                        deviceChildDao.update(deviceChild);
                        if (deviceChild != null) {
                            if (deviceChild.getOnLint()){
                                setMode(deviceChild);
                            }else {
                                Utils.showToast(getActivity(),"该设备已离线");
                            }
                        }
                    }
                }
            }
        }
    }

    private MessageReceiver receiver;
    private DeviceChild deviceChild;
    private DeviceChildDaoImpl deviceChildDao;

    private int mCurWeek = 0;
    Calendar calendar;
    List<DeviceChild> deviceChildList;
    private boolean isBound=false;

    @Override
    public void onResume() {
        super.onResume();
        deviceChildDao = new DeviceChildDaoImpl(MyApplication.getContext());
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week2 = calendar.get(Calendar.DAY_OF_WEEK);
        String mWeek = Utils.getWeek(year, month, day, week2).substring(2);
        if (!Utils.isEmpty(mWeek)) {
            if ("日".equals(mWeek)) {
                mWeek = "七";
            }
            mCurWeek = ChineseNumber.chineseNumber2Int(mWeek);
        }
        Bundle bundle = getArguments();
        deviceChild = (DeviceChild) bundle.get("deviceChild");

//        String childPosiotn = bundle.getString("deviceId");

        Intent intent = new Intent(getActivity(), MQService.class);
        isBound=getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter("HeaterFragment");
        receiver = new MessageReceiver();
        getActivity().registerReceiver(receiver, intentFilter);


        running = true;
//        image_switch.setTag("关");
//        image_hand_task.setTag("定时");
//        model_protect.setTag("不保护");
//        image_lock.setTag("解锁");
//        image_srceen.setTag("屏保关");
        semicBar.setDeviceId(deviceChild.getId() + "");

        if (deviceChild != null) {
            setMode(deviceChild);
//            send(deviceChild);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        running = false;
        if(deviceChildDao!=null){
            deviceChildDao.closeDaoSession();
        }

    }



    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};

    private boolean click=false;
    private void buildOpenChildProjectDialog(){
//        final Dialog dialog=new DeviceChildDialog(getActivity());
        final DeviceChildProjectDialog dialog=new DeviceChildProjectDialog(getActivity());
        dialog.setOnNegativeClickListener(new DeviceChildProjectDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DeviceChildProjectDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                deviceChild.setOutputMod("childProtect");
                setMode(deviceChild);
                send(deviceChild);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private int position=-1;
    @OnClick({R.id.image_switch, R.id.image_mode2, R.id.image_mode, R.id.image_mode3, R.id.image_mode4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_switch:
                if (NoFastClickUtils.isFastClick()){

                    String deviceState = deviceChild.getDeviceState();
                    if ("open".equals(deviceState)) {
                        position=0;
                        relative4.setVisibility(View.GONE);
                        relative5.setVisibility(View.VISIBLE);
                        image_switch.setTag("关");
                        deviceChild.setDeviceState("close");
//                        tv_outmode.setVisibility(View.GONE);
                        deviceChild.setImg(imgs[0]);
                        deviceChildDao.update(deviceChild);
                        setMode(deviceChild);
                        send(deviceChild);
//                        tv_timeShutDown.setVisibility(View.GONE);


                    } else {
//                        tv_outmode.setVisibility(View.VISIBLE);
                        position=1;
//                        relative4.setVisibility(View.VISIBLE);
//                        relative5.setVisibility(View.GONE);
                        image_switch.setTag("开");
                        deviceChild.setDeviceState("open");
                        deviceChild.setImg(imgs[1]);
                        deviceChildDao.update(deviceChild);
                        setMode(deviceChild);
                        send(deviceChild);

                    }
                }else {
                    Utils.showToast(getActivity(), "主人，请对我温柔点!");
                }
//                semicBar.invalidate();

                break;
            case R.id.image_mode:
                if(NoFastClickUtils.isFastClick()){
                    position=-1;
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
                            send(deviceChild);
                        }else {
                            String workMode=deviceChild.getWorkMode();
                            if ("manual".equals(workMode)){
                                deviceChild.setWorkMode("timer");
                                image_hand_task.setImageResource(R.mipmap.module_handle);
                            }else if ("timer".equals(workMode)){
                                deviceChild.setWorkMode("manual");
                                image_hand_task.setImageResource(R.mipmap.module_task);
                            }
                            setMode(deviceChild);
                            send(deviceChild);
                        }
                    }
                }else {
                    Utils.showToast(getActivity(), "主人，请对我温柔点!");
                }



                break;
            case R.id.image_mode2:
                if (NoFastClickUtils.isFastClick()){
//                    buildOpenChildProjectDialog();
                    position=-1;
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
//                                tv_outmode.setText("速热模式");
                            } else if (curTemp >= (manualMatTemp + 3)) {
                                deviceChild.setOutputMod("saveTemp");//保温模式
//                                tv_outmode.setText("保温模式");
                            } else {
                                deviceChild.setOutputMod("savePwr");//节能模式
//                                tv_outmode.setText("节能模式");
                            }
                        } else if ("timer".equals(workMode)) {

                            int timerTemp = deviceChild.getTimerTemp();
                            int curTemp = deviceChild.getCurTemp();

                            if (timerTemp >= (curTemp + 3)) {
                                deviceChild.setOutputMod("fastHeat");//速热模式
//                                tv_outmode.setText("速热模式");
                            } else if (curTemp >= (timerTemp + 3)) {
                                deviceChild.setOutputMod("saveTemp");//保温模式
//                                tv_outmode.setText("保温模式");
                            } else {
                                deviceChild.setOutputMod("savePwr");//节能模式
//                                tv_outmode.setText("节能模式");
                            }
                        }
                        setMode(deviceChild);
                        send(deviceChild);
                    }else {
                        buildOpenChildProjectDialog();
                    }
                }else {
                    Utils.showToast(getActivity(), "主人，请对我温柔点!");
                }
                break;
            case R.id.image_mode3:
                if (NoFastClickUtils.isFastClick()){
                    position=-1;
                    if ("open".equals(deviceChild.getDeviceState())) {
                        String lock = (String) image_lock.getTag();
                        if ("上锁".equals(lock)) {
                            image_lock.setTag("解锁");
                            deviceChild.setLockScreen("close");
                            setMode(deviceChild);
                            send(deviceChild);
                        } else if ("解锁".equals(lock)) {
                            image_lock.setTag("上锁");
                            deviceChild.setLockScreen("open");
                            setMode(deviceChild);
                            send(deviceChild);
                        }
                    }
                }else {
                    Utils.showToast(getActivity(), "主人，请对我温柔点!");
                }

                break;
            case R.id.image_mode4:
                if (NoFastClickUtils.isFastClick()){
                    position=-1;
                    if ("open".equals(deviceChild.getDeviceState())) {
                        String srceen = (String) image_srceen.getTag();
                        if ("屏保关".equals(srceen)) {
                            image_srceen.setTag("屏保开");
                            deviceChild.setBackGroundLED("open");
                            setMode(deviceChild);
                            send(deviceChild);
                        } else {
                            image_srceen.setTag("屏保关");
                            deviceChild.setBackGroundLED("close");
                            setMode(deviceChild);
                            send(deviceChild);
                        }
                    }
                }else {
                    Utils.showToast(getActivity(), "主人，请对我温柔点!");
                }

                break;
        }
    }

    private void setMode(DeviceChild deviceChild) {

        String timerShutDown=deviceChild.getTimerShutdown();
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
            if ("manual".equals(workMode)) {
                image_hand_task.setImageResource(R.mipmap.module_handle);
                image_hand_task.setTag("手动");

//
            } else if ("timer".equals(workMode)) {
                image_hand_task.setImageResource(R.mipmap.module_task);
                image_hand_task.setTag("定时");
            }
            model_protect.setEnabled(true);
            if ("open".equals(deviceState)) {
                semicBar.setCanTouch(true);
                animationDrawable.start();
            } else {
                semicBar.setCanTouch(false);
                animationDrawable.stop();
            }

            model_protect.setTag("保护");
            model_protect.setBackgroundResource(R.mipmap.img_child_pro);

            image_temp.setImageResource(R.mipmap.img_child_pro);

            tv_outmode.setText("保护模式");
            tv_set_temp.setText(protectSetTemp + "℃");
//            tv_cur_temp.setText(protectProTemp + "℃");
            Message msg = handler.obtainMessage();
            msg.arg1 = 6;
            msg.what = protectSetTemp;
//            handler.sendMessageDelayed(msg,1000);
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
            tv_outmode.setVisibility(View.VISIBLE);
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
            tv_outmode.setVisibility(View.GONE);
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
//                handler.sendMessageDelayed(msg,1000);


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
//                handler.sendMessageDelayed(msg,1000);
            }
        }

        if ("open".equals(BackGroundLED)) {
            image_srceen.setTag("屏保开");
            image_srceen.setBackgroundResource(R.mipmap.img_screen_open);
        } else if ("close".equals(BackGroundLED)) {
            image_srceen.setTag("屏保关");
            image_srceen.setImageResource(R.mipmap.img_screen);
            image_srceen.setBackgroundResource(0);
        }

        if ("open".equals(LockScreen)) {
            image_lock.setTag("上锁");
            image_lock.setImageResource(R.mipmap.open_lockscreen);
        } else if ("close".equals(LockScreen)) {
            image_lock.setTag("解锁");
            image_lock.setImageResource(R.mipmap.close_lockscreen);
        }
        if ("err".equals(tempState)) {
            tv_cur_temp.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        }
        if ("enable".equals(timerShutDown) && "timer".equals(workMode)) {
            if ("childProtect".equals(outputMode)) {
                model_protect.setEnabled(true);
                if ("open".equals(deviceState)) {
                    semicBar.setCanTouch(true);
                    animationDrawable.start();
                } else {
                    semicBar.setCanTouch(false);
                    animationDrawable.stop();
                }
                model_protect.setTag("保护");
                model_protect.setBackgroundResource(R.mipmap.img_temp_circle);
                image_temp.setImageResource(R.mipmap.img_child_pro);
                tv_set_temp.setText(protectSetTemp + "℃");
                tv_cur_temp.setText(protectProTemp + "℃");
                semicBar.setModule("2");
                Message msg = handler.obtainMessage();
                msg.arg1 = 6;
                msg.what = protectSetTemp;
                handler.sendMessage(msg);
                if ("enable".equals(deviceChild.getTimerShutdown()) && "timer".equals(deviceChild.getWorkMode())) {
//                                    tv_timeShutDown.setVisibility(View.VISIBLE);
//                                    tv_outmode.setVisibility(View.GONE);
//                    tv_outmode.setText("定时关加热");
                    animationDrawable.stop();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (isBound){
                if (connection != null) {
                    getActivity().unbindService(connection);
                }
            }

            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}