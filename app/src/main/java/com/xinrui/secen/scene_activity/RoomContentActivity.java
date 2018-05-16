package com.xinrui.secen.scene_activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_adapter.MyAdapter;
import com.xinrui.secen.scene_pojo.Equipment;
import com.xinrui.secen.scene_util.BitmapCompressUtils;
import com.xinrui.secen.scene_util.GetUrl;
import com.xinrui.secen.scene_view_custom.CustomDialog;
import com.xinrui.secen.scene_view_custom.DragImageView;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.activity.MainActivity;
import com.xinrui.smart.pojo.DeviceChild;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.util.mqtt.MQService;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by win7 on 2018/3/30.
 */

public class RoomContentActivity extends Activity {
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.change_scene)
    Button changeScene;
    @BindView(R.id.return_scene)
    ImageButton returnScene;
    @BindView(R.id.return_homepage)
    ImageButton returnHomepage;
    @BindView(R.id.rl3)
    RelativeLayout rl3;
    GetUrl getUrl = new GetUrl();

    float alpha;
    Uri imageUri; //图片路径
    File imageFile; //图片文件
    String imagePath;
    Bitmap bitmapdown;
    final static int CAMERA = 1;//拍照
    final static int ICON = 2;//相册
    final static int CAMERAPRESS = 3;//拍照权限
    final static int ICONPRESS = 4;//相册权限
    final static int PICTURE_CUT = 5;//剪切图片
    @BindView(R.id.background)
    ImageView background;
    @BindView(R.id.fl)
    FrameLayout fl;
    @BindView(R.id.extTemp)
    TextView extTemp1;
    @BindView(R.id.extHut)
    TextView extHut1;
//    public static int running=0;
    private List<Equipment> mDatas;
    private DragImageView dragImageView;
    private MyAdapter myAdapter;
    private Context mContext;
    private PopupWindow popupWindow;
    private boolean isClickCamera;//是否是拍照裁剪
    public static boolean running=false;
    //网络返回的数据
    List<Equipment> equipment_network = new ArrayList<>();
    final int REQUEST_TAKE_PHOTO_PERMISSION = 1;

    private static final int SUCCESS = 1;
    private static final int FALL = 2;
    private static final String TAG = "RoomContentActivity";
    private Uri outputUri;//裁剪完照片保存地址
    String url;

    MessageReceiver receiver = new MessageReceiver();
    SharedPreferences sharedPreferences1;
    private CustomDialog.Builder builder;
    private CustomDialog mDialog;
    DeviceChildDaoImpl deviceChildDao;

    MyApplication application;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", MODE_PRIVATE);
        int roomId = sharedPreferences.getInt("roomId", 0);
        sharedPreferences1 = this.getSharedPreferences("data",0);
        url = "http://120.77.36.206:8082/warmer/v1.0/room/" + roomId + "/background";
