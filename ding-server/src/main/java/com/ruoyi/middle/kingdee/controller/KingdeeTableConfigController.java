package com.ruoyi.middle.kingdee.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.middle.kingdee.domain.KingdeeTableConfig;
import com.ruoyi.middle.kingdee.service.IKingdeeTableConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 金蝶表单配置 Controller
 * 
 * @author ruoyi
 */
@Tag(name = "金蝶表单配置管理")
@Anonymous
@RestController
@RequestMapping("/kingdee/tableConfig")
public class KingdeeTableConfigController extends BaseController
{

    @Autowired
    private IKingdeeTableConfigService kingdeeTableConfigService;

    @Operation(summary = "查询金蝶表单配置列表")
    @GetMapping("/list")
    public TableDataInfo list(KingdeeTableConfig kingdeeTableConfig)
    {
        startPage();
        List<KingdeeTableConfig> list = kingdeeTableConfigService.selectKingdeeTableConfigList(kingdeeTableConfig);
        return getDataTable(list);
    }

    @Operation(summary = "获取金蝶表单配置详细信息")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return AjaxResult.success(kingdeeTableConfigService.selectKingdeeTableConfigById(configId));
    }

    @Operation(summary = "新增金蝶表单配置")
    @PostMapping
    public AjaxResult add(@RequestBody KingdeeTableConfig kingdeeTableConfig)
    {
        if (StringUtils.isNotNull(kingdeeTableConfig.getFormKey()) 
                && !kingdeeTableConfigService.checkFormKeyUnique(kingdeeTableConfig.getFormKey()))
        {
            return AjaxResult.error("新增表单配置'" + kingdeeTableConfig.getFormName() + "'失败，表单标识已存在");
        }
        return toAjax(kingdeeTableConfigService.insertKingdeeTableConfig(kingdeeTableConfig));
    }

    @Operation(summary = "修改金蝶表单配置")
    @PutMapping
    public AjaxResult edit(@RequestBody KingdeeTableConfig kingdeeTableConfig)
    {
        return toAjax(kingdeeTableConfigService.updateKingdeeTableConfig(kingdeeTableConfig));
    }

    @Operation(summary = "删除金蝶表单配置")
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(kingdeeTableConfigService.deleteKingdeeTableConfigByIds(configIds));
    }

    @Operation(summary = "获取表单列表（下拉选择用）")
    @GetMapping("/options")
    public AjaxResult options()
    {
        KingdeeTableConfig query = new KingdeeTableConfig();
        List<KingdeeTableConfig> list = kingdeeTableConfigService.selectKingdeeTableConfigList(query);
        return AjaxResult.success(list);
    }

    @Operation(summary = "导出金蝶表单配置")
    @PostMapping("/export")
    public void export(HttpServletResponse response, KingdeeTableConfig kingdeeTableConfig)
    {
        List<KingdeeTableConfig> list = kingdeeTableConfigService.selectKingdeeTableConfigList(kingdeeTableConfig);
        ExcelUtil<KingdeeTableConfig> util = new ExcelUtil<KingdeeTableConfig>(KingdeeTableConfig.class);
        util.exportExcel(response, list, "金蝶表单配置数据");
    }

    @Operation(summary = "导入模板")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<KingdeeTableConfig> util = new ExcelUtil<KingdeeTableConfig>(KingdeeTableConfig.class);
        util.importTemplateExcel(response, "金蝶表单配置数据");
    }

    @Operation(summary = "导入数据")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<KingdeeTableConfig> util = new ExcelUtil<KingdeeTableConfig>(KingdeeTableConfig.class);
        List<KingdeeTableConfig> list = util.importExcel(file.getInputStream());
        int successNum = 0;
        for (KingdeeTableConfig config : list) {
            if (kingdeeTableConfigService.checkFormKeyUnique(config.getFormKey())) {
                kingdeeTableConfigService.insertKingdeeTableConfig(config);
                successNum++;
            }
        }
        return AjaxResult.success("导入成功" + successNum + "条");
    }
}
