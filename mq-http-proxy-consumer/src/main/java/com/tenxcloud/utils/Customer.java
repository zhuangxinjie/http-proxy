/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 * Customer
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-05 10:56
 */
@Component
@EnableJms
@Slf4j
public class Customer {

    @Autowired
    private K8sProxyBuilder proxyBuilder;

    @Value("${cluster.address}")
    String clusterAddress;


    @JmsListener(destination = "${queue.name}")
    public String consume(String message) {
        log.info("received message: {}", message);
        ProxyBody pb = JSON.toJavaObject(JSONObject.parseObject(message), ProxyBody.class);
        Object body = pb.getContent();
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(pb.getHeaders());
        HttpEntity request = new HttpEntity(body, headers);
        ResponseEntity<String> ret = null;
        try {
            ret = proxyBuilder.build().call(
                    new K8sServiceURL(null, clusterAddress + pb.getUrl()),
                    HttpMethod.valueOf(pb.getMethod().toUpperCase()), request, String.class);
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            String errorInfo = e.getMessage();
            if (e instanceof HttpClientException) {
                ResponseInfo ri = new ResponseInfo(HttpStatus.OK.value(), errorInfo);
                return JSON.toJSONString(ri);
            }
            ResponseInfo ri = new ResponseInfo(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorInfo);
            return JSON.toJSONString(ri);
        }
        log.info("return from remote server: {}", ret.getBody());
        ResponseInfo ri = new ResponseInfo(HttpStatus.OK.value(), ret.getBody());
        return JSON.toJSONString(ri);
    }

}
