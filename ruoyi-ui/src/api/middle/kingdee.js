import request from '@/utils/request'

export function listTableConfig(query) {
  return request({
    url: '/middle/kingdee/tableConfig/list',
    method: 'get',
    params: query
  })
}

export function getTableConfig(configId) {
  return request({
    url: '/middle/kingdee/tableConfig/' + configId,
    method: 'get'
  })
}

export function addTableConfig(data) {
  return request({
    url: '/middle/kingdee/tableConfig',
    method: 'post',
    data: data
  })
}

export function updateTableConfig(data) {
  return request({
    url: '/middle/kingdee/tableConfig',
    method: 'put',
    data: data
  })
}

export function delTableConfig(configId) {
  return request({
    url: '/middle/kingdee/tableConfig/' + configId,
    method: 'delete'
  })
}

export function getTableConfigOptions() {
  return request({
    url: '/middle/kingdee/tableConfig/options',
    method: 'get'
  })
}
