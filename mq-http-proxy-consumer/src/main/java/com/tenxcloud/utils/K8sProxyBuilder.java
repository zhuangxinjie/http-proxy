/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import com.squareup.okhttp.OkHttpClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.OkHttpClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * K8sProxyBuilder
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-05 17:38
 */
@Component
public class K8sProxyBuilder {

    /**
     * restTemplate  mq-http-proxy-consumer/src/main/resources/application-test.yml
     */

   // public static String configFileName = "C:/http-proxy/http-proxy/admin.conf";
    @Value("${k8s.conf.path}")
    String configFileName;

    @Value("${app.debug}")
    boolean debug;

    @Autowired
    public K8sProxyBuilder() {

    }

    private ApiClient createApiClient() {
        ApiClient client = null;
        try {
            if (debug) {
                client = Config.fromConfig(configFileName);
            }else {
                client = Config.defaultClient();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return client;
    }


    @Autowired
    DefaultErrorHandlerWrapper wrapper;

    private RestTemplate createRestTemplate(ApiClient client) {
        OkHttpClient okHttpClient = client.getHttpClient();
//        okHttpClient.setSslSocketFactory(createSSLSocketFactory());
        OkHttpClientHttpRequestFactory factory = new OkHttpClientHttpRequestFactory(okHttpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setErrorHandler(wrapper);
        return restTemplate;
    }

    public SSLSocketFactory  createSSLSocketFactory(){
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return sSLSocketFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public K8sProxy build() {
        ApiClient client = createApiClient();
        return new K8sProxy(createRestTemplate(client), client);
    }
}
