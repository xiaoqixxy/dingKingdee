import request from '@/utils/request'

export function listTableConfig(query) {
  return request({
    url: '/kingdee/tableConfig/list',
    method: 'get',
    params: query
  })
}

export function getTableConfig(configId) {
  return request({
    url: '/kingdee/tableConfig/' + configId,
    method: 'get'
  })
}

export function addTableConfig(data) {
  return request({
    url: '/kingdee/tableConfig',
    method: 'post',
    data: data
  })
}

export function updateTableConfig(data) {
  return request({
    url: '/kingdee/tableConfig',
    method: 'put',
    data: data
  })
}

export function delTableConfig(configId) {
  return request({
    url: '/kingdee/tableConfig/' + configId,
    method: 'delete'
  })
}

export function getTableConfigOptions() {
  return request({
    url: '/kingdee/tableConfig/options',
    method: 'get'
  })
}

export function importTableConfig(data) {
  return request({
    url: '/kingdee/tableConfig/importData',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
