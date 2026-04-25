package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingSyncLog;
import com.ruoyi.middle.ding.service.IDingSyncLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "数据同步日志管理")
@Anonymous
@RestController
@RequestMapping("/ding/syncLog")
public class DingSyncLogController extends BaseController
{
    @Autowired
    private IDingSyncLogService dingSyncLogService;

    @Operation(summary = "查询数据同步日志列表")
    @GetMapping("/list")
    public TableDataInfo list(DingSyncLog dingSyncLog)
    {
        startPage();
        List<DingSyncLog> list = dingSyncLogService.selectDingSyncLogList(dingSyncLog);
        return getDataTable(list);
    }

    @Operation(summary = "获取数据同步日志详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingSyncLogService.selectDingSyncLogById(id));
    }

    @Operation(summary = "根据企业ID获取同步日志")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSyncLogService.selectDingSyncLogByCorpId(dingCorpId));
    }

    @Operation(summary = "新增数据同步日志")
    @PostMapping
    public AjaxResult add(@RequestBody DingSyncLog dingSyncLog)
    {
        return toAjax(dingSyncLogService.insertDingSyncLog(dingSyncLog));
    }

    @Operation(summary = "删除数据同步日志")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingSyncLogService.deleteDingSyncLogByIds(ids));
    }

    @Operation(summary = "获取企业本月同步数据总量")
    @GetMapping(value = "/monthCount/{dingCorpId}/{month}")
    public AjaxResult getMonthCount(@PathVariable("dingCorpId") String dingCorpId, @PathVariable("month") String month)
    {
        return AjaxResult.success(dingSyncLogService.sumSyncCountByCorpIdAndMonth(dingCorpId, month));
    }
}
