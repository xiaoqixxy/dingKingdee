package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingInvoice;
import com.ruoyi.middle.ding.service.IDingInvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "发票申请管理")
@Anonymous
@RestController
@RequestMapping("/ding/invoice")
public class DingInvoiceController extends BaseController
{
    @Autowired
    private IDingInvoiceService dingInvoiceService;

    @Operation(summary = "查询发票申请列表")
    @GetMapping("/list")
    public TableDataInfo list(DingInvoice dingInvoice)
    {
        startPage();
        List<DingInvoice> list = dingInvoiceService.selectDingInvoiceList(dingInvoice);
        return getDataTable(list);
    }

    @Operation(summary = "获取发票申请详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingInvoiceService.selectDingInvoiceById(id));
    }

    @Operation(summary = "根据企业ID获取发票申请")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingInvoiceService.selectDingInvoiceByCorpId(dingCorpId));
    }

    @Operation(summary = "新增发票申请")
    @PostMapping
    public AjaxResult add(@RequestBody DingInvoice dingInvoice)
    {
        return toAjax(dingInvoiceService.insertDingInvoice(dingInvoice));
    }

    @Operation(summary = "修改发票申请")
    @PutMapping
    public AjaxResult edit(@RequestBody DingInvoice dingInvoice)
    {
        return toAjax(dingInvoiceService.updateDingInvoice(dingInvoice));
    }

    @Operation(summary = "删除发票申请")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingInvoiceService.deleteDingInvoiceByIds(ids));
    }
}
