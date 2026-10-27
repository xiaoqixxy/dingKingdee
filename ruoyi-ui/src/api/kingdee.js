/** 金蝶及钉钉业务接口或展示字典，沿用统一请求封装与服务端字段约定。 */
import request from '@/utils/request'

/** 按条件查询金蝶表单配置，通过统一请求封装处理鉴权与错误。 */
export function listTableConfig(query) {
  return request({
    url: '/kingdee/tableConfig/list',
    method: 'get',
    params: query
  })
}

/** 读取金蝶表单配置，通过统一请求封装处理鉴权与错误。 */
export function getTableConfig(configId) {
  return request({
    url: '/kingdee/tableConfig/' + configId,
    method: 'get'
  })
}

/** 新增金蝶表单配置，通过统一请求封装处理鉴权与错误。 */
export function addTableConfig(data) {
  return request({
    url: '/kingdee/tableConfig',
    method: 'post',
    data: data
  })
}

/** 更新金蝶表单配置，通过统一请求封装处理鉴权与错误。 */
export function updateTableConfig(data) {
  return request({
    url: '/kingdee/tableConfig',
    method: 'put',
    data: data
  })
}

/** 删除金蝶表单配置，通过统一请求封装处理鉴权与错误。 */
export function delTableConfig(configId) {
  return request({
    url: '/kingdee/tableConfig/' + configId,
    method: 'delete'
  })
}

/** 读取可选项金蝶表单配置，通过统一请求封装处理鉴权与错误。 */
export function getTableConfigOptions() {
  return request({
    url: '/kingdee/tableConfig/options',
    method: 'get'
  })
}

/** 上传金蝶表单配置文件并接收导入结果。 */
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
