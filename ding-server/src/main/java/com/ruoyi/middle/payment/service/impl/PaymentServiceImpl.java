package com.ruoyi.middle.payment.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.middle.payment.channel.PaymentChannel;
import com.ruoyi.middle.payment.dto.PaymentRequest;
import com.ruoyi.middle.payment.dto.PaymentResponse;
import com.ruoyi.middle.payment.service.IPaymentService;

@Service
public class PaymentServiceImpl implements IPaymentService
{
    @Autowired
    private List<PaymentChannel> paymentChannels;

    private Map<String, PaymentChannel> channelMap;

    private Map<String, PaymentChannel> getChannelMap()
    {
        if (channelMap == null) {
            channelMap = new HashMap<>();
            for (PaymentChannel channel : paymentChannels) {
                channelMap.put(channel.getChannelCode(), channel);
            }
        }
        return channelMap;
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request, String channel)
    {
        PaymentChannel paymentChannel = getChannelMap().get(channel);
        if (paymentChannel == null) {
            throw new IllegalArgumentException("不支持的支付渠道: " + channel);
        }
        return paymentChannel.createQrPayment(request);
    }

    @Override
    public boolean verifyCallback(String channel, String rawData)
    {
        PaymentChannel paymentChannel = getChannelMap().get(channel);
        if (paymentChannel == null) {
            return false;
        }
        return paymentChannel.verifyCallback(rawData);
    }

    @Override
    public boolean verifyCallback(String channel, String rawData, Map<String, String> headers)
    {
        PaymentChannel paymentChannel = getChannelMap().get(channel);
        if (paymentChannel == null) {
            return false;
        }
        return paymentChannel.verifyCallback(rawData);
    }
}
