package com.dj.middle.ding.mapper;

import java.util.List;
import com.dj.middle.ding.domain.DingProduct;

/** 产品套餐数据访问契约。 */
public interface DingProductMapper
{
    /** 查询指定主键的产品套餐记录。 */
    public DingProduct selectDingProductById(Long id);

    /** 查询符合筛选条件的产品套餐列表。 */
    public List<DingProduct> selectDingProductList(DingProduct dingProduct);

    /** 新增产品套餐记录。 */
    public int insertDingProduct(DingProduct dingProduct);

    /** 更新产品套餐记录。 */
    public int updateDingProduct(DingProduct dingProduct);

    /** 删除指定主键的产品套餐记录。 */
    public int deleteDingProductById(Long id);

    /** 删除指定主键集合的产品套餐记录。 */
    public int deleteDingProductByIds(Long[] ids);

    /** 校验产品套餐名称是否唯一。 */
    public DingProduct checkProductNameUnique(String productName);

    /** 读取当前默认产品套餐。 */
    public DingProduct selectDefaultProduct();

    /** 首次访问赠送的上架基础版，与后台默认产品设置无关。 */
    public DingProduct selectTrialBasicProduct();
}
