package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingPackageChangeLog;
import com.ruoyi.middle.ding.service.IDingPackageChangeLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "套餐变更记录管理")
@Anonymous
@RestController
@RequestMapping("/ding/packageChangeLog")
public class DingPackageChangeLogController extends BaseController
{
    @Autowired
    private IDingPackageChangeLogService dingPackageChangeLogService;

    @Operation(summary = "查询套餐变更记录列表")
    @GetMapping("/list")
    public TableDataInfo list(DingPackageChangeLog dingPackageChangeLog)
    {
        startPage();
        List<DingPackageChangeLog> list = dingPackageChangeLogService.selectDingPackageChangeLogList(dingPackageChangeLog);
        return getDataTable(list);
    }

    @Operation(summary = "获取套餐变更记录详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingPackageChangeLogService.selectDingPackageChangeLogById(id));
    }

    @Operation(summary = "根据企业ID获取套餐变更记录")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingPackageChangeLogService.selectDingPackageChangeLogByCorpId(dingCorpId));
    }

    @Operation(summary = "新增套餐变更记录")
    @PostMapping
    public AjaxResult add(@RequestBody DingPackageChangeLog dingPackageChangeLog)
    {
        return toAjax(dingPackageChangeLogService.insertDingPackageChangeLog(dingPackageChangeLog));
    }

    @Operation(summary = "删除套餐变更记录")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingPackageChangeLogService.deleteDingPackageChangeLogByIds(ids));
    }
}
