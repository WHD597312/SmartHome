package com.xinrui.smart.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.pojo.DeviceGroup;
import com.xinrui.smart.util.IsBase64;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.camera.CameraManager;
import com.xinrui.smart.util.decoding.CaptureActivityHandler;
import com.xinrui.smart.util.decoding.InactivityTimer;
import com.xinrui.smart.util.mqtt.MQService;
import com.xinrui.smart.util.view.ViewfinderView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class QRScannerActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    ViewfinderView viewfinderView;

    private CaptureActivityHandler handler;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    String shareDeviceId;
    String shareContent;
    String shareMacAddress;
    private String userId;

    ImageView back;
    Unbinder unbinder;
    MyApplication application;

    private boolean isBound=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_qrscanner);
        if (application == null) {
            application = (MyApplication) getApplication();
            application.addActivity(this);
        }

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        unbinder = ButterKnife.bind(this);
        init();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        SharedPreferences my = getSharedPreferences("my", MODE_PRIVATE);
        userId = my.getString("userId", "");
        Intent service = new Intent(QRScannerActivity.this, MQService.class);
        isBound = bindService(service, connection, Context.BIND_AUTO_CREATE);
    }

    private void init() {
        CameraManager.init(getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    DeviceChildDaoImpl deviceChildDao;
    DeviceGroupDaoImpl deviceGroupDao;
    SharedPreferences preferences;

    @Override
    protected void onStart() {
        super.onStart();
        deviceGroupDao = new DeviceGroupDaoImpl(getApplicationContext());
        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());
        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
//        initBeepSound();
        vibrate = true;
    }

    @OnClick({R.id.back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }
    private String success;

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        try {
            if (TextUtils.isEmpty(success)) {
                if (isBound) {
                    unbindService(connection);
                }
            }
            if (handler!=null){
                handler.removeCallbacksAndMessages(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 处理扫描结果
     */
    public void handleDecode(Result result) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();

        try {
            if (TextUtils.isEmpty(resultString)) {
                Toast.makeText(QRScannerActivity.this, "扫描失败!", Toast.LENGTH_SHORT).show();
            } else {
                String content = resultString;
                if (!Utils.isEmpty(content)) {
                    boolean isBase64=IsBase64.isBase64(content);
                    if (isBase64){
                        content = new String(Base64.decode(content, Base64.DEFAULT));
                        Log.i("content","-->"+content);
                        if (!Utils.isEmpty(content)) {
                            shareContent = content;
                            if (!content.contains("macAddress") && !content.contains("deviceId")){
                                Toast.makeText(QRScannerActivity.this, "扫描内容不符合!", Toast.LENGTH_SHORT).show();
                            }else {
                                String[] ss = content.split("&");
                                String s0 = ss[0];
                                String deviceId = s0.substring(s0.indexOf("'") + 1);
                                String s2 = ss[2];
                                String macAddress = s2.substring(s2.indexOf("'") + 1);
                                shareMacAddress = macAddress;

                                Map<String, Object> params = new HashMap<>();
                                params.put("deviceId", deviceId);
                                params.put("userId", userId);
                                new QrCodeAsync().execute(params);
                            }
                        }
                    }else {
                        Toast.makeText(QRScannerActivity.this, "扫描内容不符合!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //http://host:port/app/version/device/getDeviceById?deviceId='deviceId'
    String macAddress;
    int deviceId;
    private String sharedDeviceId;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};


    private String qrCodeConnectionUrl = "http://47.98.131.11:8082/warmer/v1.0/device/createShareDevice";

    class QrCodeAsync extends AsyncTask<Map<String, Object>, Void, Integer> {
        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];
            String result = HttpUtils.postOkHpptRequest(qrCodeConnectionUrl, params);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        if (!Utils.isEmpty(shareContent)) {
                            shareContent = shareContent.replace("'", "");
                            int deviceId;
                            int userId;
                            String macAddress;
                            int type;
                            int controlled;
                            int shareHouseId;
                            String deviceName;
                            String ss[] = shareContent.split("&");
                            deviceId = Integer.parseInt(ss[0].substring(8));
                            QRScannerActivity.this.deviceId = deviceId;
                            userId = Integer.parseInt(ss[1].substring(6));
                            macAddress = ss[2].substring(10);
                            type = Integer.parseInt(ss[3].substring(4));
                            controlled = Integer.parseInt(ss[4].substring(10));
                            shareHouseId = Integer.parseInt(ss[5].substring(7));
                            deviceName = ss[6].substring(10);

                            long houseId = Long.MAX_VALUE;
//                            DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, houseId, userId, type, 0);
                            DeviceChild deviceChild = new DeviceChild((long)deviceId, houseId, deviceName, macAddress, type);
//                            deviceChild.setImg(imgs[0]);
                            deviceChild.setControlled(controlled);
//                            deviceChild.setOnLint(true);
                            deviceChild.setShareHouseId(shareHouseId);

//                            List<DeviceChild> deviceChildren = deviceChildDao.findAllDevice();
//
//                            for (DeviceChild deviceChild2 : deviceChildren) {
//                                if (macAddress.equals(deviceChild2.getMacAddress())) {
//                                    deviceChildDao.delete(deviceChild2);
//                                    break;
//                                }
//                            }
                            List<DeviceChild> deleteDevices=deviceChildDao.findDeviceByMacAddress(macAddress);
                            if (deleteDevices!=null && !deleteDevices.isEmpty()){
                                deviceChildDao.deleteDevices(deleteDevices);
                            }
                            String topicName2 = "rango/" + macAddress + "/transfer";
                            String topicOffline = "rango/" + macAddress + "/lwt";
                            boolean succ = mqService.subscribe(topicName2, 1);
                            succ = mqService.subscribe(topicOffline, 1);
                            deviceChildDao.insert(deviceChild);
                            if (type==1){
                                if (succ) {
                                    JSONObject jsonObject2 = new JSONObject();
                                    jsonObject2.put("loadDate", "1");
                                    String s = jsonObject2.toString();
                                    String topicName = "rango/" + macAddress + "/set";
                                    boolean success = mqService.publish(topicName, 1, s);
                                    if (success)
                                        if (!success) {
                                            success = mqService.publish(topicName, 1, s);
                                        }
                                    Log.i("sss", "-->" + success);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    if (isBound) {
                        unbindService(connection);
                    }
                    success = "success";
                    Utils.showToast(QRScannerActivity.this, "添加设备成功");
                    Intent intent2 = new Intent(QRScannerActivity.this, MainActivity.class);
                    intent2.putExtra("deviceList", "deviceList");
                    intent2.putExtra("deviceId", deviceId + "");
                    startActivity(intent2);
                    break;
                case -3007:
                    Utils.showToast(QRScannerActivity.this, "分享设备添加失败");
                    break;
                case -3018:
                    Utils.showToast(QRScannerActivity.this, "自己的设备只能分享给别人");
                    break;
                    default:
                        Utils.showToast(QRScannerActivity.this, "分享设备添加失败");
                        break;
            }
        }
    }
    MQService mqService;
    private boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            bound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
//            handler = new CaptureActivityHandler(QRScannerActivity.this, decodeFormats, characterSet);
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
}