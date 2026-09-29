# 多阶段构建：第一阶段用 Gradle 镜像编译，第二阶段用精简 JRE 运行
# 用镜像自带 gradle（8.14.5，与 wrapper 版本一致），跳过 wrapper 发行版下载，加速国内构建
FROM gradle:8.14.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
