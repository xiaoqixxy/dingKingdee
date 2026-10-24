# 扩展数据字典

## 用途

普通业务对象继续从 `QueryBusinessInfo` 获取字段。对于接口有返回数据、但普通元数据没有字段定义的对象，使用项目内维护的扩展字典提供字段名称、类型和取值映射。目前已接入分组对象 `BOS_FORMGROUP`，通过 `QueryGroupInfo` 读取真实数据，不向通用分组对象发起 `ExecuteBillQuery`。

字典文件：`ding-server/src/main/resources/kingdee/extension-dictionaries.json`。

## 已维护的分组字段

| 配置字段 | 名称 | 响应字段 | 类型 |
| --- | --- | --- | --- |
| FNumber | 编码 | FNUMBER | 文本 |
| FName | 名称 | FNAME | 文本 |
| FID | 内码 | FID | 文本 数值筛选 |
| FDescription | 描述 | FDESCRIPTION | 文本 |
| FGroupId | 分组标识 | FGROUPID | 文本 |
| FParentId | 父级分组 | FPARENTID | 同一分组字典引用 |
| FFullParentId | 完整父级路径 | FFULLPARENTID | 文本 |
| FLeft | 左值 | FLEFT | 数字 |
| FRight | 右值 | FRIGHT | 数字 |

字段来源名称继续使用业务元数据原名，不添加“编码”等后缀。扩展属性的名称在字典中维护。默认取值保持 FNumber，用户可选择名称、描述、内码等，也可继续选择父级分组的属性。

## 查询上下文

业务元数据转换时，分组引用保留 `referenceDictionaryId`，以及 `referenceContext.formId` 和 `referenceContext.groupFieldKey`。例如物料分组为 `BD_MATERIAL + FMaterialGroup`，销售分组为 `BD_MATERIAL + FSalGroup`。不根据中文名称推断表或分组。

执行器先读取源引用内码，再按上下文分批调用 QueryGroupInfo。每批最多200个内码，设置 `GroupPkIds`，`Ids` 保持为空；空值和0不发起查询，避免无条件读取整个分组。

当前 Java SDK 的 `queryGroupInfo` 方法只接收一个 JSON 字符串，FormId 放在 JSON 内；已用本地 SDK 和目标金蝶服务验证。与用户提供的 C# 双参数调用形式不同，不将 formid 作为该 Java SDK 方法的第一个参数。

同一次请求内缓存完整分组记录，同一上下文的名称、编码、描述等列共用返回结果；不同表单、不同分组字段的相同内码隔离。父级分组查询沿用当前分组上下文。权限、格式和业务错误明确抛出，不作为空数据吞掉。找不到对应内码时显示为空。

配置后的分组属性继续走既有引用筛选与排序流程，先取值并筛选排序，再分页。普通无配置同步保留原查询方式。

## 配置兼容

新增或重新选择路径时，前端保存 `dictionaryVersion: 1`，真实内码可使用 FID。历史未带版本的分组 FID 是旧界面自动选择的合成字段，前后端均将其兼容为原分组编码，避免升级后列值改变。用户重新选择内码并提交后，使用真实 FID。输出列 ID、用户别名和新增列标识保留。

## 维护方式

调整字典属性时维护 key、name、type、responseKey；引用属性还需维护 referenceFormId。primaryKey 和 defaultField 必须指向已声明字段，字段 key 不可重复。

当前查询适配器支持 QueryGroupInfo。新增使用其他接口的字典时，应同时实现对应适配器与上下文校验，不能仅添加一个接口名称。字典随应用构建发布，修改后需重新打包并重启后端。

## 验证与发布

- 后端全模块87项测试、前端37项测试和 TypeScript 检查通过；前后端生产包构建通过，前端保留原有3项 ESLint 警告。
- 采购入库单只读联调覆盖分组编码、名称、内码、描述和父级名称；旧配置结果保持一致。样本中销售分组为空，其上下文隔离及非空取值由回归测试覆盖。
- 结果摘要：`output/extension-dictionary-live-validation.json`；仅保存状态和计数，不保存业务值或凭证。
- 本次需要同时部署前端 `ding-web/build` 和后端 `dj-admin/target/dingKingdee.jar`，重启后端并刷新配置页。
