package com.ruoyi.middle.kingdee.service;

import java.util.List;
import com.ruoyi.middle.kingdee.domain.KingdeeTableConfig;

/**
 * 金蝶表单配置 服务层
 */
public interface IKingdeeTableConfigService
{
    /**
     * 查询金蝶表单配置
     * 
     * @param configId 金蝶表单配置ID
     * @return 金蝶表单配置
     */
    public KingdeeTableConfig selectKingdeeTableConfigById(Long configId);

    /**
     * 查询金蝶表单配置列表
     * 
     * @param kingdeeTableConfig 金蝶表单配置
     * @return 金蝶表单配置集合
     */
    public List<KingdeeTableConfig> selectKingdeeTableConfigList(KingdeeTableConfig kingdeeTableConfig);

    /**
     * 新增金蝶表单配置
     * 
     * @param kingdeeTableConfig 金蝶表单配置
     * @return 结果
     */
    public int insertKingdeeTableConfig(KingdeeTableConfig kingdeeTableConfig);

    /**
     * 修改金蝶表单配置
     * 
     * @param kingdeeTableConfig 金蝶表单配置
     * @return 结果
     */
    public int updateKingdeeTableConfig(KingdeeTableConfig kingdeeTableConfig);

    /**
     * 删除金蝶表单配置
     * 
     * @param configId 金蝶表单配置ID
     * @return 结果
     */
    public int deleteKingdeeTableConfigById(Long configId);

    /**
     * 批量删除金蝶表单配置
     * 
     * @param configIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteKingdeeTableConfigByIds(Long[] configIds);

    /**
     * 校验表单标识是否唯一
     * 
     * @param formKey 表单标识
     * @return 结果
     */
    public boolean checkFormKeyUnique(String formKey);
}
