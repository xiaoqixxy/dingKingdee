package com.dj.middle.admin.mapper;
import org.apache.ibatis.annotations.*;
import java.util.*;
import java.math.BigDecimal;
/** 退款状态及审计事件持久化。 */
public interface RefundAdminMapper {
 /** 写入退款申请并回填记录主键。 */
 @Select("SELECT id,order_no,ding_corp_id,pay_status,pay_amount,pay_channel FROM ding_order WHERE order_no=#{no} FOR UPDATE") Map<String,Object> lockOrder(String no);
 /** 按退款申请编号读取退款记录。 */
 @Select("SELECT * FROM ding_refund WHERE refund_no=#{no}") Map<String,Object> byNo(String no);
 /** 按主键读取退款记录。 */
 @Select("SELECT * FROM ding_refund WHERE id=#{id}") Map<String,Object> byId(Long id);
 /** 计算订单已预留的退款金额。 */
 @Select("SELECT COALESCE(SUM(refund_amount),0) FROM ding_refund WHERE order_no=#{no} AND status IN ('REQUESTED','PROCESSING','SUCCEEDED')") BigDecimal reserved(String no);
 /** 保存退款申请记录。 */
 @Insert("INSERT INTO ding_refund(refund_no,order_no,ding_corp_id,pay_channel,merchant_account_id,refund_amount,status,reason,requested_by) VALUES(#{refundNo},#{orderNo},#{corp},#{channel},#{merchant},#{amount},'REQUESTED',#{reason},#{actor})")
 int insert(Map<String,Object> row);
 /** 撤销退款申请并更新状态。 */
 @Update("UPDATE ding_refund SET status='CANCELLED',completed_at=NOW(3),version=version+1 WHERE id=#{id} AND status='REQUESTED'") int cancel(Long id);
 /** 写入退款流程审计事件。 */
 @Insert("INSERT INTO ding_business_event_log(event_key,ding_corp_id,trace_id,business_type,business_id,event_type,from_status,to_status,actor_type,actor_id,occurred_at) VALUES(#{key},#{corp},#{trace},'REFUND',#{no},#{event},#{before},#{after},'USER',#{actor},NOW(3))") int event(Map<String,Object> row);
}
