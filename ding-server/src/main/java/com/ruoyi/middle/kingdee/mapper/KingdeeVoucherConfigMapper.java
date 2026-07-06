package com.ruoyi.middle.kingdee.mapper;

import com.ruoyi.middle.kingdee.domain.KingdeeVoucherConfig;
import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2026-07-04
 */
public interface KingdeeVoucherConfigMapper 
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
     * 删除【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteKingdeeVoucherConfigById(String id);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteKingdeeVoucherConfigByIds(String[] ids);
}
