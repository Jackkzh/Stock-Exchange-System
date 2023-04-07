#import the latest jdk version
FROM openjdk:17-jdk-alpine

ENV GRADLE_VERSION 7.2
ENV GRADLE_HOME /opt/gradle
RUN curl -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle-bin.zip && \
    unzip gradle-bin.zip -d /opt && \
    rm gradle-bin.zip && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle

RUN apt-get update && apt-get install -y postgresql-client

# 安装 netcat 工具
RUN apk add --no-cache netcat-openbsd

# 拷贝项目代码到容器中
COPY . /app

# 设置工作目录
WORKDIR /app

# 构建项目
RUN ./gradlew build

# 运行服务器
CMD ["java", "-cp", "build/libs/Stock_Exchange_System-1.0-SNAPSHOT.jar", "Server"]
