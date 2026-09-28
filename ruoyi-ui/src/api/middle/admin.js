import request from '@/utils/request'
export const listRecords = (kind, params) => request({ url: `/admin/records/${kind}/list`, params })
export const recordDetail = (kind, id) => request({ url: `/admin/records/${kind}/${id}` })
export const executionItems = (id, params) => request({ url: `/admin/records/execution/${id}/items`, params })
export const requestRefund = data => request({ url: '/admin/refunds', method: 'post', data })
export const cancelRefund = id => request({ url: `/admin/refunds/${id}/cancel`, method: 'post' })

export const executionApis = (id, params) => request({ url: `/admin/records/execution/${id}/apis`, params })

export const executionPages = (id, params) => request({ url: `/admin/records/execution/${id}/pages`, params })
export const executionPageStack = (id, pageId) => request({ url: `/admin/records/execution/${id}/pages/${pageId}/stack` })
