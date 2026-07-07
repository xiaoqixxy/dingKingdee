// const API_BASE = 'https://web.xiexinyang.com/prod-api';
const API_BASE = 'http://localhost:1007';//本地

export const API = {
  // 登录
  login: `${API_BASE}/api/login`,
  // 表单元数据
  sheetMeta: `${API_BASE}/api/sheetMeta`,
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
