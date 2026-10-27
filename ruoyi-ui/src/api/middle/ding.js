/** 金蝶及钉钉业务接口或展示字典，沿用统一请求封装与服务端字段约定。 */
import request from '@/utils/request'

/** 按条件查询产品套餐，通过统一请求封装处理鉴权与错误。 */
export function listProduct(query) {
  return request({
    url: '/ding/product/list',
    method: 'get',
    params: query
  })
}

/** 读取产品套餐，通过统一请求封装处理鉴权与错误。 */
export function getProduct(id) {
  return request({
    url: '/ding/product/' + id,
    method: 'get'
  })
}

/** 新增产品套餐，通过统一请求封装处理鉴权与错误。 */
export function addProduct(data) {
  return request({
    url: '/ding/product',
    method: 'post',
    data: data
  })
}

/** 更新产品套餐，通过统一请求封装处理鉴权与错误。 */
export function updateProduct(data) {
  return request({
    url: '/ding/product',
    method: 'put',
    data: data
  })
}

/** 删除产品套餐，通过统一请求封装处理鉴权与错误。 */
export function delProduct(id) {
  return request({
    url: '/ding/product/' + id,
    method: 'delete'
  })
}

/** 读取可选项产品套餐，通过统一请求封装处理鉴权与错误。 */
export function getProductOptions() {
  return request({
    url: '/ding/product/options',
    method: 'get'
  })
}

/** 按条件查询套餐列表。 */
export function listPackage(query) {
  return request({
    url: '/ding/package/list',
    method: 'get',
    params: query
  })
}

/** 按标识读取套餐详情。 */
export function getPackage(id) {
  return request({
    url: '/ding/package/' + id,
    method: 'get'
  })
}

/** 新增套餐定义。 */
export function addPackage(data) {
  return request({
    url: '/ding/package',
    method: 'post',
    data: data
  })
}

/** 更新已有套餐信息。 */
export function updatePackage(data) {
  return request({
    url: '/ding/package',
    method: 'put',
    data: data
  })
}

/** 删除指定套餐。 */
export function delPackage(id) {
  return request({
    url: '/ding/package/' + id,
    method: 'delete'
  })
}

/** 读取可选套餐下拉列表。 */
export function getPackageOptions() {
  return request({
    url: '/ding/product/options',
    method: 'get'
  })
}

/** 按条件查询企业，通过统一请求封装处理鉴权与错误。 */
export function listTenant(query) {
  return request({
    url: '/ding/tenant/list',
    method: 'get',
    params: query
  })
}

/** 读取企业，通过统一请求封装处理鉴权与错误。 */
export function getTenant(id) {
  return request({
    url: '/ding/tenant/' + id,
    method: 'get'
  })
}

/** 读取指定企业的企业，通过统一请求封装处理鉴权与错误。 */
export function getTenantByCorpId(dingCorpId) {
  return request({
    url: '/ding/tenant/corp/' + dingCorpId,
    method: 'get'
  })
}

/** 新增企业，通过统一请求封装处理鉴权与错误。 */
export function addTenant(data) {
  return request({
    url: '/ding/tenant',
    method: 'post',
    data: data
  })
}

/** 更新企业，通过统一请求封装处理鉴权与错误。 */
export function updateTenant(data) {
  return request({
    url: '/ding/tenant',
    method: 'put',
    data: data
  })
}

/** 删除企业，通过统一请求封装处理鉴权与错误。 */
export function delTenant(id) {
  return request({
    url: '/ding/tenant/' + id,
    method: 'delete'
  })
}

/** 按条件查询订阅，通过统一请求封装处理鉴权与错误。 */
export function listSubscription(query) {
  return request({
    url: '/ding/subscription/list',
    method: 'get',
    params: query
  })
}

/** 读取订阅，通过统一请求封装处理鉴权与错误。 */
export function getSubscription(id) {
  return request({
    url: '/ding/subscription/' + id,
    method: 'get'
  })
}

/** 读取指定企业的订阅，通过统一请求封装处理鉴权与错误。 */
export function getSubscriptionByCorpId(dingCorpId) {
  return request({
    url: '/ding/subscription/corp/' + dingCorpId,
    method: 'get'
  })
}

/** 新增订阅，通过统一请求封装处理鉴权与错误。 */
export function addSubscription(data) {
  return request({
    url: '/ding/subscription',
    method: 'post',
    data: data
  })
}

/** 更新订阅，通过统一请求封装处理鉴权与错误。 */
export function updateSubscription(data) {
  return request({
    url: '/ding/subscription',
    method: 'put',
    data: data
  })
}

/** 删除订阅，通过统一请求封装处理鉴权与错误。 */
export function delSubscription(id) {
  return request({
    url: '/ding/subscription/' + id,
    method: 'delete'
  })
}

/** 按条件查询订单，通过统一请求封装处理鉴权与错误。 */
export function listOrder(query) {
  return request({
    url: '/ding/order/list',
    method: 'get',
    params: query
  })
}

