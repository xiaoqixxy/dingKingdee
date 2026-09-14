## 钉钉AI表格数据源插件开发demo
开发指南：https://alidocs.dingtalk.com/i/nodes/1OQX0akWmBejogd0TAlmRyQGVGlDd3mE

### `npm install`
Install dependencies

### `npm start`

Runs the app in the development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

### `npm run build`

Builds the app for production to the `build` folder.
### 部署资源

当前线上入口为 `https://ding.jxdingjiang.com/jxdj`，静态资源由根目录 `/static/` 提供，构建保留默认绝对资源路径。不要改成相对路径，否则带尾部斜杠的入口会请求 `/jxdj/static/`。

部署时同步上传完整 `build` 目录，确保 HTML 和哈希资源来自同一次构建。

2026-09-14 排查：线上 CSS 返回 `Content-Type: text/plain`，需在 Nginx 配置中加载 MIME 映射。通常在 `http` 块中配置 `include mime.types;`（路径按实际 Nginx 安装位置确定），并检查静态资源的 `server` / `location` 是否以 `types {}` 清空或覆盖映射。CSS 必须返回 `text/css`，JS 应返回 `application/javascript` 或 `text/javascript`。不要只用 `add_header Content-Type` 叠加响应头。

修改服务器配置后先运行 `nginx -t`，成功后再运行 `nginx -s reload`。通过 `curl -I https://ding.jxdingjiang.com/static/css/<实际构建文件名>.css` 检查响应类型。静态资源不存在时应返回 404，不能回退到 index.html。
