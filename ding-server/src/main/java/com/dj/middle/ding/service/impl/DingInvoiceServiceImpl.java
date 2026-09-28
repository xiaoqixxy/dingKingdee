package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingInvoice;
import com.dj.middle.ding.mapper.DingInvoiceMapper;
import com.dj.middle.ding.service.IDingInvoiceService;

@Service
public class DingInvoiceServiceImpl implements IDingInvoiceService
{
    @Autowired
    private DingInvoiceMapper dingInvoiceMapper;

    @Override
    public DingInvoice selectDingInvoiceById(Long id)
    {
        return dingInvoiceMapper.selectDingInvoiceById(id);
    }

    @Override
    public List<DingInvoice> selectDingInvoiceList(DingInvoice dingInvoice)
    {
        return dingInvoiceMapper.selectDingInvoiceList(dingInvoice);
    }

    @Override
    public List<DingInvoice> selectDingInvoiceByCorpId(String dingCorpId)
    {
        return dingInvoiceMapper.selectDingInvoiceByCorpId(dingCorpId);
    }

    @Override
    public int insertDingInvoice(DingInvoice dingInvoice)
    {
        dingInvoice.setCreateTime(DateUtils.getNowDate());
        return dingInvoiceMapper.insertDingInvoice(dingInvoice);
    }

    @Override
    public int updateDingInvoice(DingInvoice dingInvoice)
    {
        dingInvoice.setUpdateTime(DateUtils.getNowDate());
        return dingInvoiceMapper.updateDingInvoice(dingInvoice);
    }

    @Override
    public int deleteDingInvoiceById(Long id)
    {
        return dingInvoiceMapper.deleteDingInvoiceById(id);
    }

    @Override
    public int deleteDingInvoiceByIds(Long[] ids)
    {
        return dingInvoiceMapper.deleteDingInvoiceByIds(ids);
    }
}
