import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import 'element-plus/dist/index.css';

// 建立插件页面唯一的React根节点，宿主初始化及同步配置恢复由App组件负责。
const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
// 严格模式会在开发环境重放副作用，宿主SDK初始化必须在组件内保证幂等。
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
