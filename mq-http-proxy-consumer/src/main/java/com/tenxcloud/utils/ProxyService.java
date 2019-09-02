/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.tenxcloud.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.*;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Handler;

/**
 * ProxyService
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-05 18:24
 */
@Service
@Slf4j
public class ProxyService {

    @Autowired
    JmsTemplate jmsTemplate;

    public String resolve(String queueName, ProxyBody pb) throws JMSException {
        Destination des = new ActiveMQQueue(queueName);
        Message message = jmsTemplate.sendAndReceive(des, session -> {
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(JSONObject.toJSONString(pb));
            log.info("send to {} with message: {}", queueName, textMessage);
            return textMessage;
        });
        ActiveMQTextMessage realMsg = (ActiveMQTextMessage)message;
        log.info("消息接收返回： {}", realMsg);
        return realMsg.getText();
    }
}
