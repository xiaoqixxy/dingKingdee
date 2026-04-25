#!/bin/bash
# 重新打包包含金蝶SDK的jar包

cd "$(dirname "$0")"

echo "=== 开始打包 ding-server ==="
cd ding-server
mvn clean package -Dmaven.test.skip=true

echo "=== 复制金蝶SDK到target目录 ==="
cp lib/k3cloud-webapi-sdk8.0.6.jar target/classes/lib/

echo "=== 开始打包 ruoyi-admin ==="
cd ../ruoyi-admin
mvn clean package -Dmaven.test.skip=true

echo "=== 打包完成 ==="
echo "生成的jar文件位于: ruoyi-admin/target/ruoyi-admin.jar"