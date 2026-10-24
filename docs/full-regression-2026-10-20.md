# 升级全量回归验证报告

验证日期：2026-10-20（Asia/Shanghai）。升级前 Git 基线：`94ef244`。

## 结果

当前工作区全部 Maven 模块执行 `verify` 成功。后端16个测试类共77项测试、插件前端3个测试套件共31项测试全部通过，无失败、错误或跳过。插件 TypeScript 检查、插件生产构建、后台管理前端生产构建均通过。

将升级前 Git 基线中的转换及明细同步代码提取到独立包，与当前版本对比：4种真实元数据快照、14种表头/旧版单明细配置，共246项一致性检查全部通过。当前测试覆盖范围内未发现历史同步行为回归。

测试没有修改生产数据库、发送消息、创建真实订单、发起支付或写入钉钉表格。数据库持久化及 SQL 测试使用 H2 或模拟对象。

## 后端全量测试

命令：`mvn verify -DskipTests=false`，使用本地 JDK 17。未设置 `-Dtest` 限制。

| 测试类 | 测试数 | 失败 | 错误 | 跳过 |
| --- | ---: | ---: | ---: | ---: |
| `AdminRecordsTest` | 5 | 0 | 0 | 0 |
| `EnterpriseListJoinTest` | 2 | 0 | 0 | 0 |
| `SyncReportServiceTest` | 5 | 0 | 0 | 0 |
| `DatabaseRepairRegressionTest` | 3 | 0 | 0 | 0 |
| `SyncExecutionPersistenceTest` | 6 | 0 | 0 | 0 |
| `SyncExecutionRecorderTest` | 11 | 0 | 0 | 0 |
| `DateSyncTest` | 2 | 0 | 0 | 0 |
| `DetailSyncSupportTest` | 14 | 0 | 0 | 0 |
| `EmployeeSyncTest` | 2 | 0 | 0 | 0 |
| `FieldConfigurationSupportTest` | 8 | 0 | 0 | 0 |
| `FilterOperatorsTest` | 3 | 0 | 0 | 0 |
| `KingdeeAuthResponseTest` | 1 | 0 | 0 | 0 |
| `MaterialGroupSyncTest` | 3 | 0 | 0 | 0 |
| `MaterialSpecificationSyncTest` | 3 | 0 | 0 | 0 |
| `ReferenceQuerySupportTest` | 6 | 0 | 0 | 0 |
| `BackendNamespaceTest` | 3 | 0 | 0 | 0 |

覆盖范围包括：业务 Mapper 解析、创建时间写入、新购与续订套餐类型、企业关联查询、同步报表统计、执行批次持久化、分页重试/重复请求、错误记录与凭证脱敏、旧版分页令牌、日期、枚举、文本/数值筛选、员工证件、物料分组、表头/明细、引用对象、多级引用、新增列及额度截断。

## 插件前端全量测试

命令：`node scripts/test.js --watchAll=false --runInBand --coverage --coverageReporters=json-summary --coverageReporters=text`。

31项测试通过。覆盖旧配置读取及原样保留未知配置项、平铺/分组条件兼容、现代/旧版钉钉宿主接口、SDK StrictMode 初始化、失败重试、切换表单/明细时无效配置清理、字段别名、引用选择、模糊搜索、同一来源新增独立输出列及保存。

整体覆盖率：语句63.84%，分支60.04%，函数56.59%，行63.64%。这些比例是整个插件的覆盖率，不能解释为全部历史业务都已自动验证。

## 升级前后直接对比

使用 Git 基线的 `ContoryUtil` 和 `DetailSyncSupport` 原实现，单独编译到基线包。样本值为人工构造，不使用真实业务行。对比项目：对外字段定义、字段顺序、默认查询列、平铺/分组筛选、包含/不包含/空值、数字条件、日期快捷范围、枚举多选、排序、记录字段值、记录标识、跨页表头填充和权益截断。

比较对外字段时仅移除新增的内部引用发现属性；比较 SQL 条件时仅归一化明细主键 `> 0` 外层的冗余括号。未忽略字段、类型、排序、业务条件或返回值差异。

| 场景 | 输出字段数 | 查询组合数 |
| --- | ---: | ---: |
| `ER_ExpenseRequest:legacy-header` | 59 | 15 |
| `ER_ExpenseRequest:FEntity:FIRST_ROW` | 82 | 15 |
| `ER_ExpenseRequest:FEntity:ALL_ROWS` | 82 | 15 |
| `BD_Empinfo:legacy-header` | 38 | 13 |
| `BD_Empinfo:FPostEntity:FIRST_ROW` | 46 | 13 |
| `BD_Empinfo:FPostEntity:ALL_ROWS` | 46 | 13 |
| `BD_Empinfo:FBankInfo:FIRST_ROW` | 54 | 13 |
| `BD_Empinfo:FBankInfo:ALL_ROWS` | 54 | 13 |
| `PUR_Requisition:legacy-header` | 34 | 15 |
| `PUR_Requisition:FEntity:FIRST_ROW` | 137 | 15 |
| `PUR_Requisition:FEntity:ALL_ROWS` | 137 | 15 |
| `STK_InStock:legacy-header` | 47 | 13 |
| `STK_InStock:FInStockEntry:FIRST_ROW` | 186 | 15 |
| `STK_InStock:FInStockEntry:ALL_ROWS` | 186 | 15 |

246项对比全部一致。上述场景均未配置新的字段显示方式，使用旧版 `selectedEntryKey` 或原表头同步配置。

## 构建及部署产物

- 后端：`dj-admin/target/dingKingdee.jar`，所有模块 `verify` 通过。
- 同步插件：`ding-web/build`，生产构建通过。检查最终 JavaScript 包，线上 API 地址存在，本地 `http://localhost:1009` 地址不存在。
- 管理前端：`ruoyi-ui/dist`，`npm run build:prod` 通过。该项目未提供独立单元测试脚本，本次执行了生产构建。
- 插件：`npx tsc --noEmit` 通过。

插件构建保留3条原有 ESLint 告警（两个 effect 依赖和表单选择输入框 ARIA 角色），管理前端保留大包体积提示。未将这些已有告警视为业务回归通过的证据；严格将警告当错误的 CI 仍需处理这些告警。

## 验证边界与上线检查

本次没有在钉钉真实宿主中执行落表，也没有触发生产支付/退款回调。没有覆盖所有租户、自定义表单和所有历史元数据版本。offset 分页期间源数据变动、引用字段筛选/排序下的大数据量扫描，仍需在接近生产规模的预发布环境确认。

部署顺序应为后端先更新，再更新同步插件和管理前端。保留当前线上版本与配置备份；上线前选取一个既有表头同步、一个既有明细同步，在钉钉中确认字段、记录数、跨页、重试、套餐状态和管理报表，再逐步切换。

## 证据文件

- `output/full-regression-backend.log`
- `output/full-regression-plugin-tests.log`
- `output/full-regression-plugin-coverage.log`
- `output/full-regression-plugin-build.log`
- `output/full-regression-admin-build.log`
- `output/full-regression-baseline.json`
- `output/full-regression-summary.json`
- `output/RegressionBaseline.java`（差分检查程序）
