package com.ruoyi.middle.ding.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.middle.ding.service.IDingOrderService;

@Component("dingOrderTask")
public class DingOrderTask
{
    @Autowired
    private IDingOrderService dingOrderService;

    public void cancelExpiredOrders()
    {
        dingOrderService.cancelExpiredOrders();
    }
}
