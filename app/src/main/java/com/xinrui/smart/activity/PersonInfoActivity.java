package com.xinrui.smart.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.transcode.GifBitmapWrapperDrawableTranscoder;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.xinrui.http.HttpUtils;
import com.xinrui.secen.scene_activity.AddEquipmentActivity;
import com.xinrui.secen.scene_activity.RoomContentActivity;
import com.xinrui.secen.scene_util.BitmapCompressUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.util.GlideCircleTransform;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DeviceUpdatePersonDialog;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PersonInfoActivity extends AppCompatActivity {

    @BindView(R.id.tv_image)
    TextView tv_image;
    @BindView(R.id.image_user)
    ImageView image_user;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_user)
    TextView tv_user;
    Unbinder unbinder;
    SharedPreferences preferences;

    MyApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);
        unbinder = ButterKnife.bind(this);
        if (application == null) {
            application = (MyApplication) getApplication();
            application.addActivity(this);
        }
        Intent intent = getIntent();

        device = intent.getStringExtra("device");
        change = intent.getStringExtra("change");
        smart=intent.getStringExtra("smart");
        live=intent.getStringExtra("live");
    }

    String device;
    String change;
    String content;
    String smart;
    String live;
    @Override
    protected void onStart() {
        super.onStart();


        preferences = getSharedPreferences("my", MODE_PRIVATE);

//        File file = new File(getExternalCacheDir(), "crop_image2.jpg");

        try {
            String image=preferences.getString("image","");
            if (!Utils.isEmpty(image)){
                File file=new File(image);
                if (file.exists()){
                    Glide.with(PersonInfoActivity.this).load(file).transform(new GlideCircleTransform(getApplicationContext())).into(image_user);
                }else {
                    String userId = preferences.getString("userId", "");
                    String url = "http://47.98.131.11:8082/warmer/v1.0/user/" + userId + "/headImg";
                    Glide.with(PersonInfoActivity.this).load(url).transform(new GlideCircleTransform(getApplicationContext())).error(R.mipmap.touxiang).into(image_user);
                }
            }else {
                String userId = preferences.getString("userId", "");
                String url = "http://47.98.131.11:8082/warmer/v1.0/user/" + userId + "/headImg";
                Glide.with(PersonInfoActivity.this).load(url).transform(new GlideCircleTransform(getApplicationContext())).error(R.mipmap.touxiang).into(image_user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String username = preferences.getString("username", "");
        String phone = preferences.getString("phone", "");
        tv_user.setText(phone);
        tv_user_name.setText(username);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        application.removeActivity(this);
    }

    @OnClick({R.id.head_linearout, R.id.head_name, R.id.img_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.head_linearout:
                popupWindow();
                break;
            case R.id.head_name:
                buildUpdatePersonDialog();
                break;
            case R.id.img_back:
                if (!Utils.isEmpty(device)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("deviceList","deviceList");
                    startActivity(intent);
                } else if (!Utils.isEmpty(smart)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("mainControl","mainControl");
                    startActivity(intent);
                }else if (!Utils.isEmpty(live)){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("live","live");
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (dialog!=null && dialog.isShowing()){
            backgroundAlpha(1.0f);
            dialog.dismiss();
            return;
        }
        if (popupWindow!=null && popupWindow.isShowing()){
            backgroundAlpha(1.0f);
            popupWindow.dismiss();
            return;
        }
        if (!Utils.isEmpty(device)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("deviceList","deviceList");
            startActivity(intent);
        }else if (!Utils.isEmpty(smart)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("mainControl","mainControl");
            startActivity(intent);
        }else if (!Utils.isEmpty(live)){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("live","live");
            startActivity(intent);
        }

    }

    private String name;
    DeviceUpdatePersonDialog dialog;

    private void buildUpdatePersonDialog() {
        dialog = new DeviceUpdatePersonDialog(this);
        dialog.setOnNegativeClickListener(new DeviceUpdatePersonDialog.OnNegativeClickListener() {
            @Override
            public void onNegativeClick() {
                dialog.dismiss();
                dialog = null;
                backgroundAlpha(1.0f);
            }
        });
        backgroundAlpha(0.4f);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnPositiveClickListener(new DeviceUpdatePersonDialog.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                name = dialog.getName();
                if (Utils.isEmpty(name)) {
                    Utils.showToast(PersonInfoActivity.this, "用户名称不能为空");
                } else {
                    backgroundAlpha(1.0f);
                    String userId = preferences.getString("userId", "");
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", userId);
                    map.put("username", name);
//                        new UpdateHomeNameAsync().execute(updateDeviceGroup);
                    new UpdatePersonAsync().execute(map);
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });
        dialog.show();
    }

    //设置蒙版
    private void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp =getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }
    class UpdatePersonAsync extends AsyncTask<Map<String, Object>, Void, Integer> {

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> map = maps[0];
            try {
                String updateUrl = "http://47.98.131.11:8082/warmer/v1.0/user/modInfo";
                String result = HttpUtils.postOkHpptRequest(updateUrl, map);

                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        preferences.edit().putString("username", name).commit();
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
                    Utils.showToast(PersonInfoActivity.this, "修改成功");
                    tv_user_name.setText(name);
                    break;
                default:
                    Utils.showToast(PersonInfoActivity.this, "修改失败");
                    break;
            }
        }
    }

    private PopupWindow popupWindow;

    //底部popupWindow
    public void popupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }


        View view = View.inflate(this, R.layout.changepicture, null);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);

//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow.showAtLocation(tv_image, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        //添加按键事件监听
        setButtonListeners(view);
        backgroundAlpha(0.4f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
    }

    private void setButtonListeners(View layout) {
        Button camera = (Button) layout.findViewById(R.id.camera);
        Button gallery = (Button) layout.findViewById(R.id.gallery);
        TextView cancel = (TextView) layout.findViewById(R.id.cancel);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    if (Build.VERSION.SDK_INT >= 23) {
                        //android 6.0权限问题
                        if (ContextCompat.checkSelfPermission(PersonInfoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(PersonInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(PersonInfoActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERAPRESS);
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
                        if (ContextCompat.checkSelfPermission(PersonInfoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(PersonInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(PersonInfoActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, ICONPRESS);
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
                    backgroundAlpha(1.0f);
                    popupWindow.dismiss();
                }
            }
        });
    }

    final static int CAMERA = 1;//拍照
    final static int ICON = 2;//相册
    final static int CAMERAPRESS = 3;//拍照权限
    final static int ICONPRESS = 4;//相册权限
    final static int PICTURE_CUT = 5;//剪切图片
    private static final String TAG = "RoomContentActivity";
    private Uri outputUri;//裁剪完照片保存地址
    Uri imageUri; //图片路径
    File imageFile; //图片文件
    String imagePath;
    private boolean isClickCamera;//是否是拍照裁剪

    //拍照
    public void startCamera() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        imageFile = new File(getExternalCacheDir(), "background2.png");
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
            imageUri = FileProvider.getUriForFile(PersonInfoActivity.this, "com.hm.camerademo.fileprovider", imageFile);
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

    /**
     * 裁剪图片
     */
    private void cropPhoto(Uri uri) {
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        // 创建File对象，用于存储裁剪后的图片，避免更改原图
        File file = new File(getExternalCacheDir(), "crop_image2.jpg");
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

    File file2;

    private void upImage(File file) {

        if (file != null) {
            new LoadUserInfo().execute(file);
            file2 = file;
        }
    }

    class LoadUserInfo extends AsyncTask<File, Void, Integer> {

        @Override
        protected Integer doInBackground(File... files) {
            int code = 0;
            File file = files[0];
            String userId = preferences.getString("userId", "");
            String url = "http://47.98.131.11:8082/warmer/v1.0/user/" + userId + "/headImg";
            String result = HttpUtils.upLoadFile(url, "HeadPortrait2.jpg", file);
            if (!Utils.isEmpty(result)) {
                code = Integer.parseInt(result);
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 201:
                    Utils.showToast(PersonInfoActivity.this, "上传成功");
                    String userId = preferences.getString("userId", "");
                    String url = "http://47.98.131.11:8082/warmer/v1.0/user/" + userId + "/headImg";
//                    Picasso.with(PersonInfoActivity.this).load(url).error(R.drawable.bedroom1).memoryPolicy(MemoryPolicy.NO_CACHE).into(image_user);
                    Glide.with(PersonInfoActivity.this).load(file2).transform(new GlideCircleTransform(getApplicationContext())).into(image_user);
                    Log.i("file",file2.getPath());
                    String image=preferences.getString("image","");
                    if (!Utils.isEmpty(image)){
                        File file=new File(image);
                        if (file.exists()){
                            file.delete();
                        }
                    }
                    preferences.edit().putString("image",file2.getPath()).commit();
                    break;
                default:
                    Utils.showToast(PersonInfoActivity.this, "上传失败");
                    break;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File file;

        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "requestCode" + requestCode + "resultCode" + resultCode);
        switch (requestCode) {
            case CAMERA:
                if (resultCode == RESULT_OK) {
                    cropPhoto(imageUri);
                }
                break;
            case ICON:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
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
                if (bitmap2 == null) {
                    break;
                }
                File file2 = BitmapCompressUtils.compressImage(bitmap2);
                upImage(file2);
                break;
        }
    }
}
