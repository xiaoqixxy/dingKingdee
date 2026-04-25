package com.ruoyi.middle.kingdee.constant;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * 金蝶字段类型常量定义
 * <p>
 * 定义金蝶云星空业务单据中字段类型的常量值，
 * 用于在字段类型转换和判断时使用。
 *
 * @author ruoyi
 * @see com.ruoyi.middle.kingdee.util.ContoryUtil
 */
public class FieldTypeConstant {

    /**
     * 字符串类型
     * <p>
     * 金蝶字段类型值：6
     */
    public static final int TYPE_STRING = 6;

    /**
     * 枚举类型（元数据）
     * <p>
     * 金蝶字段类型值：40
     * 枚举值直接从字段元数据中获取
     */
    public static final int TYPE_ENUM = 40;

    /**
     * 枚举类型（需查询）
     * <p>
     * 金蝶字段类型值：9
     * 枚举值需要通过额外查询获取
     */
    public static final int TYPE_ENUM_OBJ = 9;

    /**
     * 机构类型
     * <p>
     * 金蝶字段类型值：7
     * 查询时需要追加 .FName 获取名称
     */
    public static final int TYPE_ORG = 7;

    /**
     * 人员类型
     * <p>
     * 金蝶字段类型值：27
     * 查询时需要追加 .FName 获取名称
     */
    public static final int TYPE_PERSON = 27;

    /**
     * 数字类型
     * <p>
     * 金蝶字段类型值：21
     */
    public static final int TYPE_NUMBER = 21;

    /**
     * 日期类型（备用）
     * <p>
     * 金蝶字段类型值：4
     */
    public static final int TYPE_DATE = 4;

    /**
     * 日期类型
     * <p>
     * 金蝶字段类型值：61
     * 用于 ContoryUtil 中的类型判断
     */
    public static final int FieldType_DATE = 61;

    /**
     * 关联表单类型
     * <p>
     * 金蝶字段类型值：127
     * 查询时需要追加 .FName 获取关联对象名称
     */
    public static final int FieldType_OBJ = 127;
    public static final int FieldType_BOOLEN = 8;
    public static final List<Integer> ElementType_OBJ_FNAME =  List.of(16,44);
    public static final List<Integer> ElementType_OBJ = List.of(30,31,44);
}
