# 钉钉-金蝶数据同步系统 (ding-kingdee)

## 项目概述

本项目是基于 RuoYi v3.9.1 框架开发的**钉钉多维表数据同步插件**，用于将钉钉多维表数据同步至金蝶云星空(K3 Cloud)系统。项目包含完整的计费模块、支付集成、租户管理等企业级功能。

### 核心技术栈

| 层级 | 技术选型 |
|------|---------|
| **后端框架** | Spring Boot 4.0.3 + JDK 17 |
| **安全框架** | Spring Security + JWT |
| **ORM框架** | MyBatis + PageHelper |
| **数据库** | MySQL |
| **缓存** | Redis |
| **前端框架** | Vue 3.5 + Element Plus 2.13 |
| **构建工具** | Vite 6.4 (前端) / Maven (后端) |

---

## 项目结构

```
ding-kingdee/
├── ruoyi-admin/           # 后台管理模块（启动入口、Controller层）
├── ruoyi-common/          # 通用工具模块（注解、工具类、核心类）
├── ruoyi-framework/       # 框架核心模块（配置、安全、数据源）
├── ruoyi-system/          # 系统业务模块（用户、角色、菜单、字典等）
├── ruoyi-quartz/          # 定时任务模块
├── ruoyi-generator/      # 代码生成器模块
├── ding-server/           # 【核心业务】钉钉-金蝶集成模块
├── ruoyi-ui/              # Vue3 前端项目
├── sql/                   # 数据库SQL脚本
└── pom.xml                # Maven父工程配置
```

### ding-server 核心模块结构

```
ding-server/
└── src/main/java/com/ruoyi/middle/
    ├── ding/                    # 钉钉集成模块
    │   ├── config/              # 钉钉配置类
    │   ├── controller/          # 钉钉相关控制器
    │   │   ├── DingProductController      # 产品管理
    │   │   ├── DingTenantController       # 租户管理
    │   │   ├── DingSubscriptionController  # 订阅管理
    │   │   ├── DingOrderController        # 订单管理
    │   │   ├── DingSyncFormController     # 同步表单配置
    │   │   ├── DingSyncLogController      # 同步日志
    │   │   ├── DingInvoiceController      # 发票管理
    │   │   └── DingPackageChangeLogController  # 产品变更记录
    │   ├── service/            # 服务层接口与实现
    │   ├── mapper/             # MyBatis Mapper接口
    │   ├── domain/             # 实体类
    │   ├── task/               # 定时任务
    │   └── util/               # 工具类
    ├── kingdee/                # 金蝶集成模块
    │   ├── controller/         # K3Cloud代理控制器
    │   ├── service/            # 金蝶API服务
    │   ├── mapper/             # 表配置Mapper
    │   ├── domain/             # 实体类
    │   ├── dto/                # 数据传输对象
    │   ├── util/               # 工具类
    │   └── constant/           # 常量定义
    ├── payment/                # 支付模块
    │   ├── controller/         # 支付控制器
    │   ├── service/            # 支付服务
    │   ├── channel/            # 支付渠道实现
    │   │   ├── PaymentChannel.java          # 支付渠道接口
    │   │   ├── AlipayChannelImpl.java       # 支付宝支付
    │   │   ├── WechatPayChannelImpl.java   # 微信支付
    │   │   └── DingPayChannelImpl.java      # 钉钉支付
    │   ├── config/             # 支付配置
    │   └── dto/                # 支付请求/响应DTO
    └── common/                 # 通用定义
```

---

## 数据库表结构

### 钉钉计费相关表 (`sql/ding_billing.sql`)

| 表名 | 说明 |
|------|------|
| `ding_product` | 产品配置表（基础版/专业版/企业版） |
| `ding_tenant` | 租户企业表（钉钉企业信息） |
| `ding_subscription` | 产品订阅表（企业订阅状态） |
| `ding_order` | 订单表 |
| `ding_payment_log` | 支付日志表 |
| `ding_product_change_log` | 产品变更记录表 |
| `ding_sync_form` | 同步表单配置表 |
| `ding_sync_log` | 数据同步日志表 |
| `ding_invoice` | 发票申请表 |

### 金蝶相关表 (`sql/kingdee_table_config.sql`)

| 表名 | 说明 |
|------|------|
| `kingdee_table_config` | 金蝶表单配置表 |

---

## 核心功能模块

### 1. 钉钉集成模块 (ding)

**功能说明**：管理钉钉企业、订阅、订单等业务

**主要API**：

```
GET  /ding/product/list        # 产品列表
GET  /ding/product/options    # 产品下拉选项
GET  /ding/tenant/list        # 租户列表
GET  /ding/tenant/corp/{corpId}  # 根据企业ID获取租户
GET  /ding/subscription/corp/{corpId}  # 根据企业ID获取订阅
GET  /ding/order/list         # 订单列表
GET  /ding/order/no/{orderNo} # 根据订单号查询
GET  /ding/syncForm/list      # 同步表单列表
GET  /ding/syncForm/count/{corpId}  # 获取企业已绑定表单数量
GET  /ding/syncLog/list       # 同步日志列表
GET  /ding/invoice/list       # 发票列表
```

**核心业务逻辑**：

- **订阅管理**：企业订阅产品，限制绑定表单数量和同步数据量
- **订单管理**：支持新购、升级、降级、续费四种订单类型
- **同步表单**：企业绑定钉钉多维表与金蝶表单的对应关系

