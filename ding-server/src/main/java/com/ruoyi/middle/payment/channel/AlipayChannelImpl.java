package com.ruoyi.middle.payment.channel;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.aliyun.tea.TeaUnretryableException;
import com.ruoyi.middle.payment.util.PaymentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.ruoyi.middle.payment.config.AlipayConfig;
import com.ruoyi.middle.payment.dto.PaymentRequest;
import com.ruoyi.middle.payment.dto.PaymentResponse;

@Component
public class AlipayChannelImpl implements PaymentChannel
{
    @Autowired
    private AlipayConfig alipayConfig;

    private volatile boolean initialized = false;

    private void init()
    {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    Config config = new Config();
                    config.protocol = "https";
                    config.gatewayHost = alipayConfig.getGatewayUrl();
                    config.signType = "RSA2";
                    config.appId = alipayConfig.getAppId();
                    config.merchantPrivateKey = alipayConfig.getPrivateKey();
                    config.alipayPublicKey = alipayConfig.getAlipayPublicKey();
                    config.notifyUrl = alipayConfig.getNotifyUrl();
//                    config.encryptKey = "HiTu/fsyeor1Td1SoXgLbA==";
                    Factory.setOptions(config);
                    initialized = true;
                }
            }
        }
    }

    @Override
    public String getChannelCode()
    {
        return "alipay";
    }

    @Override
    public String getChannelName()
    {
        return "支付宝";
    }

    @Override
    public PaymentResponse createQrPayment(PaymentRequest request)
    {
        init();
        try {
            AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace()
                    .preCreate(request.getSubject(), request.getOrderNo(), request.getAmount().toString());
            PaymentResponse result = new PaymentResponse();
            result.setOrderNo(request.getOrderNo());
            result.setQrCode(generateQRCodeBase64(response.qrCode, 250, 250));
            result.setPayUrl(response.qrCode);
            result.setChannel(getChannelCode());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("支付宝当面付创建失败: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyCallback(String rawData)
    {
        init();
        try {
            Map<String, String> params = PaymentUtil.parseAliCallbackParams(rawData);
            return Factory.Payment.Common().verifyNotify(params);
        } catch (Exception e) {
            return false;
        }
    }

    private String generateQRCodeBase64(String content, int width, int height)
    {
        try {
            com.google.zxing.qrcode.QRCodeWriter qrCodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            java.util.Hashtable<com.google.zxing.EncodeHintType, Object> hints = new java.util.Hashtable<>();
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 1);
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(content,
                    com.google.zxing.BarcodeFormat.QR_CODE, width, height, hints);
            java.awt.image.BufferedImage image = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }
    }
}
