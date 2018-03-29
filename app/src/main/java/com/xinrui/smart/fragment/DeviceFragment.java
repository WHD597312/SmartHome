package com.xinrui.smart.fragment;

import android.app.Service;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinrui.database.dao.daoimpl.DeviceHomeDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.DeviceAdapter;
import com.xinrui.smart.pojo.DeviceHome;
import com.xinrui.smart.pojo.GroupEntry;
import com.xinrui.smart.pojo.GroupModel;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DeviceHomeDialog;
import com.xinrui.smart.view_custom.DividerItemDecoration;
import com.xinrui.smart.view_custom.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by win7 on 2018/3/8.
 */

public class DeviceFragment extends Fragment{
    private View view;
    private Unbinder unbinder;
    /** children items with a key and value list */
    @BindView(R.id.rv_list) RecyclerView rv_list;

    ArrayList<GroupEntry> groups;
    DeviceAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    private DeviceHomeDaoImpl deviceHomeDao;
    private List<Integer> list;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_device,container,false);
        unbinder=ButterKnife.bind(this,view);
        deviceHomeDao=new DeviceHomeDaoImpl(getActivity());
        rv_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        groups= GroupModel.getGroups(10,5);

        adapter=new DeviceAdapter(getActivity(),groups);
        rv_list.setAdapter(adapter);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    SharedPreferences preferences;

    @Override
    public void onStart() {
        super.onStart();


    }

    @OnClick({R.id.btn_add_residence})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_add_residence:
                buildDialog();
                break;
        }
    }
    private void buildDialog(){
        final DeviceHomeDialog dialog=new DeviceHomeDialog(getActivity());

        dialog.setOnNegativeClickListener(new DeviceHomeDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new DeviceHomeDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                String name=dialog.getName();
                DeviceHome home=new DeviceHome();
                home.setName(name);
                boolean success=deviceHomeDao.insert(home);
                if (success){
                    Utils.showToast(getActivity(),"创建成功");
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            Map<String,Object> param=new HashMap<>();
                            param.put("houseName","ssss");
                            param.put("location","sss");
                            param.put("userId",3);
                            HttpUtils.getOkHpptRequest(param);
                        }
                    }.start();
                    dialog.dismiss();
                }else {
                    Utils.showToast(getActivity(),"创建失败");
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }





}