package com.dj.middle.admin;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.dj.common.core.domain.AjaxResult;
/** 退款申请与撤销的后台入口。 */
@RestController
@RequestMapping("/admin/refunds")
public class RefundAdminController {
 /** 业务处理服务。 */
 private final RefundAdminService service;
 /** 初始化当前组件并保存构造参数。 */
 public RefundAdminController(RefundAdminService service){this.service=service;}
 /** 校验权限后创建后台退款申请。 */
 @PostMapping
 @PreAuthorize("@ss.hasPermi('ding:refund:add')")
 public AjaxResult create(@RequestBody RefundRequest request){return AjaxResult.success(service.create(request));}
 /** 校验权限后撤销允许撤销的退款申请。 */
 @PostMapping("/{id}/cancel")
 @PreAuthorize("@ss.hasPermi('ding:refund:cancel')")
 public AjaxResult cancel(@PathVariable Long id){service.cancel(id);return AjaxResult.success();}
}
