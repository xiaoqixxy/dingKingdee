package com.dj.middle.ding.service;

import java.util.Collection;

/** Only used for selected error messages, never for storing raw response/request bodies. */
final class SyncErrorSanitizer {
    private SyncErrorSanitizer() {}

    static String sanitize(String message, Collection<String> secrets) {
        String result = redact(message, secrets).replaceAll("[\\p{Cntrl}]", " ");
        return result.substring(0, Math.min(result.length(), 1000));
    }

    static String stackTrace(Throwable error, Collection<String> secrets) {
        java.io.StringWriter text = new java.io.StringWriter();
        error.printStackTrace(new java.io.PrintWriter(text));
        return redact(text.toString(), secrets);
    }

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
