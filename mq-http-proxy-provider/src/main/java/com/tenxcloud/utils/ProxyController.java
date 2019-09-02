/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


/**
 * ProxyController
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-05 10:52
 */
@RestController
@Slf4j
public class ProxyController {

  //  public static final String HEADER_QUEUE = "mq-queue-name";
    @Value("${queue.name}")
    String headQueue;

    @Value("${app.debug}")
    boolean debug;

    @Autowired
    ProxyService proxyService;

    @RequestMapping()
    public ResponseEntity<String> resolve(HttpServletRequest request) {
        try {

            String queueName;
            if (debug) {
                queueName = "zb.zj.ETM.ETM.QUEUE";
            }else{
                queueName = request.getHeader(headQueue);
            }
            if (queueName == null) {
                throw new IllegalArgumentException("mq queue name header not exist");
            }
            // todo: to resolve different Content-Type
            Enumeration<String> paramNames = request.getParameterNames();
            String queryStr = request.getQueryString();
            String url = request.getRequestURI();
            if (!StringUtils.isBlank(queryStr)) {
                url += "?" + queryStr;
            }
            Map<String, String> params = new HashMap<>();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                params.put(name, request.getParameter(name));
            }
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> names = request.getHeaderNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                headers.put(name, request.getHeader(name));
                System.out.println(String.format("header: %s, value=%s", name, request.getHeader(name)));
            }
            ProxyBody pb = new ProxyBody(url, request.getMethod(), headers, params,
                    IOUtils.toString(request.getInputStream(), request.getCharacterEncoding()));
            String responseText = proxyService.resolve(queueName, pb);
            ResponseInfo info = JSON.toJavaObject(JSONObject.parseObject(responseText), ResponseInfo.class);
            return new ResponseEntity<>(info.getData(), HttpStatus.valueOf(info.getCode()));
        } catch (JMSException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            return new ResponseEntity<String>("mq error.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            return new ResponseEntity<String>("parse request body error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   /* public Map<String, Object>  pull(HttpServletRequest request){

        return null;
    }*/
}
