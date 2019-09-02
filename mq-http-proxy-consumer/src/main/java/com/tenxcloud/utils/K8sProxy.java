/*-
 * #%L
 * dsb-api
 * %%
 * Copyright (C) 2017 TenxCloud Inc.
 * %%
 * Licensed Materials - Property of tenxcloud.com
 * Copyright (C) 2017 TenxCloud Inc. All Rights Reserved.
 * #L%
 */
/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2017 TenxCloud. All Rights Reserved.
 *
 * 2017-11-24 @author lizhen
 */

package com.tenxcloud.utils;

import io.kubernetes.client.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * K8sProxy
 *
 * @author weiwei
 * @date 2018-07-27 16:16:00
 */
@Slf4j
public final class K8sProxy {
    /**
     * DEBUG
     */
    public static boolean DEBUG = false;
    /**
     * headers 请求headers信息
     */
    private final Map<String, String> headers;
    /**
     * messageConverters
     */
    private final List<HttpMessageConverter<?>> messageConverters;
    /**
     * restTemplate
     */
    private final RestTemplate restTemplate;
    /**
     * client
     */
    private final ApiClient client;

    public K8sProxy(RestTemplate restTemplate, ApiClient client) {
        this.headers = new HashMap<>();
        this.messageConverters = new ArrayList<>();
        this.restTemplate = restTemplate;
        this.client = client;
    }

    /**
     * @param key   键
     * @param value 值
     * @return K8sProxy
     * @withHeader 添加请求头
     * @Date 2018年8月2日 上午9:58:31
     * @author weiwei
     */
    public K8sProxy withHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * @param converter 转换器
     * @return K8sProxy
     * @withMessageConverter 添加消息转换器
     * @Date 2018年8月2日 上午9:58:51
     * @author weiwei
     */
    public K8sProxy withMessageConverter(HttpMessageConverter<?> converter) {
        messageConverters.add(converter);
        return this;
    }

