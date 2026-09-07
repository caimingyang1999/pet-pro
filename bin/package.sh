#!/bin/bash
# =====================================================
# 宠迹 - Maven 打包脚本（macOS / Linux）
# 用法：
#   ./package.sh dev    # 测试环境打包（默认）
#   ./package.sh prod   # 生产环境打包
# =====================================================

set -e

# 切换到项目根目录
cd "$(dirname "$0")/.."

PROFILE=${1:-dev}

# 自动检测 JDK 21（项目基于 Java 8 编译，Lombok 1.18.30 不兼容 Homebrew OpenJDK 26）
if [ -z "$JAVA_HOME" ] || ! "$JAVA_HOME/bin/java" -version 2>&1 | grep -q "21\."; then
    MAC_JDK21="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
    if [ -d "$MAC_JDK21" ]; then
        export JAVA_HOME="$MAC_JDK21"
        echo "使用 JDK: $JAVA_HOME"
    fi
fi

echo ""
echo "============================================="
echo "  宠迹 - 开始打包 [$PROFILE 环境]"
echo "============================================="
echo ""

mvn clean package -P"$PROFILE" -Dmaven.test.skip=true

echo ""
echo "============================================="
echo "  打包完成！输出: ruoyi-admin/target/ruoyi-admin.jar"
echo "  环境: $PROFILE"
echo "============================================="
