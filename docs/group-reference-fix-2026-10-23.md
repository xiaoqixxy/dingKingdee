# 采购入库单分组引用修复

本页记录初次兼容修复。后续已按 QueryGroupInfo 接入可维护的扩展数据字典，支持选择名称、编码、描述、内码及父级分组，最新实现参见 [扩展数据字典](extension-data-dictionaries.md)。

## 原因

物料元数据中，`FMaterialGroup` 和 `FSalGroup` 的引用对象均为 `BOS_FORMGROUP`，实际分组表由各自的 `GroupFieldTableName` 指定。只读联调确认，该通用对象返回 `Entrys: []`，虽然声明 `PkFieldName: FID`，却无法独立查询 `FID` 或 `FNumber`。

旧引用解析器为该空对象补出了可选 `FID`，前端默认回退选中后，执行器向 `BOS_FORMGROUP` 发出独立查询，报“元数据中标识为FID的字段不存在”。

## 修复

- 分组作为所属业务对象的编码属性输出，使用 `BD_MATERIAL` 上的 `FMaterialGroup.FNumber`、`FSalGroup.FNumber`，不将其作为可继续展开的独立基础资料。
- 空元数据对象不再补出可选择的主键字段。
- 兼容历史自动保存的 `FMaterialGroup → FID`、`FSalGroup → FID` 配置，沿所属物料读取分组编码，保留原输出列 ID 和别名。直接分组列的历史默认配置同样兼容。
- 其他未支持的分组子级路径明确报错，避免任意更改取值含义。
- 前端对按所属对象读取的分组字段，不再误报引用元数据缺失。

## 验证

- 后端全模块打包与82项测试通过，失败、错误、跳过均为0。
- 前端36项测试与 TypeScript 检查通过。
- 采购入库单真实只读联调抽取5条记录，新旧分组配置10项结果对比一致；查询仅涉及采购入库单与物料对象，通用分组对象查询次数为0。样本包含5个非空分组值，其余为空值，不代表所有分组都已配置业务数据。
- 未写入金蝶业务数据，未执行钉钉落表。联调结果仅保存状态与数量：`output/group-reference-live-validation.json`。

## 发布

本次需更新后端 `dj-admin/target/dingKingdee.jar` 并重启服务，同时更新前端 `ding-web/build`。发布后刷新页面重新进入字段配置，分组编码选项不再带可展开子级箭头；已保存的默认 `FID` 子级由后端兼容，无需删除同步表。
