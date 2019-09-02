FROM 192.168.1.52/system_containers/oraclejdk:8u192

MAINTAINER wuhuhu huhu.wu@tenxcloud.com

ENV PROFILE test
ENV MQ_HOST 192.168.28.17
ENV MQ_PORT 61616
ENV MQ_USER admin
ENV MQ_PASSWORD admin
ENV QUEUE_NAME "zb.zj.ETM.ETM.QUEUE"

ADD mq-http-proxy-provider.jar /app/app.jar

WORKDIR "/app"

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx1g", \
            "-XX:+UnlockExperimentalVMOptions", \
            "-XX:+UseCGroupMemoryLimitForHeap", \
            "-Dspring.profiles.active=${PROFILE}",\
            "-Dmq.host=${MQ_HOST}",\
            "-Dmq.port=${MQ_PORT}",\
            "-Dmq.user=${MQ_USER}",\
            "-Dmq.password=${MQ_PASSWORD}",\
            "-Dmq.queue=${QUEUE_NAME}",\
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "/app/app.jar"]