    /**
     * @param pathAndQuery     请求地址
     * @param method           请求方法
     * @param request          请求体
     * @param responseBodyType 返回响应类型
     * @param uriVariables     请求链接参数
     * @param <RequestBody>    <RequestBody>
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> call(
            K8sServiceURL pathAndQuery,
            HttpMethod method,
            HttpEntity<RequestBody> request,
            Class<ResponseBody> responseBodyType,
            Object... uriVariables) {
        return getRestTemplate().exchange(getUrl(pathAndQuery), method, request, responseBodyType, uriVariables);
    }

    /**
     * 不让restTemplate内部encode url,直接构造 url
     *
     * @param pathAndQuery     请求地址
     * @param method           请求方法
     * @param request          请求体
     * @param responseBodyType 返回响应类型
     * @param <RequestBody>    <RequestBody>
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> call(
            K8sServiceURL pathAndQuery,
            HttpMethod method,
            HttpEntity<RequestBody> request,
            Class<ResponseBody> responseBodyType) {
        URI uri = URI.create(getUrl(pathAndQuery));
        return getRestTemplate().exchange(uri, method, request, responseBodyType);
    }

    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> call(
            K8sServiceURL pathAndQuery,
            HttpMethod method,
            HttpEntity<RequestBody> request,
            Class<ResponseBody> responseBodyType,
            Map<String, ?> uriVariables) {
        return getRestTemplate().exchange(getUrl(pathAndQuery), method, request, responseBodyType, uriVariables);
    }

    /**
     * @param pathAndQuery      请求地址
     * @param method            请求方法
     * @param request           请求体
     * @param responseBodyType  返回响应类型
     * @param messageConverters 消息转换器
     * @param <RequestBody>     <RequestBody>
     * @param <ResponseBody>    <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @throws Exception 异常
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> call(
            K8sServiceURL pathAndQuery,
            HttpMethod method,
            HttpEntity<RequestBody> request,
            Class<ResponseBody> responseBodyType,
            List<HttpMessageConverter<?>> messageConverters) throws Exception {
        try {
            RestTemplate rt = getRestTemplate();
            List<HttpMessageConverter<?>> converters = rt.getMessageConverters();
            converters.addAll(messageConverters);
            rt.setMessageConverters(converters);
            return rt.exchange(getUrl(pathAndQuery), method, request, responseBodyType);
        } catch (Throwable e) {
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException clientError = (HttpClientErrorException) e;
                log.error("request error ->" + clientError.getResponseBodyAsString());
            }
            throw e;
        }
    }

    /**
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <ResponseBody> ResponseEntity<ResponseBody> get(K8sServiceURL pathAndQuery, Class<ResponseBody> responseBodyType, Object... uriVariables) {
        HttpEntity<Void> request = new HttpEntity<>(null, getHeaders());
        return call(pathAndQuery, HttpMethod.GET, request, responseBodyType, uriVariables);
    }

    public <ResponseBody> ResponseEntity<ResponseBody> get(K8sServiceURL pathAndQuery, Class<ResponseBody> responseBodyType) {
        HttpEntity<Void> request = new HttpEntity<>(null, getHeaders());
        return call(pathAndQuery, HttpMethod.GET, request, responseBodyType);
    }

    /**
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <ResponseBody> ResponseEntity<ResponseBody> get(K8sServiceURL pathAndQuery, Class<ResponseBody> responseBodyType, Map<String, ?> uriVariables) {
        HttpEntity<Void> request = new HttpEntity<>(null, getHeaders());
        return call(pathAndQuery, HttpMethod.GET, request, responseBodyType, uriVariables);
    }

    /**
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <ResponseBody> ResponseEntity<ResponseBody> delete(K8sServiceURL pathAndQuery, Class<ResponseBody> responseBodyType, Object... uriVariables) {
        HttpEntity<Void> request = new HttpEntity<>(null, getHeaders());
        return call(pathAndQuery, HttpMethod.DELETE, request, responseBodyType, uriVariables);
    }

    /**
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <ResponseBody> ResponseEntity<ResponseBody> delete(K8sServiceURL pathAndQuery, Class<ResponseBody> responseBodyType, Map<String, ?> uriVariables) {
        HttpEntity<Void> request = new HttpEntity<>(null, getHeaders());
        return call(pathAndQuery, HttpMethod.DELETE, request, responseBodyType, uriVariables);
    }

    /**
     * @param body             请求体
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <RequestBody>    <RequestBody>
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> post(
            K8sServiceURL pathAndQuery,
            RequestBody body,
            Class<ResponseBody> responseBodyType,
            Object... uriVariables) {
        HttpEntity<RequestBody> request = new HttpEntity<>(body, getHeaders());
        return call(pathAndQuery, HttpMethod.POST, request, responseBodyType, uriVariables);
    }

    /**
     * @param body             请求体
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <RequestBody>    <RequestBody>
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> post(
            K8sServiceURL pathAndQuery,
            RequestBody body,
            Class<ResponseBody> responseBodyType,
            Map<String, ?> uriVariables) {
        HttpEntity<RequestBody> request = new HttpEntity<>(body, getHeaders());
        return call(pathAndQuery, HttpMethod.POST, request, responseBodyType, uriVariables);
    }

    /**
     * @param body             请求体
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <RequestBody>    <RequestBody>
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> put(
            K8sServiceURL pathAndQuery,
            RequestBody body,
            Class<ResponseBody> responseBodyType,
            Object... uriVariables) {
        HttpEntity<RequestBody> request = new HttpEntity<>(body, getHeaders());
        return call(pathAndQuery, HttpMethod.PUT, request, responseBodyType, uriVariables);
    }

    /**
     * @param body             请求体
     * @param pathAndQuery     请求地址
     * @param responseBodyType 响应体类型
     * @param uriVariables     请求链接查询参数
     * @param <RequestBody>    <RequestBody>
     * @param <ResponseBody>   <ResponseBody>
     * @return ResponseEntity<ResponseBody>
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> put(
            K8sServiceURL pathAndQuery,
            RequestBody body,
            Class<ResponseBody> responseBodyType,
            Map<String, ?> uriVariables) {
        HttpEntity<RequestBody> request = new HttpEntity<>(body, getHeaders());
        return call(pathAndQuery, HttpMethod.PUT, request, responseBodyType, uriVariables);
    }

    /**
     * @param pathAndQuery 请求链接
     * @return String
     * @date 2018-07-27 16:16:00
     * @author weiwei
     */
    private String getUrl(K8sServiceURL pathAndQuery) {
        if (DEBUG) {
            Pattern pattern = Pattern.compile("^[a-z]([-a-z0-9]*[a-z0-9])?:([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])(/.+)$");
            Matcher matcher = pattern.matcher(String.format("%s/%s", pathAndQuery.getService(), pathAndQuery.getPathAndQuery()));
            if (matcher.find()) {
                String requestUrl = "http://127.0.0.1:8080" + matcher.group(3);
                log.info(">>> local debug request url - {}", requestUrl);
                return requestUrl;
            }
        }
        String requestUrl = String.format("%s",
                pathAndQuery.getPathAndQuery());
        log.debug(">>> request url - " + requestUrl);
        return requestUrl;
    }

    /**
     * @return HttpHeaders
     * @getHeaders 获取请求头
     * @Date 2018年8月2日 下午6:39:01
     * @author weiwei
     */
    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (!DEBUG) {
            client.updateParamsForAuth(new String[]{"BearerToken"}, new ArrayList<>(), headers);
        }
        httpHeaders.setAll(headers);
        headers.clear();
        return httpHeaders;
    }

    /**
     * @return RestTemplate
     * @getRestTemplate 获取restTemplate
     * @Date 2018年8月2日 下午6:39:11
     * @author weiwei
     */
    private RestTemplate getRestTemplate() {
        if (!messageConverters.isEmpty()) {
            restTemplate.setMessageConverters(messageConverters);
            messageConverters.clear();
        }
        return restTemplate;
    }
}
