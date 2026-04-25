import request from '@/utils/request'

export function listProduct(query) {
  return request({
    url: '/ding/product/list',
    method: 'get',
    params: query
  })
}

export function getProduct(id) {
  return request({
    url: '/ding/product/' + id,
    method: 'get'
  })
}

export function addProduct(data) {
  return request({
    url: '/ding/product',
    method: 'post',
    data: data
  })
}

export function updateProduct(data) {
  return request({
    url: '/ding/product',
    method: 'put',
    data: data
  })
}

export function delProduct(id) {
  return request({
    url: '/ding/product/' + id,
    method: 'delete'
  })
}

export function getProductOptions() {
  return request({
    url: '/ding/product/options',
    method: 'get'
  })
}

export function listPackage(query) {
  return request({
    url: '/ding/package/list',
    method: 'get',
    params: query
  })
}

export function getPackage(id) {
  return request({
    url: '/ding/package/' + id,
    method: 'get'
  })
}

export function addPackage(data) {
  return request({
    url: '/ding/package',
    method: 'post',
    data: data
  })
}

export function updatePackage(data) {
  return request({
    url: '/ding/package',
    method: 'put',
    data: data
  })
}

export function delPackage(id) {
  return request({
    url: '/ding/package/' + id,
    method: 'delete'
  })
}

export function getPackageOptions() {
  return request({
    url: '/ding/product/options',
    method: 'get'
  })
}

export function listTenant(query) {
  return request({
    url: '/ding/tenant/list',
    method: 'get',
    params: query
  })
}

export function getTenant(id) {
  return request({
    url: '/ding/tenant/' + id,
    method: 'get'
  })
}

export function getTenantByCorpId(dingCorpId) {
  return request({
    url: '/ding/tenant/corp/' + dingCorpId,
    method: 'get'
  })
}

export function addTenant(data) {
  return request({
    url: '/ding/tenant',
    method: 'post',
    data: data
  })
}

export function updateTenant(data) {
  return request({
    url: '/ding/tenant',
    method: 'put',
    data: data
  })
}

export function delTenant(id) {
  return request({
    url: '/ding/tenant/' + id,
    method: 'delete'
  })
}

export function listSubscription(query) {
  return request({
    url: '/ding/subscription/list',
    method: 'get',
    params: query
  })
}

export function getSubscription(id) {
  return request({
    url: '/ding/subscription/' + id,
    method: 'get'
  })
}

export function getSubscriptionByCorpId(dingCorpId) {
  return request({
    url: '/ding/subscription/corp/' + dingCorpId,
    method: 'get'
  })
}

export function addSubscription(data) {
  return request({
    url: '/ding/subscription',
    method: 'post',
    data: data
  })
}

export function updateSubscription(data) {
  return request({
    url: '/ding/subscription',
    method: 'put',
    data: data
  })
}

export function delSubscription(id) {
  return request({
    url: '/ding/subscription/' + id,
    method: 'delete'
  })
}

export function listOrder(query) {
  return request({
    url: '/ding/order/list',
    method: 'get',
    params: query
  })
}

export function getOrder(id) {
  return request({
    url: '/ding/order/' + id,
    method: 'get'
  })
}

export function getOrderByNo(orderNo) {
  return request({
    url: '/ding/order/no/' + orderNo,
    method: 'get'
  })
}

export function addOrder(data) {
  return request({
    url: '/ding/order',
    method: 'post',
    data: data
  })
}

export function updateOrder(data) {
  return request({
    url: '/ding/order',
    method: 'put',
    data: data
  })
}

export function delOrder(id) {
  return request({
    url: '/ding/order/' + id,
    method: 'delete'
  })
}

export function listPaymentLog(query) {
  return request({
    url: '/ding/paymentLog/list',
    method: 'get',
    params: query
  })
}

export function getPaymentLog(id) {
  return request({
    url: '/ding/paymentLog/' + id,
    method: 'get'
  })
}

export function listSyncForm(query) {
  return request({
    url: '/ding/syncForm/list',
    method: 'get',
    params: query
  })
}

export function getSyncForm(id) {
  return request({
    url: '/ding/syncForm/' + id,
    method: 'get'
  })
}

export function getSyncFormByCorpId(dingCorpId) {
  return request({
    url: '/ding/syncForm/corp/' + dingCorpId,
    method: 'get'
  })
}

export function addSyncForm(data) {
  return request({
    url: '/ding/syncForm',
    method: 'post',
    data: data
  })
}

export function updateSyncForm(data) {
  return request({
    url: '/ding/syncForm',
    method: 'put',
    data: data
  })
}

export function delSyncForm(id) {
  return request({
    url: '/ding/syncForm/' + id,
    method: 'delete'
  })
}

export function getSyncFormCount(dingCorpId) {
  return request({
    url: '/ding/syncForm/count/' + dingCorpId,
    method: 'get'
  })
}

export function listSyncLog(query) {
  return request({
    url: '/ding/syncLog/list',
    method: 'get',
    params: query
  })
}

export function getSyncLog(id) {
  return request({
    url: '/ding/syncLog/' + id,
    method: 'get'
  })
}

export function delSyncLog(id) {
  return request({
    url: '/ding/syncLog/' + id,
    method: 'delete'
  })
}

export function listInvoice(query) {
  return request({
    url: '/ding/invoice/list',
    method: 'get',
    params: query
  })
}

export function getInvoice(id) {
  return request({
    url: '/ding/invoice/' + id,
    method: 'get'
  })
}

export function addInvoice(data) {
  return request({
    url: '/ding/invoice',
    method: 'post',
    data: data
  })
}

export function updateInvoice(data) {
  return request({
    url: '/ding/invoice',
    method: 'put',
    data: data
  })
}

export function delInvoice(id) {
  return request({
    url: '/ding/invoice/' + id,
    method: 'delete'
  })
}

export function onlinePay(data) {
  return request({
    url: '/payment/create',
    method: 'post',
    data: data
  })
}

export function paymentCallback(data) {
  return request({
    url: '/payment/handleResult',
    method: 'post',
    data: data
  })
}
