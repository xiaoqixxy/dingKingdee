package com.ruoyi.middle.payment.util;

import java.util.Map;

public class PaymentUtil {
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
