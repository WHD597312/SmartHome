package com.xinrui.smart.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.SmartFragmentAdapter;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.pojo.SmartSet;

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
    @BindView(R.id.viewpager) ViewPager mPager;
    List<Fragment> fragmentList;

    DeviceGroupDaoImpl deviceGroupDao;
    DeviceChildDaoImpl deviceChildDao;
    List<DeviceGroup> deviceGroups;
    ImageView[] imageViews;
    View view;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.fragment_smart_manager,container,false);
        unbinder=ButterKnife.bind(this,view);
        return view;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        //初始化fragment
        deviceGroupDao=new DeviceGroupDaoImpl(getActivity());
        deviceGroups=deviceGroupDao.findAllDevices();
        deviceChildDao=new DeviceChildDaoImpl(getActivity());
        fragmentList=new ArrayList<Fragment>();
        for (DeviceGroup deviceGroup:deviceGroups){
            fragmentList.add(new SmartFragment());
        }

        FragmentPagerAdapter fragmentPagerAdapter=new SmartFragmentAdapter(getChildFragmentManager(),fragmentList);
        LinearLayout layout= (LinearLayout) view.findViewById(R.id.linearout);
        mPager.setAdapter(fragmentPagerAdapter);
        mPager.setCurrentItem(0);
        MyOnPageChangeListener listener=new MyOnPageChangeListener(getActivity(),mPager,layout,fragmentList.size());
        listener.onPageSelected(0);
        mPager.setOnPageChangeListener(listener);


        Message msg=handler.obtainMessage();
        msg.what=0;
        handler.sendMessage(msg);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        public void onPageSelected(int poistion) {
            Message msg=handler.obtainMessage();
            msg.what=poistion;
            handler.sendMessage(msg);
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int postion=msg.what;
            DeviceGroup deviceGroup=deviceGroups.get(postion);
            if (deviceGroup!=null){
                try {
                    String header=deviceGroup.getHeader();
                    SmartFragment smartFragment= (SmartFragment) fragmentList.get(postion);
                    if (smartFragment!=null){
                        smartFragment.houseId=deviceGroup.getId()+"";
                        smartFragment.tv_home.setText(header);
                        List<DeviceChild> deviceChildren=deviceChildDao.findDeviceType(deviceGroup.getId(),2);//外置传感器
                        if (deviceChildren.isEmpty()){
                            smartFragment.relative.setVisibility(View.GONE);
                        }else {
                            smartFragment.relative.setVisibility(View.VISIBLE);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };
}
