package com.xinrui.smart.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddRoomActivity;
import com.xinrui.smart.activity.CustomRoomActivity;
import com.xinrui.smart.adapter.DragAdapter;
import com.xinrui.smart.adapter.FragmentViewPagerAdapter;
import com.xinrui.smart.pojo.Room;
import com.xinrui.smart.view_custom.DragGridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/19.
 */

public class LiveFragment extends Fragment {
    @BindView(R.id.add_room)
    Button addRoom;
    @BindView(R.id.custom_house_type)
    Button customHouseType;
    @BindView(R.id.copy_and_paste)
    Button copyAndPaste;
    @BindView(R.id.delete)
    Button delete;
    @BindView(R.id.btn1)
    Button btn1;
    @BindView(R.id.btn2)
    Button btn2;
    @BindView(R.id.btn3)
    Button btn3;
    @BindView(R.id.btn4)
    Button btn4;
    @BindView(R.id.new_btn)
    Button btn5;

    int postion_current;

    private ImageView drawing_room,bedroom,toilet,study;

    private ViewPager viewPager;

//    private LayoutInflater btn1_viewPager,btn2_viewPager,btn3_viewPager,btn4_viewPager;

    private View one_pager,two_pager,three_pager,four_pager;

    private List<Fragment> fragmentslist;

    private  int current_key = 1;
    private int add_key = 1;
    private boolean isestablied = false;
    private List<HashMap<String, Object>> dataSourceList = new ArrayList<>();

    private List<Room> roomList = new ArrayList<>();


    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    FragmentViewPagerAdapter fragmentViewPagerAdapter;


    Btn1_fragment btn1_fragment;
    Btn2_fragment btn2_fragment;
    Btn3_fragment btn3_fragment;
    Btn4_fragment btn4_fragment;
    Default_fragment default_fragment;

    View view;
    Unbinder unbinder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_live,container,false);
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
   
        fragmentslist = new ArrayList<>();

        

        /**
         * FragmentManager,FragmentTransaction管理类
         */
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        //实例化fragment
//        btn1_fragment = new Btn1_fragment();
//        btn2_fragment = new Btn2_fragment();
//        btn3_fragment = new Btn3_fragment();
//        btn4_fragment = new Btn4_fragment();
//        default_fragment = new Default_fragment();

//        //将fragment添加到布局中
//        fragmentTransaction.add(R.id.frag1,btn1_fragment);
//        fragmentTransaction.add(R.id.frag2,btn2_fragment);
//        fragmentTransaction.add(R.id.frag3,btn3_fragment);
//        fragmentTransaction.add(R.id.frag4,btn4_fragment);

        //提交
//        fragmentTransaction.commit();


        initView();


        DragGridView mDragGridView = (DragGridView) view.findViewById(R.id.dragGridView);


        for (int i = 0; i < 4; i++) {
            HashMap<String, Object> itemHashMap = new HashMap<>();
            itemHashMap.put("item_image", R.drawable.com_tencent_open_notice_msg_icon_big);
            itemHashMap.put("item_text", "房间" + Integer.toString(i));
            dataSourceList.add(itemHashMap);
        }
        final DragAdapter mDragAdapter = new DragAdapter(getActivity(), dataSourceList);

        // mDragGridView.setAdapter(mDragAdapter);

//        mDragGridView.setOnChangeListener(new DragGridView.OnChanageListener() {
//            @Override
//            public void onChange(int from, int to) {
//                HashMap<String, Object> temp = dataSourceList.get(from);
//
//                //直接交互item
//                dataSourceList.set(from, dataSourceList.get(to));
//                dataSourceList.set(to, temp);
//                dataSourceList.set(to, temp);


