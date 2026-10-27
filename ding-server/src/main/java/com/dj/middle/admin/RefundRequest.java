package com.dj.middle.admin;
import lombok.Data;
import java.math.BigDecimal;
/** 后台退款申请参数。 */
@Data
public class RefundRequest {
 /** 退款单号。 */
 private String refundNo;
 /** 业务订单号。 */
 private String orderNo;
 /** 商户账户标识。 */
 private String merchantAccountId;
 /** 金额。 */
 private BigDecimal amount;
 /** 操作原因。 */
 private String reason;
}
