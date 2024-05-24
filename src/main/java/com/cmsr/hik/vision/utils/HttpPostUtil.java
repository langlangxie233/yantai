package com.cmsr.hik.vision.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpPostUtil {

    public static StringBuffer sendPostRequest(String url, Map<String, String> headers, String body) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求方法为POST
        con.setRequestMethod("POST");

        // 设置请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        // 设置请求体
        if (body != null) {
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 获取响应
        int responseCode = con.getResponseCode();
        //System.out.println("Response Code : " + responseCode);

        // 读取响应内容（如果需要）
         BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
         String inputLine;
         StringBuffer response = new StringBuffer();
         while ((inputLine = in.readLine()) != null) {
             response.append(inputLine);
         }
         in.close();

        // 打印响应内容（如果需要）
        // System.out.println(response.toString());
        return response;
    }
}