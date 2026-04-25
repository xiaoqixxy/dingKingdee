package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingOrder;
import com.ruoyi.middle.ding.service.IDingOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "订单管理")
@Anonymous
@RestController
@RequestMapping("/ding/order")
public class DingOrderController extends BaseController
{
    @Autowired
    private IDingOrderService dingOrderService;

    @Operation(summary = "查询订单列表")
    @GetMapping("/list")
    public TableDataInfo list(DingOrder dingOrder)
    {
        startPage();
        List<DingOrder> list = dingOrderService.selectDingOrderList(dingOrder);
        return getDataTable(list);
    }

    @Operation(summary = "获取订单详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingOrderService.selectDingOrderById(id));
    }

    @Operation(summary = "根据订单号获取订单")
    @GetMapping(value = "/no/{orderNo}")
    public AjaxResult getByOrderNo(@PathVariable("orderNo") String orderNo)
    {
        return AjaxResult.success(dingOrderService.selectDingOrderByOrderNo(orderNo));
    }

    @Operation(summary = "新增订单")
    @PostMapping
    public AjaxResult add(@RequestBody DingOrder dingOrder)
    {
        return toAjax(dingOrderService.insertDingOrder(dingOrder));
    }

    @Operation(summary = "修改订单")
    @PutMapping
    public AjaxResult edit(@RequestBody DingOrder dingOrder)
    {
        return toAjax(dingOrderService.updateDingOrder(dingOrder));
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingOrderService.deleteDingOrderByIds(ids));
    }
}
