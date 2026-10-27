/** 金蝶及钉钉业务接口或展示字典，沿用统一请求封装与服务端字段约定。 */
import request from '@/utils/request'

// 查询【请填写功能名称】列表
/** 按条件查询连接配置，通过统一请求封装处理鉴权与错误。 */
export function listConfig(query) {
    return request({
        url: '/kingdee/voucherConfig/list',
        method: 'get',
        params: query
    })
}

// 查询【请填写功能名称】详细
/** 读取连接配置，通过统一请求封装处理鉴权与错误。 */
export function getConfig(id) {
    return request({
        url: '/kingdee/voucherConfig/' + id,
        method: 'get'
    })
}

// 新增【请填写功能名称】
/** 新增连接配置，通过统一请求封装处理鉴权与错误。 */
export function addConfig(data) {
    return request({
        url: '/kingdee/voucherConfig',
        method: 'post',
        data: data
    })
}

// 修改【请填写功能名称】
/** 更新连接配置，通过统一请求封装处理鉴权与错误。 */
export function updateConfig(data) {
    return request({
        url: '/kingdee/voucherConfig',
        method: 'put',
        data: data
    })
}

// 删除【请填写功能名称】
/** 删除连接配置，通过统一请求封装处理鉴权与错误。 */
export function delConfig(id) {
    return request({
        url: '/kingdee/voucherConfig/' + id,
        method: 'delete'
    })
}
