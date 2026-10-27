package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingTenant;
import com.dj.middle.ding.service.IDingTenantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 钉钉企业接口控制器。 */
@Tag(name = "租户企业管理")
@Anonymous
@RestController
@RequestMapping("/ding/tenant")
public class DingTenantController extends BaseController
{
    /** 处理钉钉企业业务的服务组件。 */
    @Autowired
    private IDingTenantService dingTenantService;

    /** 按条件分页查询钉钉企业列表。 */
    @Operation(summary = "查询租户企业列表")
    @GetMapping("/list")
    public TableDataInfo list(DingTenant dingTenant)
    {
        // 查询租户企业列表。
        startPage();
        List<DingTenant> list = dingTenantService.selectDingTenantList(dingTenant);
        return getDataTable(list);
    }

    /** 获取租户企业详细信息。 */
    @Operation(summary = "获取租户企业详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingTenantService.selectDingTenantById(id));
    }

    /** 根据企业ID获取租户。 */
    @Operation(summary = "根据企业ID获取租户")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingTenantService.selectDingTenantByCorpId(dingCorpId));
    }

    /** 新增钉钉企业并返回保存结果。 */
    @Operation(summary = "新增租户企业")
    @PostMapping
    public AjaxResult add(@RequestBody DingTenant dingTenant)
    {
        return toAjax(dingTenantService.insertDingTenant(dingTenant));
    }

    /** 更新指定钉钉企业并返回修改结果。 */
    @Operation(summary = "修改租户企业")
    @PutMapping
    public AjaxResult edit(@RequestBody DingTenant dingTenant)
    {
        return toAjax(dingTenantService.updateDingTenant(dingTenant));
    }

    /** 按主键删除钉钉企业。 */
    @Operation(summary = "删除租户企业")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingTenantService.deleteDingTenantByIds(ids));
    }
}
