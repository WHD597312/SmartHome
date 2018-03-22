package com.xinrui.smart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.xinrui.smart.R;
import com.xinrui.smart.activity.AddRoomActivity;
import com.xinrui.smart.activity.CustomRoomActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/13.
 */

public class SceneActivity extends AppCompatActivity {
    @BindView(R.id.menu)
    ImageView menu;
    @BindView(R.id.new_btn)
    Button newFloor;
    @BindView(R.id.add_room)
    Button addRoom;
    @BindView(R.id.custom_house_type)
    Button customHouseType;
    @BindView(R.id.copy_and_paste)
    Button copyAndPaste;
    @BindView(R.id.delete)
    Button delete;
    Button secondFloor;
    @BindView(R.id.image_room)
    ImageView imageRoom;
    @BindView(R.id.btn1)
    Button btn1;
    @BindView(R.id.btn2)
    Button btn2;
    @BindView(R.id.btn3)
    Button btn3;
    @BindView(R.id.btn4)
    Button btn4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scene);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.new_btn, R.id.add_room, R.id.custom_house_type, R.id.copy_and_paste, R.id.delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                break;
            case R.id.btn2:
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.VISIBLE);

                break;
            case R.id.btn3:
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                imageRoom.setImageResource(R.drawable.drawing_room);
                finish();
                break;
            case R.id.btn4:
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                imageRoom.setImageResource(R.drawable.drawing_room);
                break;
            case R.id.new_btn:



                break;
            case R.id.add_room:
                Intent add_room = new Intent(this, AddRoomActivity.class);
                startActivity(add_room);
                break;
            case R.id.custom_house_type:
                Intent custom_house_type = new Intent(this, CustomRoomActivity.class);
                startActivity(custom_house_type);
                break;
            case R.id.copy_and_paste:
                break;
            case R.id.delete:
                break;
        }
    }

}
