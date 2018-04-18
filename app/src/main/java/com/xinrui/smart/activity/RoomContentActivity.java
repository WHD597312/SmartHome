package com.xinrui.smart.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
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
import android.widget.Toast;

import com.xinrui.http.HttpUtils;
import com.xinrui.smart.R;
import com.xinrui.smart.adapter.MyAdapter;
import com.xinrui.smart.pojo.Equipment;
import com.xinrui.smart.util.GetUrl;
import com.xinrui.smart.util.OkHttp;
import com.xinrui.smart.util.Utils;
import com.xinrui.smart.view_custom.DragImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    final static int CAMERA = 1;
    final static int ICON = 2;
    final static int CAMERAPRESS = 3;
    final static int ICONPRESS = 4;
    @BindView(R.id.background)
    ImageView background;
    @BindView(R.id.fl)
    FrameLayout fl;

    private List<Equipment> mDatas;
    private DragImageView dragImageView;
    private MyAdapter myAdapter;
    private Context mContext;
    private PopupWindow popupWindow;
    //网络返回的数据
    List<Equipment> equipment_network = new ArrayList<>();
    final int REQUEST_TAKE_PHOTO_PERMISSION = 1;

    private static final int SUCCESS = 1;
    private static final int FALL = 2;
    private static final String TAG = "RoomContentActivity";


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //加载网络成功进行UI的更新,处理得到的图片资源
                case SUCCESS:
                    //通过message，拿到字节数组
                    byte[] Picture = (byte[]) msg.obj;
                    //使用BitmapFactory工厂，把字节数组转化为bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
                    //通过imageview，设置图片
                    if(bitmap == null){
                        Toast.makeText(RoomContentActivity.this,"no pictures",Toast.LENGTH_LONG).show();
                    }else {
                        background.setImageBitmap(bitmap);
                        Toast.makeText(RoomContentActivity.this,"Have a picture",Toast.LENGTH_LONG).show();

                    }

                    break;
                //当加载网络失败执行的逻辑代码
                case FALL:
                    Toast.makeText(RoomContentActivity.this, "网络出现了问题", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", MODE_PRIVATE);
        int roomId = sharedPreferences.getInt("roomId",0);
        String url = "http://120.77.36.206:8082/warmer/v1.0/room/"+roomId+"/background";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_content);
        ButterKnife.bind(this);
        mDatas = new ArrayList<>();

        initViews();
        getRoomAllDevices();

