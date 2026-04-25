package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.middle.ding.domain.DingSyncForm;
import com.ruoyi.middle.ding.service.IDingSyncFormService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "同步表单配置管理")
@Anonymous
@RestController
@RequestMapping("/ding/syncForm")
public class DingSyncFormController extends BaseController
{
    @Autowired
    private IDingSyncFormService dingSyncFormService;

    @Operation(summary = "查询同步表单配置列表")
    @GetMapping("/list")
    public TableDataInfo list(DingSyncForm dingSyncForm)
    {
        startPage();
        List<DingSyncForm> list = dingSyncFormService.selectDingSyncFormList(dingSyncForm);
        return getDataTable(list);
    }

    @Operation(summary = "获取同步表单配置详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingSyncFormService.selectDingSyncFormById(id));
    }

    @Operation(summary = "根据企业ID获取同步表单列表")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSyncFormService.selectDingSyncFormByCorpId(dingCorpId));
    }

    @Operation(summary = "新增同步表单配置")
    @PostMapping
    public AjaxResult add(@RequestBody DingSyncForm dingSyncForm)
    {
        return toAjax(dingSyncFormService.insertDingSyncForm(dingSyncForm));
    }

    @Operation(summary = "修改同步表单配置")
    @PutMapping
    public AjaxResult edit(@RequestBody DingSyncForm dingSyncForm)
    {
        return toAjax(dingSyncFormService.updateDingSyncForm(dingSyncForm));
    }

    @Operation(summary = "删除同步表单配置")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingSyncFormService.deleteDingSyncFormByIds(ids));
    }

    @Operation(summary = "获取企业已绑定表单数量")
    @GetMapping(value = "/count/{dingCorpId}")
    public AjaxResult getCount(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSyncFormService.countByCorpId(dingCorpId));
    }
}
