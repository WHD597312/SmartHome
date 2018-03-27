package com.xinrui.http;

import android.util.Log;


import com.zhy.http.okhttp.OkHttpUtils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by whd on 2017/12/23.
 */

public class HttpUtils {
    public static String getInputStream(InputStream is) {
        String result = null;
        byte[] buffer = new byte[1024 * 10];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            result = new String(bos.toByteArray(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getOkHpptRequest(Map<String, Object> map) {
        String result=null;
        try{
            String url="http://120.77.36.206:8082/warmer/v1.0/house/registerHouse";
            OkHttpClient client=new OkHttpClient();
            FormBody.Builder builder=new FormBody.Builder();
            if(map!=null || map.size()!=0){
                for (Map.Entry<String,Object> param:map.entrySet()){
                    builder.addEncoded(param.getKey(),param.getValue()+"");
                }
            }
            FormBody body=builder.build();
            Request request=new Request.Builder().url(url).post(body).build();
            Call call=client.newCall(request);
            Response response=call.execute();
            if(response.isSuccessful()){
                result= response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String upLoadFile(String url, String fileNmae, File file) {
        String result = null;
        try {
            com.squareup.okhttp.Response response = OkHttpUtils.post()
                    .addFile("file", fileNmae, file)
                    .url(url)
                    .build()
                    .execute();
            if (response.isSuccessful()) {
                result = response.body().string();
                Log.d("result", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
