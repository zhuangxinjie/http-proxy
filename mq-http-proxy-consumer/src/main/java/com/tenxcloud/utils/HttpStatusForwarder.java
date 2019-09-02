/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2017 TenxCloud. All Rights Reserved.
 *
 */

package com.tenxcloud.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class HttpStatusForwarder {

    private static final Map<Integer, ExceptionFactory> factories;

    static {
        factories = new HashMap<>();
    }

    public RuntimeException forward(RestClientResponseException exception) {
        int statusCode = exception.getRawStatusCode();
        ExceptionFactory factory = factories.get(statusCode);
        if (factory == null) {
            log.error(exception.getResponseBodyAsString());
            return new HttpClientException(exception.getResponseBodyAsString());
        }
        return factory.create(exception.getMessage(), exception);
    }
}

interface ExceptionFactory {
    RuntimeException create(String message, Exception exception);
}
