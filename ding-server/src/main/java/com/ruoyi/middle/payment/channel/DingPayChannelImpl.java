package com.ruoyi.middle.payment.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.middle.payment.config.DingPayConfig;
import com.ruoyi.middle.payment.dto.PaymentRequest;
import com.ruoyi.middle.payment.dto.PaymentResponse;

@Component
public class DingPayChannelImpl implements PaymentChannel
{
    @Autowired
    private DingPayConfig dingPayConfig;

    @Override
    public String getChannelCode()
    {
        return "ding_pay";
    }

    @Override
    public String getChannelName()
    {
        return "钉钉支付";
    }

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
