package com.ruoyi.middle.payment.service;

import java.util.Map;
import com.ruoyi.middle.payment.dto.PaymentRequest;
import com.ruoyi.middle.payment.dto.PaymentResponse;

public interface IPaymentService
{
    PaymentResponse createPayment(PaymentRequest request, String channel);

    boolean verifyCallback(String channel, String rawData);

    boolean verifyCallback(String channel, String rawData, Map<String, String> headers);
}
