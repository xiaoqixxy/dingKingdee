package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.middle.ding.domain.DingSyncForm;
import com.dj.middle.ding.service.IDingSyncFormService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 同步表单绑定接口控制器。 */
@Tag(name = "同步表单配置管理")
@Anonymous
@RestController
@RequestMapping("/ding/syncForm")
public class DingSyncFormController extends BaseController
{
    /** 处理同步表单绑定业务的服务组件。 */
    @Autowired
    private IDingSyncFormService dingSyncFormService;

    /** 按条件分页查询同步表单绑定列表。 */
    @Operation(summary = "查询同步表单配置列表")
    @GetMapping("/list")
    public TableDataInfo list(DingSyncForm dingSyncForm)
    {
        // 查询同步表单配置列表。
        startPage();
        List<DingSyncForm> list = dingSyncFormService.selectDingSyncFormList(dingSyncForm);
        return getDataTable(list);
    }

    /** 获取同步表单配置详细信息。 */
    @Operation(summary = "获取同步表单配置详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingSyncFormService.selectDingSyncFormById(id));
    }

    /** 根据企业ID获取同步表单列表。 */
    @Operation(summary = "根据企业ID获取同步表单列表")
    @GetMapping(value = "/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSyncFormService.selectDingSyncFormByCorpId(dingCorpId));
    }

    /** 新增同步表单绑定并返回保存结果。 */
    @Operation(summary = "新增同步表单配置")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:syncForm:add')")
    @PostMapping
    public AjaxResult add(@RequestBody DingSyncForm dingSyncForm)
    {
        return toAjax(dingSyncFormService.insertDingSyncForm(dingSyncForm));
    }

    /** 更新指定同步表单绑定并返回修改结果。 */
    @Operation(summary = "修改同步表单配置")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:syncForm:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody DingSyncForm dingSyncForm)
    {
        return toAjax(dingSyncFormService.updateDingSyncForm(dingSyncForm));
    }

    /** 按主键删除同步表单绑定。 */
    @Operation(summary = "删除同步表单配置")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:syncForm:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingSyncFormService.deleteDingSyncFormByIds(ids));
    }

    /** 获取企业已绑定表单数量。 */
    @Operation(summary = "获取企业已绑定表单数量")
    @GetMapping(value = "/count/{dingCorpId}")
    public AjaxResult getCount(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingSyncFormService.countByCorpId(dingCorpId));
    }
}
