package com.xinrui.http;

import android.util.Log;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhy.http.okhttp.OkHttpUtils;


import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

    public static String postOkHpptRequest(String url,Map<String, Object> map) {
        String result=null;
        try{
            String CONTENT_TYPE = "application/json";

            String JSON_DATA = "{\n" +
                    "\"deviceId\":1129,\n" +
                    "\"deviceTimeControlDtos\":\n" +
                    "[\n" +
                    "{\n" +
                    "\"week\":2,\n" +
                    "\"deviceTimeControlList\":[\n" +
                    "         {\"temp\":2.0,\"openTime\":2,\"closeTime\":3},\n" +
                    "         {\"temp\":2.0,\"openTime\":4,\"closeTime\":5}\n" +
                    "     ]\n" +
                    "}";
            JSONObject jsonObject=new JSONObject();
            for (Map.Entry<String,Object> param:map.entrySet()){
                jsonObject.put(param.getKey(),param.getValue());
            }



            RequestBody requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE),jsonObject.toJSONString());

            Request request = new Request.Builder()
                    .addHeader("client","android-xr")
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient=new OkHttpClient();
            Response response=okHttpClient.newCall(request).execute();

            if(response.isSuccessful()){
                result= response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String postOkHpptRequest2(String url, JSONArray jsonArray) {
        String result=null;
        try{
            String CONTENT_TYPE = "application/json";

            String JSON_DATA = "{\n" +
                    "    \"houseId\":1000,\n" +
                    "    \"controlledId\":[5,6]\n" +
                    "}";
            JSONObject jsonObject=new JSONObject();
//            for (Map.Entry<String,Object> param:map.entrySet()){
//                jsonObject.put(param.getKey(),param.getValue());
//            }

            RequestBody requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE),jsonArray.toString());

            Request request = new Request.Builder()
                    .addHeader("client","android-xr")
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient=new OkHttpClient();
            Response response=okHttpClient.newCall(request).execute();

            if(response.isSuccessful()){
                result= response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String postOkHpptRequest3(String url, org.json.JSONObject jsonObject) {
        String result=null;
        try{
            String CONTENT_TYPE = "application/json";

            String JSON_DATA = "{\n" +
                    "\"deviceId\":1129,\n" +
                    "\"deviceTimeControlDtos\":\n" +
                    "[\n" +
                    "{\n" +
                    "\"week\":2,\n" +
                    "\"deviceTimeControlList\":[\n" +
                    "         {\"temp\":2.0,\"openTime\":2,\"closeTime\":3},\n" +
                    "         {\"temp\":2.0,\"openTime\":4,\"closeTime\":5}\n" +
                    "     ]\n" +
                    "}";


            RequestBody requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE),jsonObject.toString());

            Request request = new Request.Builder()
                    .addHeader("client","android-xr")
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient=new OkHttpClient();
            Response response=okHttpClient.newCall(request).execute();

            if(response.isSuccessful()){
                result= response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public static String doDelete(String url,JSONArray jsonArray){
        String result=null;
        try {
            String CONTENT_TYPE = "application/json";


            RequestBody requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE),jsonArray.toString());

            Request request = new Request.Builder()
                    .url(url)
                    .delete(requestBody)
                    .build();
            OkHttpClient okHttpClient=new OkHttpClient();
            Response response=okHttpClient.newCall(request).execute();

            if(response.isSuccessful()){
                result= response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public static String getOkHpptRequest(String url) {
        String result=null;
        try{

            Request request = new Request.Builder()
                    .addHeader("client","android-xr")
                    .url(url)
                    .get()
                    .build();
            OkHttpClient okHttpClient=new OkHttpClient();
            Response response=okHttpClient.newCall(request).execute();

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
