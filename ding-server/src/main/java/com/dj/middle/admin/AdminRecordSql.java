package com.dj.middle.admin;
import java.util.Map;
import com.dj.common.exception.ServiceException;
public final class AdminRecordSql {
 public record Dataset(String table,String time,String status,String number,String columns) {}
 public static Dataset dataset(String kind) {
  return switch(kind) {
   case "syncLog" -> new Dataset("ding_sync_log","create_time","status","ding_form_id","id,ding_corp_id,ding_form_id,sync_count,status,error_msg,create_time,trace_id,execution_id");
   case "execution" -> new Dataset("ding_sync_execution","created_at","status","id","id,ding_corp_id,sync_form_id,trace_id,request_key,retry_of_id,attempt_no,trigger_type,direction,status,total_count,success_count,failed_count,skipped_count,started_at,finished_at,duration_ms,error_code,error_message,log_scope,page_count,created_at");
   case "apiLog" -> new Dataset("ding_api_call_log","occurred_at","result","api_name","id,ding_corp_id,trace_id,execution_id,page_id,platform,api_name,request_path,http_method,http_status,business_code,result,duration_ms,error_message,occurred_at,created_at");
   case "audit" -> new Dataset("ding_business_event_log","occurred_at",null,"business_id","id,event_key,ding_corp_id,trace_id,business_type,business_id,event_type,from_status,to_status,actor_type,actor_id,change_summary,occurred_at,created_at");
   case "refund" -> new Dataset("ding_refund","requested_at","status","order_no","id,refund_no,order_no,ding_corp_id,pay_channel,merchant_account_id,channel_refund_id,refund_amount,currency,status,reason,requested_by,requested_at,completed_at,version");
   case "paymentLog" -> new Dataset("ding_payment_log","create_time","status","order_no","id,order_no,ding_corp_id,pay_channel,transaction_id,pay_amount,status,create_time,trace_id");
   case "packageChangeLog" -> new Dataset("ding_product_change_log","create_time","change_type","order_no","id,ding_corp_id,old_product_id,new_product_id,change_type,order_no,operator,create_time,trace_id");
   default -> throw new ServiceException("不支持的记录类型",400);
  };
 }
 private static String where(Dataset d,AdminRecordQuery q) {
  StringBuilder s=new StringBuilder(" WHERE 1=1");
  if(has(q.getDingCorpId()))s.append(" AND ding_corp_id=#{q.dingCorpId}");
  if(has(q.getBusinessNo()))s.append(" AND ").append(d.number()).append("=#{q.businessNo}");
  if(has(q.getStatus()) && d.status()!=null)s.append(" AND ").append(d.status()).append("=#{q.status}");
  if(has(q.getTraceId()) && !d.table().equals("ding_refund"))s.append(" AND trace_id=#{q.traceId}");
  if(has(q.getStartDate()))s.append(" AND ").append(d.time()).append(" >= #{q.startDate}");
  if(has(q.getEndDate()))s.append(" AND ").append(d.time()).append(" < DATE_ADD(#{q.endDate},INTERVAL 1 DAY)");
  return s.toString();
 }
 private static boolean has(String s){return s!=null&&!s.isBlank();}
 public static String list(Map<String,Object> p){Dataset d=dataset((String)p.get("kind"));return "SELECT records.*,tenant.corp_name FROM (SELECT "+d.columns()+" FROM "+d.table()+where(d,(AdminRecordQuery)p.get("q"))+" ORDER BY "+d.time()+" DESC,id DESC LIMIT #{q.pageSize} OFFSET #{q.offset}) records LEFT JOIN ding_tenant tenant ON tenant.ding_corp_id=records.ding_corp_id ORDER BY records."+d.time()+" DESC,records.id DESC";}
 public static String count(Map<String,Object> p){Dataset d=dataset((String)p.get("kind"));return "SELECT COUNT(*) FROM "+d.table()+where(d,(AdminRecordQuery)p.get("q"));}
 public static String detail(Map<String,Object> p){Dataset d=dataset((String)p.get("kind"));return "SELECT "+d.columns()+(java.util.Set.of("execution","apiLog").contains((String)p.get("kind"))?",error_stack":"")+" FROM "+d.table()+" WHERE id=#{id}";}
}
