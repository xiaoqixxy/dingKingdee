package com.dj.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.common.utils.poi.ExcelUtil;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingProductVo;
import com.dj.middle.ding.service.IDingProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 产品套餐接口控制器。 */
@Tag(name = "产品配置管理")
@Anonymous
@RestController
@RequestMapping("/ding/product")
public class DingProductController extends BaseController
{
    /** 处理产品套餐业务的服务组件。 */
    @Autowired
    private IDingProductService dingProductService;

    /** 按条件分页查询产品套餐列表。 */
    @Operation(summary = "查询产品配置列表")
    @GetMapping("/list")
    public TableDataInfo list(DingProduct dingProduct)
    {
        // 查询产品配置列表。
        startPage();
        List<DingProduct> list = dingProductService.selectDingProductList(dingProduct);
        return getDataTable(list);
    }

    /** 获取产品配置详细信息。 */
    @Operation(summary = "获取产品配置详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingProductService.selectDingProductById(id));
    }

    /** 新增产品套餐并返回保存结果。 */
    @Operation(summary = "新增产品配置")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:product:add')")
    @PostMapping
    public AjaxResult add(@RequestBody DingProduct dingProduct)
    {
        return toAjax(dingProductService.insertDingProduct(dingProduct));
    }

    /** 更新指定产品套餐并返回修改结果。 */
    @Operation(summary = "修改产品配置")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:product:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody DingProduct dingProduct)
    {
        return toAjax(dingProductService.updateDingProduct(dingProduct));
    }

    /** 按主键删除产品套餐。 */
    @Operation(summary = "删除产品配置")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasPermi('ding:product:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingProductService.deleteDingProductByIds(ids));
    }

    /** 返回供下拉选择的产品套餐选项。 */
    @Operation(summary = "获取所有上架产品（下拉选择用）")
    @GetMapping("/options")
    public AjaxResult options()
    {
        List<DingProduct> list = dingProductService.selectDingProductAll();
        return AjaxResult.success(list);
    }

    /** 根据企业ID获取产品订阅信息。 */
    @Operation(summary = "根据企业ID获取产品订阅信息")
    @GetMapping("/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingProductService.getProductVoByCorpId(dingCorpId));
    }
}