//        running=2;
        Intent intent=new Intent(this,MQService.class);
        bindService(intent,connection,Context.BIND_AUTO_CREATE);
        IntentFilter intentFilter=new IntentFilter("RoomContentActivity");
        registerReceiver(receiver,intentFilter);
        deviceChildDao=new DeviceChildDaoImpl(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_content);
        ButterKnife.bind(this);
        mDatas = new ArrayList<>();
        initBackgroundImage();
        getRoomAllDevices();

        if (application==null){
            application= (MyApplication) getApplication();
            application.addActivity(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        application.removeActivity(this);
    }

    //服务器获取数据并初始化背景图片
    private void initBackgroundImage() {
        builder = new CustomDialog.Builder(this);
        dragImageView = (DragImageView) findViewById(R.id.dragGridView1);
        Picasso.with(RoomContentActivity.this).load(url).error(R.drawable.bedroom1).into(background);
    }

    //从服务器获取设备数据并初始化
    public void initEquipmentData(List<Equipment> equipmentList) {
        mDatas = equipmentList;
        myAdapter = new MyAdapter(mDatas);
        dragImageView.setLayoutManager(new GridLayoutManager(this, 4));
        dragImageView.setAdapter(myAdapter);
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View v, final int postion) {

                showDoubleButtonDialog("确实解除设备吗!", "确定", "取消",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JSONArray jsonArray = new JSONArray();
                                Equipment equipment = mDatas.get(postion);
                                deviceId = equipment.getId();
                                jsonArray.put(deviceId);
                                deleteDevices(jsonArray);
                                mDatas.remove(postion);
                                myAdapter.notifyDataSetChanged();
                                mDialog.dismiss();

                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });
            }
        });
        dragImageView.setItemAnimator(new DefaultItemAnimator());

        TextView extTemp = (TextView) extTemp1.findViewById(R.id.extTemp);
        TextView extHut = (TextView)extHut1.findViewById(R.id.extHut);
        for (int i = 0; i < mDatas.size(); i++) {
            if(mDatas.get(i).getDevice_type() == 2){
                long deviceId = mDatas.get(i).getId();
                DeviceChild deviceChild2=deviceChildDao.findDeviceById(deviceId);

                if (deviceChild2.getTemp()==0 && deviceChild2.getHum()==0){
                    break;
                }
                if (deviceChild2!=null){
                    String et=deviceChild2.getTemp()+"";
                    String eh=deviceChild2.getHum()+"";
                    extTemp.setText(et+"℃");
                    extHut.setText(eh+"%");
                }
            }

        }

    }

    //拍照
    public void startCamera() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        imageFile = new File(getExternalCacheDir(), "background.png");
        try {
            if (imageFile.exists()) {
                imageFile.delete();
            }
            imageFile.createNewFile();
        } catch (IOException e) {

            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(imageFile);
        } else {
            //Android 7.0系统开始 使用本地真实的Uri路径不安全,使用FileProvider封装共享Uri
            //参数二:fileprovider绝对路径 com.dyb.testcamerademo：项目包名
            imageUri = FileProvider.getUriForFile(RoomContentActivity.this, "com.hm.camerademo.fileprovider", imageFile);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //照相
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
        startActivityForResult(intent, CAMERA); //启动照相
    }

    //打开相册
    public void startGallery() {
        Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent1, ICON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        running=true;
    }


    @OnClick({R.id.imageView, R.id.change_scene, R.id.return_scene, R.id.return_homepage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imageView:
                break;
            case R.id.change_scene:
                popupWindow(changeScene);
                break;
            case R.id.return_scene:
                //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
                Intent intent = new Intent(this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Activity_return", "Activity_return");
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.return_homepage:
                Intent intent1 = new Intent(this, MainActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("return_homepage", "return_homepage");
                intent1.putExtras(bundle1);
                startActivity(intent1);
                break;
        }
    }

    int deviceId;

    //获取所有设备的异步操作
    class GainAllEquipmentAsyncTask extends AsyncTask<Void, Void, List<Equipment>> {
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", Context.MODE_PRIVATE);
        AsyncResponse asyncResponse;

        void setOnAsyncResponse(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected List<Equipment> doInBackground(Void... voids) {
            int code = 0;
            int roomId = sharedPreferences.getInt("roomId", 0);
            Map<String, Object> params = new HashMap<>();
            params.put("roomId", roomId);
            List<Equipment> list = new ArrayList<>();
            try {
                String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/getRoomDevices", params);
                String result = HttpUtils.getOkHpptRequest(url);

                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    JSONArray jsonArray = jsonObject.getJSONArray("content");


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        String deviceName = object.getString("deviceName");
                        int type = object.getInt("type");
                        int houseId = object.getInt("houseId");
                        int masterControllerUserId = object.getInt("masterControllerUserId");
                        int isUnlock = object.getInt("isUnlock");

                        int device_drawable = 0;
                        if(type == 1){
                            device_drawable = R.drawable.equipment_warmer;
                        }else if(type == 2){
                            device_drawable = R.drawable.equipment_external_sensor;
                        }
                        Equipment equipment = new Equipment(type,id, deviceName, device_drawable, houseId, masterControllerUserId, isUnlock, false);
                        list.add(equipment);
                    }
                    return list;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Equipment> list) {
            List<Equipment> equipment_list = new ArrayList<>();
            try {
                if (null != list&& list.size() != 0) {
                    asyncResponse.onDataReceivedSuccess(list);
                } else {
                    asyncResponse.onDataReceivedFailed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(list);
        }
    }

    //回调接口，用于异步返回数据
    public interface AsyncResponse {
        void onDataReceivedSuccess(List<Equipment> listData);

        void onDataReceivedFailed();
    }

    public void deleteDevices(JSONArray jsonArray) {
        JSONArray j;
        j = jsonArray;
        DeleteRoomDevicesAsynTask deleteRoomDevicesAsynTask = new DeleteRoomDevicesAsynTask();
        deleteRoomDevicesAsynTask.execute(j);
    }
    //获取所有设备
    public void getRoomAllDevices() {
        final GainAllEquipmentAsyncTask gainAllEquipmentAsyncTask = new GainAllEquipmentAsyncTask();
        gainAllEquipmentAsyncTask.execute();
        gainAllEquipmentAsyncTask.setOnAsyncResponse(new AsyncResponse() {
            @Override
            public void onDataReceivedSuccess(List<Equipment> listData) {
                equipment_network = listData;
                initEquipmentData(equipment_network);
            }

            @Override
            public void onDataReceivedFailed() {

            }
        });
    }

    //移除设备的异步操作
    class DeleteRoomDevicesAsynTask extends AsyncTask<JSONArray, Void, Integer> {

        @Override
        protected Integer doInBackground(JSONArray... jsonArrays) {
            int code = 0;
            JSONArray request = jsonArrays[0];
            Map<String, Object> params = new HashMap<>();
            params.put("deviceId", deviceId);
            String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/deleteDevice", params);

            String result = HttpUtils.doDelete(url, request);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        JSONArray content = jsonObject.getJSONArray("content");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer integer) {

            super.onPostExecute(integer);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK||event.getRepeatCount() == 0){
            setResult(Activity.RESULT_CANCELED);
            //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
            Intent intent = new Intent(RoomContentActivity.this,MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("Activity_return", "Activity_return");
            intent.putExtras(bundle);
            startActivity(intent);
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File file;
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", MODE_PRIVATE);
        int roomId = sharedPreferences.getInt("roomId", 0);
        String url = "http://120.77.36.206:8082/warmer/v1.0/room/" + roomId + "/background";
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "requestCode" + requestCode + "resultCode" + resultCode);
        switch (requestCode) {
            case CAMERA:
                if(resultCode == RESULT_OK){
                    cropPhoto(imageUri);
                }
                break;
            case ICON:
                if(resultCode == RESULT_OK){
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }else {
                    Intent intent = new Intent(this, RoomContentActivity.class);
                    startActivity(intent);
                }
                break;
            case PICTURE_CUT://裁剪完成
                isClickCamera = true;
                Bitmap bitmap2 = null;
                try {
                    if (isClickCamera) {
                        bitmap2 = BitmapFactory.decodeStream(getContentResolver().openInputStream(outputUri));
                    } else {
                        bitmap2 = BitmapFactory.decodeFile(imagePath);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if(bitmap2 == null){
                    break;
                }
                File file2 = BitmapCompressUtils.compressImage(bitmap2);
                upImage(file2);
                break;
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERAPRESS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //获取到了权限
                    startCamera();
                } else {
                    Toast.makeText(this, "对不起你没有同意该权限", Toast.LENGTH_LONG).show();
                }
                break;

            case ICONPRESS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //获取到了权限
                    startGallery();
                } else {
                    Toast.makeText(this, "对不起你没有同意该权限", Toast.LENGTH_LONG).show();
                }
                break;

        }

    }

    //底部popupWindow
    public void popupWindow(View view) {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.changepicture, null);
        popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.BOTTOM, 0, -location[1]);
        //添加按键事件监听
        setButtonListeners(layout);
    }

    private void setButtonListeners(LinearLayout layout) {
        Button camera = (Button) layout.findViewById(R.id.camera);
        Button gallery = (Button) layout.findViewById(R.id.gallery);
        Button cancel = (Button) layout.findViewById(R.id.cancel);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    if (Build.VERSION.SDK_INT >= 23) {
                        //android 6.0权限问题
                        if (ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(RoomContentActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERAPRESS);
                        } else {
                            startCamera();
                        }
                    } else {
                        startCamera();
                    }
                }
                popupWindow.dismiss();
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        //android 6.0权限问题
                        if (ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(RoomContentActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, ICONPRESS);
                        } else {
                            startGallery();
                        }

                    } else {
                        startGallery();
                    }
                }
                popupWindow.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    backgroundAlpha((float) msg.obj);
                    break;
            }
        }
    };

    private void upImage(File file) {
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", MODE_PRIVATE);
        int roomId = sharedPreferences.getInt("roomId", 0);
        String url = "http://120.77.36.206:8082/warmer/v1.0/room/" + roomId + "/background";

        if (file != null) {
            new AddPicuterAsync().execute(url, file);
        }

    }

    class AddPicuterAsync extends AsyncTask<Object, Void, Integer> {

        @Override
        protected Integer doInBackground(Object... files) {
            int code = 0;
            String url = (String) files[0];
            File file = (File) files[1];
            if (file != null) {
                String result = HttpUtils.upLoadFile(url, "HeadPortrait2.jpg", file);
                if (!Utils.isEmpty(result)) {
                    code = Integer.parseInt(result);
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 201:
                    Picasso.with(RoomContentActivity.this).load(url).error(R.drawable.bedroom1).memoryPolicy(MemoryPolicy.NO_CACHE).into(background);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        OkHttpUtils.getInstance().cancelTag(1);
        super.onPause();
    }

    @Override
    protected void onStop() {
        OkHttpUtils.getInstance().cancelTag(1);
        super.onStop();
        running=false;
    }

    @Override
    protected void onDestroy() {
        if (connection!=null){
            unbindService(connection);
        }
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    //mqtt获取外置传感器温度,湿度数据
    MQService mqService;
    boolean bound = false;
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


    String extTemp2;
    String extHut2;
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceChild deviceChild2 = (DeviceChild) intent.getSerializableExtra("deviceChild");

            extTemp2 = String.valueOf(intent.getIntExtra("extTemp", 0));
            extHut2 = String.valueOf(intent.getIntExtra("extHut", 0));
            for (int i = 0; i < mDatas.size(); i++) {
                int type = mDatas.get(i).getDevice_type();
                String macAddress = mDatas.get(i).getMacAddress();
                if (type == 2) {
//                    if(extTemp1 == null|| extHut1 == null){
//
//                    }else {
//                        extTemp1.setText(extTemp2+"℃");
//                        extHut1.setText(extHut2+"％");
//                    }
//
//                    String et = extTemp2;
//                    String eh = extHut2;
//                    getData(et,eh);
//                    break;
                    if(deviceChild2!=null){
                        String macAddress2 = deviceChild2.getMacAddress();
                        if(macAddress2.equals(macAddress)){
                            extTemp1.setText(deviceChild2.getTemp()+"℃");
                            extHut1.setText(deviceChild2.getHum()+"％");
                        }
                    }
                }
            }
        }

    }
    String extTemp ;
    String extHut ;
    public void getData(String extTemp,String extHut){
        this.extTemp = extTemp;
        this.extHut = extHut;
        SharedPreferences.Editor sp = getSharedPreferences("data", 0).edit();
        sp.putString("extTemp1", extTemp);
        sp.putString("extHut1", extHut);
        sp.commit();
    }
    /**
     * 裁剪图片
     */
    private void cropPhoto(Uri uri) {
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        // 创建File对象，用于存储裁剪后的图片，避免更改原图
        File file = new File(getExternalCacheDir(), "crop_image.jpg");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        outputUri = Uri.fromFile(file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        //裁剪图片的宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("crop", "true");//可裁剪
        // 裁剪后输出图片的尺寸大小
//        intent.putExtra("outputX", 150);
//        intent.putExtra("outputY", 150);
        intent.putExtra("scale", true);//支持缩放
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//输出图片格式
        intent.putExtra("noFaceDetection", true);//取消人脸识别
        startActivityForResult(intent, PICTURE_CUT);
    }

    // 4.4及以上系统使用这个方法处理图片 相册图片返回的不再是真实的Uri,而是分装过的Uri
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        cropPhoto(uri);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        imagePath = getImagePath(uri, null);
        cropPhoto(uri);
    }

    private void showDoubleButtonDialog(String alertText, String okText, String noText, View.OnClickListener okClickListener, View.OnClickListener cancelClickListener) {
        mDialog = builder.setMessage(alertText)
                .setPositiveButton(okText, okClickListener)
                .setNegativeButton(noText,cancelClickListener)
                .createTwoButtonDialog();
        mDialog.show();
    }

}
