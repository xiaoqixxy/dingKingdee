package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingTenant;
import com.ruoyi.middle.ding.service.IDingTenantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "租户企业管理")
@Anonymous
@RestController
@RequestMapping("/ding/tenant")
public class DingTenantController extends BaseController
{
    @Autowired
    private IDingTenantService dingTenantService;

    @Operation(summary = "查询租户企业列表")
    @GetMapping("/list")
    public TableDataInfo list(DingTenant dingTenant)
    {
        startPage();
        List<DingTenant> list = dingTenantService.selectDingTenantList(dingTenant);
        return getDataTable(list);
    }

    @Operation(summary = "获取租户企业详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingTenantService.selectDingTenantById(id));
    }

    @Operation(summary = "根据企业ID获取租户")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingTenantService.selectDingTenantByCorpId(dingCorpId));
    }

    @Operation(summary = "新增租户企业")
    @PostMapping
    public AjaxResult add(@RequestBody DingTenant dingTenant)
    {
        return toAjax(dingTenantService.insertDingTenant(dingTenant));
    }

    @Operation(summary = "修改租户企业")
    @PutMapping
    public AjaxResult edit(@RequestBody DingTenant dingTenant)
    {
        return toAjax(dingTenantService.updateDingTenant(dingTenant));
    }

    @Operation(summary = "删除租户企业")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingTenantService.deleteDingTenantByIds(ids));
    }
}
