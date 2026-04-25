package com.ruoyi.middle.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 * <p>
 * 提供日期与字符串之间的常用转换功能，支持：
 * <ul>
 *   <li>日期转字符串（指定格式）</li>
 *   <li>字符串转日期（指定格式）</li>
 *   <li>时间戳转换</li>
 * </ul>
 *
 * @author ruoyi
 */
public class DateUtil {

    /** 日期格式：yyyy-MM-dd */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 日期时间格式化器 */
    private static final SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat(DATETIME_FORMAT);

    /** 日期格式化器 */
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    /**
     * 获取当前日期的字符串格式（yyyy-MM-dd HH:mm:ss）
     *
     * @return 日期时间字符串
     * @throws ParseException 解析异常
     */
    public static String getNowToDayStr() throws ParseException {
        return getDateToDayStr(new Date());
    }

    /**
     * 将Date转换为日期时间字符串（yyyy-MM-dd HH:mm:ss）
     * <p>
     * 将时间部分设置为00:00:00后转换。
     *
     * @param date Date对象
     * @return yyyy-MM-dd HH:mm:ss 格式字符串
     * @throws ParseException 解析异常
     */
    public static String getDateToDayStr(Date date) throws ParseException {
        Date today = DATE_FORMATTER.parse(DATE_FORMATTER.format(date));
        return DATETIME_FORMATTER.format(today);
    }

    /**
     * 将Date转换为日期时间字符串
     *
     * @param date Date对象
     * @return yyyy-MM-dd HH:mm:ss 格式字符串
     * @throws ParseException 解析异常
     */
    public static String convertDateToString(Date date) throws ParseException {
        return DATETIME_FORMATTER.format(date);
    }

    /**
     * 将日期时间字符串转换为Date
     * <p>
     * 默认格式：yyyy-MM-dd HH:mm:ss
     *
     * @param str 日期时间字符串
     * @return Date对象
     * @throws ParseException 解析异常
     */
    public static Date convertStringToDate(String str) throws ParseException {
        return DATETIME_FORMATTER.parse(str);
    }

    /**
     * 将日期字符串转换为日期时间字符串
     * <p>
     * 将yyyy-MM-dd格式转换为yyyy-MM-dd HH:mm:ss格式
     *
     * @param data yyyy-MM-dd格式日期字符串
     * @return yyyy-MM-dd HH:mm:ss 格式字符串（时间部分为00:00:00）
     * @throws ParseException 解析异常
     */
    public static String convertDataToDateTime(String data) throws ParseException {
        return DATETIME_FORMATTER.format(DATE_FORMATTER.parse(data));
    }

    /**
     * 将时间戳转换为日期字符串
     *
     * @param timestamp 时间戳（毫秒）
     * @return yyyy-MM-dd HH:mm:ss 格式字符串
     */
    public static String getDate(long timestamp) {
        return DATETIME_FORMATTER.format(new Date(timestamp));
    }

    /**
     * 将日期时间字符串转换为Date
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss 格式字符串
     * @return Date对象
     * @throws ParseException 解析异常
     */
    public static Date getFormatDate(String dateStr) throws ParseException {
        return DATETIME_FORMATTER.parse(dateStr);
    }

    /**
     * 将Date转换为日期时间字符串
     *
     * @param date Date对象
     * @return yyyy-MM-dd HH:mm:ss 格式字符串
     * @throws ParseException 解析异常
     */
    public static String getFormatDateStr(Date date) throws ParseException {
        return DATETIME_FORMATTER.format(date);
    }
}
