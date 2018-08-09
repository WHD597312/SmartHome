package com.xinrui.smart.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.TimeDaoImpl;
import com.xinrui.secen.scene_view_custom.MySeekBar;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.device.AboutUsActivity;
import com.xinrui.smart.activity.device.ShareDeviceActivity;
import com.xinrui.smart.adapter.DeviceListAdapter;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.Timer;
import com.xinrui.smart.util.ChineseNumber;
import com.xinrui.smart.util.NoFastClickUtils;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.view_custom.DeviceChildProjectDialog;
import com.xinrui.smart.view_custom.RestoreSetDialog;
import com.xinrui.smart.view_custom.SemicircleBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener {

    GestureDetector mDetector;
    private Unbinder unbinder;
    @BindView(R.id.img_back)
    ImageView img_back;//返回键
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.relative)
    RelativeLayout relative;
    @BindView(R.id.gradView)
    GridView gradView;
    private int[] colors = {R.color.color_black, R.color.holo_orange_dark};
    private List<String> list;
    DeviceListAdapter adapter;
    private FragmentManager fragmentManager;
    @BindView(R.id.linearout)
    LinearLayout linearout;
    @BindView(R.id.tv_offline)
    TextView tv_offline;
    MyApplication application;
    private String childPosition;
    private DeviceChildDaoImpl deviceChildDao;
    public static boolean running = false;
    private ProgressDialog progressDialog;

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
    @BindView(R.id.tv_timeShutDown)
    TextView tv_timeShutDown;/**定时关加热*/
    /**
     * 关机
     */
    private int mCurrent = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        unbinder = ButterKnife.bind(this);
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());
        timeDao = new TimeDaoImpl(getApplicationContext());
        progressDialog = new ProgressDialog(this);
    }


    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};
    private int position = -1;

    @OnClick({R.id.img_back, R.id.image_home, R.id.layout, R.id.linearout, R.id.image_switch, R.id.image_mode2, R.id.image_mode, R.id.image_mode3, R.id.image_mode4,R.id.semicBar})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.semicBar:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    gradView.setVisibility(View.VISIBLE);
                    break;
                }
                break;
            case R.id.image_switch:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    gradView.setVisibility(View.VISIBLE);
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
                    String deviceState = deviceChild.getDeviceState();
                    if ("open".equals(deviceState)) {
                        position = 0;
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
                        position = 1;
//                        relative4.setVisibility(View.VISIBLE);
//                        relative5.setVisibility(View.GONE);
                        image_switch.setTag("开");
                        deviceChild.setDeviceState("open");
                        deviceChild.setImg(imgs[1]);
                        deviceChildDao.update(deviceChild);
                        setMode(deviceChild);
                        send(deviceChild);

                    }
                } else {
                    Utils.showToast(this, "主人，请对我温柔点!");
                }
//                semicBar.invalidate();

                break;
            case R.id.image_mode:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    gradView.setVisibility(View.VISIBLE);
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
                    position = -1;
                    if ("open".equals(deviceChild.getDeviceState())) {
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
                            } else {
                                String workMode = deviceChild.getWorkMode();
                                if ("manual".equals(workMode)) {
                                    image_hand_task.setImageResource(R.mipmap.module_handle);
                                } else if ("timer".equals(workMode)) {
                                    image_hand_task.setImageResource(R.mipmap.module_task);
                                }
                            }
