# 使用官方的 OpenJDK 17 基础镜像
FROM openjdk:17-jdk

# 设置工作目录
WORKDIR /app

# 复制项目的 JAR 文件到容器中（匹配实际生成的 JAR 文件名）
COPY target/account-service-*.jar app.jar

# 暴露服务端口
EXPOSE 8761

# 启动应用程序
CMD ["java", "-jar", "app.jar"]