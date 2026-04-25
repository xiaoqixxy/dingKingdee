package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingSubscription;
import com.ruoyi.middle.ding.service.IDingSubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "套餐订阅管理")
@Anonymous
@RestController
@RequestMapping("/ding/subscription")
public class DingSubscriptionController extends BaseController
{
    @Autowired
    private IDingSubscriptionService dingSubscriptionService;

    @Operation(summary = "查询套餐订阅列表")
    @GetMapping("/list")
    public TableDataInfo list(DingSubscription dingSubscription)
    {
        startPage();
        List<DingSubscription> list = dingSubscriptionService.selectDingSubscriptionList(dingSubscription);
        return getDataTable(list);
    }

    @Operation(summary = "获取套餐订阅详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingSubscriptionService.selectDingSubscriptionById(id));
    }

    @Operation(summary = "根据企业ID获取订阅")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSubscriptionService.selectDingSubscriptionByCorpId(dingCorpId));
    }

    @Operation(summary = "新增套餐订阅")
    @PostMapping
    public AjaxResult add(@RequestBody DingSubscription dingSubscription)
    {
        return toAjax(dingSubscriptionService.insertDingSubscription(dingSubscription));
    }

    @Operation(summary = "修改套餐订阅")
    @PutMapping
    public AjaxResult edit(@RequestBody DingSubscription dingSubscription)
    {
        return toAjax(dingSubscriptionService.updateDingSubscription(dingSubscription));
    }

    @Operation(summary = "删除套餐订阅")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingSubscriptionService.deleteDingSubscriptionByIds(ids));
    }
}
