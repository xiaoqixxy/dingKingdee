package com.dj.middle.admin;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class RefundRequest {
 private String refundNo;
 private String orderNo;
 private String merchantAccountId;
 private BigDecimal amount;
 private String reason;
}
