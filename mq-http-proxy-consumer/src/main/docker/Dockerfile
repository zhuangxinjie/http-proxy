FROM 192.168.1.52/system_containers/oraclejdk:8u192

MAINTAINER wuhuhu huhu.wu@tenxcloud.com

ENV PROFILE test
ENV MQ_HOST "192.168.28.17"
ENV MQ_PORT 61616
ENV MQ_USER admin
ENV MQ_PASSWORD admin
ENV QUEUE_NAME "zb.zj.ETM.ETM.QUEUE"
ENV CLUSTER_ADDRESS "https://192.168.3.105:6443"
ENV HARBOR_ADDRESS "tcp://192.168.1.52:2375"
ENV K8S_CONF_PATH "/app/admin.conf"
ENV APP_DEBUG true

ADD mq-http-proxy-consumer.jar /app/app.jar

WORKDIR "/app"

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx1g", \
            "-XX:+UnlockExperimentalVMOptions", \
            "-XX:+UseCGroupMemoryLimitForHeap", \
            "-Dspring.profiles.active=${PROFILE}",\
            "-Dmq.host=${MQ_HOST}",\
            "-Dapp.debug=${APP_DEBUG}",\
            "-Dmq.port=${MQ_PORT}",\
            "-Dmq.user=${MQ_USER}",\
            "-Dmq.password=${MQ_PASSWORD}",\
            "-Dqueue.name=${QUEUE_NAME}",\
            "-Dcluster.address=${CLUSTER_ADDRESS}",\
            "-Dharbor.address=${HARBOR_ADDRESS}",\
            "-Dk8s.conf.path=${K8S_CONF_PATH}",\
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "/app/app.jar"]
