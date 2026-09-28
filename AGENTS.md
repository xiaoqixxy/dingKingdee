## 前端修改规范
- App.tsx 中 styles 内联对象与 App.css 变量需保持颜色同步，修改主色时两处都要更新
- 新增工具函数应定义在组件级别（非 render 函数内部），避免作用域问题
- 套餐界面为全页替换模式（非弹窗），通过 upgradeDialogVisible 状态控制主内容区切换
- 表单布局改动时，inputWrapper/inputIconBox 等新增样式需同步加入 styles 对象
- 第一步/第二步表单字段使用单列布局（非网格），每项上行标签+图标，下行 inputWrapper 包裹输入框
- 用户对布局有明确偏好：单列 > 多列，间距适中不留白
- 多列卡片内若文本行数不一致，需用 minHeight 固定最短内容区高度以保持横向对齐
- 筛选/排序类 Section 使用卡片包裹 + 条件行浅灰背景 + emoji 标题图标风格

## 钉钉加群链接规范
- 钉钉加群不能使用 dingtalk://dingtalkclient/action/joingroup?chatId=xxx，会报「二维码校验失败」
- 正确方式：扫描群二维码获取 https://qr.dingtalk.com/action/joingroup?code=xxx 链接，直接使用该完整链接
- code 参数从群主在钉钉「群设置→群二维码」生成的链接中提取，不可手动构造
