package com.dj.middle.payment.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dj.middle.payment.config.DingPayConfig;
import com.dj.middle.payment.dto.PaymentRequest;
import com.dj.middle.payment.dto.PaymentResponse;

/** 钉钉支付渠道的请求签名与二维码支付适配。 */
@Component
public class DingPayChannelImpl implements PaymentChannel
{
    /** 钉钉支付渠道配置。 */
    @Autowired
 private DingPayConfig dingPayConfig;

    /** 返回当前支付渠道的编码。 */
    @Override
    public String getChannelCode()
    {
        return "ding_pay";
    }

    /** 返回当前支付渠道的显示名称。 */
    @Override
    public String getChannelName()
    {
        return "钉钉支付";
    }

    /** 调用当前支付渠道创建二维码支付订单并返回统一响应。 */
    @Override
    public PaymentResponse createQrPayment(PaymentRequest request)
    {
        try {
            String accessToken = getAccessToken();
            String url = "https://oapi.dingtalk.com/topapi/pay/trade/create?access_token=" + accessToken;

            String body = String.format(
                "{\"out_trade_no\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"%s\",\"timeout_express\":\"15m\",\"notify_url\":\"%s\"}",
                request.getOrderNo(),
                request.getAmount().toString(),
                request.getSubject(),
                dingPayConfig.getNotifyUrl()
            );

            String response = httpPost(url, body);
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(response);
            String qrCode = json.getJSONObject("result").getString("qr_code");

            PaymentResponse result = new PaymentResponse();
            result.setOrderNo(request.getOrderNo());
            result.setQrCode(generateQRCodeBase64(qrCode, 250, 250));
            result.setPayUrl(qrCode);
            result.setChannel(getChannelCode());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("钉钉支付创建失败: " + e.getMessage());
        }
    }

    /** 按支付渠道约定验证回调内容，返回验签结果。 */
    @Override
    public boolean verifyCallback(String rawData)
    {
        try {
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(rawData);
            String sign = json.getString("sign");
            json.remove("sign");
            String content = json.toJSONString();
            String expectedSign = computeSign(content, dingPayConfig.getAppSecret());
            return expectedSign.equals(sign);
        } catch (Exception e) {
            return false;
        }
    }

    /** 读取接口访问令牌。 */
    private String getAccessToken()
    {
        try {
            String url = "https://oapi.dingtalk.com/gettoken?appkey=" + dingPayConfig.getAppId()
                + "&appsecret=" + dingPayConfig.getAppSecret();
            String response = httpGet(url);
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(response);
            return json.getString("access_token");
        } catch (Exception e) {
            throw new RuntimeException("获取钉钉access_token失败: " + e.getMessage());
        }
    }

    /** 向钉钉支付网关发送POST请求并读取响应。 */
    private String httpPost(String url, String body) throws Exception
    {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** 向钉钉支付网关发送GET请求并读取响应。 */
    private String httpGet(String url) throws Exception
    {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setRequestMethod("GET");
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** 按钉钉支付协议计算请求签名。 */
    private String computeSign(String content, String appSecret)
    {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(appSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signBytes = mac.doFinal(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("签名计算失败: " + e.getMessage());
        }
    }

    /** 把支付链接生成二维码图片并编码为Base64。 */
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
