package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingSyncLog;
import com.dj.middle.ding.service.IDingSyncLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 同步日志接口控制器。 */
@Tag(name = "数据同步日志管理")
@Anonymous
@RestController
@RequestMapping("/ding/syncLog")
public class DingSyncLogController extends BaseController
{
    /** 处理同步日志业务的服务组件。 */
    @Autowired
    private IDingSyncLogService dingSyncLogService;

    /** 按条件分页查询同步日志列表。 */
    @Operation(summary = "查询数据同步日志列表")
    @GetMapping("/list")
    public TableDataInfo list(DingSyncLog dingSyncLog)
    {
        // 查询数据同步日志列表。
        startPage();
        List<DingSyncLog> list = dingSyncLogService.selectDingSyncLogList(dingSyncLog);
        return getDataTable(list);
    }

    /** 获取数据同步日志详细信息。 */
    @Operation(summary = "获取数据同步日志详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingSyncLogService.selectDingSyncLogById(id));
    }

    /** 根据企业ID获取同步日志。 */
    @Operation(summary = "根据企业ID获取同步日志")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSyncLogService.selectDingSyncLogByCorpId(dingCorpId));
    }

    /** 新增同步日志并返回保存结果。 */
    @Operation(summary = "新增数据同步日志")
    @PostMapping
    public AjaxResult add(@RequestBody DingSyncLog dingSyncLog)
    {
        return toAjax(dingSyncLogService.insertDingSyncLog(dingSyncLog));
    }

    /** 按主键删除同步日志。 */
    @Operation(summary = "删除数据同步日志")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingSyncLogService.deleteDingSyncLogByIds(ids));
    }

    /** 获取企业本月同步数据总量。 */
    @Operation(summary = "获取企业本月同步数据总量")
    @GetMapping(value = "/monthCount/{dingCorpId}/{month}")
    public AjaxResult getMonthCount(@PathVariable("dingCorpId") String dingCorpId, @PathVariable("month") String month)
    {
        return AjaxResult.success(dingSyncLogService.sumSyncCountByCorpIdAndMonth(dingCorpId, month));
    }
}
