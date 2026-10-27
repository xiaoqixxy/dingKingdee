package com.dj.middle.payment.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.middle.payment.channel.PaymentChannel;
import com.dj.middle.payment.dto.PaymentRequest;
import com.dj.middle.payment.dto.PaymentResponse;
import com.dj.middle.payment.service.IPaymentService;

/** 按渠道编码路由支付创建与回调校验。 */
@Service
public class PaymentServiceImpl implements IPaymentService
{
    /** 框架注入的全部支付渠道实现。 */
    @Autowired
    private List<PaymentChannel> paymentChannels;

    /** 支付渠道编码与实现的索引。 */
    private Map<String, PaymentChannel> channelMap;

    /** 读取按渠道编码索引的支付实现。 */
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

    /** 按选择的支付渠道发起订单创建。 */
    @Override
    public PaymentResponse createPayment(PaymentRequest request, String channel)
    {
        PaymentChannel paymentChannel = getChannelMap().get(channel);
        if (paymentChannel == null) {
            throw new IllegalArgumentException("不支持的支付渠道: " + channel);
        }
        return paymentChannel.createQrPayment(request);
    }

    /** 按支付渠道约定验证回调内容，返回验签结果。 */
    @Override
    public boolean verifyCallback(String channel, String rawData)
    {
        PaymentChannel paymentChannel = getChannelMap().get(channel);
        if (paymentChannel == null) {
            return false;
        }
        return paymentChannel.verifyCallback(rawData);
    }

    /** 按支付渠道约定验证回调内容，返回验签结果。 */
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
