package com.dj.middle.payment.controller;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import com.dj.middle.payment.util.PaymentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.utils.DateUtils;
import com.dj.middle.payment.dto.PaymentRequest;
import com.dj.middle.payment.dto.PaymentResponse;
import com.dj.middle.payment.service.IPaymentService;
import com.dj.middle.ding.domain.DingOrder;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingInvoice;
import com.dj.middle.ding.mapper.DingOrderMapper;
import com.dj.middle.ding.mapper.DingProductMapper;
import com.dj.middle.ding.mapper.DingInvoiceMapper;
import com.dj.middle.ding.service.IDingOrderService;
import com.dj.middle.payment.config.WechatPayConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 支付订单创建、渠道回调与订阅生效入口。 */
@Tag(name = "支付管理")
@Anonymous
@RestController
@RequestMapping("/payment")
public class PaymentController extends BaseController
{
    /** 统一创建支付订单和处理支付通知的服务。 */
    @Autowired
    private IPaymentService paymentService;

    /** 提供订单数据访问能力的映射接口。 */
    @Autowired
    private DingOrderMapper dingOrderMapper;

    /** 提供产品套餐数据访问能力的映射接口。 */
    @Autowired
    private DingProductMapper dingProductMapper;

    /** 提供发票数据访问能力的映射接口。 */
    @Autowired
    private DingInvoiceMapper dingInvoiceMapper;

    /** 处理订单业务的服务组件。 */
    @Autowired
    private IDingOrderService dingOrderService;

    /** 微信支付商户身份与证书配置。 */
    @Autowired
    private WechatPayConfig wechatPayConfig;

    /** 按选择的支付渠道发起订单创建。 */
    @Operation(summary = "创建支付订单")
    @PostMapping("/create")
    public AjaxResult createPayment(@RequestBody Map<String, Object> params)
    {
        String dingCorpId = (String) params.get("dingCorpId");
        Long productId = Long.valueOf(params.get("productId").toString());
        String channel = params.containsKey("channel") ? params.get("channel").toString() : "alipay";

        // 金额和产品信息以服务端套餐记录为准，不使用前端传入的价格。
        DingProduct product = dingProductMapper.selectDingProductById(productId);
        if (product == null) {
            return AjaxResult.error("产品不存在");
        }

        String orderNo = "ORD" + System.currentTimeMillis();
        Date now = new Date();
        // 支付订单有效期设为15分钟，创建支付请求和前端倒计时使用相同到期时间。
        Date expireTime = new Date(now.getTime() + 15 * 60 * 1000);

        // 先登记待支付订单，使支付渠道流水能关联本地订单。
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

        // 把内部订单转换为渠道无关的支付请求，由支付服务选择实际渠道实现。
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

    /** 验证支付通知后解析支付结果，更新订单并创建订阅和开票申请。 */
    @Operation(summary = "支付成功回调")
    @PostMapping("/callback/{channel}")
    public AjaxResult callback(@PathVariable String channel, 
                               @RequestBody String rawData,
                               @RequestHeader Map<String, String> headers)
    {
        // 任何状态修改前先执行渠道验签，失败直接拒绝继续处理。
        if (!paymentService.verifyCallback(channel, rawData, headers)) {
            return AjaxResult.error("验签失败");
        }

        String orderNo;
        String transactionId;
        
        // 微信回调正文经过加密，验签后仍需按商户APIv3密钥解密资源。
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
                // 非交易成功的通知只应被接收，不触发订阅或开票。
                if (!"SUCCESS".equals(plainJson.getString("trade_state"))) {
                    return AjaxResult.success("非支付成功状态，跳过处理");
                }
                orderNo = plainJson.getString("out_trade_no");
                transactionId = plainJson.getString("transaction_id");
            } catch (Exception e) {
                return AjaxResult.error("解密失败: " + e.getMessage());
            }
        }
        // 支付宝使用表单回调，读取交易状态和外部订单号后统一进入订单处理。
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
        // 只有待支付订单可在此入口变为已支付，避免重复执行后续生效逻辑。
        if (order.getPayStatus() != 0) {
            return AjaxResult.error("订单状态异常");
        }

        order.setPayStatus(1);
        order.setPayTime(new Date());
        order.setPayChannel(channel);
        order.setTransactionId(transactionId);
        dingOrderMapper.updateDingOrder(order);

        // 订单更新后开通或更新订阅，再生成待补充的发票申请。
        dingOrderService.createSubscription(order);
        // 根据已支付订单生成待补充抬头的开票申请。
        createInvoice(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("status", "paid");
        result.put("message", "支付成功，订单已生效，已自动生成开票申请");
        return AjaxResult.success(result);
    }

    /** 根据当前接口传入的操作处理订单状态，沿用前端模拟结果的现有行为。 */
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

        // 此接口沿用现有前端模拟操作分支，与真实渠道通知入口分开说明。
        if ("pay".equals(action)) {
            if (order.getPayStatus() != 0) {
                return AjaxResult.error("订单状态异常");
            }
            order.setPayStatus(1);
            order.setPayTime(new Date());
            order.setPayChannel("在线支付");
            dingOrderMapper.updateDingOrder(order);

            dingOrderService.createSubscription(order);
            // 根据已支付订单生成待补充抬头的开票申请。
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

    /** 根据已支付订单生成待补充抬头的开票申请。 */
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
