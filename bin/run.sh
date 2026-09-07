#!/bin/bash
# =====================================================
# 宠迹 - 启动脚本（macOS / Linux）
# 用法：
#   ./run.sh              # 直接启动（需先打包）
#   ./run.sh prod         # 生产环境启动（加载 .env.prod 环境变量）
# =====================================================

cd "$(dirname "$0")/.."

ENV=${1:-dev}
JAR_PATH="ruoyi-admin/target/ruoyi-admin.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "错误: 找不到 $JAR_PATH，请先执行 ./bin/package.sh $ENV 打包"
    exit 1
fi

JAVA_OPTS="-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -Djava.awt.headless=true"

if [ "$ENV" = "prod" ]; then
    # 生产环境：加载环境变量文件
    ENV_FILE=".env.prod"
    if [ -f "$ENV_FILE" ]; then
        echo "加载生产环境变量: $ENV_FILE"
        set -a
        source "$ENV_FILE"
        set +a
    else
        echo "警告: 未找到 $ENV_FILE，请确认环境变量已通过其他方式设置"
    fi
    # 生产环境增加后台运行
    echo "启动生产环境服务..."
    nohup java $JAVA_OPTS -jar "$JAR_PATH" > /dev/null 2>&1 &
    echo "服务已启动，PID: $!"
else
    echo "启动测试环境服务..."
    java $JAVA_OPTS -jar "$JAR_PATH"
fi