//                if(from < to){
//                    for (int i = 0; i < to; i++) {
//                        Collections.swap(dataSourceList,i,i+1);
//                    }
//                }else if(from > to){
//                    for (int i = 0; i > to; i++) {
//                        Collections.swap(dataSourceList,i,i-1);
//                    }
//                }
//
//                dataSourceList.set(to, temp);
//
//                mDragAdapter.notifyDataSetChanged();
//            }
//        });
    }

    //初始化View
    public void initView() {
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);
        btn4.setVisibility(View.GONE);
        btn1.setBackgroundColor(getResources().getColor(R.color.floor_button));
        btn1.setTextColor(getResources().getColor(R.color.white));

        viewPager = (ViewPager) view.findViewById(R.id.fragment_viewPager);

        btn1_fragment = new Btn1_fragment();
        fragmentslist.add(btn1_fragment);
//        btn2_fragment = new Btn2_fragment();
//        fragmentslist.add(btn2_fragment);
//        btn3_fragment = new Btn3_fragment();
//        fragmentslist.add(btn3_fragment);
//        btn4_fragment = new Btn4_fragment();
//        fragmentslist.add(btn4_fragment);
//        default_fragment = new Default_fragment();
//        fragmentslist.add(default_fragment);

        fragmentViewPagerAdapter = new FragmentViewPagerAdapter(
                getChildFragmentManager(),fragmentslist);

        viewPager.setAdapter(fragmentViewPagerAdapter);
        //viewPager滑动监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Button btns[] = {btn1,btn2,btn3,btn4};
                for (int i = 0; i < fragmentslist.size(); i++) {
                    if(position == i){
                        btns[i].setBackgroundColor(getResources().getColor(R.color.floor_button));
                        btns[i].setTextColor(getResources().getColor(R.color.white));
                    }else{
                        btns[i].setBackgroundResource(R.drawable.floor_frame_colour);
                        btns[i].setTextColor(getResources().getColor(R.color.floor_button));
                    }
                }
                current_key = position+1;
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        viewPager.setCurrentItem(0);
    }

    @OnClick({R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.new_btn, R.id.add_room, R.id.custom_house_type, R.id.copy_and_paste, R.id.delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                method_btn_1();
                break;
            case R.id.btn2:
                method_btn_2();
                break;
            case R.id.btn3:
                method_btn_3();
                break;
            case R.id.btn4:
                method_btn_4();
                break;
            case R.id.new_btn:
                method_new_btn();
                break;
            case R.id.add_room:
                Intent add_room = new Intent(getActivity(), AddRoomActivity.class);
                startActivity(add_room);
                break;
            case R.id.custom_house_type:
                Intent custom_house_type = new Intent(getActivity(), CustomRoomActivity.class);
                startActivity(custom_house_type);
                break;
            case R.id.copy_and_paste:
                method_copy_paste_btn();
                break;
            case R.id.delete:
                method_delete_btn();
                break;
        }
    }

    //展示，删除，添加各层
    public void method_btn_1() {
        if (current_key == 2) {
            btn1.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn1.setTextColor(getResources().getColor(R.color.white));
            btn2.setBackgroundResource(R.drawable.floor_frame_colour);
            btn2.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn1_fragment);
//            fragmentTransaction.commit();

            current_key = 1;
        } else if (current_key == 3) {
            btn1.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn1.setTextColor(getResources().getColor(R.color.white));
            btn3.setBackgroundResource(R.drawable.floor_frame_colour);
            btn3.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn1_fragment);
//            fragmentTransaction.commit();
            current_key = 1;
        } else if (current_key == 4) {
            btn1.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn1.setTextColor(getResources().getColor(R.color.white));
            btn4.setBackgroundResource(R.drawable.floor_frame_colour);
            btn4.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn1_fragment);
