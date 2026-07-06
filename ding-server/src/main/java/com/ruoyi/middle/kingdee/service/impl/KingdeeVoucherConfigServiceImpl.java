package com.ruoyi.middle.kingdee.service.impl;

import java.util.List;
import java.util.UUID;

import com.ruoyi.middle.kingdee.domain.KingdeeVoucherConfig;
import com.ruoyi.middle.kingdee.mapper.KingdeeVoucherConfigMapper;
import com.ruoyi.middle.kingdee.service.IKingdeeVoucherConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 金蝶凭证配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-04
 */
@Service
public class KingdeeVoucherConfigServiceImpl implements IKingdeeVoucherConfigService
{
    @Autowired
    private KingdeeVoucherConfigMapper kingdeeVoucherConfigMapper;

    /**
     * 查询金蝶凭证配置
     * 
     * @param id 金蝶凭证配置主键
     * @return 金蝶凭证配置
     */
    @Override
    public KingdeeVoucherConfig selectKingdeeVoucherConfigById(String id)
    {
        return kingdeeVoucherConfigMapper.selectKingdeeVoucherConfigById(id);
    }

    /**
     * 查询金蝶凭证配置列表
     * 
     * @param kingdeeVoucherConfig 金蝶凭证配置
     * @return 金蝶凭证配置
     */
    @Override
    public List<KingdeeVoucherConfig> selectKingdeeVoucherConfigList(KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        return kingdeeVoucherConfigMapper.selectKingdeeVoucherConfigList(kingdeeVoucherConfig);
    }

    /**
     * 新增金蝶凭证配置
     * 
     * @param kingdeeVoucherConfig 金蝶凭证配置
     * @return 结果
     */
    @Override
    public int insertKingdeeVoucherConfig(KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        // 生成UUID作为主键
        if (kingdeeVoucherConfig.getId() == null || kingdeeVoucherConfig.getId().isEmpty()) {
            kingdeeVoucherConfig.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        return kingdeeVoucherConfigMapper.insertKingdeeVoucherConfig(kingdeeVoucherConfig);
    }

    /**
     * 修改金蝶凭证配置
     * 
     * @param kingdeeVoucherConfig 金蝶凭证配置
     * @return 结果
     */
    @Override
    public int updateKingdeeVoucherConfig(KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        return kingdeeVoucherConfigMapper.updateKingdeeVoucherConfig(kingdeeVoucherConfig);
    }

    /**
     * 批量删除金蝶凭证配置
     * 
     * @param ids 需要删除的金蝶凭证配置主键
     * @return 结果
     */
    @Override
    public int deleteKingdeeVoucherConfigByIds(String[] ids)
    {
        return kingdeeVoucherConfigMapper.deleteKingdeeVoucherConfigByIds(ids);
    }

    /**
     * 删除金蝶凭证配置信息
     * 
     * @param id 金蝶凭证配置主键
     * @return 结果
     */
    @Override
    public int deleteKingdeeVoucherConfigById(String id)
    {
        return kingdeeVoucherConfigMapper.deleteKingdeeVoucherConfigById(id);
    }

    /**
     * 根据钉钉企业ID查询凭证配置
     * 
     * @param dingCorpId 钉钉企业ID
     * @return 金蝶凭证配置
     */
    @Override
    public KingdeeVoucherConfig selectKingdeeVoucherConfigByCorpId(String dingCorpId)
    {
        return kingdeeVoucherConfigMapper.selectKingdeeVoucherConfigByCorpId(dingCorpId);
    }

    /**
     * 根据钉钉企业ID查询凭证配置列表
     * 
     * @param dingCorpId 钉钉企业ID
     * @return 金蝶凭证配置集合
     */
    @Override
    public List<KingdeeVoucherConfig> selectKingdeeVoucherConfigListByCorpId(String dingCorpId)
    {
        return kingdeeVoucherConfigMapper.selectKingdeeVoucherConfigListByCorpId(dingCorpId);
    }

    /**
     * 根据凭证名称和钉钉企业ID查询凭证配置
     */
    @Override
    public KingdeeVoucherConfig selectKingdeeVoucherConfigByNameAndCorpId(String name, String dingCorpId)
    {
        return kingdeeVoucherConfigMapper.selectKingdeeVoucherConfigByNameAndCorpId(name, dingCorpId);
    }
}
