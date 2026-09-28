# BD_MATERIAL 同步查询修复

链路：`42c958d2-ff26-4aa7-ad43-1019b919b487`，执行编号81。

## 诊断

用户提供的 app.log 显示 QueryBusinessInfo 成功，ExecuteBillQuery 在约30ms后抛出 IllegalArgumentException；日志写入成功，但此前分类摘要覆盖了上游具体错误。

使用数据库中该企业已有的连接配置，对同一金蝶账套进行只读查询，每次最多读取1行，不输出业务行或连接凭据。最小复现结果：

| 查询 | 结果 |
| --- | --- |
| FMATERIALID,FMaterialGroup.FName | 金蝶报“对象名 'FormGroup_L' 无效。” |
| FMATERIALID,FMaterialGroup.Name | 引用字段Name不存在 |
| FMATERIALID,FMaterialGroup | 查询成功 |
| FMATERIALID,FMaterialGroup.FNumber | 查询成功 |
| 原完整字段去掉FMaterialGroup.FName | 查询成功，50列 |
| 修复后完整字段 | 查询成功，51列，1行格式转换成功 |

物料分组元数据指向 BOS_FORMGROUP，且携带专用分组表 T_BD_MATERIALGROUP。原代码将所有引用字段一律拼接 .FName，导致金蝶生成指向通用 FormGroup_L 的无效查询。元数据读取成功，因此没有依据把SDK的“找不到kdwebapi.properties”提示判定为本次失败原因。

## 修改

1. BOS_FORMGROUP 分组字段改用经验证可查询的 .FNumber，字段名称明确追加“（编码）”。普通组织、人员等引用字段仍取名称。本次不伪造分组名称，也不修改金蝶数据库表。
2. 金蝶查询业务拒绝使用独立异常类型，外层包装后仍能识别原因，执行错误码为 KINGDEE_QUERY_REJECTED。
3. 执行日志、接口日志和回调错误显示经过脱敏、限长的上游错误消息；保留上游实际返回的业务码，未提供的码不伪造。HTTP状态仍未知。
4. 不记录完整返回报文或业务行；请求中的已知凭据、密码/令牌键值、认证头和URL经过清理，错误摘要最多1000字符。

## 发布

产物：`dj-admin/target/dingKingdee.jar`。本次没有重启远程服务，也没有修改生产业务数据或数据库结构。

替换JAR并重启后，刷新钉钉数据源字段配置。“物料分组”现在为“物料分组（编码）”，字段ID从 FMaterialGroup.FName 改为 FMaterialGroup.FNumber。已有筛选或排序若引用旧字段，需重新选择并使用分组编码；旧列是否保留由钉钉宿主处理。本次仅验证源数据查询与转换，未验证钉钉最终入表。

验证包括完整后端构建、分组字段表头/明细模式测试、业务异常包装与业务码保留测试、凭据脱敏测试及同账套只读实测。
