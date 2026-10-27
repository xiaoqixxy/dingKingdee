package com.dj.middle.ding.service;

import java.util.Collection;

/** 仅处理选定的错误信息，不保存原始请求或响应正文。 同步异常信息和堆栈的凭证脱敏。 */
final class SyncErrorSanitizer {
    /** 工具类仅提供静态方法，禁止外部实例化。 */
    private SyncErrorSanitizer() {}

    /** 生成可展示的脱敏错误信息，并清理控制字符。 */
    static String sanitize(String message, Collection<String> secrets) {
        String result = redact(message, secrets).replaceAll("[\\p{Cntrl}]", " ");
        return result.substring(0, Math.min(result.length(), 1000));
    }

    /** 将异常堆栈转换为脱敏文本，供后台排查。 */
    static String stackTrace(Throwable error, Collection<String> secrets) {
        java.io.StringWriter text = new java.io.StringWriter();
        error.printStackTrace(new java.io.PrintWriter(text));
        return redact(text.toString(), secrets);
    }

    /** 删除错误文本中的凭证值及已知敏感参数。 */
    private static String redact(String message, Collection<String> secrets) {
        if (message == null) return "";
        String result = message;
        if (secrets != null) {
            for (String secret : secrets.stream().filter(s -> s != null && !s.isBlank())
                    .sorted(java.util.Comparator.comparingInt(String::length).reversed()).toList()) {
                result = result.replace(secret, "[redacted]");
            }
        }
        result = result.replaceAll("(?i)(authorization|cookie)\\s*[\\\"']?\\s*[:=]\\s*[^\\r\\n]*", "$1=[redacted]");
        result = result.replaceAll("(?i)(app[_-]?secret|app[_-]?id|access[_-]?token|refresh[_-]?token|password|secret|token)"
            + "[\\\"']?\\s*[:=]\\s*(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s,;&}]+)", "$1=[redacted]");
        result = result.replaceAll("(?i)https?://[^\\s\\\"'<>]+", "[url]");
        return result;
    }
}
