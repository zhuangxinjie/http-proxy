/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * ApplicationStarter
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-05 10:51
 */
@SpringBootApplication
@EnableJms
public class ApplicationConsumerStarter {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationConsumerStarter.class, args);
//        System.setProperty("javax.net.ssl.trustStore", "c:/Users/huhu/workspace/tenxcloudwork/projects/wuyangwei/http-proxy/jssecacerts");
    }

/*    @Bean
    public RestTemplate restTemplate() throws Exception {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(HttpClients.custom().setSSLSocketFactory(
                new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null,
                        (x509Certificates, s) -> true).build(), new NoopHostnameVerifier())).build());
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }*/
}
