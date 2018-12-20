package com.xinrui.http;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface HttpService {

    @GET
    @Headers({"client:android-xr"})
    Call<ResponseBody> getRequest(@Url String url);

    @POST
    @Headers({"client:android-xr"})
    Call<ResponseBody> postQuest(@Url String url,@Body RequestBody body);

    @Multipart
    @POST("user/{userId}/headImg")
    Call<ResponseBody> uploadFile(@Path("userId") String userId, @Part MultipartBody.Part file);


}