//        new LoadPicture().execute(url);

        //1.创建一个okhttpclient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request.Builder对象，设置参数，请求方式如果是Get，就不用设置，默认就是Get
        Request request = new Request.Builder()
                .url(url)
                .build();
        //3.创建一个Call对象，参数是request对象，发送请求
        Call call = okHttpClient.newCall(request);
        //4.异步请求，请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //得到从网上获取资源，转换成我们想要的类型
                byte[] Picture_bt = response.body().bytes();
                //通过handler更新UI
                Message message = handler.obtainMessage();
                message.obj = Picture_bt;
                message.what = SUCCESS;
                handler.sendMessage(message);
            }
        });
    }
    class LoadPicture extends AsyncTask<String,Void,String>{


        @Override
        protected String doInBackground(String... strings) {
            String result2=null;
            String url=strings[0];
            String result=HttpUtils.getOkHpptRequest(url);
            if (!Utils.isEmpty(result)){
                result2 = result;
            }
            return result2;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            if (s!=null){
                //使用BitmapFactory工厂，把字节数组转化为bitmap
                Bitmap bitmap = decodeImg(s);
                //通过imageview，设置图片
                if(bitmap == null){
                    Toast.makeText(RoomContentActivity.this,"no pictures",Toast.LENGTH_LONG).show();
                }else {
                    background.setImageBitmap(bitmap);
                    Toast.makeText(RoomContentActivity.this,"Have a picture",Toast.LENGTH_LONG).show();
                }
            }
        }
        /**
         * 将从Message中获取的，表示图片的字符串解析为Bitmap对象
         *
         * @param picStrInMsg
         * @return
         */
        public  Bitmap decodeImg(String picStrInMsg) {
            Bitmap bitmap = null;

            byte[] imgByte = null;
            InputStream input = null;
            try{
                imgByte = Base64.decode(picStrInMsg, Base64.DEFAULT);
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inSampleSize = 8;
                input = new ByteArrayInputStream(imgByte);
                SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options));
                bitmap = (Bitmap)softRef.get();;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if(imgByte!=null){
                    imgByte = null;
                }

                if(input!=null){
                    try {
                        input.close();
                    } catch (IOException e) {
// TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

//
// byte[] imgByte = Base64.decode(picStrInMsg, Base64.DEFAULT);
//
// try {
// bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
// imgByte = null;
// } catch (OutOfMemoryError e) {
// e.printStackTrace();
// try {
// bitmap = BitmapFactory.decodeByteArray(imgByte, 0,
// imgByte.length);
// } catch (OutOfMemoryError e1) {
// e.printStackTrace();
// } catch (Exception e1) {
// e.printStackTrace();
// }
// } catch (Exception e) {
// e.printStackTrace();
// }


            return bitmap;
        }
        public  Bitmap byteToBitmap(byte[] imgByte) {
            InputStream input = null;
            Bitmap bitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            input = new ByteArrayInputStream(imgByte);
            SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                    input, null, options));
            bitmap = (Bitmap) softRef.get();
            if (imgByte != null) {
                imgByte = null;
            }

            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    private void initViews() {
        GetBackgroundAsyncTask getBackgroundAsyncTask = new GetBackgroundAsyncTask();
        getBackgroundAsyncTask.execute();
        dragImageView = (DragImageView) findViewById(R.id.dragGridView1);
    }

    public void initData(List<Equipment> equipmentList) {


        mDatas = equipmentList;
        myAdapter = new MyAdapter(mDatas);
        dragImageView.setLayoutManager(new GridLayoutManager(this, 4));
        dragImageView.setAdapter(myAdapter);
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View v, int postion) {
                showPopupMenu(v, postion);
            }
        });
        dragImageView.setItemAnimator(new DefaultItemAnimator());

    }

    //拍照
    public void startCamera() {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        imageFile = new File(path, "background.png");
        try {
            if (imageFile.exists()) {
                imageFile.delete();
            }
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将File对象转换为Uri并启动照相程序
        imageUri = Uri.fromFile(imageFile);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
        startActivityForResult(intent, CAMERA); //启动照相
    }

    public void startIcon() {
        Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent1, ICON);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.imageView, R.id.change_scene, R.id.return_scene, R.id.return_homepage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imageView:
                break;
            case R.id.change_scene:
                bottomwindow(changeScene);
                break;
            case R.id.return_scene:
                //回退到MainActivity判断是哪个fragment，并切换回之前的fragment
                Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
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

    private void showPopupMenu(View view, final int postion) {
        // 这里的view代表popupMenu需要依附的view
        final int postions = postion;
        PopupMenu popupMenu = new PopupMenu(RoomContentActivity.this, view);
        // 获取布局文件
        popupMenu.getMenuInflater().inflate(R.menu.remove_devices_menu, popupMenu.getMenu());
        popupMenu.show();
        // 通过上面这几行代码，就可以把控件显示出来了
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // 控件每一个item的点击事件

                JSONArray jsonArray = new JSONArray();
                Equipment equipment = mDatas.get(postion);
                deviceId = equipment.getId();
                jsonArray.put(deviceId);
                deleteDevices(jsonArray);
                mDatas.remove(postions);
                myAdapter.notifyDataSetChanged();
                return true;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                // 控件消失时的事件
            }
        });

    }

    class GainAllEquipmentAsyncTask extends AsyncTask<Void, Void, String> {
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", Context.MODE_PRIVATE);
        AsyncResponse asyncResponse;

        void setOnAsyncResponse(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected String doInBackground(Void... voids) {
            int code = 0;
            int roomId = sharedPreferences.getInt("roomId", 0);
            Map<String, Object> params = new HashMap<>();
            params.put("roomId", roomId);
            try {
                String url = getUrl.getRqstUrl("http://120.77.36.206:8082/warmer/v1.0/room/getRoomDevices", params);
                String result = HttpUtils.getOkHpptRequest(url);

                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    JSONArray jsonArray = jsonObject.getJSONArray("content");
                    return result;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!Utils.isEmpty(s)) {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("content");

                    List<Equipment> equipment_list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        String deviceName = object.getString("deviceName");
                        int type = object.getInt("type");
                        int houseId = object.getInt("houseId");
                        int masterControllerUserId = object.getInt("masterControllerUserId");
                        int isUnlock = object.getInt("isUnlock");

                        if (type == 1) {
                            type = R.drawable.equipment_warmer;
                        } else if (type == 2) {
                            type = R.drawable.equipment_external_sensor;
                        }
                        Equipment equipment = new Equipment(id, deviceName, type, houseId, masterControllerUserId, isUnlock, false);
                        equipment_list.add(equipment);
                    }
                    asyncResponse.onDataReceivedSuccess(equipment_list);
                } else {
                    asyncResponse.onDataReceivedFailed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
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

    public void getRoomAllDevices() {
        final GainAllEquipmentAsyncTask gainAllEquipmentAsyncTask = new GainAllEquipmentAsyncTask();
        gainAllEquipmentAsyncTask.execute();
        gainAllEquipmentAsyncTask.setOnAsyncResponse(new AsyncResponse() {
            @Override
            public void onDataReceivedSuccess(List<Equipment> listData) {
                equipment_network = listData;
                initData(equipment_network);
            }

            @Override
            public void onDataReceivedFailed() {

            }
        });
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", MODE_PRIVATE);
        int roomId = sharedPreferences.getInt("roomId",0);
        String url = "http://120.77.36.206:8082/warmer/v1.0/room/"+roomId+"/background";
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "requestCode" + requestCode + "resultCode" + resultCode);
        switch (requestCode) {
            case CAMERA:
                Bitmap bitmap1 = null;
                try {
                    bitmap1 = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    imagePath = getPath(this, imageUri);
                    bitmapdown = bitmap1;
                    if(bitmapdown == null){
                        break;
                    }
                    background.setImageBitmap(bitmapdown);
                } catch (FileNotFoundException e) {
                    imageFile = null;
                    e.printStackTrace();
                }
                upImage(imageFile);
//                getFileRequest(url,imageFile,null);
                Log.d("chenzhu", "imagePath" + imagePath);

                break;
            case ICON:
                //设置图片的宽高
                int height = fl.getHeight();
                int width = fl.getWidth();
                DisplayMetrics metric = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metric);
                if(null == data){
                    break;
                }
                String dst = getPath(this, data.getData());
                imageFile = new File(dst);
                imagePath = dst;
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(getBitmapFromFile(imageFile), width, height+50);
                bitmapdown = bitmap;
                if(bitmapdown == null){
                    break;
                }
                background.setImageBitmap(bitmapdown);
//                getFileRequest(url,imageFile,null);

                upImage(imageFile);


                Log.d("chenzhu", "imagePath" + imagePath);

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
                    startIcon();
                } else {
                    Toast.makeText(this, "对不起你没有同意该权限", Toast.LENGTH_LONG).show();
                }
                break;

        }

    }

    public Bitmap getBitmapFromFile(File dst) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            //opts.inJustDecodeBounds = false;
            opts.inSampleSize = 2;

            try {
                return BitmapFactory.decodeFile(dst.getPath(), opts);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    void bottomwindow(View view) {
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
        //添加pop窗口关闭事件，主要是实现关闭时改变背景的透明度
//        popupWindow.setOnDismissListener(new poponDismissListener());
//        backgroundAlpha(1f);
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
                        Toast.makeText(RoomContentActivity.this, "当前的版本号" + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();
                        //android 6.0权限问题
                        if (ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(RoomContentActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERAPRESS);
                            Toast.makeText(RoomContentActivity.this, "执行了权限请求", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(RoomContentActivity.this, "当前的版本号" + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();
                        //android 6.0权限问题
                        if (ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(RoomContentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(mContext, "执行了权限请求", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(RoomContentActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERAPRESS);
                        } else {
                            startIcon();
                        }

                    } else {
                        startIcon();
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
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);           getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
    /**
     * 返回或者点击空白位置的时候将背景透明度改回来
     */
    class poponDismissListener implements PopupWindow.OnDismissListener{

        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            new Thread(new Runnable(){
                @Override
                public void run() {
                    //此处while的条件alpha不能<= 否则会出现黑屏
                    while(alpha<1f){
                        try {
                            Thread.sleep(4);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("HeadPortrait","alpha:"+alpha);
                        Message msg =mHandler.obtainMessage();
                        msg.what = 1;
                        alpha+=0.01f;
                        msg.obj =alpha ;
                        mHandler.sendMessage(msg);
                    }
                }

            }).start();
        }

    }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    backgroundAlpha((float)msg.obj);
                    break;
            }
        }
    };

    //获取背景
    class GetBackgroundAsyncTask extends AsyncTask<Void,Void,String>{
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", Context.MODE_PRIVATE);

        @Override
        protected String doInBackground(Void... voids) {
            int code = 0;
            int roomId = sharedPreferences.getInt("roomId",0);
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", roomId);
            map.put("background",bitmapdown);
            String url = "http://120.77.36.206:8082/warmer/v1.0/room/"+roomId+"/background";
            String result = HttpUtils.getOkHpptRequest(url);
            try{
                JSONObject jsonObject = new JSONObject(result);
                code = jsonObject.getInt("code");
                if (code == 2000) {
                    return result;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!Utils.isEmpty(s)){

            }
            super.onPostExecute(s);
        }
    }

//    //换景
//    class ChangeBackgroundAsyncTask extends AsyncTask<JSONArray,Void,Integer>{
//        SharedPreferences sharedPreferences = getSharedPreferences("roomId", Context.MODE_PRIVATE);
//
//        @Override
//        protected Integer doInBackground(JSONArray... jsonArrays) {
//           int coid = 0;
//           JSONArray params = jsonArrays[0];
//            int roomId = sharedPreferences.getInt("roomId",0);
//            Map<String, Object> map = new HashMap<>();
//            map.put("roomId", roomId);
//            map.put("background",bitmapdown);
//            String url = "http://120.77.36.206:8082/warmer/v1.0/room/"+roomId+"/background";
//            String result = HttpUtils.postOkHpptRequest2(url,params);
//            return null;
//        }
//    }


    public static Request  getFileRequest(String url,File file,Map<String, String> maps){
        MultipartBody.Builder builder=  new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(maps==null){
            builder.addPart( Headers.of("Content-Disposition", "form-data; name=\"file\";filename=\"file.jpg\""), RequestBody.create(MediaType.parse("image/png"),file)
            ).build();

        }else{
            for (String key : maps.keySet()) {
                builder.addFormDataPart(key, maps.get(key));
            }

            builder.addPart( Headers.of("Content-Disposition", "form-data; name=\"file\";filename=\"file.jpg\""), RequestBody.create(MediaType.parse("image/png"),file)
            );

        }

//        MultipartBody body = new MultipartBody.Builder("AaB03x")
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("files", null, new MultipartBody.Builder("BbC04y")
//                        .addPart(Headers.of("Content-Disposition", "form-data; filename=\"img.png\""),
//                                RequestBody.create(MediaType.parse("image/png"), new File(url)))
//                        .build())
//                .build();
        RequestBody body=builder.build();
        return   new Request.Builder().url(url).post(body).build();

    }

    private void upImage(File file) {
        SharedPreferences sharedPreferences = getSharedPreferences("roomId", MODE_PRIVATE);
        int roomId = sharedPreferences.getInt("roomId",0);
        String url="http://120.77.36.206:8082/warmer/v1.0/room/"+roomId+"/background";

        if (file!=null){
            new AddPicuterAsync().execute(url,file);
        }



//        OkHttpClient mOkHttpClent = new OkHttpClient();
////        File file = new File(Environment.getExternalStorageDirectory()+"/HeadPortrait.jpg");
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", "HeadPortrait.jpg",
//                        RequestBody.create(MediaType.parse("image/png"), file));
//
//        RequestBody requestBody = builder.build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//        Call call = mOkHttpClent.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e(TAG, "onFailure: "+e );
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(RoomContentActivity.this, "失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                response.body().toString();
//                Log.e(TAG, "成功"+response);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(RoomContentActivity.this, "成功", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });

    }
    class AddPicuterAsync extends AsyncTask<Object,Void,Integer>{

        @Override
        protected Integer doInBackground(Object... files) {
            int code=0;
           String url= (String) files[0];
           File file= (File) files[1];
           if (file!=null){
               String result=HttpUtils.upLoadFile(url,"HeadPortrait2.jpg",file);
               if (!Utils.isEmpty(result)){
                   code=Integer.parseInt(result);
               }
           }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code){
                case 201:
                    Toast.makeText(RoomContentActivity.this, "成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(RoomContentActivity.this, "失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

}
