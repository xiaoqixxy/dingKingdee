package com.dj.middle.admin;

import com.dj.common.core.domain.AjaxResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sync-report")
public class SyncReportController {
    private final SyncReportService service;
    public SyncReportController(SyncReportService service) { this.service=service; }

    @GetMapping
    @PreAuthorize("@ss.hasPermi('ding:execution:list')")
    public AjaxResult report(@RequestParam(defaultValue="30") int days,
                             @RequestParam(defaultValue="BATCH") String scope) {
        return AjaxResult.success(service.report(days,scope));
    }
}
