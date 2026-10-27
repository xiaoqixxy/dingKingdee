package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingPackageChangeLog;
import com.dj.middle.ding.service.IDingPackageChangeLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 套餐变更日志接口控制器。 */
@Tag(name = "套餐变更记录管理")
@Anonymous
@RestController
@RequestMapping("/ding/packageChangeLog")
public class DingPackageChangeLogController extends BaseController
{
    /** 处理套餐变更日志业务的服务组件。 */
    @Autowired
    private IDingPackageChangeLogService dingPackageChangeLogService;

    /** 按条件分页查询套餐变更日志列表。 */
    @Operation(summary = "查询套餐变更记录列表")
    @GetMapping("/list")
    public TableDataInfo list(DingPackageChangeLog dingPackageChangeLog)
    {
        // 查询套餐变更记录列表。
        startPage();
        List<DingPackageChangeLog> list = dingPackageChangeLogService.selectDingPackageChangeLogList(dingPackageChangeLog);
        return getDataTable(list);
    }

    /** 获取套餐变更记录详细信息。 */
    @Operation(summary = "获取套餐变更记录详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingPackageChangeLogService.selectDingPackageChangeLogById(id));
    }

    /** 根据企业ID获取套餐变更记录。 */
    @Operation(summary = "根据企业ID获取套餐变更记录")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingPackageChangeLogService.selectDingPackageChangeLogByCorpId(dingCorpId));
    }

    /** 新增套餐变更日志并返回保存结果。 */
    @Operation(summary = "新增套餐变更记录")
    @PostMapping
    public AjaxResult add(@RequestBody DingPackageChangeLog dingPackageChangeLog)
    {
        return toAjax(dingPackageChangeLogService.insertDingPackageChangeLog(dingPackageChangeLog));
    }

    /** 按主键删除套餐变更日志。 */
    @Operation(summary = "删除套餐变更记录")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingPackageChangeLogService.deleteDingPackageChangeLogByIds(ids));
    }
}
