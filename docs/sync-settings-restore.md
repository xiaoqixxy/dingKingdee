# 钉钉修改数据同步设置：配置恢复

## 行为

- 插件 `initView.onReady` 后读取当前表格的 `getSyncConfig()`。
- 优先使用 `Dingdocs.base.host.dataSource`，兼容旧版 `Dingdocs.base.host`。
- 无旧配置时进入连接列表；有旧配置时恢复原连接、表单、明细范围、表头填充方式、筛选分组和排序。
- 修改已有同步时进入「表单选择」；该页不能返回连接列表，明细页可返回表单选择，筛选排序页可返回明细页。新增流程保持不变。
- 修改后提交时合并未改动的配置属性，仍由钉钉保存当前表格配置；不使用浏览器缓存或企业级连接默认值恢复。
- 读取失败时阻止进入空白配置，提供重新读取入口。
- 表单或明细范围发生变化时，保留原有的失效条件清理逻辑。

## 接口依据

官方 npm 包 `dingtalk-docs-cool-app@0.3.16` 的 `dist-types/types/base.d.ts` 声明了：

```ts
Dingdocs.base.host.dataSource.getSyncConfig<T>(): Promise<T>
Dingdocs.base.host.dataSource.saveConfigAndGoNext<T>(config: T): Promise<void>
```

同时保留已弃用的 `host.getSyncConfig` / `host.saveConfigAndGoNext`。
包来源：https://registry.npmjs.org/dingtalk-docs-cool-app/-/dingtalk-docs-cool-app-0.3.16.tgz

项目仍使用 0.3.2 初始化包；该包按钉钉 iframe 提供的版本加载运行时，因此本次添加本地接口声明和新旧运行时兼容，不升级依赖。

## 验证

自动测试覆盖当前表格恢复、修改一项后再次打开、不同表格隔离、修改模式返回边界、新建空配置、读取失败重试、开发环境 StrictMode 及原明细流程。

部署后需在钉钉实际确认：

1. 打开已同步表格的「修改数据同步设置」，进入插件配置页，检查原表单、明细、筛选和排序。
2. 只改一项并保存，再打开确认其他设置不变。
3. 使用同一连接打开另一张同步表，确认恢复的是另一张表的设置。

自动同步时间和钉钉同步字段选择属于钉钉后续配置页，本插件不直接修改。
