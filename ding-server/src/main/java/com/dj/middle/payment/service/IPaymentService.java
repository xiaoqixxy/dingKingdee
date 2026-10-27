package com.dj.middle.payment.service;

import java.util.Map;
import com.dj.middle.payment.dto.PaymentRequest;
import com.dj.middle.payment.dto.PaymentResponse;

/** 支付业务服务契约。 */
public interface IPaymentService
{
    /** 按选择的支付渠道发起订单创建。 */
    PaymentResponse createPayment(PaymentRequest request, String channel);

    /** 按支付渠道约定验证回调内容，返回验签结果。 */
    boolean verifyCallback(String channel, String rawData);

    /** 按支付渠道约定验证回调内容，返回验签结果。 */
    boolean verifyCallback(String channel, String rawData, Map<String, String> headers);
}
