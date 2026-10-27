package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import java.util.List;
import java.util.Map;

/** ExecuteBillQuery can return a business error inside a one-cell result row. 识别单据查询结果中嵌套的业务失败响应。 */
public final class KingdeeQueryResponse {
    /** 工具类仅提供静态方法，禁止外部实例化。 */
    private KingdeeQueryResponse() {}
    /** 校验上游响应中的业务成功状态，失败时保留可识别的错误原因。 */
    public static void requireSuccess(List<List<Object>> rows) {
        if (rows == null) throw new IllegalArgumentException("金蝶查询返回空响应");
        for (List<Object> row : rows) {
            if (row == null || row.size() != 1 || !(row.get(0) instanceof Map)) continue;
            JSONObject value = JSONObject.parseObject(JSON.toJSONString(row.get(0)));
            JSONObject result = value.getJSONObject("Result");
            JSONObject status = (result == null ? value : result).getJSONObject("ResponseStatus");
            if (status == null || !Boolean.FALSE.equals(status.getBoolean("IsSuccess"))) continue;
            StringBuilder message = new StringBuilder("金蝶查询失败");
            JSONArray errors = status.getJSONArray("Errors");
            if (errors != null) for (int i = 0; i < Math.min(errors.size(), 3); i++) {
                String detail = errors.getJSONObject(i).getString("Message");
                if (detail != null) message.append("；").append(detail);
            }
            throw new KingdeeQueryException(status.getString("ErrorCode"), message.toString());
        }
    }
}
