FROM eclipse-temurin:25-jdk-alpine

# 패키지 업데이 및 보안 취약 방지용
RUN apk update && apk add --no-cache tzdata

# 작업 디렉토리
WORKDIR /app

ENV TZ=Asia/Seoul

COPY build/libs/*-SNAPSHOT.jar app.jar

# Root 대신 일반 사용자 권한으로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 포트 개방
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]