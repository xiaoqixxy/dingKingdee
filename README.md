# 钉钉-金蝶数据同步系统 (ding-kingdee)

<p align="center">
  <img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Ding-Kingdee Sync System</h1>
<h4 align="center">基于 RuoYi-Vue 框架的钉钉多维表与金蝶云星空数据集成平台</h4>

## 项目简介

本项目是一个企业级数据集成解决方案，旨在打通**钉钉（DingTalk）**与**金蝶云星空（Kingdee K3 Cloud）**之间的数据壁垒。通过本系统，企业可以将钉钉多维表中的业务数据自动、安全地同步至金蝶 ERP 系统，实现业务流程的自动化闭环。

系统基于 **RuoYi-Vue** 快速开发平台构建，集成了完善的**租户管理**、**订阅计费**、**多支付渠道**以及**可视化配置**功能，支持 SaaS 化运营模式。

### 核心亮点

*   **无缝集成**：深度对接钉钉开放平台与金蝶云星空 API，支持双向数据交互。
*   **灵活配置**：提供可视化的表单映射配置，无需编写代码即可定义同步规则。
*   **SaaS 化运营**：内置多租户体系，支持基础版/专业版/企业版分级订阅与用量控制。
*   **全渠道支付**：集成支付宝、微信支付及钉钉支付，支持订单自动流转与发票申请。
*   **高稳定性**：基于 Spring Boot 4.0 + Vue 3 技术栈，具备完善的日志监控与异常处理机制。

---

## 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| **Spring Boot** | 4.0.3 | 核心框架，提供 RESTful API 支持 |
| **JDK** | 17 | 运行环境 |
| **MyBatis** | 4.0.1 | 持久层框架，配合 PageHelper 实现分页 |
| **Spring Security** | - | 安全框架，基于 JWT 实现多终端认证 |
| **Redis** | - | 缓存数据库，用于权限缓存与会话管理 |
| **Druid** | 1.2.28 | 阿里数据库连接池，提供监控功能 |
| **Fastjson2** | 2.0.61 | 高性能 JSON 解析器 |
| **Quartz** | - | 分布式任务调度，处理定时同步任务 |

### 前端技术栈

| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| **Vue** | 3.5 | 渐进式 JavaScript 框架 |
| **Vite** | 6.4 | 下一代前端构建工具 |
| **Element Plus** | 2.13 | 基于 Vue 3 的组件库 |
| **Pinia** | - | 状态管理库 |
| **Vue Router** | 4 | 路由管理器 |

---

## 项目结构

```text
ding-kingdee/
├── ruoyi-admin           # 【启动模块】后台管理入口，包含 Controller 层与配置文件
├── ruoyi-common          # 【通用模块】核心工具类、注解、常量、异常处理
├── ruoyi-framework       # 【框架模块】安全配置、数据源配置、AOP 切面
├── ruoyi-system          # 【系统模块】用户、角色、菜单、部门等基础管理
├── ruoyi-quartz          # 【任务模块】定时任务调度与管理
├── ruoyi-generator       # 【生成模块】代码生成器，支持 CRUD 一键生成
├── ding-server           # 【核心业务】钉钉与金蝶集成逻辑
│   ├── middle/ding       # 钉钉集成：租户、订阅、订单、同步配置
│   ├── middle/kingdee    # 金蝶集成：K3 Cloud API 代理、表单元数据
│   └── middle/payment    # 支付中心：支付宝、微信、钉钉支付实现
├── ruoyi-ui              # 【前端项目】Vue 3 管理后台源码
├── sql                   # 【数据库脚本】初始化 SQL 文件
└── pom.xml               # Maven 父工程依赖管理
```

---

## 核心功能模块

### 1. 钉钉集成模块 (Ding Integration)
*   **租户管理**：自动识别并注册钉钉企业租户，维护企业信息。
*   **产品订阅**：提供三种标准套餐（基础版/专业版/企业版），限制绑定表单数量与同步条数。
*   **订单中心**：支持新购、升级、降级、续费等多种订单类型，实时跟踪订单状态。
*   **同步配置**：可视化配置钉钉多维表与金蝶表单的字段映射关系。
*   **同步日志**：详细记录每一次数据同步的请求与响应，支持异常排查。

### 2. 金蝶集成模块 (Kingdee Integration)
*   **数据源代理**：遵循钉钉 AI 表格数据源规范，提供 `/api/login`, `/api/sheetMeta`, `/api/records` 接口。
*   **表单配置**：管理金蝶云星空的业务对象（FormId）与字段定义。
*   **API 转发**：封装金蝶 WebAPI，实现数据的增删改查操作。

### 3. 支付与计费模块 (Payment & Billing)
*   **多渠道支付**：
    *   **支付宝**：支持 PC 扫码与 H5 支付。
    *   **微信支付**：支持 Native 扫码支付。
    *   **钉钉支付**：集成钉钉应用内支付能力。
*   **自动履约**：支付成功后自动开通/续期订阅，并生成发票申请记录。
*   **用量监控**：实时监控各租户的同步次数与数据量，超限自动拦截。

---

## 快速开始

### 1. 环境准备
*   **JDK**: 17+
*   **Maven**: 3.6+
*   **MySQL**: 5.7+ / 8.0+
*   **Redis**: 6.0+
*   **Node.js**: 18+ (前端开发用)

### 2. 数据库初始化
请按顺序执行以下 SQL 脚本：
1.  `sql/quartz.sql` (定时任务表)
2.  `sql/ry_20260320.sql` (若依基础表)
3.  `sql/ding_billing.sql` (钉钉计费业务表)
4.  `sql/kingdee_table_config.sql` (金蝶配置表)
5.  `sql/ding_menu.sql` (菜单权限数据)

### 3. 后端启动
1.  修改 `ruoyi-admin/src/main/resources/application.yml`，配置数据库、Redis 及钉钉/金蝶密钥。
2.  使用 Maven 编译并启动 `ruoyi-admin` 模块：
    ```bash
    mvn clean install
    cd ruoyi-admin
    mvn spring-boot:run
    ```
3.  启动成功后，访问 `http://localhost:1007/swagger-ui.html` 查看 API 文档。

### 4. 前端启动
```bash
cd ruoyi-ui
npm install
npm run dev
```
访问 `http://localhost:80` 进入管理后台（默认账号：admin/admin123）。

---

## 配置说明

在 `application.yml` 中需重点配置以下信息：

| 配置项 | 说明 | 示例 |
| :--- | :--- | :--- |
| `dingtalk.app-id` | 钉钉应用 AppKey | `dingxxxxxx` |
| `dingtalk.app-secret` | 钉钉应用 AppSecret | `xxxxxxxx` |
| `kingdee.api-url` | 金蝶云星空 API 地址 | `http://x.x.x.x/K3Cloud/` |
| `payment.alipay.*` | 支付宝应用配置 | AppId, PrivateKey, PublicKey |
| `payment.wechat.*` | 微信支付配置 | AppId, MchId, APIv3Key |

---

## 常见问题

1.  **同步失败怎么办？**
    *   请查看“同步日志”模块，根据错误码检查金蝶 API 连通性或字段映射是否正确。
2.  **如何增加新的同步表单？**
    *   在“同步表单配置”中新建记录，选择对应的钉钉表与金蝶表单，并配置字段对应关系。
3.  **支付回调不生效？**
    *   确保服务器具有公网 IP，并在支付宝/微信后台配置了正确的回调地址（Notify URL）。

---

## 许可证

本项目基于 MIT 协议开源，详情请参阅 [LICENSE](LICENSE) 文件。

## 联系方式

如有技术问题或合作意向，请联系项目负责人。
