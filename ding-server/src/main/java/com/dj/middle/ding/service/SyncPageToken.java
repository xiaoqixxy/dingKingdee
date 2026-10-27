package com.dj.middle.ding.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Opaque continuation token; database checks tenant, source binding and expected position. 同步续传令牌及绑定数据源的规范化指纹。 */
final class SyncPageToken {
    /** 续传令牌解析后的链路标识与分页位置；同步执行链路标识；下一页的源数据偏移量。 同步执行链路标识；下一页的源数据偏移量。 */
    record Position(String trace, int offset) {}

    /** 解析新续传令牌并兼容旧数字偏移；格式异常时要求重新发起同步。 */
    static Position parse(String value) {
        if (value == null || value.isBlank()) return new Position(null, 0);
        // 兼容旧客户端的纯数字偏移；新令牌还携带链路编号，由数据库校验归属和位置。
        if (value.matches("[0-9]{1,10}")) return new Position(null, nonNegative(value));
        String[] parts = value.split(":", -1);
        if (parts.length != 3 || !"s1".equals(parts[0])
                || !parts[1].matches("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}"))
            throw new IllegalArgumentException("分页令牌无效，请重新发起同步");
        return new Position(parts[1], nonNegative(parts[2]));
    }

    /** 把分页偏移解析为非负整数。 */
    private static int nonNegative(String value) {
        int offset = Integer.parseInt(value);
        if (offset < 0) throw new IllegalArgumentException("分页位置无效");
        return offset;
    }

    /** 生成携带链路编号和分页偏移的续传令牌。 */
    static String encode(String trace, int offset) { return "s1:" + trace + ":" + offset; }

    /** 对同步配置规范化后计算指纹，绑定令牌与数据源。 */
    static String sourceKey(JSONObject request) {
        Object params;
        try { params = JSON.parse(request.getString("params")); }
        catch (RuntimeException invalid) { params = request.getString("params"); }
        return digest(JSON.toJSONString(canonical(params)));
    }

    /** 递归排序对象键以获得稳定指纹，保留数组顺序。 */
    private static Object canonical(Object value) {
        if (value instanceof Map<?, ?> map) {
            // 对象键顺序不应影响配置指纹；数组顺序则具有业务含义，必须原样保留。
            Map<String, Object> sorted = new TreeMap<>();
            map.forEach((key, item) -> sorted.put(key.toString(), canonical(item)));
            return sorted;
        }
        if (value instanceof List<?> list) return list.stream().map(SyncPageToken::canonical).toList();
        return value;
    }

    /** 对文本计算SHA256摘要。 */
    static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
