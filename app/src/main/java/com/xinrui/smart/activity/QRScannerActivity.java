package com.xinrui.smart.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
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
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.camera.CameraManager;
import com.xinrui.smart.util.decoding.CaptureActivityHandler;
import com.xinrui.smart.util.decoding.InactivityTimer;
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

public class QRScannerActivity extends AppCompatActivity implements SurfaceHolder.Callback{

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_qrscanner);
        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        unbinder=ButterKnife.bind(this);
        init();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        SharedPreferences my = getSharedPreferences("my", MODE_PRIVATE);
        userId = my.getString("userId", "");
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
    public void onClick(View view){
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        if (unbinder!=null){
            unbinder.unbind();
        }
    }

    /**
     * 处理扫描结果
     */
    public void handleDecode(Result result) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();

        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(QRScannerActivity.this, "扫描失败!", Toast.LENGTH_SHORT).show();
        } else {
            String content = resultString;
            if (!Utils.isEmpty(content)) {
                content = new String(Base64.decode(content, Base64.DEFAULT));
                if (!Utils.isEmpty(content)) {
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
//                tv_result.setText(content);

        }
    }
    //http://host:port/app/version/device/getDeviceById?deviceId='deviceId'
    String macAddress;
    int deviceId;
    private String sharedDeviceId;
    int[] imgs = {R.mipmap.image_unswitch, R.mipmap.image_switch};
    class LoadDevice extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            int code = 0;
            String url = strings[0];
            String result = HttpUtils.getOkHpptRequest(url);
            try {
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {

                        JSONObject content = jsonObject.getJSONObject("content");
                        deviceId = content.getInt("id");
                        String deviceName = content.getString("deviceName");
                        int type = content.getInt("type");
                        int shareHouseId = content.getInt("houseId");
                        int masterControllerUserId = content.getInt("masterControllerUserId");
                        int isUnlock = content.getInt("isUnlock");
                        int version = content.getInt("version");
                        macAddress = content.getString("macAddress");
                        int controlled = content.getInt("controlled");

                        long houseId = Long.MAX_VALUE;
                        DeviceChild deviceChild = new DeviceChild((long) deviceId, deviceName, imgs[0], 0, houseId, masterControllerUserId, type, isUnlock);
                        deviceChild.setImg(imgs[0]);
                        deviceChild.setMacAddress(macAddress);
                        deviceChild.setVersion(version);
                        deviceChild.setControlled(controlled);
                        deviceChild.setOnLint(true);
                        deviceChild.setDeviceState("open");
                        deviceChild.setShareHouseId(shareHouseId);

                        List<DeviceChild> deviceChildren = deviceChildDao.findGroupIdAllDevice(houseId);
                        DeviceChild deviceChild3 = null;

                        for (DeviceChild deviceChild2 : deviceChildren) {
                            if (macAddress.equals(deviceChild2.getMacAddress())) {
                                deviceChild3 = deviceChild2;
                                break;
                            }
                        }
                        if (deviceChild3 == null) {
                            deviceChildDao.insert(deviceChild);
                        } else {
                            deviceChild3 = deviceChildDao.findDeviceById(deviceChild3.getId());
                            deviceChild3.setType(type);
                            deviceChild3.setDeviceName(deviceName);
                            deviceChild3.setHouseId((long) houseId);
                            deviceChild3.setMasterControllerUserId(masterControllerUserId);
                            deviceChild3.setIsUnlock(isUnlock);
                            deviceChild3.setVersion(version);
                            deviceChild3.setMacAddress(macAddress);
                            deviceChild3.setControlled(controlled);
                            deviceChild3.setOnLint(true);
                            deviceChild3.setDeviceState("open");
                            deviceChildDao.update(deviceChild3);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    Utils.showToast(QRScannerActivity.this, "创建成功");
                    Intent intent2 = new Intent(QRScannerActivity.this, MainActivity.class);
                    intent2.putExtra("deviceList", "deviceList");
                    intent2.putExtra("deviceId", deviceId + "");
                    startActivity(intent2);
                    break;
                default:
                    Utils.showToast(QRScannerActivity.this, "创建失败");
                    break;
            }
        }
    }

    private String qrCodeConnectionUrl = "http://120.77.36.206:8082/warmer/v1.0/device/createShareDevice";
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
                        String deviceId = (String) params.get("deviceId");
                        String url = "http://120.77.36.206:8082/warmer/v1.0/device/getDeviceById?deviceId=" + deviceId;
                        new LoadDevice().execute(url);
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
//                    Utils.showToast(AddDeviceActivity.this, "分享设备创建成功");
//                    Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
//                    intent.putExtra("shareMacAddress", shareMacAddress);
//                    startActivity(intent);
                    break;
                case -3007:
                    Utils.showToast(QRScannerActivity.this, "分享设备添加失败");
                    break;
            }
        }
    }

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
            handler=new CaptureActivityHandler(this,decodeFormats,characterSet);
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