# 后端 DJ 命名迁移

## 新结构

- Maven 父工程：`com.dj:dj:3.9.1`。
- 模块：`dj-admin`、`dj-common`、`dj-framework`、`dj-system`、`dj-quartz`、`dj-generator`；业务模块仍为 `ding-server`，坐标改为 `com.dj:ding-server`。
- Java 根包统一为 `com.dj`，包含业务模块和测试代码。
- 主类：`com.dj.DjApplication`；Servlet 初始化类：`com.dj.DjServletInitializer`；项目配置类：`com.dj.common.config.DjConfig`。
- 项目配置前缀由 `ruoyi.*` 改为 `dj.*`，外部配置、环境变量及启动参数也应使用新前缀。
- Spring 扫描、MyBatis Mapper XML、反射类名、JSON 白名单、任务白名单、代码生成模板及启动脚本已同步。
- 前端目录 `ruoyi-ui` 不属于本次后端重命名；原作者署名、许可证、框架来源说明及已有连接凭据保留。

## 构建与发布

使用 JDK 17，在项目根目录执行 `mvn clean verify`。新产物为 `dj-admin/target/dingKingdee.jar`；使用 `java -jar dj-admin/target/dingKingdee.jar` 启动。现有 `bin/run.bat`、`ry.bat`、`ry.sh` 已指向新产物。

IDEA 中重新加载根 `pom.xml`，运行 `DjApplication`。本机运行配置已同步。原 `ruoyi-admin` 顶层目录被进程占用，源码和 POM 已迁出，目前仅保留旧 `target`；它不在 Maven reactor 中，停止旧进程后可清理。

本次未停止、重启或发布运行中的服务。

## 运行数据兼容

- 登录缓存改用 `dj:login_tokens:`，字典缓存改用 `dj:sys_dict:`，避免反序列化旧 Java 包名。发布后需要重新登录；不清空 Redis，不影响其他业务缓存。
- 上传根目录默认改为 `D:/dj/uploadPath`，可通过 `DJ_UPLOAD_PATH` 指向现有文件目录；生产发布时明确设置原有文件路径或迁移文件。本机检查原默认上传目录不存在，未移动工作区外文件。
- 日志默认路径为 `/home/dj/logs`。支付证书配置中的后端模块路径已同步到 `dj-admin`，证书内容未改动。
- 数据库只读检查发现 0 条旧定时任务目标、1 条旧代码生成包名、0 条旧生成目录。已在事务中将该生成包名改为 `com.dj` 前缀并验证，原值备份在 `output/backend-dj-generator-before.json`。
- 其他环境可使用 `sql/dj-namespace-migration.sql` 迁移已保存的任务类名和生成包名；历史审计记录无需改写。若自行启用了 Quartz JDBC 持久化，还需在发布时重建任务，不能直接替换序列化二进制内容。
- 上一轮同步日志数据库变更仍有效，本次保留了所有同步日志实现。

## 验证

完整后端 Maven 构建通过。业务测试及新增命名迁移测试通过，新增测试覆盖全部 Mapper XML 的命名空间/类型别名解析、`dj` 配置绑定、登录缓存新类型序列化。检查最终 JAR 的启动类、应用类和内部依赖均为新命名。本次未连接真实钉钉/金蝶执行同步，也未启动第二个应用实例访问生产数据库。
