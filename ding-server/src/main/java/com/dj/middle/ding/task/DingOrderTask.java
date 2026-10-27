package com.dj.middle.ding.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dj.middle.ding.service.IDingOrderService;

/** 定时关闭过期未支付订单。 */
@Component("dingOrderTask")
public class DingOrderTask
{
    /** 处理订单业务的服务组件。 */
    @Autowired
    private IDingOrderService dingOrderService;

    /** 扫描过期未支付订单并更新为取消状态。 */
    public void cancelExpiredOrders()
    {
        dingOrderService.cancelExpiredOrders();
    }
}
