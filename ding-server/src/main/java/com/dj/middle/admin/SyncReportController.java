package com.dj.middle.admin;

import com.dj.common.core.domain.AjaxResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 同步报表查询入口。 */
@RestController
@RequestMapping("/admin/sync-report")
public class SyncReportController {
    /** 业务处理服务。 */
    private final SyncReportService service;
    /** 初始化当前组件并保存构造参数。 */
    public SyncReportController(SyncReportService service) { this.service=service; }

    /** 接收统计周期和日志口径并返回同步报表。 */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('ding:execution:list')")
    public AjaxResult report(@RequestParam(defaultValue="30") int days,
                             @RequestParam(defaultValue="BATCH") String scope) {
        return AjaxResult.success(service.report(days,scope));
    }
}