//                    deviceChildDao.update(deviceChild);
                            setMode(deviceChild);
                            send(deviceChild);
                        } else {
                            String workMode = deviceChild.getWorkMode();
                            if ("manual".equals(workMode)) {
                                deviceChild.setWorkMode("timer");
                                image_hand_task.setImageResource(R.mipmap.module_handle);
                            } else if ("timer".equals(workMode)) {
                                deviceChild.setWorkMode("manual");
                                image_hand_task.setImageResource(R.mipmap.module_task);
                            }
                            setMode(deviceChild);
                            send(deviceChild);
                        }
                    }
                } else {
                    Utils.showToast(this, "主人，请对我温柔点!");
                }

                break;
            case R.id.image_mode2:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    gradView.setVisibility(View.VISIBLE);
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
//                    buildOpenChildProjectDialog();
                    position = -1;
                    String outputMode = deviceChild.getOutputMod();
                    String protectEnable = deviceChild.getProtectEnable();
                    if ("disable".equals(protectEnable)) {
                        Utils.showToast(this, "保护模式未开启!");
                        break;
                    }
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
                    } else {
                        buildOpenChildProjectDialog();
                    }
                } else {
                    Utils.showToast(this, "主人，请对我温柔点!");
                }
                break;
            case R.id.image_mode3:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    gradView.setVisibility(View.VISIBLE);
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
                    position = -1;
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
                } else {
                    Utils.showToast(this, "主人，请对我温柔点!");
                }

                break;
            case R.id.image_mode4:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    gradView.setVisibility(View.VISIBLE);
                    break;
                }
                if (NoFastClickUtils.isFastClick()) {
                    position = -1;
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
                } else {
                    Utils.showToast(this, "主人，请对我温柔点!");
                }

                break;
            case R.id.layout:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                }
                gradView.setVisibility(View.VISIBLE);
                break;
            case R.id.linearout:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                }
                gradView.setVisibility(View.VISIBLE);
                break;
            case R.id.img_back:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    gradView.setVisibility(View.VISIBLE);
                    break;
                }
                Intent intent = new Intent();
                intent.putExtra("houseId", houseId);
                setResult(6000, intent);
                finish();
                break;
            case R.id.image_home:
                Intent intent2 = new Intent(this, MainActivity.class);
                intent2.putExtra("deviceList", "deviceList");
                startActivity(intent2);
                break;
        }
    }

    /**
     * 打开儿童保护模式对话框
     */
    private void buildOpenChildProjectDialog() {
//        final Dialog dialog=new DeviceChildDialog(getActivity());
        final DeviceChildProjectDialog dialog = new DeviceChildProjectDialog(this);
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

    /**
     * 打开恢复设置对话框
     */
    private void buildRestoreDialog(){
        final RestoreSetDialog dialog=new RestoreSetDialog(this);
        dialog.setOnNegativeClickListener(new RestoreSetDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new RestoreSetDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                dialog.dismiss();
                new PasteWeekAsync().execute();
            }
        });
        dialog.show();
    }


    int mPoistion = -1;
    ;// 选中的位置
    private int hour = 0;
    int year = 0;
    int month;
    int day = 0;
    SharedPreferences preferences;
    DeviceChild deviceChild;
    long deviceId;
    private TimeDaoImpl timeDao;
    private boolean isBound;
    long houseId;
    private boolean outside = true;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        childPosition = intent.getStringExtra("childPosition");

        deviceChild = deviceChildDao.findDeviceById(Long.parseLong(childPosition));
        deviceId = deviceChild.getId();
        houseId = deviceChild.getHouseId();
        if (deviceChild != null) {
            tv_name.setText(content);
//            fragmentManager =getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            HeaterFragment heaterFragment=new HeaterFragment();
//            Bundle bundle=new Bundle();
//            bundle.putSerializable("deviceChild",deviceChild);
//            heaterFragment.setArguments(bundle);
//            fragmentTransaction.replace(R.id.linearout, heaterFragment);
//            int commit=fragmentTransaction.commit();
//            Log.i("mmmmm",commit+"");

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth() - 200;
            Log.w("width", "width" + width);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);
            params.leftMargin = 100;

            semicBar.setLayoutParams(params);
