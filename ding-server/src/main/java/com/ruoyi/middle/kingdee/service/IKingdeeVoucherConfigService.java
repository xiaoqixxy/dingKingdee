package com.ruoyi.middle.kingdee.service;

import com.ruoyi.middle.kingdee.domain.KingdeeVoucherConfig;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author ruoyi
 * @date 2026-07-04
 */
public interface IKingdeeVoucherConfigService 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public KingdeeVoucherConfig selectKingdeeVoucherConfigById(String id);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param kingdeeVoucherConfig 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<KingdeeVoucherConfig> selectKingdeeVoucherConfigList(KingdeeVoucherConfig kingdeeVoucherConfig);

    /**
     * 新增【请填写功能名称】
     * 
     * @param kingdeeVoucherConfig 【请填写功能名称】
     * @return 结果
     */
    public int insertKingdeeVoucherConfig(KingdeeVoucherConfig kingdeeVoucherConfig);

    /**
     * 修改【请填写功能名称】
     * 
     * @param kingdeeVoucherConfig 【请填写功能名称】
     * @return 结果
     */
    public int updateKingdeeVoucherConfig(KingdeeVoucherConfig kingdeeVoucherConfig);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteKingdeeVoucherConfigByIds(String[] ids);

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteKingdeeVoucherConfigById(String id);

    /**
     * 根据钉钉企业ID查询凭证配置
     * 
     * @param dingCorpId 钉钉企业ID
     * @return 金蝶凭证配置
     */
    public KingdeeVoucherConfig selectKingdeeVoucherConfigByCorpId(String dingCorpId);

    /**
     * 根据钉钉企业ID查询凭证配置列表
     * 
     * @param dingCorpId 钉钉企业ID
     * @return 金蝶凭证配置集合
     */
    public List<KingdeeVoucherConfig> selectKingdeeVoucherConfigListByCorpId(String dingCorpId);

    /**
     * 根据凭证名称和钉钉企业ID查询凭证配置
     * 
     * @param name 凭证名称
     * @param dingCorpId 钉钉企业ID
     * @return 金蝶凭证配置
     */
    public KingdeeVoucherConfig selectKingdeeVoucherConfigByNameAndCorpId(String name, String dingCorpId);
}
