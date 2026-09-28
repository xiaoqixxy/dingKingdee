package com.dj.middle.admin;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.dj.common.core.domain.AjaxResult;
@RestController
@RequestMapping("/admin/refunds")
public class RefundAdminController {
 private final RefundAdminService service;
 public RefundAdminController(RefundAdminService service){this.service=service;}
 @PostMapping
 @PreAuthorize("@ss.hasPermi('ding:refund:add')")
 public AjaxResult create(@RequestBody RefundRequest request){return AjaxResult.success(service.create(request));}
 @PostMapping("/{id}/cancel")
 @PreAuthorize("@ss.hasPermi('ding:refund:cancel')")
 public AjaxResult cancel(@PathVariable Long id){service.cancel(id);return AjaxResult.success();}
}
