package com.dj.middle.payment.channel;

import com.dj.middle.payment.dto.PaymentRequest;
import com.dj.middle.payment.dto.PaymentResponse;

/** 支付渠道统一契约。 */
public interface PaymentChannel
{
    /** 返回当前支付渠道的编码。 */
    String getChannelCode();

    /** 调用当前支付渠道创建二维码支付订单并返回统一响应。 */
    PaymentResponse createQrPayment(PaymentRequest request);

    /** 按支付渠道约定验证回调内容，返回验签结果。 */
    boolean verifyCallback(String rawData);

    /** 返回当前支付渠道的显示名称。 */
    String getChannelName();
}
