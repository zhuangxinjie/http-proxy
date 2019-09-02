package com.tenxcloud.utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.BlobMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.catalina.security.SecurityUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;

public class DockerUtil {
	private static final Logger log = LoggerFactory.getLogger(DockerUtil.class);
	@Autowired
	JmsTemplate jmsTemplate;

	public static  void  pullImages(String host,String user ,String passwd,String image){

		StringBuilder cmd = new StringBuilder();

		cmd.append("docker pull "+image+" && docker save "+image+" > "+image+".tar");
		SSH ssh = new SSH();
		try {
			String res = ssh.sshcmd_str(host,user,passwd,cmd.toString());
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//发送文件

	public String send(String queueName,String image) throws JMSException{
		Destination des = new ActiveMQQueue(queueName);
			// 选择文件
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(image+".tar");
			if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
				;
			}
			File file = fileChooser.getSelectedFile();
			// 获取 ConnectionFactory
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"tcp://localhost:61616?jms.blobTransferPolicy.defaultUploadUrl=http://localhost:8161/fileserver/");
			// 创建 Connection
		  Connection connection = null;
			connection = connectionFactory.createConnection();
			connection.start();

			// 创建 Session
		  ActiveMQSession session = null;
			session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// 创建 Destination
			Destination destination = session.createQueue("File.Transport");
			// 创建 Producer
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);// 设置为非持久性
			// 设置持久性的话，文件也可以先缓存下来，接收端离线再连接也可以收到文件
			// 构造 BlobMessage，用来传输文件
			//如果设置 producer.setDeliveryMode(DeliveryMode.PERSISTENT); 消息持久性的话，
			//发送方传文件的时候，接收方可以不在线，文件会暂存在 ActiveMQ 服务器上，等到接收程序上线后仍然可以收到发过来的文件。
			BlobMessage blobMessage = session.createBlobMessage(file);
			blobMessage.setStringProperty("FILE.NAME", file.getName());
			blobMessage.setLongProperty("FILE.SIZE", file.length());
			System.out.println("开始发送文件：" + file.getName() + "，文件大小：" + file.length() + " 字节");
			// 7. 发送文件
			producer.send(blobMessage);
			System.out.println("完成文件发送：" + file.getName());
			producer.close();
			session.close();
			connection.close(); // 不关闭 Connection, 程序则不退出
		return "";
	}

	public static void main(String[] args) {

		DockerUtil.pullImages("192.168.1.221", "root", "root", "busybox");

	}
}
