package com.dj.middle.payment.util;

import java.util.Map;

/** 支付通知参数的格式解析工具。 */
public class PaymentUtil {
    /** 将支付宝回调表单内容解析为键值参数。 */
    public static Map<String, String> parseAliCallbackParams(String rawData)
    {
        Map<String, String> params = new java.util.HashMap<>();
        try {
            String[] pairs = rawData.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(java.net.URLDecoder.decode(kv[0], "UTF-8"),
                            java.net.URLDecoder.decode(kv[1], "UTF-8"));
                }
            }
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("URL解码失败", e);
        }
        return params;
    }
}
