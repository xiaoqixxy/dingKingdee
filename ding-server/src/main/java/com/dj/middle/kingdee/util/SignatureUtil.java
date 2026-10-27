package com.dj.middle.kingdee.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** 同步请求签名校验。 */
public class SignatureUtil {
    /** 接口签名计算使用的密钥。 */
    public static final String secretKey = "g8Kx3mPq7wE9jR5vN2yB4aH6cF1dL0sT";

    /**
     * @param secretKey  密钥
     * @param body 请求体
     * @param timestamp 时间戳
     * @param signature 请求签名
     * @return 校验签名是否相等的 boolean
     */
    public static boolean verifySignature(String secretKey, String body, long timestamp, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String content = body + timestamp;
            byte[] hash = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            String signatureExpected = Base64.getEncoder().encodeToString(hash);
            return signature.equals(signatureExpected);
        } catch (Exception e) {
            throw new RuntimeException("signature not match", e);
        }
    }
}
