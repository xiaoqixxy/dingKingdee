package com.ruoyi.middle.kingdee.service.impl;

import java.util.List;

import com.ruoyi.middle.kingdee.domain.KingdeeVoucherConfig;
import com.ruoyi.middle.kingdee.mapper.KingdeeVoucherConfigMapper;
import com.ruoyi.middle.kingdee.service.IKingdeeVoucherConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 【请填写功能名称】Service业务层处理
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
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public KingdeeVoucherConfig selectKingdeeVoucherConfigById(String id)
    {
        return kingdeeVoucherConfigMapper.selectKingdeeVoucherConfigById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param kingdeeVoucherConfig 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<KingdeeVoucherConfig> selectKingdeeVoucherConfigList(KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        return kingdeeVoucherConfigMapper.selectKingdeeVoucherConfigList(kingdeeVoucherConfig);
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param kingdeeVoucherConfig 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertKingdeeVoucherConfig(KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        return kingdeeVoucherConfigMapper.insertKingdeeVoucherConfig(kingdeeVoucherConfig);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param kingdeeVoucherConfig 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateKingdeeVoucherConfig(KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        return kingdeeVoucherConfigMapper.updateKingdeeVoucherConfig(kingdeeVoucherConfig);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteKingdeeVoucherConfigByIds(String[] ids)
    {
        return kingdeeVoucherConfigMapper.deleteKingdeeVoucherConfigByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteKingdeeVoucherConfigById(String id)
    {
        return kingdeeVoucherConfigMapper.deleteKingdeeVoucherConfigById(id);
    }
}
