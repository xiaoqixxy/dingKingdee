package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingPaymentLog;
import com.dj.middle.ding.service.IDingPaymentLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 支付日志接口控制器。 */
@Tag(name = "支付日志管理")
@Anonymous
@RestController
@RequestMapping("/ding/paymentLog")
public class DingPaymentLogController extends BaseController
{
    /** 处理支付日志业务的服务组件。 */
    @Autowired
    private IDingPaymentLogService dingPaymentLogService;

    /** 按条件分页查询支付日志列表。 */
    @Operation(summary = "查询支付日志列表")
    @GetMapping("/list")
    public TableDataInfo list(DingPaymentLog dingPaymentLog)
    {
        // 查询支付日志列表。
        startPage();
        List<DingPaymentLog> list = dingPaymentLogService.selectDingPaymentLogList(dingPaymentLog);
        return getDataTable(list);
    }

    /** 获取支付日志详细信息。 */
    @Operation(summary = "获取支付日志详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingPaymentLogService.selectDingPaymentLogById(id));
    }

    /** 根据订单号获取支付日志。 */
    @Operation(summary = "根据订单号获取支付日志")
    @GetMapping(value = "/order/{orderNo}")
    public AjaxResult getByOrderNo(@PathVariable("orderNo") String orderNo)
    {
        return AjaxResult.success(dingPaymentLogService.selectDingPaymentLogByOrderNo(orderNo));
    }

    /** 新增支付日志并返回保存结果。 */
    @Operation(summary = "新增支付日志")
    @PostMapping
    public AjaxResult add(@RequestBody DingPaymentLog dingPaymentLog)
    {
        return toAjax(dingPaymentLogService.insertDingPaymentLog(dingPaymentLog));
    }

    /** 更新指定支付日志并返回修改结果。 */
    @Operation(summary = "修改支付日志")
    @PutMapping
    public AjaxResult edit(@RequestBody DingPaymentLog dingPaymentLog)
    {
        return toAjax(dingPaymentLogService.updateDingPaymentLog(dingPaymentLog));
    }

    /** 按主键删除支付日志。 */
    @Operation(summary = "删除支付日志")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingPaymentLogService.deleteDingPaymentLogByIds(ids));
    }
}
