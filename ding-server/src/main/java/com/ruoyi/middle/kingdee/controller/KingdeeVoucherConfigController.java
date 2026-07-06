package com.ruoyi.middle.kingdee.controller;

import java.util.List;

import com.ruoyi.middle.kingdee.domain.KingdeeVoucherConfig;
import com.ruoyi.middle.kingdee.service.IKingdeeVoucherConfigService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 【请填写功能名称】Controller
 * 
 * @author ruoyi
 * @date 2026-07-04
 */
@RestController
@RequestMapping("/kingdee/voucherConfig")
public class KingdeeVoucherConfigController extends BaseController
{
    @Autowired
    private IKingdeeVoucherConfigService kingdeeVoucherConfigService;

    /**
     * 查询【请填写功能名称】列表
     */
    @PreAuthorize("@ss.hasPermi('kingdee:voucherConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        startPage();
        List<KingdeeVoucherConfig> list = kingdeeVoucherConfigService.selectKingdeeVoucherConfigList(kingdeeVoucherConfig);
        return getDataTable(list);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @PreAuthorize("@ss.hasPermi('kingdee:voucherConfig:export')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        List<KingdeeVoucherConfig> list = kingdeeVoucherConfigService.selectKingdeeVoucherConfigList(kingdeeVoucherConfig);
        ExcelUtil<KingdeeVoucherConfig> util = new ExcelUtil<KingdeeVoucherConfig>(KingdeeVoucherConfig.class);
        util.exportExcel(response, list, "【请填写功能名称】数据");
    }

    /**
     * 获取【请填写功能名称】详细信息
     */
    @PreAuthorize("@ss.hasPermi('kingdee:voucherConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(kingdeeVoucherConfigService.selectKingdeeVoucherConfigById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('kingdee:voucherConfig:add')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        return toAjax(kingdeeVoucherConfigService.insertKingdeeVoucherConfig(kingdeeVoucherConfig));
    }

    /**
     * 修改【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('kingdee:voucherConfig:edit')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody KingdeeVoucherConfig kingdeeVoucherConfig)
    {
        return toAjax(kingdeeVoucherConfigService.updateKingdeeVoucherConfig(kingdeeVoucherConfig));
    }

    /**
     * 删除【请填写功能名称】
     */
    @PreAuthorize("@ss.hasPermi('kingdee:voucherConfig:remove')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(kingdeeVoucherConfigService.deleteKingdeeVoucherConfigByIds(ids));
    }
}