### 2. 金蝶集成模块 (kingdee)

**功能说明**：提供钉钉AI表格数据源同步插件服务端实现

**核心API**（钉钉AI表格数据源规范）：

```
POST /api/login      # 登录验证
POST /api/sheetMeta  # 获取表结构（钉钉格式）
POST /api/records    # 获取表记录（钉钉格式）
```

**核心类**：

- `K3CloudProxyController`：钉钉数据源插件入口
- `K3CloudProxyService`：金蝶API转发服务
- `KingdeeTableConfigService`：表单配置管理

### 3. 支付模块 (payment)

**功能说明**：集成支付宝、微信支付、钉钉支付

**支付渠道**：

| 渠道 | 实现类 | 配置 |
|------|--------|------|
| 支付宝 | `AlipayChannelImpl` | `payment.alipay.*` |
| 微信支付 | `WechatPayChannelImpl` | `payment.wechat.*` |
| 钉钉支付 | `DingPayChannelImpl` | `payment.dingpay.*` |

**核心API**：

```
POST /payment/create     # 创建支付订单
POST /payment/callback/{channel}  # 支付回调（alipay/wechat_pay/ding_pay）
POST /payment/handleResult        # 手动处理支付结果（测试用）
```

**支付流程**：

1. 前端调用 `/payment/create` 创建订单，获取支付二维码
2. 用户扫码支付，第三方支付平台回调 `/payment/callback/{channel}`
3. 回调验签通过后，更新订单状态，自动创建订阅和发票申请

### 4. 定时任务 (quartz)

**任务类**：`DingOrderTask`

**功能**：处理订单超时、订阅续期等定时任务

---

## 前端路由 (ruoyi-ui)

```
钉钉管理 (/ding)
├── 产品管理    /ding/product
├── 租户管理    /ding/tenant
├── 订阅管理    /ding/subscription
├── 订单管理    /ding/order
├── 同步表单    /ding/syncForm
├── 同步日志    /ding/syncLog
├── 发票管理    /ding/invoice
└── 产品变更    /ding/package

金蝶集成 (/kingdee)
└── 表单配置    /kingdee/tableConfig
```

---

## 配置说明

### 应用配置 (ruoyi-admin/src/main/resources/application.yml)

| 配置项 | 说明 | 示例 |
|--------|------|------|
| `server.port` | 服务端口 | 1007 |
| `spring.data.redis.*` | Redis配置 | localhost:6379 |
| `dingtalk.app-id` | 钉钉应用AppId | dingff4cjsnwam0r8pxn |
| `dingtalk.app-secret` | 钉钉应用密钥 | (加密) |
| `payment.alipay.*` | 支付宝配置 | AppId、私钥、公钥 |
| `payment.wechat.*` | 微信支付配置 | AppId、MchId、APIv3Key |
| `payment.dingpay.*` | 钉钉支付配置 | AppId、AppSecret |

### 产品订阅限制

| 产品版本 | 价格 | 绑定表单数 | 单次同步上限 | 月同步上限 |
|----------|------|-----------|-------------|-----------|
| 基础版 | 0元 | 3个 | 1000条 | 10000条 |
| 专业版 | 299元/月 | 10个 | 5000条 | 50000条 |
| 企业版 | 2999元/年 | 50个 | 20000条 | 200000条 |

---

## API文档

访问 `/swagger-ui.html` 查看完整API文档（需在application.yml中启用springdoc）

分组配置：
- `default` - 测试模块
- `kingdee` - 金蝶集成模块
- `payment` - 支付模块

---

## 开发指南

### 后端开发

1. **启动项目**：`ruoyi-admin/RuoYiApplication.java`
2. **代码生成**：使用 `ruoyi-generator` 模块生成CRUD代码
3. **业务扩展**：在 `ding-server` 模块添加新的业务逻辑

### 前端开发

```bash
cd ruoyi-ui
npm install
npm run dev    # 开发环境
npm run build:prod  # 生产环境
```

### 数据库初始化

```bash
# 按顺序执行SQL脚本
sql/quartz.sql           # 定时任务表
sql/ry_20260320.sql      # RuoYi基础表
sql/ding_billing.sql     # 钉钉计费表
sql/kingdee_table_config.sql  # 金蝶配置表
sql/ding_menu.sql        # 菜单数据
```

---

## 项目特性

1. **前后端分离**：后端Spring Boot + 前端Vue 3
2. **多支付渠道**：支持支付宝、微信、钉钉支付
3. **订阅计费**：灵活的订阅管理和用量控制
4. **数据同步**：钉钉多维表与金蝶系统数据互通
5. **代码生成**：基于RuoYi的快速代码生成能力
6. **权限控制**：基于RBAC的细粒度权限管理
7. **定时任务**：基于Quartz的任务调度

---

## 注意事项

1. **敏感信息**：生产环境请使用环境变量或配置中心管理密钥
2. **支付回调**：确保回调地址公网可访问
3. **Redis**：确保Redis服务正常运行
4. **钉钉配置**：需在钉钉开放平台创建应用并配置插件信息

---

## 相关文档

- [RuoYi框架文档](http://doc.ruoyi.vip)
- [钉钉数据源同步插件开发文档](https://alidocs.dingtalk.com/i/nodes/1OQX0akWmBejogd0TAlmRyQGVGlDd3mE)
- [金蝶云星空API文档](https://open.kingdee.com/)
