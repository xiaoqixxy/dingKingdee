// 开发环境默认访问本地后端，生产环境使用部署网关；显式环境变量优先，避免生产包意外指向localhost。
const API_BASE = process.env.REACT_APP_API_BASE ?? (process.env.NODE_ENV === 'development'
  ? 'http://localhost:1009'
  : 'https://web.jxdingjiang.com/prod-api');

export const API = {
  // 登录
  login: `${API_BASE}/api/login`,
  // 表单元数据
  sheetMeta: `${API_BASE}/api/sheetMeta`,
  // 应用字段配置后的最终结构校验接口，提交前由服务端验证别名和引用路径。
  formSchema: `${API_BASE}/api/formSchema`,
  // 引用元数据入口：普通对象读取元数据，分组等扩展对象返回项目维护的数据字典。
  referenceSchema: `${API_BASE}/api/referenceSchema`,
  // 表单配置
  tableConfig: {
    options: `${API_BASE}/kingdee/tableConfig/options`,
  },
  // 产品信息
  product: {
    getByCorpId: (dingCorpId: string) => `${API_BASE}/ding/product/corp/${dingCorpId}`,
    // 套餐列表选项
    options: `${API_BASE}/ding/product/options`,
  },
  // 支付
  payment: {
    // 创建支付订单
    create: `${API_BASE}/payment/create`,
    // 支付结果处理
    handleResult: `${API_BASE}/payment/handleResult`,
    // 根据订单号查询订单
    getByOrderNo: (orderNo: string) => `${API_BASE}/ding/order/no/${orderNo}`,
  },
  // 凭证配置
  voucherConfig: {
    // 根据企业ID查询凭证配置列表
    listByCorpId: (dingCorpId: string) => `${API_BASE}/kingdee/voucherConfig/list/${dingCorpId}`,
    // 保存或更新凭证配置
    saveOrUpdate: `${API_BASE}/kingdee/voucherConfig/saveOrUpdate`,
    // 删除凭证配置
    delete: (id: string) => `${API_BASE}/kingdee/voucherConfig/${id}`,
  },
};