/** 读取订单，通过统一请求封装处理鉴权与错误。 */
export function getOrder(id) {
  return request({
    url: '/ding/order/' + id,
    method: 'get'
  })
}

/** 读取指定订单号的订单，通过统一请求封装处理鉴权与错误。 */
export function getOrderByNo(orderNo) {
  return request({
    url: '/ding/order/no/' + orderNo,
    method: 'get'
  })
}

/** 新增订单，通过统一请求封装处理鉴权与错误。 */
export function addOrder(data) {
  return request({
    url: '/ding/order',
    method: 'post',
    data: data
  })
}

/** 更新订单，通过统一请求封装处理鉴权与错误。 */
export function updateOrder(data) {
  return request({
    url: '/ding/order',
    method: 'put',
    data: data
  })
}

/** 删除订单，通过统一请求封装处理鉴权与错误。 */
export function delOrder(id) {
  return request({
    url: '/ding/order/' + id,
    method: 'delete'
  })
}

/** 按条件查询支付日志，通过统一请求封装处理鉴权与错误。 */
export function listPaymentLog(query) {
  return request({
    url: '/ding/paymentLog/list',
    method: 'get',
    params: query
  })
}

/** 读取支付日志，通过统一请求封装处理鉴权与错误。 */
export function getPaymentLog(id) {
  return request({
    url: '/ding/paymentLog/' + id,
    method: 'get'
  })
}

/** 按条件查询同步表单，通过统一请求封装处理鉴权与错误。 */
export function listSyncForm(query) {
  return request({
    url: '/ding/syncForm/list',
    method: 'get',
    params: query
  })
}

/** 读取同步表单，通过统一请求封装处理鉴权与错误。 */
export function getSyncForm(id) {
  return request({
    url: '/ding/syncForm/' + id,
    method: 'get'
  })
}

/** 读取指定企业的同步表单，通过统一请求封装处理鉴权与错误。 */
export function getSyncFormByCorpId(dingCorpId) {
  return request({
    url: '/ding/syncForm/corp/' + dingCorpId,
    method: 'get'
  })
}

/** 新增同步表单，通过统一请求封装处理鉴权与错误。 */
export function addSyncForm(data) {
  return request({
    url: '/ding/syncForm',
    method: 'post',
    data: data
  })
}

/** 更新同步表单，通过统一请求封装处理鉴权与错误。 */
export function updateSyncForm(data) {
  return request({
    url: '/ding/syncForm',
    method: 'put',
    data: data
  })
}

/** 删除同步表单，通过统一请求封装处理鉴权与错误。 */
export function delSyncForm(id) {
  return request({
    url: '/ding/syncForm/' + id,
    method: 'delete'
  })
}

/** 读取绑定数量同步表单，通过统一请求封装处理鉴权与错误。 */
export function getSyncFormCount(dingCorpId) {
  return request({
    url: '/ding/syncForm/count/' + dingCorpId,
    method: 'get'
  })
}

/** 按条件查询同步日志，通过统一请求封装处理鉴权与错误。 */
export function listSyncLog(query) {
  return request({
    url: '/ding/syncLog/list',
    method: 'get',
    params: query
  })
}

/** 读取同步日志，通过统一请求封装处理鉴权与错误。 */
export function getSyncLog(id) {
  return request({
    url: '/ding/syncLog/' + id,
    method: 'get'
  })
}

/** 删除同步日志，通过统一请求封装处理鉴权与错误。 */
export function delSyncLog(id) {
  return request({
    url: '/ding/syncLog/' + id,
    method: 'delete'
  })
}

/** 按条件查询发票，通过统一请求封装处理鉴权与错误。 */
export function listInvoice(query) {
  return request({
    url: '/ding/invoice/list',
    method: 'get',
    params: query
  })
}

/** 读取发票，通过统一请求封装处理鉴权与错误。 */
export function getInvoice(id) {
  return request({
    url: '/ding/invoice/' + id,
    method: 'get'
  })
}

/** 新增发票，通过统一请求封装处理鉴权与错误。 */
export function addInvoice(data) {
  return request({
    url: '/ding/invoice',
    method: 'post',
    data: data
  })
}

/** 更新发票，通过统一请求封装处理鉴权与错误。 */
export function updateInvoice(data) {
  return request({
    url: '/ding/invoice',
    method: 'put',
    data: data
  })
}

/** 删除发票，通过统一请求封装处理鉴权与错误。 */
export function delInvoice(id) {
  return request({
    url: '/ding/invoice/' + id,
    method: 'delete'
  })
}

/** 创建线上支付订单并返回二维码信息。 */
export function onlinePay(data) {
  return request({
    url: '/payment/create',
    method: 'post',
    data: data
  })
}

/** 调用现有模拟支付结果入口，更新订单处理状态。 */
export function paymentCallback(data) {
  return request({
    url: '/payment/handleResult',
    method: 'post',
    data: data
  })
}
