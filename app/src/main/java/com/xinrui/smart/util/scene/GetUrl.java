package com.xinrui.smart.util.scene;

import java.util.Map;

/**
 * Created by win7 on 2018/4/10.
 */

public class GetUrl {
    /**
     * 拼接get请求的url请求地址
     */
    public static String getRqstUrl(String url, Map<String, Object> params) {
        StringBuilder builder = new StringBuilder(url);
        boolean isFirst = true;
        for (String key : params.keySet()) {
            if (key != null && params.get(key) != null) {
                if (isFirst) {
                    isFirst = false;
                    builder.append("?");
                } else {
                    builder.append("&");
                }
                builder.append(key)
                        .append("=")
                        .append(params.get(key));
            }
        }
        return builder.toString();
    }
}
