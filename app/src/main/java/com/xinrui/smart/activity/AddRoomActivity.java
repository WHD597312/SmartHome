package com.xinrui.smart.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xinrui.smart.R;
import com.xinrui.smart.adapter.RoomAdapter;
import com.xinrui.smart.pojo.Room;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/10.
 */

public class AddRoomActivity extends AppCompatActivity {
    @BindView(R.id.title)
    RelativeLayout title;
    @BindView(R.id.return_button)
    ImageButton returnButton;
    @BindView(R.id.title_text)
    TextView titleText;
    @BindView(R.id.confirm)
    Button confirm;
    @BindView(R.id.room_type_item)
    GridView roomTypeItem;

    private ArrayList<Room> roomList = new ArrayList<>();
    Room[] rooms = {
            new Room(R.drawable.bedroom, "卧室"), new Room(R.drawable.study, "书房"),
            new Room(R.drawable.drawing_room, "客厅"), new Room(R.drawable.toilet, "卫生间"),
    };

    private RoomAdapter roomAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_room);
        ButterKnife.bind(this);


        //?????????

        initRooms();

        roomAdapter = new RoomAdapter(roomList);
        roomTypeItem.setAdapter(roomAdapter);

    }

    private void initRooms() {
        roomList.clear();
        for (int i = 0; i < 16; i++) {
            Random random = new Random();
            int index = random.nextInt(rooms.length);
            roomList.add(rooms[index]);
        }
    }


    @OnClick({R.id.return_button, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.return_button:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.confirm:
                break;
        }
    }
}
