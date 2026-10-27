package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingProductVo;

/** 产品套餐服务契约。 */
public interface IDingProductService
{
    /** 查询指定主键的产品套餐记录。 */
    public DingProduct selectDingProductById(Long id);

    /** 查询符合筛选条件的产品套餐列表。 */
    public List<DingProduct> selectDingProductList(DingProduct dingProduct);

    /** 查询所有可选的产品套餐列表。 */
    public List<DingProduct> selectDingProductAll();

    /** 新增产品套餐记录。 */
    public int insertDingProduct(DingProduct dingProduct);

    /** 更新产品套餐记录。 */
    public int updateDingProduct(DingProduct dingProduct);

    /** 删除指定主键的产品套餐记录。 */
    public int deleteDingProductById(Long id);

    /** 删除指定主键集合的产品套餐记录。 */
    public int deleteDingProductByIds(Long[] ids);

    /** 校验产品套餐名称是否唯一。 */
    public boolean checkProductNameUnique(String productName);

    /** 读取当前默认产品套餐。 */
    public DingProduct selectDefaultProduct();

    /** 根据企业有效订阅读取可用产品，无订阅时沿用默认产品逻辑。 */
    public DingProduct getProductByCorpId(String dingCorpId);

    /** 把企业当前产品和订阅信息转换为页面所需的权益视图。 */
    public DingProductVo getProductVoByCorpId(String dingCorpId);
}