//        params=new RelativeLayout.LayoutParams(width/2+100,width/2+100);
//        params.leftMargin=200;
//
//        img_circle.setLayoutParams(params);
            semicBar.setModule("1");
            semicBar.setCanTouch(false);

            semicBar.setEnd(0);
            setMode(deviceChild);
            semicBar.setOnSeekBarChangeListener(new SemicircleBar.OnSeekBarChangeListener() {
                @Override
                public void onChanged(SemicircleBar seekbar, double curValue) {

                    try {
                        if (popupWindow != null && popupWindow.isShowing()) {
                            popupWindow.dismiss();
                            backgroundAlpha(1.0f);
                            gradView.setVisibility(View.VISIBLE);
                            return;
                        }
                        String handTask = (String) image_hand_task.getTag();

                        String open = (String) image_switch.getTag();
                        img_circle.setImageResource(R.drawable.lottery_animlist);
                        AnimationDrawable animationDrawable = (AnimationDrawable) img_circle.getDrawable();
                        String module = semicBar.getModule();
                        String workMode = deviceChild.getWorkMode();
                        String deviceState = deviceChild.getDeviceState();
                        Log.i("curValue", "curValue:" + curValue);
                        double curAngle = semicBar.getmCurAngle();
                        if ("1".equals(module)) {
                            if (curAngle > 272 && curAngle <= 330) {
                                mCurrent = 42;
                                if ("open".equals(deviceState)) {
                                    if ("manual".equals(workMode)) {
                                        if (outside) {
                                            tv_set_temp.setText(mCurrent + "℃");
                                            int curTemp = deviceChild.getCurTemp();
                                            tv_cur_temp.setText(curTemp + "℃");
                                            deviceChild.setManualMatTemp(mCurrent);
//                                        send(deviceChild);
                                            outside = false;
                                        }
                                    }
                                }
                            } else if (curAngle > 330 && curAngle <= 360) {

                                if ("open".equals(deviceState)) {
                                    if ("manual".equals(workMode)) {
                                        outside = true;
                                        mCurrent = 5;
                                        if (outside) {
                                            tv_set_temp.setText(mCurrent + "℃");
                                            int curTemp = deviceChild.getCurTemp();
                                            tv_cur_temp.setText(curTemp + "℃");
                                            deviceChild.setManualMatTemp(mCurrent);
//                                        send(deviceChild);
                                            outside = false;
                                        }
                                    }
                                }

                            } else {
                                mCurrent = (int) curAngle / 7 + 3;
                                if (mCurrent < 5) {
                                    mCurrent = 5;
                                }
                            }

                            if ("close".equals(deviceState)) {
//                            Message msg = handler.obtainMessage();
//                            msg.arg1 = 2;
//                            msg.what = mCurrent;
//                            handler.sendMessage(msg);
                                tv_set_temp.setText("--" + "℃");
                                int curTemp = deviceChild.getCurTemp();
                                tv_cur_temp.setText(curTemp + "℃");
                                tv_outmode.setText("");
                            } else if ("open".equals(deviceState)) {
                                if ("manual".equals(workMode)) {
                                    deviceChild.setManualMatTemp(mCurrent);
                                    deviceChildDao.update(deviceChild);
//                                tv_timeShutDown.setVisibility(View.GONE);
//                                tv_outmode.setText("定时关加热");
//                                tv_timeShutDown.setVisibility(View.GONE);
                                    String tempState = deviceChild.getTempState();
                                    String outputMode = deviceChild.getOutputMod();
                                    if ("err".equals(tempState)) {
                                        if ("saveTemp".equals(outputMode)) {
                                            tv_outmode.setText("保温模式");
                                            animationDrawable.stop();
                                        } else if ("savePwr".equals(outputMode)) {
                                            tv_outmode.setText("节能模式");
                                            animationDrawable.start();
                                        }
                                    } else {
                                        int manualMatTemp = deviceChild.getManualMatTemp();
                                        int curTemp = deviceChild.getCurTemp();
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
                                        Message msg = handler.obtainMessage();
                                        msg.arg1 = 7;
                                        handler.sendMessage(msg);
                                    }
                                } else if ("timer".equals(workMode)) {
                                    deviceChild.setTimerTemp(mCurrent);
                                    deviceChildDao.update(deviceChild);
                                    if ("timer".equals(deviceChild.getWorkMode()) && "enable".equals(deviceChild.getTimerShutdown())) {
//                                tv_timeShutDown.setVisibility(View.VISIBLE);
                                        tv_outmode.setText("定时关加热");
                                    } else {
                                        if ("err".equals(deviceChild.getTempState())) {
                                            if (mCurrent >= 40) {
                                                tv_outmode.setText("速热模式");
                                                animationDrawable.start();
                                            } else if (mCurrent > 20 && mCurrent < 40) {
                                                tv_outmode.setText("节能模式");
                                                animationDrawable.start();
                                            } else {
                                                tv_outmode.setText("保温模式");
                                                animationDrawable.stop();
                                            }
                                        } else {
                                            int curTemp = deviceChild.getCurTemp();
                                            if ((mCurrent - 3) > curTemp) {
                                                tv_outmode.setText("速热模式");
                                                animationDrawable.start();
                                            } else if ((mCurrent + 2) < curTemp) {
                                                tv_outmode.setText("保温模式");
                                                animationDrawable.stop();
                                            } else if ((mCurrent - 3) > curTemp) {
                                                tv_outmode.setText("节能模式");
                                                animationDrawable.start();
                                            }
                                        }

                                    }
                                }


                                tv_set_temp.setText(mCurrent + "℃");
                                int curTemp = deviceChild.getCurTemp();
                                tv_cur_temp.setText(curTemp + "℃");

                            }
                        } else if ("2".equals(module)) {
                            if ("close".equals(deviceState)) {
//                            Message msg = handler.obtainMessage();
//                            msg.arg1 = 2;
//                            msg.what = mCurrent;
//                            handler.sendMessageDelayed(msg,1000);
                                tv_set_temp.setText("--" + "℃");
                                int curTemp = deviceChild.getProtectProTemp();
                                tv_cur_temp.setText(curTemp + "℃");
                                tv_outmode.setText("");
                                animationDrawable.stop();
                            } else if ("open".equals(deviceState)) {
                                if ("enable".equals(deviceChild.getTimerShutdown()) && "timer".equals(deviceChild.getWorkMode())) {
//                                    tv_timeShutDown.setVisibility(View.VISIBLE);
//                                    tv_outmode.setVisibility(View.GONE);
                                    tv_outmode.setText("定时关加热");
                                    animationDrawable.stop();
                                } else {
                                    animationDrawable.start();
                                }
                                if (curAngle > 272 && curAngle <= 330) {
                                    outside = true;
                                    if (outside) {
                                        mCurrent = 60;
                                        deviceChild.setProtectSetTemp(mCurrent);
                                        deviceChildDao.update(deviceChild);
                                        tv_set_temp.setText(mCurrent + "℃");
                                        int curTemp = deviceChild.getProtectProTemp();
                                        tv_cur_temp.setText(curTemp + "℃");
//                                    send(deviceChild);
                                        outside = false;
                                    }

                                } else if ((curAngle > 330 && curAngle <= 360)) {
                                    outside = true;
                                    if (outside) {
                                        mCurrent = 48;

                                        deviceChild.setProtectSetTemp(mCurrent);
                                        deviceChildDao.update(deviceChild);
                                        tv_set_temp.setText(mCurrent + "℃");
                                        int curTemp = deviceChild.getProtectProTemp();
                                        tv_cur_temp.setText(curTemp + "℃");
//                                    send(deviceChild);
                                        outside = false;
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
                                    tv_cur_temp.setText(curTemp + "℃");

                                    if (seekbar.getEnd() == 1) {

                                        send(deviceChild);
                                        seekbar.setEnd(0);
                                        Message msg = handler.obtainMessage();
                                        msg.arg1 = 7;
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
            list = new ArrayList<>();
            String[] titles = {"分享设备", "亮度调节", "定时任务", "设备状态", "常见问题", "恢复设置"};
            for (int i = 0; i < titles.length; i++) {
                list.add(titles[i]);
            }
            adapter = new DeviceListAdapter(this, list);
            gradView.setAdapter(adapter);

            gradView.setOnItemClickListener(this);
            adapter.setSelectedPosition(mPoistion);

            IntentFilter intentFilter = new IntentFilter("DeviceListActivity");
            receiver = new MessageReceiver();
            registerReceiver(receiver, intentFilter);

            Intent service = new Intent(this, MQService.class);
            isBound = bindService(service, connection, Context.BIND_AUTO_CREATE);
            boolean online = deviceChild.getOnLint();
            Log.i("online", "-->" + online);
            String machineFall = deviceChild.getMachineFall();
            Log.i("machineFall", "-->" + machineFall);
            if (online) {
                if ("fall".equals(machineFall)) {
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    tv_offline.setText("设备已倾倒");
                    gradView.setVisibility(View.GONE);
                } else {
                    linearout.setVisibility(View.VISIBLE);
                    tv_offline.setVisibility(View.GONE);
                    gradView.setVisibility(View.VISIBLE);
                }
            } else {
                if ("fall".equals(machineFall)) {
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    tv_offline.setText("设备已倾倒");
                    gradView.setVisibility(View.GONE);
                } else {
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    tv_offline.setText("设备已离线");
                    gradView.setVisibility(View.GONE);
                }
            }
        } else {
            Toast.makeText(this, "设备已重置", Toast.LENGTH_SHORT).show();
            Intent intent2 = new Intent();
            intent2.putExtra("houseId", houseId);
            setResult(6000, intent2);
            finish();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String workMode = deviceChild.getWorkMode();
            String output = deviceChild.getOutputMod();
            String online = deviceChild.getDeviceState();
            try {
                switch (msg.arg1) {
                    case 2:
                        int current = msg.what;
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
                        if (semicBar != null) {
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
                    String houseId = deviceChild.getHouseId() + "";
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

    private void setMode(DeviceChild deviceChild) {

        String timerShutDown = deviceChild.getTimerShutdown();
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
//            model_protect.setEnabled(false);
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

    MessageReceiver receiver;

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                backgroundAlpha(1.0f);
                gradView.setVisibility(View.VISIBLE);
                return false;
            }
            application.removeActivity(this);
            Intent intent = new Intent();
            intent.putExtra("houseId", houseId);
            setResult(6000, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceChildDao.closeDaoSession();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (isBound) {
            if (connection != null) {
                unbindService(connection);
            }
        }
        running = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            //解绑界面元素
            unbinder.unbind();
        }
        application.removeActivity(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            backgroundAlpha(1.0f);
            return;
        }
        mPoistion = position;
        adapter.setSelectedPosition(mPoistion);
        adapter.notifyDataSetInvalidated();
        switch (position) {
            case 0:
                Intent intent = new Intent(DeviceListActivity.this, ShareDeviceActivity.class);
                intent.putExtra("deviceId", childPosition);
                startActivity(intent);
                break;
            case 1:
                popupWindow();
//                linearout2.setVisibility(View.VISIBLE);
//                gradView.setVisibility(View.GONE);
                break;
            case 2:
                Intent timeTask = new Intent(this, TimeTaskActivity.class);
                timeTask.putExtra("deviceId", childPosition);
                startActivity(timeTask);

//                Toast.makeText(this,"我的订阅",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Intent intent3 = new Intent(this, TempChartActivity.class);
                intent3.putExtra("deviceId", childPosition);
                startActivity(intent3);
                break;
            case 4:
                Intent intent4 = new Intent(this, ComProblemActivity.class);
                intent4.putExtra("deviceId", childPosition);
                startActivity(intent4);
                break;
            case 5:
                buildRestoreDialog();

                break;
            default:
                Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class PasteWeekAsync extends AsyncTask<Map<String, TextView>, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if ("恢复成功".equals(s)) {
                Utils.showToast(DeviceListActivity.this, "恢复成功");
            } else {
                Utils.showToast(DeviceListActivity.this, "恢复失败");
            }
        }

        @Override
        protected String doInBackground(Map<String, TextView>... maps) {
            int count = 0;
            String result = "";

            try {
                for (int timerTaskWeek = 1; timerTaskWeek <= 7; timerTaskWeek++) {
                    count++;
                    JSONObject jsonObject = new JSONObject();
                    List<Timer> timers = timeDao.findAll(deviceId, timerTaskWeek);
                    Collections.sort(timers, new Comparator<Timer>() {
                        @Override
                        public int compare(Timer o1, Timer o2) {
                            if (o1.getHour() > o2.getHour())
                                return 1;
                            else if (o1.getHour() < o2.getHour())
                                return -1;
                            return 0;
                        }
                    });
                    if (timers.size() == 24) {
                        jsonObject.put("timerTaskWeek", timerTaskWeek);

                        for (int i = 0; i < 24; i++) {
                            Timer timer = timers.get(i);
                            jsonObject.put("h" + i, "off");
                            jsonObject.put("t" + i, 0);
                        }
                        String jsonData = jsonObject.toString();
                        Log.i("jsonData", jsonData);

                        if (bound) {
                            boolean success = false;
                            String mac = deviceChild.getMacAddress();
                            String topicName;
                            topicName = "rango/" + mac + "/set";
                            success = mqService.publish(topicName, 1, jsonData);
                        }
                    }
                    if (count == 7) {
                        String mac = deviceChild.getMacAddress();
                        String topic = "rango/" + mac + "/set";

                        Log.i("connection", "-->" + count);
                        if (mqService != null && count != 168) {
                            try {
                                Log.i("ggggggggg", "-->" + "ggggggggggggggggg");
                                JSONObject jsonObject2 = new JSONObject();
                                jsonObject2.put("loadDate", "1");
                                String s = jsonObject2.toString();
                                boolean success = false;
                                success = mqService.publish(topic, 1, s);
                                if (!success) {
                                    success = mqService.publish(topic, 1, s);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        result = "恢复成功";
                    }
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("请稍后...");
            progressDialog.setCancelable(false);
            progressDialog.show();
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
            String mac = deviceChild.getMacAddress();
            String topic = "rango/" + mac + "/set";

            int count = timeDao.findAll(deviceId).size();
            Log.i("connection", "-->" + count);
            try {
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("loadDate", "1");
                String s2 = jsonObject2.toString();
                boolean success2 = false;
                success2 = mqService.publish(topic, 1, s2);
                Log.i("success", "-->" + success2);
                if (mqService != null && count != 168) {

                    Log.i("ggggggggg", "-->" + "ggggggggggggggggg");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("loadDate", "7");
                    String s = jsonObject.toString();
                    boolean success = false;
                    success = mqService.publish(topic, 1, s);
                    if (!success) {
                        success = mqService.publish(topic, 1, s);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceChild deviceChild2 = (DeviceChild) intent.getSerializableExtra("deviceChild");
            String online = intent.getStringExtra("online");
            String noNet = intent.getStringExtra("noNet");
            String machineFall = intent.getStringExtra("machineFall");
            String macAddress2 = intent.getStringExtra("macAddress2");
            String macAddress = intent.getStringExtra("macAddress");
            String macAddress3 = intent.getStringExtra("macAddress3");
            if (!Utils.isEmpty(macAddress3) && macAddress3.equals(deviceChild.getMacAddress())) {
                Utils.showToast(DeviceListActivity.this, "该设备类型已为受控机");
                Intent intent2 = new Intent();
                intent2.putExtra("houseId", houseId);
                DeviceListActivity.this.setResult(6000, intent2);
                DeviceListActivity.this.finish();
            } else if (!Utils.isEmpty(macAddress) && deviceChild2==null&& macAddress.equals(deviceChild.getMacAddress())) {
                Utils.showToast(DeviceListActivity.this, "该设备已被重置");
                Intent intent2 = new Intent();
                intent2.putExtra("houseId", houseId);
                DeviceListActivity.this.setResult(6000, intent2);
                DeviceListActivity.this.finish();
            } else if (!Utils.isEmpty(macAddress2) && macAddress2.equals(deviceChild.getMacAddress())) {
                String deviceName = intent.getStringExtra("deviceName");
                if (!TextUtils.isEmpty(deviceName)) {
                    tv_name.setText(deviceName);
                }
            } else {
                if (Utils.isEmpty(noNet)) {
                    if (deviceChild != null && deviceChild2 != null && deviceChild.getMacAddress().equals(deviceChild2.getMacAddress())) {
                        if ("online".equals(online)) {
                            if ("fall".equals(machineFall)) {
                                linearout.setVisibility(View.GONE);
                                tv_offline.setVisibility(View.VISIBLE);
                                tv_offline.setText("设备已倾倒");
                                gradView.setVisibility(View.GONE);
                            } else {
                                linearout.setVisibility(View.VISIBLE);
                                tv_offline.setVisibility(View.GONE);
                                gradView.setVisibility(View.VISIBLE);
                                setMode(deviceChild);
                                deviceChild = deviceChild2;
                                deviceChildDao.update(deviceChild);
                            }
                        }
                        else if ("offline".equals(online)) {

                            linearout.setVisibility(View.GONE);
                            tv_offline.setVisibility(View.VISIBLE);

                            tv_offline.setText("设备已离线");
                            gradView.setVisibility(View.GONE);
                            try {
                                String topic = "rango/" + macAddress + "/set";
                                JSONObject jsonObject2 = new JSONObject();
                                jsonObject2.put("loadDate", "1");
                                String s2 = jsonObject2.toString();
                                boolean success2 = false;
                                success2 = mqService.publish(topic, 1, s2);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }else {
                    if ("fall".equals(machineFall)) {
                        linearout.setVisibility(View.GONE);
                        tv_offline.setVisibility(View.VISIBLE);
                        tv_offline.setText("设备已倾倒");
                        gradView.setVisibility(View.GONE);
                    } else {
                        linearout.setVisibility(View.GONE);
                        tv_offline.setVisibility(View.VISIBLE);
                        tv_offline.setText("设备已离线");
                        gradView.setVisibility(View.GONE);
                    }
                }
            }
        }

    }

    private PopupWindow popupWindow;
    TextView tv_clock;
    MySeekBar mySeekBar;

    //底部popupWindow
    public void popupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popup_clockset, null);
        mySeekBar = (MySeekBar) view.findViewById(R.id.beautySeekBar1);
        mySeekBar.setOnSeekBarChangeListener(this);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        gradView.setVisibility(View.GONE);
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        backgroundAlpha(0.4f);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow.showAsDropDown(linearout, 0, 20);
    }

    int grade = 0;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.i("progress", "-->" + progress);
        float progress1 = progress;
        float progress2 = progress1 / 100;
        if (progress2 >= 0 && progress2 < 0.125) {
            grade = 1;
        } else if (progress2 >= 0.125 && progress2 < 0.25) {
            grade = 2;
        } else if (progress2 >= 0.25 && progress2 < 0.375) {
            grade = 3;
        } else if (progress2 >= 0.375 && progress2 < 0.5) {
            grade = 4;
        } else if (progress2 >= 0.5 && progress2 < 0.625) {
            grade = 5;
        } else if (progress2 >= 0.625 && progress2 < 0.75) {
            grade = 6;
        } else if (progress2 >= 0.75 && progress2 < 0.875) {
            grade = 7;
        } else if (progress2 >= 0.875 && progress2 < 1) {
            grade = 8;
        }
        Log.i("progress", "-->" + progress2);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.i("progress", "-->:onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i("progress", "-->:onStopTrackingTouch");
        Log.i("grade", "-->" + grade);
    }

    //设置蒙版
    private void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }

}