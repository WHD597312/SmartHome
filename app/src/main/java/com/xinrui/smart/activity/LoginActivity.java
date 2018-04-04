package com.xinrui.smart.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.xinrui.http.HttpUtils;
import com.xinrui.smart.MyApplication;
import com.xinrui.smart.R;
import com.xinrui.smart.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginActivity extends AppCompatActivity {

    Unbinder unbinder;
    MyApplication application;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.et_pswd)
    EditText et_pswd;
    String url = "http://120.77.36.206:8082/warmer/v1.0/user/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unbinder = ButterKnife.bind(this);
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
    }

    SharedPreferences preferences;

    @Override
    protected void onStart() {
        super.onStart();
        preferences = getSharedPreferences("my", MODE_PRIVATE);
        if (preferences.contains("phone")){
            String phone = preferences.getString("phone", "");
            et_name.setText(phone);
        }
        if (preferences.contains("phone") && preferences.contains("password")) {
            String phone = preferences.getString("phone", "");
            String password = preferences.getString("password", "");
            et_name.setText(phone);
            et_pswd.setText(password);
        }
    }

    @OnClick({R.id.btn_login, R.id.tv_register,R.id.tv_forget_pswd})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_register:
                startActivity(new Intent(this, RegistActivity.class));
                break;
            case R.id.btn_login:
                String phone = et_name.getText().toString().trim();
                String password = et_pswd.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    Utils.showToast(this, "手机号码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Utils.showToast(this, "请输入密码");
                    return;
                }
                Map<String, Object> params = new HashMap<>();
                params.put("phone", phone);
                params.put("password", password);
                new LoginAsyncTask().execute(params);
                break;
            case R.id.tv_forget_pswd:
                startActivity(new Intent(this, ForgetPswdActivity.class));
                break;
        }
    }

    class LoginAsyncTask extends AsyncTask<Map<String, Object>, Void, Integer> {

        @Override
        protected Integer doInBackground(Map<String, Object>... maps) {
            int code = 0;
            Map<String, Object> params = maps[0];
            String result = HttpUtils.postOkHpptRequest(url, params);
            try {
                if (!Utils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    JSONObject content=jsonObject.getJSONObject("content");
                    int userId=content.getInt("userId");
                    String phone=content.getString("phone");
                    String password=content.getString("password");
                    if (code==2000){

                        SharedPreferences.Editor editor=preferences.edit();
                        if (!preferences.contains("password")) {
                            editor.putString("phone",phone);
                            editor.putString("password",password);
                        }
                        editor.putString("userId",userId+"");
                        editor.commit();
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
                case -1006:
                    Utils.showToast(LoginActivity.this, "手机号码未注册");
                    break;
                case -1005:
                    Utils.showToast(LoginActivity.this, "用户名或密码错误");
                    et_name.setText("");
                    et_pswd.setText("");
                    break;
                case 2000:
                    Utils.showToast(LoginActivity.this, "登录成功");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivityForResult(intent,MainActivity.LOGIN);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==MainActivity.LOGIN){
            if (preferences.contains("phone")){
                String phone = preferences.getString("phone", "");
                et_name.setText(phone);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
