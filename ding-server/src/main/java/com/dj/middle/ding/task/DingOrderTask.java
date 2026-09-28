package com.dj.middle.ding.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dj.middle.ding.service.IDingOrderService;

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
