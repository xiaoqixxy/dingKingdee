package com.ruoyi.middle.kingdee.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.kingdee.domain.KingdeeTableConfig;
import com.ruoyi.middle.kingdee.mapper.KingdeeTableConfigMapper;
import com.ruoyi.middle.kingdee.service.IKingdeeTableConfigService;

/**
 * 金蝶表单配置 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class KingdeeTableConfigServiceImpl implements IKingdeeTableConfigService
{

    @Autowired
    private KingdeeTableConfigMapper kingdeeTableConfigMapper;

    /**
     * 查询金蝶表单配置
     * 
     * @param configId 金蝶表单配置ID
     * @return 金蝶表单配置
     */
    @Override
    public KingdeeTableConfig selectKingdeeTableConfigById(Long configId)
    {
        return kingdeeTableConfigMapper.selectKingdeeTableConfigById(configId);
    }

    /**
     * 查询金蝶表单配置列表
     * 
     * @param kingdeeTableConfig 金蝶表单配置
     * @return 金蝶表单配置
     */
    @Override
    public List<KingdeeTableConfig> selectKingdeeTableConfigList(KingdeeTableConfig kingdeeTableConfig)
    {
        return kingdeeTableConfigMapper.selectKingdeeTableConfigList(kingdeeTableConfig);
    }

    /**
     * 新增金蝶表单配置
     * 
     * @param kingdeeTableConfig 金蝶表单配置
     * @return 结果
     */
    @Override
    public int insertKingdeeTableConfig(KingdeeTableConfig kingdeeTableConfig)
    {
        kingdeeTableConfig.setCreateTime(DateUtils.getNowDate());
        return kingdeeTableConfigMapper.insertKingdeeTableConfig(kingdeeTableConfig);
    }

    /**
     * 修改金蝶表单配置
     * 
     * @param kingdeeTableConfig 金蝶表单配置
     * @return 结果
     */
    @Override
    public int updateKingdeeTableConfig(KingdeeTableConfig kingdeeTableConfig)
    {
        kingdeeTableConfig.setUpdateTime(DateUtils.getNowDate());
        return kingdeeTableConfigMapper.updateKingdeeTableConfig(kingdeeTableConfig);
    }

    /**
     * 删除金蝶表单配置
     * 
     * @param configId 金蝶表单配置ID
     * @return 结果
     */
    @Override
    public int deleteKingdeeTableConfigById(Long configId)
    {
        return kingdeeTableConfigMapper.deleteKingdeeTableConfigById(configId);
    }

    /**
     * 批量删除金蝶表单配置
     * 
     * @param configIds 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteKingdeeTableConfigByIds(Long[] configIds)
    {
        return kingdeeTableConfigMapper.deleteKingdeeTableConfigByIds(configIds);
    }

    /**
     * 校验表单标识是否唯一
     * 
     * @param formKey 表单标识
     * @return 结果
     */
    @Override
    public boolean checkFormKeyUnique(String formKey)
    {
        KingdeeTableConfig config = kingdeeTableConfigMapper.checkFormKeyUnique(formKey);
        return config == null;
    }
}
