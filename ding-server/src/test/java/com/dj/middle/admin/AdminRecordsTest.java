package com.dj.middle.admin;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.session.Configuration;
import com.dj.middle.admin.mapper.AdminRecordMapper;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class AdminRecordsTest {
 @Test void unknownKindsNeverBecomeSqlIdentifiers(){assertThrows(RuntimeException.class,()->AdminRecordSql.dataset("ding_order;DROP TABLE x"));}
 @Test void everyDatasetBindsFiltersAndPagination(){
  Configuration c=new Configuration();c.addMapper(AdminRecordMapper.class);
  AdminRecordQuery q=new AdminRecordQuery();q.setDingCorpId("x' OR 1=1 --");q.setBusinessNo("unsafe-value");q.setStatus("FAILED");q.setStartDate("2026-09-01");q.setEndDate("2026-09-30");
  for(String kind:List.of("syncLog","refund","execution","apiLog","audit","paymentLog","packageChangeLog")){
   Map<String,Object> p=Map.of("kind",kind,"q",q);
   var bound=c.getMappedStatement(AdminRecordMapper.class.getName()+".list").getBoundSql(p);
   assertFalse(bound.getSql().contains(q.getDingCorpId()));assertFalse(bound.getSql().contains("unsafe-value"));assertTrue(bound.getSql().contains("LIMIT ? OFFSET ?"));
   assertTrue(c.getMappedStatement(AdminRecordMapper.class.getName()+".count").getBoundSql(p).getSql().startsWith("SELECT COUNT(*)"));
  }
 }
 @Test void validationRejectsUnboundedPagesAndReversedDates(){
  AdminRecordQuery q=new AdminRecordQuery();q.setPageSize(10001);assertThrows(RuntimeException.class,()->AdminRecordController.validate(q));q.setPageSize(20);q.setStartDate("2026-09-30");q.setEndDate("2026-09-01");assertThrows(RuntimeException.class,()->AdminRecordController.validate(q));
 }
 @Test void refundChecksIncludeExistingReservations(){
  assertDoesNotThrow(()->RefundAdminService.validateAmount(new BigDecimal("20.00"),new BigDecimal("100.00"),new BigDecimal("80.00")));
  assertThrows(RuntimeException.class,()->RefundAdminService.validateAmount(new BigDecimal("20.01"),new BigDecimal("100.00"),new BigDecimal("80.00")));
  assertThrows(RuntimeException.class,()->RefundAdminService.validateAmount(BigDecimal.ZERO,BigDecimal.TEN,BigDecimal.ZERO));
  assertThrows(RuntimeException.class,()->RefundAdminService.validateAmount(new BigDecimal("0.001"),BigDecimal.TEN,BigDecimal.ZERO));
 }
 @Test void securityExpressionsCanResolveKindParameter() throws Exception {
  assertEquals("kind",AdminRecordController.class.getMethod("list",String.class,AdminRecordQuery.class).getParameters()[0].getName());
 }
}
