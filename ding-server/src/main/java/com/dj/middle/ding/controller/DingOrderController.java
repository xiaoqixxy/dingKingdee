package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingOrder;
import com.dj.middle.ding.service.IDingOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 订单接口控制器。 */
@Tag(name = "订单管理")
@Anonymous
@RestController
@RequestMapping("/ding/order")
public class DingOrderController extends BaseController
{
    /** 处理订单业务的服务组件。 */
    @Autowired
    private IDingOrderService dingOrderService;

    /** 按条件分页查询订单列表。 */
    @Operation(summary = "查询订单列表")
    @GetMapping("/list")
    public TableDataInfo list(DingOrder dingOrder)
    {
        // 查询订单列表。
        startPage();
        List<DingOrder> list = dingOrderService.selectDingOrderList(dingOrder);
        return getDataTable(list);
    }

    /** 获取订单详细信息。 */
    @Operation(summary = "获取订单详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingOrderService.selectDingOrderById(id));
    }

    /** 根据订单号获取订单。 */
    @Operation(summary = "根据订单号获取订单")
    @GetMapping(value = "/no/{orderNo}")
    public AjaxResult getByOrderNo(@PathVariable("orderNo") String orderNo)
    {
        return AjaxResult.success(dingOrderService.selectDingOrderByOrderNo(orderNo));
    }

    /** 新增订单并返回保存结果。 */
    @Operation(summary = "新增订单")
    @PostMapping
    public AjaxResult add(@RequestBody DingOrder dingOrder)
    {
        return toAjax(dingOrderService.insertDingOrder(dingOrder));
    }

    /** 更新指定订单并返回修改结果。 */
    @Operation(summary = "修改订单")
    @PutMapping
    public AjaxResult edit(@RequestBody DingOrder dingOrder)
    {
        return toAjax(dingOrderService.updateDingOrder(dingOrder));
    }

    /** 按主键删除订单。 */
    @Operation(summary = "删除订单")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingOrderService.deleteDingOrderByIds(ids));
    }
}
