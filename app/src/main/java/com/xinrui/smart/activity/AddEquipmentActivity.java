package com.xinrui.smart.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.EquipmentAdapter;
import com.xinrui.smart.pojo.Equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/29.
 */

public class AddEquipmentActivity extends Activity {
    @BindView(R.id.return_button)
    ImageButton returnButton;
    //这个是checkbox的Hashmap集合
    private HashMap<Integer, Boolean> map = new HashMap<>();
    private Context mContext;
    private List<Equipment> equipment_list = new ArrayList<>();

    private EquipmentAdapter equipmentAdapter;
    private LinearLayoutManager linearLayoutManager;
    @OnClick(R.id.return_button)
    public void return_MainActivity(){
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_equipment);
        ButterKnife.bind(this);

        initData();

        //初始化控件
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.add_equipment);
        //在加载数据之前配置
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        //创建一个适配器
        equipmentAdapter = new EquipmentAdapter(equipment_list);
        recyclerView.setAdapter(equipmentAdapter);
    }

    public void initData() {
        equipment_list = new ArrayList<>();
        map = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            Equipment equipment = new Equipment("智能" + i, R.drawable.equipment, false);
            //添加30条数据
            equipment_list.add(equipment);
            map.put(i, false);
        }
    }
}
