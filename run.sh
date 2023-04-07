#!/bin/bash
#docker build -t stock-exchange-system .
#docker-compose up -d

#!/bin/bash

# 定义镜像名称
IMAGE_NAME="myapp"

# 定义容器名称
CONTAINER_NAME="mycontainer"

# 定义端口号
PORT_NUMBER=12345

# 定义要构建的 Dockerfile 的路径
DOCKERFILE_PATH="./path/to/Dockerfile"

# 停止并删除现有容器
docker stop $CONTAINER_NAME > /dev/null 2>&1
docker rm $CONTAINER_NAME > /dev/null 2>&1

# 构建 Docker 镜像
docker build -t $IMAGE_NAME $DOCKERFILE_PATH

# 运行 Docker 容器
docker run -d --name $CONTAINER_NAME -p $PORT_NUMBER:$PORT_NUMBER $IMAGE_NAME

# 输出容器 ID
echo "Container ID: $(docker ps -aqf \"name=$CONTAINER_NAME\")"

# 输出日志
echo "Logs:"
docker logs $CONTAINER_NAME
