package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingInvoice;

/** 发票服务契约。 */
public interface IDingInvoiceService
{
    /** 查询指定主键的发票记录。 */
    public DingInvoice selectDingInvoiceById(Long id);
    /** 查询符合筛选条件的发票列表。 */
    public List<DingInvoice> selectDingInvoiceList(DingInvoice dingInvoice);
    /** 查询指定企业的发票记录。 */
    public List<DingInvoice> selectDingInvoiceByCorpId(String dingCorpId);
    /** 新增发票记录。 */
    public int insertDingInvoice(DingInvoice dingInvoice);
    /** 更新发票记录。 */
    public int updateDingInvoice(DingInvoice dingInvoice);
    /** 删除指定主键的发票记录。 */
    public int deleteDingInvoiceById(Long id);
    /** 删除指定主键集合的发票记录。 */
    public int deleteDingInvoiceByIds(Long[] ids);
}
