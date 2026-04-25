package com.ruoyi.middle.ding.mapper;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingInvoice;

public interface DingInvoiceMapper
{
    public DingInvoice selectDingInvoiceById(Long id);

    public List<DingInvoice> selectDingInvoiceList(DingInvoice dingInvoice);

    public List<DingInvoice> selectDingInvoiceByCorpId(String dingCorpId);

    public int insertDingInvoice(DingInvoice dingInvoice);

    public int updateDingInvoice(DingInvoice dingInvoice);

    public int deleteDingInvoiceById(Long id);

    public int deleteDingInvoiceByIds(Long[] ids);
}
