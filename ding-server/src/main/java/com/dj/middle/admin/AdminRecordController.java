package com.dj.middle.admin;
import java.util.*;
import com.dj.middle.admin.mapper.AdminRecordMapper;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.common.exception.ServiceException;
@RestController
@RequestMapping("/admin/records")
public class AdminRecordController {
 private final AdminRecordMapper mapper;
 public AdminRecordController(AdminRecordMapper mapper){this.mapper=mapper;}
 private static TableDataInfo table(List<?> rows,long total){TableDataInfo t=new TableDataInfo(rows,total);t.setCode(200);t.setMsg("查询成功");return t;}
 public static void validate(AdminRecordQuery q){
  if(q.getPageNum()<1||q.getPageNum()>100000||q.getPageSize()<1||q.getPageSize()>100)throw new ServiceException("分页范围无效",400);
  try {
   LocalDate start=q.getStartDate()==null||q.getStartDate().isBlank()?null:LocalDate.parse(q.getStartDate());
   LocalDate end=q.getEndDate()==null||q.getEndDate().isBlank()?null:LocalDate.parse(q.getEndDate());
   if(start!=null&&end!=null&&start.isAfter(end))throw new IllegalArgumentException();
  }catch(Exception e){throw new ServiceException("日期范围无效",400);}
 }
 @GetMapping("/{kind}/list")
 @PreAuthorize("@ss.hasPermi('ding:' + #kind + ':list')")
 public TableDataInfo list(@PathVariable String kind,AdminRecordQuery q){
  AdminRecordSql.dataset(kind);validate(q);return table(mapper.list(kind,q),mapper.count(kind,q));
 }
 @GetMapping("/{kind}/{id}")
 @PreAuthorize("@ss.hasPermi('ding:' + #kind + ':list')")
 public AjaxResult detail(@PathVariable String kind,@PathVariable Long id){
  Map<String,Object> result=mapper.detail(kind,id);
  if(result==null)throw new ServiceException("记录不存在",404);
  return AjaxResult.success(result);
 }
 @GetMapping("/execution/{id}/items")
 @PreAuthorize("@ss.hasPermi('ding:execution:list')")
 public TableDataInfo items(@PathVariable Long id,AdminRecordQuery q){
  validate(q);if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return table(mapper.items(id,q.getPageSize(),q.getOffset()),mapper.itemCount(id));
 }
 @GetMapping("/execution/{id}/apis")
 @PreAuthorize("@ss.hasPermi('ding:execution:list') && @ss.hasPermi('ding:apiLog:list')")
 public TableDataInfo executionApis(@PathVariable Long id,AdminRecordQuery q){
  validate(q);
  if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return table(mapper.executionApis(id,q.getPageSize(),q.getOffset()),mapper.executionApiCount(id));
 }
 @GetMapping("/execution/{id}/pages")
 @PreAuthorize("@ss.hasPermi('ding:execution:list')")
 public TableDataInfo pages(@PathVariable Long id,AdminRecordQuery q){
  validate(q);if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return table(mapper.pages(id,q.getPageSize(),q.getOffset()),mapper.pageCount(id));
 }
 @GetMapping("/execution/{id}/pages/{pageId}/stack")
 @PreAuthorize("@ss.hasPermi('ding:execution:list')")
 public AjaxResult pageStack(@PathVariable Long id,@PathVariable Long pageId){
  if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return AjaxResult.success("查询成功",mapper.pageStack(id,pageId));
 }
}
