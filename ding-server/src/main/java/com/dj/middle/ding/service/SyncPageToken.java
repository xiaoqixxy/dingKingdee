package com.dj.middle.ding.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Opaque continuation token; database checks tenant, source binding and expected position. */
final class SyncPageToken {
    record Position(String trace, int offset) {}

    static Position parse(String value) {
        if (value == null || value.isBlank()) return new Position(null, 0);
        if (value.matches("[0-9]{1,10}")) return new Position(null, nonNegative(value));
        String[] parts = value.split(":", -1);
        if (parts.length != 3 || !"s1".equals(parts[0])
                || !parts[1].matches("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}"))
            throw new IllegalArgumentException("分页令牌无效，请重新发起同步");
        return new Position(parts[1], nonNegative(parts[2]));
    }

    private static int nonNegative(String value) {
        int offset = Integer.parseInt(value);
        if (offset < 0) throw new IllegalArgumentException("分页位置无效");
        return offset;
    }

    static String encode(String trace, int offset) { return "s1:" + trace + ":" + offset; }

    static String sourceKey(JSONObject request) {
        Object params;
        try { params = JSON.parse(request.getString("params")); }
        catch (RuntimeException invalid) { params = request.getString("params"); }
        return digest(JSON.toJSONString(canonical(params)));
    }

    private static Object canonical(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            map.forEach((key, item) -> sorted.put(key.toString(), canonical(item)));
            return sorted;
        }
        if (value instanceof List<?> list) return list.stream().map(SyncPageToken::canonical).toList();
        return value;
    }

    static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
