package com.xinrui.smart.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.xinrui.smart.R;
import com.xinrui.smart.adapter.CustomAdapter;
import com.xinrui.smart.pojo.Room;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Created by win7 on 2018/3/12.
 */

public class CustomRoomActivity extends AppCompatActivity {
    @BindView(R.id.return_button)
    ImageButton returnButton;
    @BindView(R.id.title_text)
    TextView titleText;
    @BindView(R.id.customRooms)
    GridView customRooms;
    @BindView(R.id.merge)
    Button merge;
    @BindView(R.id.resolution)
    Button resolution;
    @BindView(R.id.sure)
    Button sure;
    @BindView(R.id.delete)
    Button delete;

    CustomAdapter customAdapter;


    int clickTemp = 0;

    boolean isselected = true;

   int data[] = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
   List list = new ArrayList();





    /**
     * 点击gridview一个item变色，再次点击还原
     * @param position
     */



    int colors[] = {Color.BLUE,Color.TRANSPARENT};
    int clicked=0;
    int color = 0;

    @OnItemClick(R.id.customRooms) void onItemClick(View view,int position) {

//        }
//        customRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {

//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(),"jaq1",Toast.LENGTH_LONG).show();
//        setSeclection(position);
//        if (clickTemp == position){
//            if (!isselected) {
//                view.setBackgroundColor(Color.BLUE);
//                isselected = true;
//            } else {
//                view.setBackgroundColor(Color.TRANSPARENT);
//                isselected = false;
//            }
//    }
//            }
//        });
//        customAdapter.getView(position,null,null).setBackgroundColor(Color.BLUE);
//        customAdapter.setSeclection(position);

        view.setBackgroundColor(Color.BLUE);
        customAdapter.notifyDataSetChanged();
    }


    public void setSeclection(int posiTion) {
        clickTemp = posiTion;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_room);
        ButterKnife.bind(this);


        //初始化数据
        initRooms();


    }
    Room r = new Room();

    private void initRooms() {
        titleText.setText("自定义户型");
        list.add(r);
        customAdapter = new CustomAdapter(this);
        customRooms.setAdapter(customAdapter);
        customAdapter.notifyDataSetChanged();
    }



    @OnClick({R.id.return_button,R.id.merge, R.id.resolution, R.id.sure, R.id.delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.return_button:

            case R.id.merge:

                break;
            case R.id.resolution:
                break;
            case R.id.sure:
                break;
            case R.id.delete:
                break;
        }
    }
}
