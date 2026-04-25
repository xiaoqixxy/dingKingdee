package com.ruoyi.middle.kingdee.constant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日期转换工具类
 * <p>
 * 专门处理金蝶云星空返回的日期格式转换。
 * 金蝶接口返回的日期格式为 ISO 8601 标准格式：yyyy-MM-dd'T'HH:mm:ss
 * <p>
 * 示例：2025-03-04T00:00:00
 *
 * @author ruoyi
 */
public class DateConverterUtil {

    /**
     * 目标日期时间格式（ISO 8601）
     * <p>
     * 格式：2025-03-04T00:00:00
     */
    private static final DateTimeFormatter TARGET_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 将ISO格式日期字符串转换为LocalDate
     * <p>
     * 仅支持格式：yyyy-MM-dd'T'HH:mm:ss
     *
     * @param dateStr ISO格式日期字符串，如：2025-03-04T00:00:00
     * @return LocalDate对象（仅日期部分）
     * @throws IllegalArgumentException 当格式不匹配或字符串为空时抛出
     * @example
     * <pre>
     * LocalDate date = DateConverterUtil.stringToLocalDate("2025-03-04T00:00:00");
     * // 返回 2025-03-04
     * </pre>
     */
    public static LocalDate stringToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }

        String trimmedDateStr = dateStr.trim();
        try {
            LocalDateTime dateTime = LocalDateTime.parse(trimmedDateStr, TARGET_DATETIME_FORMATTER);
            return dateTime.toLocalDate();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "日期格式错误！仅支持：yyyy-MM-dd'T'HH:mm:ss（如2025-03-04T00:00:00），当前输入：" + dateStr, e);
        }
    }

    /**
     * 将ISO格式日期字符串转换为LocalDateTime
     * <p>
     * 仅支持格式：yyyy-MM-dd'T'HH:mm:ss
     *
     * @param dateTimeStr ISO格式日期时间字符串
     * @return LocalDateTime对象
     * @throws IllegalArgumentException 当格式不匹配或字符串为空时抛出
     */
    public static LocalDateTime stringToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期时间字符串不能为空");
        }

        String trimmedStr = dateTimeStr.trim();
        try {
            return LocalDateTime.parse(trimmedStr, TARGET_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "日期时间格式错误！仅支持：yyyy-MM-dd'T'HH:mm:ss（如2025-03-04T00:00:00），当前输入：" + dateTimeStr, e);
        }
    }

    /**
     * 将ISO格式日期字符串转换为java.util.Date
     * <p>
     * 兼容旧版Date类型的使用场景。
     *
     * @param dateStr ISO格式日期字符串
     * @return java.util.Date对象
     * @throws IllegalArgumentException 日期解析失败时抛出
     */
    public static java.util.Date stringToDate(String dateStr) {
        try {
            LocalDate localDate = stringToLocalDate(dateStr);
            return java.sql.Date.valueOf(localDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期解析失败：" + e.getMessage(), e);
        }
    }
}
