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

## 前端样式规范
- 前端主色：#1677ff（Ant Design 5 蓝），深色蓝 #0958d9，浅蓝背景 #e6f4ff
- 危险色：#ff4d4f；边框色：#d1d5db；主文字：#1d2939；次级文字：#374151
- 卡片圆角：12px，弹窗圆角：12px，阴影带蓝色调
- App.css 使用 CSS 变量设计令牌统一管理颜色

## 套餐业务规则
- 每月同步上限字段已从前端 UI 全部移除（产品信息卡片 + 套餐选择弹窗）
- 专业版和企业版的可绑定表单数量显示为「不限制」
- 判断逻辑：产品名含「专业」或「企业」，或 syncFormLimit ≤ 0 / ≥ 9999，则渲染「不限制」
- 工具函数 formatFormLimit 定义在组件级别，供 renderProductInfo 和 renderUpgradeDialog 共用
- 套餐选择界面为全页替换（非弹窗），顶部含返回按钮和当前权益卡片
- 三列静态套餐卡片（已优化间距）：基础版（灰色）/ 专业版（蓝色）/ 企业版（紫色）
- 各版本功能差异：同步上限 1000/20000/50000 行；自定义表单和报表基础版不支持；AI 表格基础版限 3 张，专业/企业不限
- 支付按钮显示逻辑：当前版本或已开启更高级版本则不显示按钮
- 页面容器固定尺寸：800×620px（统一适用于第一步、第二步、第三步、选择套餐四个页面）
- 主卡片 padding 16px 20px，overflow: auto；各区块间距全面压缩适配 620px 高度
- 套餐卡片间距：gap 16px，padding 18px 16px，特性列表行间距 10px，分隔线 margin 12px/10px
- planDesc 加 minHeight: 32px + display:flex + alignItems:center，保证三列分隔线对齐
- 第三步（筛选排序）Section 改为卡片样式，条件行加浅灰背景，标题配 emoji 图标，空状态用虚线边框提示框

## 对公付款群聊配置
- 点击「对公付款」弹出群聊卡片弹窗（非直接跳转）
- 钉钉群名：金蝶云星空连接器服务群
- chatId: chat8ccf90a85881999e8d7d01b7dba6848e
- 所属企业ID: dingbf492c95f9a6eab9acaaa37764f94726
- 加群链接（有效）：https://qr.dingtalk.com/action/joingroup?code=v1,k1,O7abdQ5/e8+ADgHpaIqTm6ZkxcVyAlWUuOjiQRrCBnGdR7ksupjDEA==&_dt_no_comment=1&origin=11
- 注意：dingtalk://...chatId=xxx 协议无效，会报「二维码校验失败」；必须使用 qr.dingtalk.com 邀请链接中的 code 参数
- state: showGroupChatDialog；渲染函数: renderGroupChatDialog；弹窗宽度 480px
