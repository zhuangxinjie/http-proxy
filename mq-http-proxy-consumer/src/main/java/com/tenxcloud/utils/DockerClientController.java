/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/8/29 @author xinjie
 */
package com.tenxcloud.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName DockerClientController
 * @Description TODO
 * @Author xinjie
 * @CreateDate 2019/8/29 11:28
 */
@RestController
@Slf4j
public class DockerClientController {
    @Value("${image.queue.name}")
    String headQueue;

    @Autowired
    ProxyService proxyService;
    @RequestMapping()
    @ResponseBody
    public Object dockerImagePull(HttpServletRequest request,HttpServletResponse response)throws JMSException{
        StringBuffer url = request.getRequestURL();
        String userAgent = request.getHeader("User-Agent");
        String authorization = request.getHeader("Authorization");
        String acceptEncoding = request.getHeader("Accept-Encoding");
        String connection = request.getHeader("Connection");
        Map<String, String> params = new HashMap<>();
        params.put("url",url.toString());
        params.put("userAgent",userAgent);
        params.put("authorization",authorization);
        params.put("acceptEncoding",acceptEncoding);
        params.put("connection",connection);
        Map<String, String> headers = new HashMap<>();

        ProxyBody pb = new ProxyBody(url.toString(), request.getMethod(), headers, params,
               null);
        String responseText = proxyService.resolve(headQueue, pb);
        Map<String, Object> result = JSONObject.parseObject(responseText);
        if(result.containsKey("Content-Type") && result.get("Content-Type").toString()!=null ){
            response.setHeader("Content-Type",result.get("Content-Type").toString()!=null ? result.get("Content-Type").toString() : "");
        }
        if(result.containsKey("Docker-Distribution-Api-Version") && result.get("Docker-Distribution-Api-Version").toString() !=null ){
            response.setHeader("Docker-Distribution-Api-Version",result.get("Docker-Distribution-Api-Version").toString() !=null ? result.get("Docker-Distribution-Api-Version").toString() : "");
        }
        if(result.containsKey("Content-Encoding") && !result.get("Content-Encoding").toString().equals("") ){
            response.setHeader("Content-Encoding",result.get("Content-Encoding").toString()!= null ? result.get("Content-Encoding").toString() : "");
        }
        if(result.containsKey("Www-Authenticate") && result.get("Www-Authenticate").toString() !=null){
            response.setHeader("Www-Authenticate",result.get("Www-Authenticate").toString() !=null ? result.get("Www-Authenticate").toString() : "");
        }
       if(result.containsKey("code")){
           response.setStatus(Integer.parseInt(result.get("code").toString()!=null ? result.get("code").toString() : ""));
       }

        log.info(url+"----"+userAgent+"----"+authorization+"----"+acceptEncoding+"----"+connection);
       log.info(result.get("message").toString());
        return result.get("message");
    }


}
