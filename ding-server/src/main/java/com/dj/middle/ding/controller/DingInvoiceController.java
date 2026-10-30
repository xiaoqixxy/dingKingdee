package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingInvoice;
import com.dj.middle.ding.service.IDingInvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 发票接口控制器。 */
@Tag(name = "发票申请管理")
@Anonymous
@RestController
@RequestMapping("/ding/invoice")
public class DingInvoiceController extends BaseController
{
    /** 处理发票业务的服务组件。 */
    @Autowired
    private IDingInvoiceService dingInvoiceService;

    /** 按条件分页查询发票列表。 */
    @Operation(summary = "查询发票申请列表")
    @GetMapping("/list")
    public TableDataInfo list(DingInvoice dingInvoice)
    {
        // 查询发票申请列表。
        startPage();
        List<DingInvoice> list = dingInvoiceService.selectDingInvoiceList(dingInvoice);
        return getDataTable(list);
    }

    /** 获取发票申请详细信息。 */
    @Operation(summary = "获取发票申请详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingInvoiceService.selectDingInvoiceById(id));
    }

    /** 根据企业ID获取发票申请。 */
    @Operation(summary = "根据企业ID获取发票申请")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingInvoiceService.selectDingInvoiceByCorpId(dingCorpId));
    }

    /** 新增发票并返回保存结果。 */
    @Operation(summary = "新增发票申请")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:invoice:add')")
    @PostMapping
    public AjaxResult add(@RequestBody DingInvoice dingInvoice)
    {
        return toAjax(dingInvoiceService.insertDingInvoice(dingInvoice));
    }

    /** 更新指定发票并返回修改结果。 */
    @Operation(summary = "修改发票申请")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:invoice:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody DingInvoice dingInvoice)
    {
        return toAjax(dingInvoiceService.updateDingInvoice(dingInvoice));
    }

    /** 按主键删除发票。 */
    @Operation(summary = "删除发票申请")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:invoice:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingInvoiceService.deleteDingInvoiceByIds(ids));
    }
}
