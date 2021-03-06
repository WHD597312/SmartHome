package com.xinrui.smart.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.SmartFragmentAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.SmartSet;
import com.xinrui.smart.util.mqtt.MQService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/20.
 */

public class SmartFragmentManager extends Fragment {
    @BindView(R.id.viewpager)
    ViewPager mPager;
    @BindView(R.id.linearout)
    LinearLayout linearout;
    List<Fragment> fragmentList;

    DeviceGroupDaoImpl deviceGroupDao;
    DeviceChildDaoImpl deviceChildDao;
    List<DeviceGroup> deviceGroups;
    ImageView[] imageViews;
    View view;
    Unbinder unbinder;
    public static boolean running = false;
    SharedPreferences preferences;

    FragmentPagerAdapter fragmentPagerAdapter;
    MyOnPageChangeListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_smart_manager, container, false);
        unbinder = ButterKnife.bind(this, view);
        //初始化fragment

        deviceGroupDao = new DeviceGroupDaoImpl(MyApplication.getContext());
        deviceGroups = deviceGroupDao.findAllDevices();
        deviceChildDao = new DeviceChildDaoImpl(MyApplication.getContext());
        fragmentList = new ArrayList<>();
//        fragmentList.clear();
        for (int i = 0; i < deviceGroups.size() - 1; i++) {
            DeviceGroup deviceGroup = deviceGroups.get(i);
            String houseId = deviceGroup.getId() + "";
            SmartFragment smartFragment = new SmartFragment();
            smartFragment.setHouseId(houseId);
            fragmentList.add(smartFragment);
        }

        Log.i("SmartFragmentManager", "-->onStart");
        fragmentPagerAdapter = new SmartFragmentAdapter(getChildFragmentManager(), fragmentList);
        mPager.setAdapter(fragmentPagerAdapter);
        listener = new MyOnPageChangeListener(getActivity(), mPager, linearout, fragmentList.size());
        mPager.addOnPageChangeListener(listener);
        Log.i("SmartFragmentManager", "-->onCreateView");
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }

//        if (isBound){
//            if (connection != null) {
//                getActivity().unbindService(connection);
//            }
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        preferences = getActivity().getSharedPreferences("smart", Context.MODE_PRIVATE);
        if (preferences.contains("position")) {
            int postion = preferences.getInt("position", 0);
            Log.i("smart12222222", "-->" + postion);
            Bundle bundle = getArguments();
            if (bundle != null) {
                postion = bundle.getInt("position");
            }
            mPager.setCurrentItem(postion);
            listener.onPageSelected(postion);
        } else {
            mPager.setCurrentItem(0);
            listener.onPageSelected(0);
        }
    }


    private boolean isBound = false;

    @Override
    public void onResume() {
        super.onResume();
        running = true;
//        Intent intent = new Intent(getActivity(), MQService.class);
//        isBound=getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

//        IntentFilter intentFilter = new IntentFilter("SmartFragmentManager");
//        receiver = new MessageReceiver();
//        getActivity().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        running = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if (receiver != null) {
//            getActivity().unregisterReceiver(receiver);
//        }
        deviceGroupDao.closeDaoSession();
        deviceChildDao.closeDaoSession();
//        handler.removeCallbacksAndMessages(null);

    }


    //实现页面变化监听器OnPageChangeListener
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private Context context;
        private ViewPager viewPager;
        private LinearLayout dotLayout;
        private int size;
        private int img1 = R.drawable.shape_circle_point_selected, img2 = R.drawable.shape_circle_point_unselected;
        private int imgSize = 20;
        private List<ImageView> dotViewLists = new ArrayList<>();

        public MyOnPageChangeListener(Context context, ViewPager viewPager, LinearLayout dotLayout, int size) {
            this.context = context;
            this.viewPager = viewPager;
            this.dotLayout = dotLayout;
            this.size = size;

            for (int i = 0; i < size; i++) {
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                //为小圆点左右添加间距
                params.leftMargin = 10;
                params.rightMargin = 10;
                //手动给小圆点一个大小
                params.height = imgSize;
                params.width = imgSize;
                if (i == 0) {
                    imageView.setBackgroundResource(img1);
                } else {
                    imageView.setBackgroundResource(img2);
                }
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                //为LinearLayout添加ImageView
                dotLayout.addView(imageView, params);
                dotViewLists.add(imageView);
            }
        }

        @Override
        //当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法会一直得到调用。
        /**
         * arg0:当前页面，及你点击滑动的页面
         * arg1:当前页面偏移的百分比
         *arg2:当前页面偏移的像素位置
         */
        public void onPageScrolled(int position, float arg1, int arg2) {
            for (int i = 0; i < size; i++) {
                //选中的页面改变小圆点为选中状态，反之为未选中
                if ((position % size) == i) {
                    ((View) dotViewLists.get(i)).setBackgroundResource(img1);
                } else {
                    ((View) dotViewLists.get(i)).setBackgroundResource(img2);
                }
            }
        }

        @Override
        //当页面状态改变的时候调用
        /**
         * arg0
         *  1:表示正在滑动
         *  2:表示滑动完毕
         *  0:表示什么都没做，就是停在那
         */
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        //页面跳转完后调用此方法
        /**
         * arg0是页面跳转完后得到的页面的Position（位置编号）。
         */
        public void onPageSelected(int position) {
//            Message msg = handler.obtainMessage();
//            msg.what = position;
//            handler.sendMessage(msg);
            Log.i("smartposition", "-->" + position);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("position", position);
            editor.commit();
        }
    }

//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//                try {
//                    int position=msg.what;
//                    Log.i("posistion","-->"+position);
//                    if (position==0){
//                        mPager.setCurrentItem(0);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//        }
//    };

//    MessageReceiver receiver;
//
//    class MessageReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            DeviceChild deviceChild2 = (DeviceChild) intent.getSerializableExtra("deviceChild");
//            try {
//                deviceChild = deviceChild2;
//                deviceChildDao.update(deviceChild);
//                if (deviceChild != null) {
//                    int postion = preferences.getInt("postion", 0);
//                    SmartFragment smartFragment = (SmartFragment) fragmentList.get(postion);
//                    if (smartFragment != null) {
//                        int extTemp = deviceChild.getTemp();
//                        int extHum = deviceChild.getHum();
//                        smartFragment.temp.setText(extTemp + "℃");
//                        smartFragment.tv_cur_temp.setText(extTemp + "℃");
//                        smartFragment.hum.setText(extHum + "%");
//                        smartFragment.tv_hum.setText(extHum + "%");
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

//    MQService mqService;
//    private boolean bound = false;
//    ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            MQService.LocalBinder binder = (MQService.LocalBinder) service;
//            mqService = binder.getService();
//            bound = true;
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            bound = false;
//        }
//    };
}
