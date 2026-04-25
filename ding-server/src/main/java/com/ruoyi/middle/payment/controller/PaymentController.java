package com.ruoyi.middle.payment.controller;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import com.ruoyi.middle.payment.util.PaymentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.payment.dto.PaymentRequest;
import com.ruoyi.middle.payment.dto.PaymentResponse;
import com.ruoyi.middle.payment.service.IPaymentService;
import com.ruoyi.middle.ding.domain.DingOrder;
import com.ruoyi.middle.ding.domain.DingProduct;
import com.ruoyi.middle.ding.domain.DingInvoice;
import com.ruoyi.middle.ding.mapper.DingOrderMapper;
import com.ruoyi.middle.ding.mapper.DingProductMapper;
import com.ruoyi.middle.ding.mapper.DingInvoiceMapper;
import com.ruoyi.middle.ding.service.IDingOrderService;
import com.ruoyi.middle.payment.config.WechatPayConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "支付管理")
@Anonymous
@RestController
@RequestMapping("/payment")
public class PaymentController extends BaseController
{
    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private DingOrderMapper dingOrderMapper;

    @Autowired
    private DingProductMapper dingProductMapper;

    @Autowired
    private DingInvoiceMapper dingInvoiceMapper;

    @Autowired
    private IDingOrderService dingOrderService;

    @Autowired
    private WechatPayConfig wechatPayConfig;

    @Operation(summary = "创建支付订单")
    @PostMapping("/create")
    public AjaxResult createPayment(@RequestBody Map<String, Object> params)
    {
        String dingCorpId = (String) params.get("dingCorpId");
        Long productId = Long.valueOf(params.get("productId").toString());
        String channel = params.containsKey("channel") ? params.get("channel").toString() : "alipay";

        DingProduct product = dingProductMapper.selectDingProductById(productId);
        if (product == null) {
            return AjaxResult.error("产品不存在");
        }

        String orderNo = "ORD" + System.currentTimeMillis();
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + 15 * 60 * 1000);

        DingOrder order = new DingOrder();
        order.setOrderNo(orderNo);
        order.setDingCorpId(dingCorpId);
        order.setProductId(productId);
        order.setOrderType(1);
        order.setTotalAmount(product.getPrice());
        order.setPayAmount(product.getPrice());
        order.setPayStatus(0);
        order.setExpireTime(expireTime);
        order.setCreateTime(now);
        order.setRemark("在线支付订单");
        dingOrderMapper.insertDingOrder(order);

        PaymentRequest request = new PaymentRequest();
        request.setOrderNo(orderNo);
        request.setDingCorpId(dingCorpId);
        request.setAmount(product.getPrice());
        request.setSubject("钉钉数据同步服务-" + product.getProductName());
        request.setBody("购买产品: " + product.getProductName());

        PaymentResponse response = paymentService.createPayment(request, channel);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        response.setExpireTime(sdf.format(expireTime));

