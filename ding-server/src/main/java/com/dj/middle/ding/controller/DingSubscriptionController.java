package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingSubscription;
import com.dj.middle.ding.service.IDingSubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 企业订阅接口控制器。 */
@Tag(name = "套餐订阅管理")
@Anonymous
@RestController
@RequestMapping("/ding/subscription")
public class DingSubscriptionController extends BaseController
{
    /** 处理企业订阅业务的服务组件。 */
    @Autowired
    private IDingSubscriptionService dingSubscriptionService;

    /** 按条件分页查询企业订阅列表。 */
    @Operation(summary = "查询套餐订阅列表")
    @GetMapping("/list")
    public TableDataInfo list(DingSubscription dingSubscription)
    {
        // 查询套餐订阅列表。
        startPage();
        List<DingSubscription> list = dingSubscriptionService.selectDingSubscriptionList(dingSubscription);
        return getDataTable(list);
    }

    /** 获取套餐订阅详细信息。 */
    @Operation(summary = "获取套餐订阅详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingSubscriptionService.selectDingSubscriptionById(id));
    }

    /** 根据企业ID获取订阅。 */
    @Operation(summary = "根据企业ID获取订阅")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSubscriptionService.selectDingSubscriptionByCorpId(dingCorpId));
    }

    /** 新增企业订阅并返回保存结果。 */
    @Operation(summary = "新增套餐订阅")
    @PostMapping
    public AjaxResult add(@RequestBody DingSubscription dingSubscription)
    {
        return toAjax(dingSubscriptionService.insertDingSubscription(dingSubscription));
    }

    /** 更新指定企业订阅并返回修改结果。 */
    @Operation(summary = "修改套餐订阅")
    @PutMapping
    public AjaxResult edit(@RequestBody DingSubscription dingSubscription)
    {
        return toAjax(dingSubscriptionService.updateDingSubscription(dingSubscription));
    }

    /** 按主键删除企业订阅。 */
    @Operation(summary = "删除套餐订阅")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingSubscriptionService.deleteDingSubscriptionByIds(ids));
    }
}
