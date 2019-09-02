/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/8/29 @author xinjie
 */
package com.tenxcloud.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName PullImageListener
 * @Description TODO
 * @Author xinjie
 * @CreateDate 2019/8/29 18:00
 */
@Component
@EnableJms
@Slf4j
public class PullImageListener {

    @Value("${image.queue.name}")
    String headQueue;

    @JmsListener(destination = "${image.queue.name}")
    public String pullImage(String  request){
        ProxyBody proxyBody = JSON.toJavaObject(JSONObject.parseObject(request), ProxyBody.class);
        OkHttpClient okhttpClient = new OkHttpClient();
        try {
            String acceptEncoding = proxyBody.getParams().get("acceptEncoding");
            Request.Builder req = new Request.Builder().url(proxyBody.url);
                if(proxyBody.getParams().containsKey("Accept-Encoding") && !proxyBody.getParams().get("Accept-Encoding").equals("")){
                   req.addHeader("Accept-Encoding",proxyBody.getParams().get("acceptEncoding") != null ? proxyBody.getParams().get("acceptEncoding") : "");
                }
                if(proxyBody.getParams().containsKey("Authorization") && !proxyBody.getParams().get("Authorization").equals("")){
                    req.addHeader("Authorization",proxyBody.getParams().get("authorization") !=null ? proxyBody.getParams().get("authorization") : "");
                }
                if(proxyBody.getParams().containsKey("User-Agent") && !proxyBody.getParams().get("User-Agent").equals("")){
                    req.addHeader("User-Agent",proxyBody.getParams().get("userAgent") !=null ? proxyBody.getParams().get("userAgent") : "");
                }
               if(proxyBody.getParams().containsKey("User-Agent") && !proxyBody.getParams().get("User-Agent").equals("")){
                   req.addHeader("Connection",proxyBody.getParams().get("connection") !=null ? proxyBody.getParams().get("connection") : "");
               }
            Request request1 =req.build();
            Response response = okhttpClient.newCall(request1).execute();
            Headers headers = response.headers();
            int code = response.code();
            String type = headers.get("Content-Type");
            String dockerApiVersion = headers.get("Docker-Distribution-Api-Version");
            String contentEncoding = headers.get("Content-Encoding");
            String bearer = headers.get("Www-Authenticate");
            log.info(type+"----"+dockerApiVersion+"----"+contentEncoding+"----"+bearer);
            Map<String, Object> params=new HashMap<>();
            params.put("Content-Type",type);
            params.put("Docker-Distribution-Api-Version",dockerApiVersion);
            params.put("Content-Encoding",contentEncoding!=null ? contentEncoding : "");
            params.put("Www-Authenticate",bearer);
            params.put("message",response.message());
            params.put("code",code);
           return JSON.toJSONString(params);
        }catch (Exception e){
            log.info(e.toString());
        }

        return null;
    }

}
