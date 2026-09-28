# 项目清理说明

## 保留的工程

- `dj-admin`：后端启动入口。
- `dj-common`、`dj-framework`、`dj-system`：通用、安全和系统管理能力。
- `dj-quartz`、`dj-generator`：定时任务与代码生成，仍由启动模块依赖，保留。
- `ding-server`：钉钉、金蝶和支付业务。
- `ruoyi-ui`：Vue 管理后台；页面还可能由后端菜单动态加载，不能仅凭静态 import 判断无用。
- `ding-web`：React 钉钉插件；`config` 和 `scripts` 是实际使用的构建工具，保留。

## 本次清理

- 删除 `ding-web` 下 7 个针对旧版 `App.tsx` 做文本替换的一次性 Python 脚本。
- 删除管理后台旧模板首页 `index.vue.backup`。
- 删除插件内未读取的支付渠道状态及其赋值，以及未调用的 `loadSheetMeta` 包装函数。
- 移除没有引用的直接依赖 `element-react`、`babel-runtime`、`web-vitals`、`@testing-library/user-event`，同步 npm 锁文件。
- 保留 `element-plus`：插件入口仍引用其样式，避免清理影响页面外观。
- 忽略备份文件、根目录构建输出及临时工作目录，避免误提交。
- 删除两份本地会话流水，忽略 `memory/`；正式设计、迁移与测试说明统一保留在 `docs/`。

`ruoyi-admin` 仅剩旧构建产物，已不在 Maven 模块列表中；本次环境拒绝批量递归删除，因此仍保留在磁盘上。`output` 中的已有构建产物也保留，仅加入忽略规则。

## 验证命令

在 `ding-web` 中运行：

```sh
npx tsc --noEmit --noUnusedLocals --noUnusedParameters
npm test -- --watchAll=false --runInBand
npm run build
```

在 `ruoyi-ui` 中运行 `npm run build:prod`。后端如有改动，在根目录使用 JDK 17 和 Maven 运行 `mvn clean verify`。
