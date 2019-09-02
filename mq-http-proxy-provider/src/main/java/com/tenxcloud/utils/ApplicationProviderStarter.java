/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

/**
 * ApplicationStarter
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-05 10:51
 */
@SpringBootApplication
@EnableJms
public class ApplicationProviderStarter {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationProviderStarter.class, args);
    }


    @Bean
    public RestTemplate restTemplate() throws Exception {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(HttpClients.custom().setSSLSocketFactory(
                new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null,
                        (x509Certificates, s) -> true).build(), new NoopHostnameVerifier())).build());
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }
}
