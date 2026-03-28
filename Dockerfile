# 第一阶段：编译阶段 (使用 JDK)
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache libc6-compat gcompat libstdc++
WORKDIR /app

# 只拷贝 Gradle 相关文件，利用 Docker 缓存层优化下载速度
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
RUN ./gradlew dependencies --no-daemon

# 拷贝源代码并打包
COPY src src
RUN ./gradlew bootJar --no-daemon

# 第二阶段：运行阶段 (使用精简的 JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 从编译阶段拷贝生成的可执行 JAR
# 注意：根据你的项目名，JAR 的名字通常在 build/libs/ 下
COPY --from=build /app/build/libs/*.jar app.jar

# 设置时区为东八区（支付系统对时间非常敏感，这步很重要）
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 设置环境变量初始值 (也可以在 docker run 时覆盖)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 暴露 Spring Boot 默认端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
