FROM eclipse-temurin:21-jre-jammy

ARG PROFILE
ARG NETTY_SERVER_PORT
ARG RECEIPT_DATASOURCE_PG_URL
ARG RECEIPT_DATASOURCE_PG_USERNAME
ARG RECEIPT_DATASOURCE_PG_PASSWORD

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV \
    SPRING_PROFILES_ACTIVE=$PROFILE \
    JAVA_OPTS="--add-exports java.base/sun.net.www.protocol.https=ALL-UNNAMED \
               -Dserver.netty.port=$NETTY_SERVER_PORT \
               -Dspring.datasource.url=$RECEIPT_DATASOURCE_PG_URL \
               -Dspring.datasource.username=$RECEIPT_DATASOURCE_PG_USERNAME \
               -Dspring.datasource.password=$RECEIPT_DATASOURCE_PG_PASSWORD"

# 작업 디렉터리 설정
WORKDIR /app

COPY build/libs/ksnet-receipt-v1.0.jar ./app.jar

# 포트 노출 (스프링 부트 기본 포트)
EXPOSE 8080
EXPOSE $NETTY_SERVER_PORT

# 진입점
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]