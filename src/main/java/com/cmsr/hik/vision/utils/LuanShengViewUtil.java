package com.cmsr.hik.vision.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LuanShengViewUtil {

    public static void request() throws Exception {
        String apiHost = "http://172.168.1.69:29993";
        String appkey = "e0f2eea28f1fd98291953c936db52191";
        String path = "/view/pollution/factors/list";
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put("queryType", Arrays.asList("all"));
        Long timestamp = System.currentTimeMillis();
        //Long timestamp = 1699526333L;
        String sign = sign(queryParams, timestamp);
        Map<String, String> getQueryParams = new HashMap<>();
        getQueryParams.put("x-data365-appkey", appkey);
        getQueryParams.put("x-data365-timestamp", timestamp.toString());
        getQueryParams.put("x-data365-sign", sign);
        String url = apiHost + path + "&queryType=all";
        System.out.println(timestamp);
        System.out.println(sign);
        String result = sendGetRequest(url, getQueryParams);
        System.out.println("-----------result------------");
        System.out.println(result);

    }

    private static String sign(Map<String, List<String>> queryParams, Long timestamp) {
        String appkey = "e0f2eea28f1fd98291953c936db52191";
        StringBuilder signStr = new StringBuilder(appkey).append(timestamp);

        TreeMap<String, List<String>> sortParams = new TreeMap<>(queryParams);
        for (String key : sortParams.keySet()) {
            List<String> values = sortParams.get(key);
            if (values == null) {
                //空参数不参与校验
                continue;
            }
            String valueStr = values.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(","));
            if ("".equals(valueStr.trim())) {
                //空参数不参与校验
                continue;
            }
            signStr.append("&").append(key).append("=").append(valueStr);
        }

        String sign = sign(signStr.toString());
        return sign;
    }


    private static String sign(String signStr) {
        String secret = "efeda461fed893d0706e89d139cd84da";
        byte[] bytes = null;
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes("utf-8"), "HmacMD5");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(signStr.getBytes("utf-8"));
        } catch (Exception gse) {
            throw new RuntimeException(gse);
        }
        System.out.println("HmacMD5: " + bytes);
        return byte2hex(bytes);
    }

    private static String byte2hex(byte[] bytes) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                str.append("0");
            }
            str.append(hex.toUpperCase());
        }
        return str.toString();
    }

    private static String sendGetRequest(String url, Map<String, String> headers) throws Exception {
        HttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url); // 替换为你的URL

        // 设置headers
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity responseEntity = response.getEntity();
        String responseString = null;
        if (responseEntity != null) {
            responseString = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            System.out.println("Response: " + responseString);
        }
        return responseString;
    }
}
