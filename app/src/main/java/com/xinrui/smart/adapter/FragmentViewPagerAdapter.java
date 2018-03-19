package com.xinrui.smart.adapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by win7 on 2018/3/14.
 */

/**
 *
 * 将FragmentPagerAdapter 替换成FragmentStatePagerAdapter，因为前者只要加载过，
 * fragment中的视图就一直在内存中，在这个过程中无论你怎么刷新，清除都是无用的，直至程序退出； 后者可以满足我们的需求。
 */

public class FragmentViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;


    public FragmentViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    /**
     * 返回 POSITION_NONE;  ，表示这个页换了，会调用destroyItem()方法删除，再调用instantiateItem()方法，创建。
     返回 POSITION_UNCHANGED，表示没换，什么也不做。
     * @param object
     * @return
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
