package com.dj.middle.payment.channel;

import com.dj.middle.payment.dto.PaymentRequest;
import com.dj.middle.payment.dto.PaymentResponse;

public interface PaymentChannel
{
    String getChannelCode();

    PaymentResponse createQrPayment(PaymentRequest request);

    boolean verifyCallback(String rawData);

    String getChannelName();
}
