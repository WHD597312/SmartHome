package com.xinrui.smart.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.ImageButton;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.RoomtypeAdapter;
import com.xinrui.smart.pojo.RoomType;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/29.
 */

public class RoomTypesActivity extends Activity {
    @BindView(R.id.return_button)
    ImageButton returnButton;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private List<RoomType> mRoomtypelsit = new ArrayList<>();
    private Context mContext;
    @OnClick(R.id.return_button)
    public void rollback ( ){
            finish();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roomtypes);
        ButterKnife.bind(this);
        //初始化List数据
        initRoomType();
        //初始化RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //创建GridLayoutManager 对象
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        RoomtypeAdapter roomType = new RoomtypeAdapter(mRoomtypelsit);
        recyclerView.setAdapter(roomType);
    }

    private void initRoomType() {
        for (int i = 0; i < 20; i++) {
            RoomType roomType = new RoomType(R.drawable.drawing_room, "客厅");
            mRoomtypelsit.add(roomType);
        }

    }


}
