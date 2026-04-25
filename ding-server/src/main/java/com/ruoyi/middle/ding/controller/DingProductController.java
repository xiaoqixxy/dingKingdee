package com.ruoyi.middle.ding.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.middle.ding.domain.DingProduct;
import com.ruoyi.middle.ding.domain.DingProductVo;
import com.ruoyi.middle.ding.service.IDingProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "产品配置管理")
@Anonymous
@RestController
@RequestMapping("/ding/product")
public class DingProductController extends BaseController
{
    @Autowired
    private IDingProductService dingProductService;

    @Operation(summary = "查询产品配置列表")
    @GetMapping("/list")
    public TableDataInfo list(DingProduct dingProduct)
    {
        startPage();
        List<DingProduct> list = dingProductService.selectDingProductList(dingProduct);
        return getDataTable(list);
    }

    @Operation(summary = "获取产品配置详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dingProductService.selectDingProductById(id));
    }

    @Operation(summary = "新增产品配置")
    @PostMapping
    public AjaxResult add(@RequestBody DingProduct dingProduct)
    {
        return toAjax(dingProductService.insertDingProduct(dingProduct));
    }

    @Operation(summary = "修改产品配置")
    @PutMapping
    public AjaxResult edit(@RequestBody DingProduct dingProduct)
    {
        return toAjax(dingProductService.updateDingProduct(dingProduct));
    }

    @Operation(summary = "删除产品配置")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dingProductService.deleteDingProductByIds(ids));
    }

    @Operation(summary = "获取所有上架产品（下拉选择用）")
    @GetMapping("/options")
    public AjaxResult options()
    {
        List<DingProduct> list = dingProductService.selectDingProductAll();
        return AjaxResult.success(list);
    }

    @Operation(summary = "根据企业ID获取产品订阅信息")
    @GetMapping("/corp/{dingCorpId}")
    public AjaxResult getByCorpId(@PathVariable("dingCorpId") String dingCorpId)
    {
        return AjaxResult.success(dingProductService.getProductVoByCorpId(dingCorpId));
    }
}
