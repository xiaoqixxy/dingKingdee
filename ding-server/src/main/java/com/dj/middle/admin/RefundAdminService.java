package com.dj.middle.admin;
import com.dj.middle.admin.mapper.RefundAdminMapper;
import com.dj.common.exception.ServiceException;
import com.dj.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
/** 退款校验、状态流转与审计记录。 */
@Service
public class RefundAdminService {
 /** 提供None数据访问能力的映射接口。 */
 private final RefundAdminMapper mapper;
 /** 初始化当前组件并保存构造参数。 */
 public RefundAdminService(RefundAdminMapper mapper){this.mapper=mapper;}
 /** 校验退款金额与订单已付金额的合法关系。 */
 public static void validateAmount(BigDecimal amount,BigDecimal paid,BigDecimal reserved){
  if(amount==null||amount.signum()<=0||amount.scale()>2||amount.precision()>10||amount.add(reserved).compareTo(paid)>0)
   throw new ServiceException("退款金额必须大于零且不能超过剩余可退金额",400);
 }
 /** 校验订单与退款金额后登记退款申请及审计事件。 */
 @Transactional
 public Map<String,Object> create(RefundRequest r){
  if(r.getRefundNo()==null||!r.getRefundNo().matches("RF[a-zA-Z0-9-]{16,60}")||r.getOrderNo()==null||r.getOrderNo().length()>64||r.getMerchantAccountId()==null||r.getMerchantAccountId().isBlank()||r.getMerchantAccountId().length()>64||r.getReason()==null||r.getReason().isBlank()||r.getReason().length()>500)
   throw new ServiceException("请填写有效的订单号、商户号及退款原因",400);
  // 先锁定订单再计算已预占退款，避免并发申请同时使用同一笔剩余额度。
  Map<String,Object> order=mapper.lockOrder(r.getOrderNo());
  if(order==null)throw new ServiceException("订单不存在",404);
  Map<String,Object> existing=mapper.byNo(r.getRefundNo());
  // 相同申请编号仅允许重放完全一致的请求，参数不同必须报冲突，不能复用别人的申请。
  if(existing!=null){
   if(!r.getOrderNo().equals(existing.get("order_no"))||r.getAmount()==null||r.getAmount().compareTo((BigDecimal)existing.get("refund_amount"))!=0||!r.getMerchantAccountId().equals(existing.get("merchant_account_id"))||!r.getReason().equals(existing.get("reason")))
    throw new ServiceException("申请编号已用于其他退款请求",409);
   return existing;
  }
  if(((Number)order.get("pay_status")).intValue()!=1)throw new ServiceException("仅已支付订单可申请退款",400);
  // 校验退款金额与订单已付金额的合法关系。
  validateAmount(r.getAmount(),(BigDecimal)order.get("pay_amount"),mapper.reserved(r.getOrderNo()));
  // 只有渠道信息可识别的已付订单才能提交退款申请；这里登记申请，不直接发起资金划转。
  Object channel=order.get("pay_channel");if(channel==null||channel.toString().isBlank())throw new ServiceException("订单缺少支付渠道，需先核实",400);
  Map<String,Object> row=new HashMap<>();row.put("refundNo",r.getRefundNo());row.put("orderNo",r.getOrderNo());row.put("corp",order.get("ding_corp_id"));row.put("channel",channel);row.put("merchant",r.getMerchantAccountId());row.put("amount",r.getAmount());row.put("reason",r.getReason());row.put("actor",SecurityUtils.getUserId());
  mapper.insert(row);Map<String,Object> result=mapper.byNo(r.getRefundNo());event(result,"REFUND_REQUESTED",null,"REQUESTED");return result;
 }
 /** 校验退款当前状态后撤销申请并记录事件。 */
 @Transactional
 public void cancel(Long id){
  Map<String,Object> r=mapper.byId(id);if(r==null)throw new ServiceException("退款申请不存在",404);
  mapper.lockOrder(r.get("order_no").toString());
  if(mapper.cancel(id)!=1)throw new ServiceException("仅待处理的申请可撤销，请刷新状态",409);
  // 写入退款操作审计事件。
  event(r,"REFUND_CANCELLED","REQUESTED","CANCELLED");
 }
 /** 写入退款操作审计事件。 */
 private void event(Map<String,Object> r,String event,String before,String after){
  Map<String,Object> e=new HashMap<>();e.put("key",event+"-"+r.get("refund_no"));e.put("corp",r.get("ding_corp_id"));e.put("trace",UUID.randomUUID().toString());e.put("no",r.get("refund_no"));e.put("event",event);e.put("before",before);e.put("after",after);e.put("actor",SecurityUtils.getUserId());mapper.event(e);
 }
}
