# 管理后台重构交付

## 已落地

按确认的分组重排 sys_menu：工作台、企业管理、订单管理、金蝶管理、集成管理、系统配置、系统内置。既有菜单ID、按钮权限及角色关联保留；只为既有角色补齐其菜单祖先可见性，未自动授予新增业务权限。系统原路径 /system、/monitor、/tool 保持可访问；旧 /ding 下业务页面设置兼容跳转。

既有企业、订阅、订单、开票、产品、同步表单、金蝶连接及模板页面保留业务功能，统一标题、筛选卡片、表格及单列表单样式。

新增退款、同步执行、接口日志、业务审计、支付日志、套餐变更记录页面；历史同步日志改为只读并从同步执行页面进入。列表支持精确企业/业务标识、状态、日期、链路编号、分页、空状态及错误反馈；详情使用单列抽屉；执行详情提供分页记录明细。日志中文状态与时间已统一。

## 新接口

| 接口 | 用途 | 权限 |
|---|---|---|
| GET /admin/records/{kind}/list | 分页查询 | ding:{kind}:list |
| GET /admin/records/{kind}/{id} | 只读详情 | ding:{kind}:list |
| GET /admin/records/execution/{id}/items | 执行记录明细 | ding:execution:list |
| POST /admin/refunds | 提交退款申请 | ding:refund:add |
| POST /admin/refunds/{id}/cancel | 撤销待处理申请 | ding:refund:cancel |

kind白名单：refund、execution、apiLog、audit、paymentLog、packageChangeLog、syncLog。数据库表名和字段来自后端白名单，筛选值全部绑定参数；每页最多100条。后台为平台管理端，授权角色可跨企业查询；企业ID筛选不是租户隔离边界。没有给普通用户自动扩大业务访问范围。

退款以订单行锁串行校验额度，成功、处理中及待处理申请均计入已占用额度；金额必须为正且最多2位小数；请求编号用于重试去重。创建及撤销在同一数据库事务内写业务审计。取消仅允许REQUESTED状态。

## 验证

- Maven `-pl dj-admin -am package`：后端编译、测试、可执行jar打包。
- Vite `build:prod`：生产构建。
- 新增5项测试，覆盖SQL白名单、参数绑定、分页边界、日期区间、退款金额及权限表达式参数；加既有20项共25项测试。
- 新接口未登录返回401。
- 浏览器已验证首页真实指标、业务审计77条记录、审计详情、企业精确筛选与重置、退款表单必填校验、执行页及多个新日志页、企业/订阅页面、系统内置下原菜单管理。
- 联调没有提交真实退款申请，没有进行资金退款或修改订单金额。

## 当前业务边界

退款接口处理申请与撤销，不执行第三方渠道资金退款；渠道退款和回执核对仍需在支付平台处理，不能把“申请成功”当作“已退款”。目前没有提供人工直接改为退款成功的按钮。

同步执行和接口日志页面读取已建的新表，完整采集链路需要同步执行器/外部调用方写入；本次不伪造执行历史。旧同步日志保留独立入口。审计日志已有迁移事件及新退款申请/撤销事件。

既有对外 /ding/* 等插件接口保持兼容，未在本次迁移中全局改变它们的认证协议。新后台日志与退款接口均使用登录及菜单权限校验，未直接暴露支付原始报文或接口请求/响应载荷。

## 运行与备份

本机前端继续使用原Vite进程（localhost:80），后端运行本次构建的独立运行包C:/Users/16424/.codex/runtimes/ding-kingdee/dj-admin.jar（localhost:1009），运行PID记录于output/admin-backend.pid。后端日志为output/admin-backend.log与admin-backend-error.log。临时1019验证进程已关闭。

菜单与角色原始备份保存在仓库外：C:\Users\16424\.codex\backups\admin-navigation-20260923-143000\navigation.json。原IDE后端启动命令在同目录backend-launch.txt。原历史日志页面也备份于该目录。菜单迁移脚本为output/migrate_admin_navigation.py，读取标准输入口令，不保存口令。

重新启动方式（先核对并停止同一应用的1009监听进程，避免重复任务）：

```powershell
& 'C:/Users/16424/.jdks/jdk-17/bin/java.exe' -Dfile.encoding=UTF-8 -jar C:/Users/16424/.codex/runtimes/ding-kingdee/dj-admin.jar --logging.level.com.dj=info
```

新增功能涉及的后端代码：ding-server/src/main/java/com/dj/middle/admin；通用记录页面：ruoyi-ui/src/views/admin/records；各业务菜单使用独立组件入口，复用通用列表布局。现有未提交修改未做提交、回滚或覆盖。
