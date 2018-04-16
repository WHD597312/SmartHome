package com.xinrui.smart.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.xinrui.smart.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.media.MediaRecorder.VideoSource.CAMERA;

/**
 * Created by win7 on 2018/3/30.
 */

public class RoomContentActivity extends Activity {
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.decorat)
    Button decorat;
    @BindView(R.id.change_scene)
    Button changeScene;
    @BindView(R.id.add_equipment)
    ImageView addEquipment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_content);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.imageView, R.id.decorat, R.id.change_scene, R.id.add_equipment})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imageView:
                break;
            case R.id.decorat:
                break;
            case R.id.change_scene:
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera, CAMERA);
                break;
            case R.id.add_equipment:
                break;
        }
    }
}