        return AjaxResult.success(response);
    }

    @Operation(summary = "支付成功回调")
    @PostMapping("/callback/{channel}")
    public AjaxResult callback(@PathVariable String channel, 
                               @RequestBody String rawData,
                               @RequestHeader Map<String, String> headers)
    {
        if (!paymentService.verifyCallback(channel, rawData, headers)) {
            return AjaxResult.error("验签失败");
        }

        String orderNo;
        String transactionId;
        
        if ("wechat_pay".equals(channel)) {
            try {
                com.alibaba.fastjson2.JSONObject notifyJson = com.alibaba.fastjson2.JSON.parseObject(rawData);
                com.alibaba.fastjson2.JSONObject resource = notifyJson.getJSONObject("resource");
                String ciphertext = resource.getString("ciphertext");
                String nonce = resource.getString("nonce");
                String associatedData = resource.getString("associated_data");
                
                String apiV3Key = wechatPayConfig.getApiV3Key();
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
                javax.crypto.spec.GCMParameterSpec spec = new javax.crypto.spec.GCMParameterSpec(128, nonce.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, 
                    new javax.crypto.spec.SecretKeySpec(apiV3Key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "AES"), 
                    spec);
                cipher.updateAAD(associatedData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                byte[] decrypted = cipher.doFinal(java.util.Base64.getDecoder().decode(ciphertext));
                String plaintext = new String(decrypted, java.nio.charset.StandardCharsets.UTF_8);
                
                com.alibaba.fastjson2.JSONObject plainJson = com.alibaba.fastjson2.JSON.parseObject(plaintext);
                if (!"SUCCESS".equals(plainJson.getString("trade_state"))) {
                    return AjaxResult.success("非支付成功状态，跳过处理");
                }
                orderNo = plainJson.getString("out_trade_no");
                transactionId = plainJson.getString("transaction_id");
            } catch (Exception e) {
                return AjaxResult.error("解密失败: " + e.getMessage());
            }
        }
        else if ("alipay".equals(channel)) {
            Map<String, String> params = PaymentUtil.parseAliCallbackParams(rawData);
            if (!"TRADE_SUCCESS".equals(params.get("trade_status"))) {
                return AjaxResult.success("非支付成功状态，跳过处理");
            }
            orderNo = params.get("out_trade_no");
            transactionId = params.get("trade_no");
        }
        else {
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(rawData);
            orderNo = json.containsKey("out_trade_no") ? json.getString("out_trade_no") : json.getString("orderNo");
            transactionId = json.containsKey("trade_no") ? json.getString("trade_no") : json.getString("transactionId");
        }

        DingOrder order = dingOrderMapper.selectDingOrderByOrderNo(orderNo);
        if (order == null) {
            return AjaxResult.error("订单不存在: " + orderNo);
        }
        if (order.getPayStatus() != 0) {
            return AjaxResult.error("订单状态异常");
        }

        order.setPayStatus(1);
        order.setPayTime(new Date());
        order.setPayChannel(channel);
        order.setTransactionId(transactionId);
        dingOrderMapper.updateDingOrder(order);

        dingOrderService.createSubscription(order);
        createInvoice(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("status", "paid");
        result.put("message", "支付成功，订单已生效，已自动生成开票申请");
        return AjaxResult.success(result);
    }

    @Operation(summary = "手动处理支付结果（前端模拟回调）")
    @PostMapping("/handleResult")
    public AjaxResult handleResult(@RequestBody Map<String, String> params)
    {
        String orderNo = params.get("orderNo");
        String action = params.get("action");

        DingOrder order = dingOrderMapper.selectDingOrderByOrderNo(orderNo);
        if (order == null) {
            return AjaxResult.error("订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);

        if ("pay".equals(action)) {
            if (order.getPayStatus() != 0) {
                return AjaxResult.error("订单状态异常");
            }
            order.setPayStatus(1);
            order.setPayTime(new Date());
            order.setPayChannel("在线支付");
            dingOrderMapper.updateDingOrder(order);

            dingOrderService.createSubscription(order);
            createInvoice(order);

            result.put("status", "paid");
            result.put("message", "订单已生效，已自动生成开票申请");
        } else if ("cancel".equals(action)) {
            if (order.getPayStatus() != 0) {
                return AjaxResult.error("订单状态异常");
            }
            order.setPayStatus(2);
            dingOrderMapper.updateDingOrder(order);

            result.put("status", "cancelled");
            result.put("message", "订单已取消");
        } else if ("timeout".equals(action)) {
            if (order.getPayStatus() != 0) {
                return AjaxResult.error("订单状态异常");
            }
            order.setPayStatus(2);
            dingOrderMapper.updateDingOrder(order);

            result.put("status", "timeout");
            result.put("message", "订单已超时取消");
        } else {
            return AjaxResult.error("未知操作");
        }

        return AjaxResult.success(result);
    }

    private void createInvoice(DingOrder order) {
        DingInvoice invoice = new DingInvoice();
        invoice.setDingCorpId(order.getDingCorpId());
        invoice.setOrderNo(order.getOrderNo());
        invoice.setInvoiceType(1);
        invoice.setInvoiceTitle("待补充");
        invoice.setTaxNo("");
        invoice.setAmount(order.getPayAmount());
        invoice.setStatus(0);
        invoice.setCreateTime(new Date());
        dingInvoiceMapper.insertDingInvoice(invoice);
    }
}
