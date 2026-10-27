package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingInvoice;
import com.dj.middle.ding.mapper.DingInvoiceMapper;
import com.dj.middle.ding.service.IDingInvoiceService;

/** 发票业务实现。 */
@Service
public class DingInvoiceServiceImpl implements IDingInvoiceService
{
    /** 提供发票数据访问能力的映射接口。 */
    @Autowired
   private DingInvoiceMapper dingInvoiceMapper;

    /** 查询指定主键的发票记录。 */
    @Override
    public DingInvoice selectDingInvoiceById(Long id)
    {
        return dingInvoiceMapper.selectDingInvoiceById(id);
    }

    /** 查询符合筛选条件的发票列表。 */
    @Override
    public List<DingInvoice> selectDingInvoiceList(DingInvoice dingInvoice)
    {
        return dingInvoiceMapper.selectDingInvoiceList(dingInvoice);
    }

    /** 查询指定企业的发票记录。 */
    @Override
    public List<DingInvoice> selectDingInvoiceByCorpId(String dingCorpId)
    {
        return dingInvoiceMapper.selectDingInvoiceByCorpId(dingCorpId);
    }

    /** 新增发票记录。 */
    @Override
    public int insertDingInvoice(DingInvoice dingInvoice)
    {
        dingInvoice.setCreateTime(DateUtils.getNowDate());
        return dingInvoiceMapper.insertDingInvoice(dingInvoice);
    }

    /** 更新发票记录。 */
    @Override
    public int updateDingInvoice(DingInvoice dingInvoice)
    {
        dingInvoice.setUpdateTime(DateUtils.getNowDate());
        return dingInvoiceMapper.updateDingInvoice(dingInvoice);
    }

    /** 删除指定主键的发票记录。 */
    @Override
    public int deleteDingInvoiceById(Long id)
    {
        return dingInvoiceMapper.deleteDingInvoiceById(id);
    }

    /** 删除指定主键集合的发票记录。 */
    @Override
    public int deleteDingInvoiceByIds(Long[] ids)
    {
        return dingInvoiceMapper.deleteDingInvoiceByIds(ids);
    }
}