//            fragmentTransaction.commit();
            current_key = 1;
        }
        viewPager.setCurrentItem(0);
        Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
    }

    public void method_btn_2() {
        if (current_key == 3) {
            btn2.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn2.setTextColor(getResources().getColor(R.color.white));
            btn3.setBackgroundResource(R.drawable.floor_frame_colour);
            btn3.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//            fragmentTransaction.commit();
            current_key = 2;
        } else if (current_key == 4) {
            btn2.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn2.setTextColor(getResources().getColor(R.color.white));
            btn4.setBackgroundResource(R.drawable.floor_frame_colour);
            btn4.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//            fragmentTransaction.commit();
            current_key = 2;
        } else if (current_key == 1) {
            btn2.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn2.setTextColor(getResources().getColor(R.color.white));
            btn1.setBackgroundResource(R.drawable.floor_frame_colour);
            btn1.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//            fragmentTransaction.commit();
            current_key = 2;
        }
        viewPager.setCurrentItem(1);
        Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
    }

    public void method_btn_3() {
        if (current_key == 4) {
            btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn3.setTextColor(getResources().getColor(R.color.white));
            btn4.setBackgroundResource(R.drawable.floor_frame_colour);
            btn4.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//            fragmentTransaction.commit();
            current_key = 3;
        } else if (current_key == 2) {
            btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn3.setTextColor(getResources().getColor(R.color.white));
            btn2.setBackgroundResource(R.drawable.floor_frame_colour);
            btn2.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//            fragmentTransaction.commit();
            current_key = 3;
        } else if (current_key == 1) {
            btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn3.setTextColor(getResources().getColor(R.color.white));
            btn1.setBackgroundResource(R.drawable.floor_frame_colour);
            btn1.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//            fragmentTransaction.commit();
            current_key = 3;
        }
        viewPager.setCurrentItem(2);
        Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
    }

    public void method_btn_4() {
        if (current_key == 1) {
            btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn4.setTextColor(getResources().getColor(R.color.white));
            btn1.setBackgroundResource(R.drawable.floor_frame_colour);
            btn1.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//            fragmentTransaction.commit();
            current_key = 4;
        } else if (current_key == 2) {
            btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn4.setTextColor(getResources().getColor(R.color.white));
            btn2.setBackgroundResource(R.drawable.floor_frame_colour);
            btn2.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//            fragmentTransaction.commit();
            current_key = 4;
        } else if (current_key == 3) {
            btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn4.setTextColor(getResources().getColor(R.color.white));
            btn3.setBackgroundResource(R.drawable.floor_frame_colour);
            btn3.setTextColor(getResources().getColor(R.color.floor_button));
//            fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//            fragmentTransaction.commit();
            current_key = 4;
        }
        viewPager.setCurrentItem(3);
        Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
    }

    public void method_new_btn() {
        if (!isestablied) {
            if(add_key == 0){
                btn1.setVisibility(View.VISIBLE);
                current_key = 1;
                addPage(add_key);
                add_key++;
            }else if (add_key == 1) {
                if (current_key == 1) {
                    btn2.setVisibility(View.VISIBLE);
                    btn1.setBackgroundResource(R.drawable.floor_frame_colour);//背景变为白色，文字变为绿色（表示非选中按钮）
                    btn1.setTextColor(getResources().getColor(R.color.floor_button));
                    btn2.setBackgroundColor(getResources().getColor(R.color.floor_button));//背景变为绿色，文字变为白色（表示当前选中按钮）
                    btn2.setTextColor(getResources().getColor(R.color.white));
//                    fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//                    fragmentTransaction.commit();
                    current_key = 2;
                    addPage(add_key);
                    add_key++;
                }
            } else if (add_key == 2) {
                if (current_key == 1) {
                    btn3.setVisibility(View.VISIBLE);
                    btn2.setBackgroundResource(R.drawable.floor_frame_colour);
                    btn2.setTextColor(getResources().getColor(R.color.floor_button));
                    btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
                    btn3.setTextColor(getResources().getColor(R.color.white));
//                    fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//                    fragmentTransaction.commit();
                    current_key = 3;
                    addPage(add_key);
                    add_key++;
                } else if (current_key == 2) {
                    btn3.setVisibility(View.VISIBLE);
                    btn2.setBackgroundResource(R.drawable.floor_frame_colour);
                    btn2.setTextColor(getResources().getColor(R.color.floor_button));
                    btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
                    btn3.setTextColor(getResources().getColor(R.color.white));
//                    fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//                    fragmentTransaction.commit();
                    current_key = 3;
                    addPage(add_key);
                    add_key++;
                }
            } else if (add_key == 3) {
                if (current_key == 1) {
                    btn4.setVisibility(View.VISIBLE);
                    btn1.setBackgroundResource(R.drawable.floor_frame_colour);
                    btn1.setTextColor(getResources().getColor(R.color.floor_button));
                    btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
                    btn4.setTextColor(getResources().getColor(R.color.white));
//                    fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//                    fragmentTransaction.commit();
                    current_key = 4;
                    addPage(add_key);
                    add_key++;
                    isestablied = true;
                } else if (current_key == 2) {
                    btn4.setVisibility(View.VISIBLE);
                    btn2.setBackgroundResource(R.drawable.floor_frame_colour);
                    btn2.setTextColor(getResources().getColor(R.color.floor_button));
                    btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
                    btn3.setTextColor(getResources().getColor(R.color.white));
//                    fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//                    fragmentTransaction.commit();
                    current_key = 4;
                    addPage(add_key);
                    add_key++;
                    isestablied = true;
                } else if (current_key == 3) {
                    btn4.setVisibility(View.VISIBLE);
                    btn3.setBackgroundResource(R.drawable.floor_frame_colour);
                    btn3.setTextColor(getResources().getColor(R.color.floor_button));
                    btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
                    btn4.setTextColor(getResources().getColor(R.color.white));
//                    fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//                    fragmentTransaction.commit();
                    current_key = 4;
                    addPage(add_key);
                    add_key++;
                    isestablied = true;
                }
            }
        }
        Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
    }

    public void method_delete_btn() {
        int postion_delete = current_key - 1; //删除页面的序号
        if(add_key == 0){
            Toast.makeText(getActivity(), "数据全部清除，无法再继续删除", Toast.LENGTH_LONG).show();
        }else if (add_key == 1) {
            btn1.setVisibility(View.GONE);
            delPage(postion_delete);
            add_key = 0;
        } else if (add_key == 2) {
            if (current_key == 1) {
                btn2.setVisibility(View.GONE);
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//                fragmentTransaction.commit();
                current_key = 1;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
            } else if (current_key == 2) {
                btn2.setVisibility(View.GONE);
                btn1.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn1.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn1_fragment);
//                fragmentTransaction.commit();
                current_key = 1;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
            }

        } else if (add_key == 3) {
            if (current_key == 1) {
                btn3.setVisibility(View.GONE);
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//                fragmentTransaction.commit();
                current_key = 1;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
            } else if (current_key == 2) {
                btn3.setVisibility(View.GONE);
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//                fragmentTransaction.commit();
                current_key = 2;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();

            } else if (current_key == 3) {
                btn3.setVisibility(View.GONE);
                btn2.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn2.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//                fragmentTransaction.commit();
                current_key = 2;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
            }
        } else if (add_key == 4) {
            if (current_key == 1) {
                btn4.setVisibility(View.GONE);
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//                fragmentTransaction.commit();
                current_key = 1;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();

            } else if (current_key == 2) {
                btn4.setVisibility(View.GONE);
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//                fragmentTransaction.commit();
                current_key = 2;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();

            } else if (current_key == 3) {
                btn4.setVisibility(View.GONE);
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn4_fragment);
//                fragmentTransaction.commit();
                current_key = 3;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();

            } else if (current_key == 4) {
                btn4.setVisibility(View.GONE);
                btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn3.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//                fragmentTransaction.commit();
                current_key = 3;
                add_key--;
                isestablied = false;
                delPage(postion_delete);
                Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void method_copy_paste_btn() {
        postion_current = add_key;//当前页面序号
        if(add_key == 0){
            Toast.makeText(getActivity(),"没有数据，无法复制，请创建",Toast.LENGTH_LONG).show();
        }else if (add_key == 1) {
            btn2.setVisibility(View.VISIBLE);
            btn1.setBackgroundResource(R.drawable.floor_frame_colour);
            btn1.setTextColor(getResources().getColor(R.color.floor_button));
            btn2.setBackgroundColor(getResources().getColor(R.color.floor_button));
            btn2.setTextColor(getResources().getColor(R.color.white));

            copyPage(postion_current);

            current_key = 2;
            add_key++;

        } else if (add_key == 2) {
            if (current_key == 1) {
                btn3.setVisibility(View.VISIBLE);
                btn1.setBackgroundResource(R.drawable.floor_frame_colour);
                btn1.setTextColor(getResources().getColor(R.color.floor_button));
                btn2.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn2.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn1_fragment);
//                fragmentTransaction.commit();

                copyPage(postion_current);
                current_key = 3;
                add_key++;
            } else if (current_key == 2) {
                btn3.setVisibility(View.VISIBLE);
                btn2.setBackgroundResource(R.drawable.floor_frame_colour);
                btn2.setTextColor(getResources().getColor(R.color.floor_button));
                btn3.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn3.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//                fragmentTransaction.commit();

                copyPage(postion_current);
                current_key = 3;
                add_key++;
            }
        } else if (add_key == 3) {
            if (current_key == 1) {
                btn4.setVisibility(View.VISIBLE);
                btn1.setBackgroundResource(R.drawable.floor_frame_colour);
                btn1.setTextColor(getResources().getColor(R.color.floor_button));
                btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn4.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn1_fragment);
//                fragmentTransaction.commit();

                copyPage(postion_current);
                current_key = 4;
                add_key++;
            } else if (current_key == 2) {
                btn4.setVisibility(View.VISIBLE);
                btn2.setBackgroundResource(R.drawable.floor_frame_colour);
                btn2.setTextColor(getResources().getColor(R.color.floor_button));
                btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn4.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn2_fragment);
//                fragmentTransaction.commit();


                copyPage(postion_current);
                current_key = 4;
                add_key++;
            } else if (current_key == 3) {
                btn4.setVisibility(View.VISIBLE);
                btn3.setBackgroundResource(R.drawable.floor_frame_colour);
                btn3.setTextColor(getResources().getColor(R.color.floor_button));
                btn4.setBackgroundColor(getResources().getColor(R.color.floor_button));
                btn4.setTextColor(getResources().getColor(R.color.white));
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frag1, btn3_fragment);
//                fragmentTransaction.commit();


                copyPage(postion_current);
                current_key = 4;
                add_key++;
            }
            isestablied = true;
        } else if (add_key == 4) {
            Toast.makeText(getActivity(), "无法继续增加", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getActivity(), "current_key:" + current_key + "isestablied:" + isestablied + ";" + "add_key:" + add_key, Toast.LENGTH_LONG).show();

    }

    /**
     *新增一层页面
     */
    public void addPage(int add_key){
        default_fragment = new Default_fragment();
        fragmentslist.add(default_fragment);
        int postion = viewPager.getCurrentItem();
        Toast.makeText(getActivity(),"postion:"+postion,Toast.LENGTH_LONG).show();
        fragmentViewPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(add_key);
    }

    /**
     *复制当前页面
     */
    public void copyPage(int new_postion){
        int postion = viewPager.getCurrentItem();
        Copy_fragment copy_fragment = new Copy_fragment();
        fragmentslist.add(copy_fragment);


        ImageView imageView = (ImageView) fragmentslist.get(postion).getView().findViewById(R.id.iv_default);
        TextView textView = (TextView) fragmentslist.get(postion).getView().findViewById(R.id.tv_default);


        fragmentViewPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(new_postion);

//
//        Fragment fragment = fragmentslist.get(postion);
//        fragmentTransaction.replace(R.id.btn3, copy_fragment);
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

    }
    /**
     * 删除当前页面
     */
    public void delPage(int postion_delete){
        int position = viewPager.getCurrentItem();//获取当前页面位置
        fragmentslist.remove(position);//删除一项数据源中的数据
        viewPager.setCurrentItem(postion_delete);//postion_delete当前页面的序号，删除后跳转到
        fragmentViewPagerAdapter.notifyDataSetChanged();//通知UI更新

    }
}
