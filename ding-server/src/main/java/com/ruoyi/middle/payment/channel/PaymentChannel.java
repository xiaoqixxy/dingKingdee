package com.ruoyi.middle.payment.channel;

import com.ruoyi.middle.payment.dto.PaymentRequest;
import com.ruoyi.middle.payment.dto.PaymentResponse;

public interface PaymentChannel
{
    String getChannelCode();

    PaymentResponse createQrPayment(PaymentRequest request);

    boolean verifyCallback(String rawData);

    String getChannelName();
}
