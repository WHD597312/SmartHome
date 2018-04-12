package com.xinrui.smart.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    @BindView(R.id.sure)
    Button sure;
    private List<RoomType> mRoomtypelsit = new ArrayList<>();
    private Context mContext;

    @OnClick(R.id.return_button)
    public void rollback() {
        finish();
    }

    private int checkedPosition;

    SharedPreferences fragmentPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roomtypes);
        ButterKnife.bind(this);
        fragmentPreferences = getSharedPreferences("fragment", Context.MODE_PRIVATE);
        //初始化List数据
        initRoomType();
        String returnName ;
        //初始化RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //创建GridLayoutManager 对象
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        final RoomtypeAdapter roomType = new RoomtypeAdapter(mRoomtypelsit);
        roomType.setOnItemClickListener(new RoomtypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                for (int i = 0; i < mRoomtypelsit.size(); i++) {
                    if (i == position) {
                        mRoomtypelsit.get(i).setFladed(true);
                    } else {
                        mRoomtypelsit.get(i).setFladed(false);
                    }
                }
                SharedPreferences.Editor editor = getSharedPreferences("roomType",MODE_PRIVATE).edit();

                String returnName = mRoomtypelsit.get(position).getRoomName();
                editor.putString("return",returnName);
                checkedPosition = position;
                editor.commit();
                roomType.notifyDataSetChanged();

            }
        });
        recyclerView.setAdapter(roomType);
    }



    private void initRoomType() {
        RoomType roomType1 = new RoomType(R.drawable.drawing_room, "客厅");
        mRoomtypelsit.add(roomType1);
        RoomType roomType2 = new RoomType(R.drawable.bedroom, "卧室");
        mRoomtypelsit.add(roomType2);
        RoomType roomType3 = new RoomType(R.drawable.dining_room, "餐厅");
        mRoomtypelsit.add(roomType3);
        RoomType roomType4 = new RoomType(R.drawable.toilet, "卫生间");
        mRoomtypelsit.add(roomType4);
        RoomType roomType5 = new RoomType(R.drawable.shower_room, "浴室");
        mRoomtypelsit.add(roomType5);
        RoomType roomType6 = new RoomType(R.drawable.kitchen, "厨房");
        mRoomtypelsit.add(roomType6);
        RoomType roomType7 = new RoomType(R.drawable.children_bedroom, "儿童房");
        mRoomtypelsit.add(roomType7);
        RoomType roomType8 = new RoomType(R.drawable.kitchen2, "厨房2");
        mRoomtypelsit.add(roomType8);
        RoomType roomType9 = new RoomType(R.drawable.cloakroom, "衣帽间");
        mRoomtypelsit.add(roomType9);
        RoomType roomType10 = new RoomType(R.drawable.cloakroom2, "衣帽间2");
        mRoomtypelsit.add(roomType10);
        RoomType roomType11 = new RoomType(R.drawable.studio, "工作室");
        mRoomtypelsit.add(roomType11);
        RoomType roomType12 = new RoomType(R.drawable.studio2, "工作室2");
        mRoomtypelsit.add(roomType12);
        RoomType roomType13 = new RoomType(R.drawable.studio3, "工作室3");
        mRoomtypelsit.add(roomType13);
        RoomType roomType14 = new RoomType(R.drawable.study, "书房");
        mRoomtypelsit.add(roomType14);
        RoomType roomType15 = new RoomType(R.drawable.balcony, "阳台");
        mRoomtypelsit.add(roomType15);

    }

    @OnClick(R.id.sure)
    public void onViewClicked() {
        SharedPreferences sharedPreferences =  this.getSharedPreferences("roomType",MODE_PRIVATE);
        String returnName = sharedPreferences.getString("return","卧室");

        Bundle b = getIntent().getExtras();
        int retutnRoomId = b.getInt("roomId");
        Intent intent = new Intent(RoomTypesActivity.this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("returnName", returnName);
        bundle.putInt("returnRoomId",retutnRoomId);
        intent.putExtras(bundle);
//        SharedPreferences fragment = this.getSharedPreferences("fragment", MODE_PRIVATE);
//        fragment.edit().putString("fragment","3");
//        fragment.edit().commit();
        SharedPreferences sp = this.getSharedPreferences("room_postion", MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("returnRoomName",returnName);
        editor.putInt("returnRoomId",retutnRoomId);
        editor.commit();

        setResult(2, intent);//返回值调用函数，其中2为resultCode，返回值的标志
        finish();//传值结束
    }
}
