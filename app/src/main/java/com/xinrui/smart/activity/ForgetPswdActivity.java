package com.xinrui.smart.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xinrui.database.dao.daoimpl.DeviceChildDaoImpl;
import com.xinrui.database.dao.daoimpl.DeviceGroupDaoImpl;
import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.util.Mobile;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ForgetPswdActivity extends AppCompatActivity {

    private String TAG = "RegistActivity";
    MyApplication application;
    Unbinder unbinder;
    @BindView(R.id.et_phone)
    EditText et_phone;
    @BindView(R.id.et_code)
    EditText et_code;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.btn_get_code)
    Button btn_get_code;
    private String url = "http://47.98.131.11:8082/warmer/v1.0/user/forgetPassword";

    private DeviceChildDaoImpl deviceChildDao;
    private DeviceGroupDaoImpl deviceGroupDao;
    private String isExistUrl="http://47.98.131.11:8082/warmer/v1.0/user/isExist?phone=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pswd);

        unbinder = ButterKnife.bind(this);
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
    }

    Toast toast;

    @Override
    protected void onStart() {
        super.onStart();
        SMSSDK.registerEventHandler(eventHandler);
        deviceGroupDao = new DeviceGroupDaoImpl(getApplicationContext());
        deviceChildDao = new DeviceChildDaoImpl(getApplicationContext());

    }


    private EventHandler eventHandler = new EventHandler() {
        @Override
        public void afterEvent(int event, int result, Object data) {
            super.afterEvent(event, result, data);
            if (result == SMSSDK.RESULT_COMPLETE) {
                //回调完成
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    //提交验证码成功
                    @SuppressWarnings("unchecked") HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                    String country = (String) phoneMap.get("country");
                    String phone = (String) phoneMap.get("phone");
                    Log.d(TAG, "提交验证码成功--country=" + country + "--phone" + phone);
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    //获取验证码成功
                    Log.d(TAG, "获取验证码成功");
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    //返回支持发送验证码的国家列表
                }
            } else {
                ((Throwable) data).printStackTrace();
            }
        }
    };

    @OnClick({R.id.btn_finish, R.id.btn_get_code, R.id.image_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.btn_finish:
                String phone2 = et_phone.getText().toString().trim();
                String code = et_code.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(phone2)) {
                    Utils.showToast(this, "手机号码不能为空");
                    break;
                } else if (!Mobile.isMobile(phone2)) {
                    Utils.showToast(this, "手机号码不合法");
                    break;
                }
                if (TextUtils.isEmpty(code)) {
                    Utils.showToast(this, "请输入验证码");
                    break;
                }
                if (TextUtils.isEmpty(password)) {
                    Utils.showToast(this, "请输入密码");
                    break;
                }
                if (password.length() < 6) {
                    Utils.showToast(this, "密码最少6位");
                    break;
                }

                Map<String, Object> params = new HashMap<>();
                params.put("phone", phone2);
                params.put("code", code);
                params.put("password", password);

                new RegistAsyncTask().execute(params);

                break;
            case R.id.btn_get_code:

                String phone = et_phone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    Utils.showToast(this, "手机号码不能为空");
                } else {
                    boolean flag = Mobile.isMobile(phone);
                    if (flag) {
                       new  IsPhoneExistAsync().execute(phone);
                    } else {
                        Utils.showToast(this, "手机号码不合法");
                    }
                }
                break;
        }
    }


    class IsPhoneExistAsync extends AsyncTask<String,Void,Integer>{
        String phone= "";
        @Override
        protected Integer doInBackground(String ...s) {
            int code=0;
            phone=s[0];
            try {
                String isExistUrl="http://47.98.131.11:8082/warmer/v1.0/user/isExist?phone="+phone;
                String result=HttpUtils.getOkHpptRequest(isExistUrl);
                if (!TextUtils.isEmpty(result)){
                    JSONObject jsonObject=new JSONObject(result);
                    code=jsonObject.getInt("code");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            switch (code) {
                case 2000:
                    SMSSDK.getVerificationCode("86", phone);
                    CountTimer countTimer = new CountTimer(60000, 1000);
                    countTimer.start();
                    break;
                case  -1006:
                    Utils.showToast(ForgetPswdActivity.this, "手机号码未注册");
                    break;
            }
        }
    }




    class RegistAsyncTask extends AsyncTask<Map<String, Object>, Void, Integer> {

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];
            String result = HttpUtils.postOkHpptRequest(url, params);
            if (!Utils.isEmpty(result)) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code == 2000) {
                        SharedPreferences preferences = getSharedPreferences("my", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        String phone = et_phone.getText().toString().trim();
                        String password = et_password.getText().toString().trim();
                        editor.putString("phone", phone);
                        editor.putString("password", password);
                        deviceChildDao.deleteAll();
                        deviceGroupDao.deleteAll();
                        if (preferences.contains("login")) {
                            editor.remove("login").commit();
                        }
                        editor.commit();
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
                case -1006:
                    toast=Toast.makeText(ForgetPswdActivity.this,"手机号码未注册",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
//                    Utils.showToast(ForgetPswdActivity.this, "手机号码未注册");
                    break;
                case -1003:
                    toast=Toast.makeText(ForgetPswdActivity.this,"验证码错误",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
//                    Utils.showToast(ForgetPswdActivity.this, "验证码错误");
                    break;
                case 2000:
                    toast=Toast.makeText(ForgetPswdActivity.this,"重新设置密码成功",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
//                    Utils.showToast(ForgetPswdActivity.this, "重新设置密码成功");
                    Intent intent = new Intent(ForgetPswdActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        SMSSDK.unregisterEventHandler(eventHandler);
    }


    class CountTimer extends CountDownTimer {
        public CountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**
         * 倒计时过程中调用
         *
         * @param millisUntilFinished
         */
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Tag", "倒计时=" + (millisUntilFinished / 1000));
            if (btn_get_code!=null){
                btn_get_code.setText(millisUntilFinished / 1000 + "s后重新发送");
                //设置倒计时中的按钮外观
                btn_get_code.setClickable(false);//倒计时过程中将按钮设置为不可点击
            }

//            btn_get_code.setBackgroundColor(Color.parseColor("#c7c7c7"));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
//            btn_get_code.setTextSize(16);
        }

        /**
         * 倒计时完成后调用
         */
        @Override
        public void onFinish() {
            Log.e("Tag", "倒计时完成");
            //设置倒计时结束之后的按钮样式
//            btn_get_code.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_blue_light));
//            btn_get_code.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
//            btn_get_code.setTextSize(18);
            if (btn_get_code != null) {
                btn_get_code.setText("重新发送");
                btn_get_code.setClickable(true);
            }
        }
    }
}
