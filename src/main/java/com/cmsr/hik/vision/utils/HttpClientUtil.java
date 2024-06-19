package com.cmsr.hik.vision.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClientUtil {

    public static String sendPostRequest(String url, Map<String, String> headers, String body) throws Exception {
        HttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(url); // 替换为你的URL

        // 设置headers
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }

        // 设置POST请求的body
        StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
        entity.setContentType("application/json"); // 根据你的数据设置正确的MIME类型
        httpPost.setEntity(entity);

        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();
        String responseString = null;
        if (responseEntity != null) {
            responseString = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            //System.out.println("<==========================Response==============================>" + responseString);
        }
        return responseString;
    }
}