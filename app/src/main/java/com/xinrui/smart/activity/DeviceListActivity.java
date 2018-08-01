package com.xinrui.smart.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.xinrui.secen.scene_view_custom.MySeekBar;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.device.AboutUsActivity;
import com.xinrui.smart.activity.device.ShareDeviceActivity;
import com.xinrui.smart.adapter.DeviceListAdapter;
import com.xinrui.smart.fragment.HeaterFragment;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener{

    GestureDetector mDetector;
    private Unbinder unbinder;
    @BindView(R.id.img_back)
    ImageView img_back;//返回键
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.relative) RelativeLayout relative;
    @BindView(R.id.gradView)
    GridView gradView;
    private int[] colors = {R.color.color_black, R.color.holo_orange_dark};
    private List<String> list;
    DeviceListAdapter adapter;
    private FragmentManager fragmentManager;
    //    @BindView(R.id.linearout2) LinearLayout linearout2;
//    @BindView(R.id.timePicker) TimePicker timePicker;
//    @BindView(R.id.datePicker) DatePicker datePicker;
//    @BindView(R.id.tv_clock) TextView tv_clock;
    @BindView(R.id.linearout) LinearLayout linearout;
    @BindView(R.id.tv_offline) TextView tv_offline;
    MyApplication application;
    private String childPosition;
    private DeviceChildDaoImpl deviceChildDao;
    public static boolean running=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        unbinder = ButterKnife.bind(this);
        if (application==null){
            application= (MyApplication) getApplication();
        }
        application.addActivity(this);
    }


    @OnClick({R.id.img_back, R.id.image_home,R.id.layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout:
                if (popupWindow!=null && popupWindow.isShowing()){
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                }
                gradView.setVisibility(View.VISIBLE);
                break;
            case R.id.img_back:
                if (popupWindow!=null && popupWindow.isShowing()){
                    popupWindow.dismiss();
                    backgroundAlpha(1.0f);
                    break;
                }
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra("deviceList","deviceList");
                startActivity(intent);
                break;
            case R.id.image_home:
                Intent intent2=new Intent(this,MainActivity.class);
                intent2.putExtra("deviceList","deviceList");
                startActivity(intent2);
                break;

        }
    }

    int mPoistion=-1;;// 选中的位置
    private int hour=0;
    int year=0;
    int month;
    int day=0;
    SharedPreferences preferences;
    DeviceChild deviceChild;

    @Override
    protected void onStart() {
        super.onStart();
        deviceChildDao=new DeviceChildDaoImpl(getApplicationContext());
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        childPosition=intent.getStringExtra("childPosition");

        deviceChild=deviceChildDao.findDeviceById(Long.parseLong(childPosition));

        if (deviceChild!=null){
            tv_name.setText(content);
            fragmentManager =getSupportFragmentManager();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HeaterFragment heaterFragment=new HeaterFragment();
            Bundle bundle=new Bundle();
            bundle.putSerializable("deviceChild",deviceChild);
            heaterFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.linearout, heaterFragment);
            int commit=fragmentTransaction.commit();
            Log.i("mmmmm",commit+"");

            list = new ArrayList<>();
            String[] titles = {"分享设备", "亮度调节", "定时任务", "设备状态", "常见问题", "恢复设置"};
            for (int i = 0; i < titles.length; i++) {
                list.add(titles[i]);
            }

            adapter=new DeviceListAdapter(this,list);
            gradView.setAdapter(adapter);

            gradView.setOnItemClickListener(this);
            adapter.setSelectedPosition(mPoistion);

            running=true;

            IntentFilter intentFilter = new IntentFilter("DeviceListActivity");
            receiver = new MessageReceiver();
            registerReceiver(receiver, intentFilter);
            boolean online=deviceChild.getOnLint();
            Log.i("online","-->"+online);
            String machineFall=deviceChild.getMachineFall();
            Log.i("machineFall","-->"+machineFall);
            if (online){
                if ("fall".equals(machineFall)){
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    tv_offline.setText("设备已倾倒");
                    gradView.setVisibility(View.GONE);
                }else {
                    linearout.setVisibility(View.VISIBLE);
                    tv_offline.setVisibility(View.GONE);
                    gradView.setVisibility(View.VISIBLE);
                }
            }else{
                if ("fall".equals(machineFall)){
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    tv_offline.setText("设备已倾倒");
                    gradView.setVisibility(View.GONE);
                }else {
                    linearout.setVisibility(View.GONE);
                    tv_offline.setVisibility(View.VISIBLE);
                    tv_offline.setText("设备已离线");
                    gradView.setVisibility(View.GONE);
                }
            }

        }else {
            Toast.makeText(this,"设备已重置",Toast.LENGTH_SHORT).show();
            Intent intent2=new Intent(this,MainActivity.class);
            intent2.putExtra("deviceList","deviceList");
            startActivity(intent2);
        }



    }

    MessageReceiver receiver;
    @Override
    protected void onResume() {
        super.onResume();
        running=true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (popupWindow!=null && popupWindow.isShowing()){
                popupWindow.dismiss();
                backgroundAlpha(1.0f);
                gradView.setVisibility(View.VISIBLE);
                return false;
            }
            application.removeActivity(this);
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("deviceList","deviceList");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceChildDao.closeDaoSession();
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        running=false;
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
        if (popupWindow!=null && popupWindow.isShowing()){
            popupWindow.dismiss();
            backgroundAlpha(1.0f);
            return;
        }
        mPoistion=position;
        adapter.setSelectedPosition(mPoistion);
        adapter.notifyDataSetInvalidated();
        switch (position){
            case 0:
                Intent intent=new Intent(DeviceListActivity.this, ShareDeviceActivity.class);
                intent.putExtra("deviceId",childPosition);
                startActivity(intent);
                break;
            case 1:
                popupWindow();
//                linearout2.setVisibility(View.VISIBLE);
//                gradView.setVisibility(View.GONE);
                break;
            case 2:
                Intent timeTask=new Intent(this,TimeTaskActivity.class);
                timeTask.putExtra("deviceId",childPosition);
                startActivity(timeTask);

//                Toast.makeText(this,"我的订阅",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Intent intent3=new Intent(this,TempChartActivity.class);
                intent3.putExtra("deviceId",childPosition);
                startActivity(intent3);
                break;
            case 4:
                Intent intent4=new Intent(this,ComProblemActivity.class);
                intent4.putExtra("deviceId",childPosition);
                startActivity(intent4);
                break;
            case 5:
                Intent intent5=new Intent(this, AboutUsActivity.class);
                startActivity(intent5);
                break;
            default:
                Toast.makeText(this,"1",Toast.LENGTH_SHORT).show();
                break;
        }
    }
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceChild deviceChild2= (DeviceChild) intent.getSerializableExtra("deviceChild");
            String online=intent.getStringExtra("online");
            String noNet=intent.getStringExtra("noNet");
            String machineFall=intent.getStringExtra("machineFall");
            Log.i("machineFall","-->"+machineFall);
            String macAddress2=intent.getStringExtra("macAddress2");
            String macAddress=intent.getStringExtra("macAddress");
            String macAddress3=intent.getStringExtra("macAddress3");
            if (!Utils.isEmpty(macAddress3)  && macAddress3.equals(deviceChild.getMacAddress())){
                Utils.showToast(DeviceListActivity.this,"该设备类型已为受控机");
                Intent intent2=new Intent(DeviceListActivity.this,MainActivity.class);
                intent2.putExtra("deviceList","deviceList");
                startActivity(intent2);
            } else if (!Utils.isEmpty(macAddress) && macAddress.equals(deviceChild.getMacAddress())){
                Utils.showToast(DeviceListActivity.this,"该设备已被重置");
                Intent intent2=new Intent(DeviceListActivity.this,MainActivity.class);
                intent2.putExtra("deviceList","deviceList");
                startActivity(intent2);
            } else if (!Utils.isEmpty(macAddress2) && macAddress2.equals(deviceChild.getMacAddress())){
                String deviceName=intent.getStringExtra("deviceName");
                if (!TextUtils.isEmpty(deviceName)) {
                    tv_name.setText(deviceName);
                }
//                Intent intent2=new Intent(DeviceListActivity.this,MainActivity.class);
//                intent2.putExtra("deviceList","deviceList");
//                startActivity(intent2);
            }else {
                if(Utils.isEmpty(noNet)){
                    if (deviceChild!=null && deviceChild2!=null &&deviceChild.getMacAddress().equals(deviceChild2.getMacAddress())){
                        if ("online".equals(online)){
                            if ("fall".equals(machineFall)){
                                linearout.setVisibility(View.GONE);
                                tv_offline.setVisibility(View.VISIBLE);
                                tv_offline.setText("设备已倾倒");
                                gradView.setVisibility(View.GONE);
                            }else {
                                linearout.setVisibility(View.VISIBLE);
                                tv_offline.setVisibility(View.GONE);
                                gradView.setVisibility(View.VISIBLE);
                            }
                        }else if ("offline".equals(online)){
                            linearout.setVisibility(View.GONE);
                            tv_offline.setVisibility(View.VISIBLE);
                            tv_offline.setText("设备已离线");
                            gradView.setVisibility(View.GONE);
                        }
                    }
                }else {
                    if ("fall".equals(deviceChild.getMachineFall())){
                        linearout.setVisibility(View.GONE);
                        tv_offline.setVisibility(View.VISIBLE);
                        tv_offline.setText("设备已倾倒");
                        gradView.setVisibility(View.GONE);
                    }else {
                        linearout.setVisibility(View.GONE);
                        tv_offline.setVisibility(View.VISIBLE);
                        gradView.setVisibility(View.GONE);
                        tv_offline.setText("设备已离线");
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
        backgroundAlpha(0.6f);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow.showAsDropDown(linearout, 0, 20);

    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
    //设置蒙版
    private void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }

}