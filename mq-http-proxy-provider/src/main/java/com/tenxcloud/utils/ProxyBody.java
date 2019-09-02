/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * ProxyBody
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-04 15:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProxyBody implements Serializable{
    Map<String, String> headers;
    Map<String, String> params;
    String url;
    String method;
    Object content;


    public ProxyBody(String url, String method, Map headers, Map params, Object content) {
        this.headers = headers;
        this.method = method;
        this.url = url;
        this.content = content;
        this.params = params;
    }
}
