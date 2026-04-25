package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingPaymentLog;
import com.ruoyi.middle.ding.service.IDingPaymentLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "支付日志管理")
@Anonymous
@RestController
@RequestMapping("/ding/paymentLog")
public class DingPaymentLogController extends BaseController
{
    @Autowired
    private IDingPaymentLogService dingPaymentLogService;

    @Operation(summary = "查询支付日志列表")
    @GetMapping("/list")
    public TableDataInfo list(DingPaymentLog dingPaymentLog)
    {
        startPage();
        List<DingPaymentLog> list = dingPaymentLogService.selectDingPaymentLogList(dingPaymentLog);
        return getDataTable(list);
    }

    @Operation(summary = "获取支付日志详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingPaymentLogService.selectDingPaymentLogById(id));
    }

    @Operation(summary = "根据订单号获取支付日志")
    @GetMapping(value = "/order/{orderNo}")
    public AjaxResult getByOrderNo(@PathVariable("orderNo") String orderNo)
    {
        return AjaxResult.success(dingPaymentLogService.selectDingPaymentLogByOrderNo(orderNo));
    }

    @Operation(summary = "新增支付日志")
    @PostMapping
    public AjaxResult add(@RequestBody DingPaymentLog dingPaymentLog)
    {
        return toAjax(dingPaymentLogService.insertDingPaymentLog(dingPaymentLog));
    }

    @Operation(summary = "修改支付日志")
    @PutMapping
    public AjaxResult edit(@RequestBody DingPaymentLog dingPaymentLog)
    {
        return toAjax(dingPaymentLogService.updateDingPaymentLog(dingPaymentLog));
    }

    @Operation(summary = "删除支付日志")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingPaymentLogService.deleteDingPaymentLogByIds(ids));
    }
}
