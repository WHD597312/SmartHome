package com.xinrui.smart.activity.device;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.ZXingUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShareDeviceActivity extends AppCompatActivity {

    @BindView(R.id.img_back) ImageView img_back;
    @BindView(R.id.img_qrCode) ImageView img_qrCode;
    private DeviceChildDaoImpl deviceChildDao;
    private String share;
    Unbinder unbinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device);
        unbinder=ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        deviceChildDao=new DeviceChildDaoImpl(this);
        Intent intent=getIntent();
        long childPosition=Long.parseLong(intent.getStringExtra("childPosition"));
        DeviceChild deviceChild=deviceChildDao.findDeviceChild(childPosition);
        if (deviceChild!=null){

            long deviceId=deviceChild.getId();
            String deviceName=deviceChild.getDeviceName();
            share=deviceId+"";
            if (!Utils.isEmpty(share)){
                Message msg=handler.obtainMessage();
                msg.what=1;
                handler.sendMessage(msg);
            }

        }

    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==1){
                createQrCode();
            }
        }
    };

    @OnClick({R.id.img_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_back:
                finish();
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    /**生成二维码*/
    private void createQrCode(){
        Bitmap bitmap = ZXingUtils.createQRImage(share,img_qrCode.getWidth(), img_qrCode.getHeight());
        img_qrCode.setImageBitmap(bitmap);
    }
}
