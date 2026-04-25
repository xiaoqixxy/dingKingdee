## 项目核心定位
- 项目名称：ding-kingdee（钉钉-金蝶数据同步系统）
- 核心功能：实现钉钉应用市场与金蝶云星空（K3 Cloud）之间的数据自动化同步
- 业务模式：SaaS化运营，支持多租户管理、产品订阅及自动化计费履约

## 技术栈选型
- 后端：Spring Boot 4.0, JDK 17, MyBatis, Spring Security
- 前端：Vue 3, Vite, Element Plus, Pinia
- 数据库：MySQL (主库), Redis (缓存/会话)
- 集成协议：钉钉开放平台 API, 金蝶 K3 Cloud WebAPI

## 核心模块结构
- ding-server: 核心业务模块，包含 ding (钉钉集成), kingdee (金蝶集成), payment (支付计费)
- ruoyi-admin: 系统入口与管理后台
- ruoyi-common: 通用工具类与安全组件
- ruoyi-system: 系统基础功能（用户、角色、菜单等）
