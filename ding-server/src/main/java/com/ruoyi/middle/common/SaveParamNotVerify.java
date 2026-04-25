package com.ruoyi.middle.common;

import com.kingdee.bos.webapi.entity.SaveParam;

/**
 * 不验证基础数据的保存参数包装类
 * <p>
 * 继承自金蝶SDK的SaveParam，用于保存单据时设置特殊参数：
 * <ul>
 *   <li>IsVerifyBaseDataField = false - 不验证基础数据字段</li>
 *   <li>IsAutoSubmitAndAudit = true - 自动提交并审核</li>
 * </ul>
 * <p>
 * 使用场景：当保存的单据中包含不存在于金蝶系统中的基础数据（如新建的物料、部门等）时，
 * 使用此类可以避免保存失败。
 *
 * @author ruoyi
 * @param <T> 数据类型
 * @see com.kingdee.bos.webapi.entity.SaveParam
 * @see com.ruoyi.middle.util.KingdeeK3CloudUtil#saveAndSubmit(Object, String, boolean)
 */
public class SaveParamNotVerify<T> extends SaveParam<T> {
    
    /**
     * 是否验证基础数据字段
     * <p>
     * 设置为false时，金蝶API不会验证基础数据字段的有效性。
     */
    Boolean IsVerifyBaseDataField;
    
    /**
     * 是否自动提交并审核
     * <p>
     * 设置为true时，保存成功后自动执行提交和审核操作。
     */
    Boolean IsAutoSubmitAndAudit;

    /**
     * 构造函数
     *
     * @param data 单据数据对象
     */
    public SaveParamNotVerify(T data) {
        super(data);
        IsVerifyBaseDataField = false;
        IsAutoSubmitAndAudit = true;
    }
}
