package com.ruoyi.middle.payment.channel;

import java.math.BigDecimal;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

import com.ruoyi.middle.payment.config.WechatPayConfig;
import com.ruoyi.middle.payment.dto.PaymentRequest;
import com.ruoyi.middle.payment.dto.PaymentResponse;

@Component
public class WechatPayChannelImpl implements PaymentChannel
{
    private static final Logger log = LoggerFactory.getLogger(WechatPayChannelImpl.class);

    @Autowired
    private WechatPayConfig wechatPayConfig;

    private NativePayService nativePayService;

    @Override
    public String getChannelCode()
    {
        return "wechat_pay";
    }

    @Override
    public String getChannelName()
    {
        return "微信支付";
    }

    @Override
    public PaymentResponse createQrPayment(PaymentRequest request)
    {
        initWechatPay();
        try
        {
            PrepayRequest prepayRequest = new PrepayRequest();

            Amount amount = new Amount();
            amount.setTotal(request.getAmount().multiply(new BigDecimal("100")).intValue());
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            prepayRequest.setAppid(wechatPayConfig.getAppId());
            prepayRequest.setMchid(wechatPayConfig.getMchId());
            prepayRequest.setDescription(request.getSubject());
            prepayRequest.setNotifyUrl(wechatPayConfig.getNotifyUrl());
            prepayRequest.setOutTradeNo(request.getOrderNo());

            PrepayResponse response = nativePayService.prepay(prepayRequest);
            String codeUrl = response.getCodeUrl();

            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setOrderNo(request.getOrderNo());
            paymentResponse.setQrCode(generateQRCodeBase64(codeUrl, 250, 250));
            paymentResponse.setPayUrl(codeUrl);
            paymentResponse.setChannel(getChannelCode());
            return paymentResponse;
        }
        catch (Exception e)
        {
            throw new RuntimeException("微信支付下单失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyCallback(String rawData)
    {
        return true;
    }

    private synchronized void initWechatPay()
    {
        if (nativePayService != null)
        {
            return;
        }
        try
        {
            RSAAutoCertificateConfig config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(wechatPayConfig.getMchId())
                    .privateKeyFromPath(wechatPayConfig.getPrivateKeyPath())
                    .merchantSerialNumber(wechatPayConfig.getCertSerialNo())
                    .apiV3Key(wechatPayConfig.getApiV3Key())
                    .build();

            nativePayService = new NativePayService.Builder().config(config).build();
            log.info("微信支付SDK初始化成功");
        }
        catch (Exception e)
        {
            log.error("微信支付SDK初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("微信支付SDK初始化失败: " + e.getMessage(), e);
        }
    }

    private String generateQRCodeBase64(String content, int width, int height)
    {
        try
        {
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            java.util.Hashtable hints = new java.util.Hashtable();
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 1);
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(content,
                    com.google.zxing.BarcodeFormat.QR_CODE, width, height, hints);
            java.awt.image.BufferedImage image = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }
        catch (Exception e)
        {
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }
    }
}
