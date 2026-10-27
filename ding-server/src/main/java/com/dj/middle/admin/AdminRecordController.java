package com.dj.middle.admin;
import java.util.*;
import com.dj.middle.admin.mapper.AdminRecordMapper;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.dj.common.core.domain.AjaxResult;
import com.dj.common.core.page.TableDataInfo;
import com.dj.common.exception.ServiceException;
/** 后台业务记录的统一只读查询入口。 */
@RestController
@RequestMapping("/admin/records")
public class AdminRecordController {
 /** 提供None数据访问能力的映射接口。 */
 private final AdminRecordMapper mapper;
 /** 初始化当前组件并保存构造参数。 */
 public AdminRecordController(AdminRecordMapper mapper){this.mapper=mapper;}
 /** 把记录及总数包装为后台分页响应。 */
 private static TableDataInfo table(List<?> rows,long total){TableDataInfo t=new TableDataInfo(rows,total);t.setCode(200);t.setMsg("查询成功");return t;}
 /** 约束页码、每页条数及起止日期，防止无效或过大的查询。 */
 public static void validate(AdminRecordQuery q){
  if(q.getPageNum()<1||q.getPageNum()>100000||q.getPageSize()<1||q.getPageSize()>100)throw new ServiceException("分页范围无效",400);
  try {
   LocalDate start=q.getStartDate()==null||q.getStartDate().isBlank()?null:LocalDate.parse(q.getStartDate());
   LocalDate end=q.getEndDate()==null||q.getEndDate().isBlank()?null:LocalDate.parse(q.getEndDate());
   if(start!=null&&end!=null&&start.isAfter(end))throw new IllegalArgumentException();
  }catch(Exception e){throw new ServiceException("日期范围无效",400);}
 }
 /** 先按数据集白名单校验类型，再分页查询记录与总数。 */
 @GetMapping("/{kind}/list")
 @PreAuthorize("@ss.hasPermi('ding:' + #kind + ':list')")
 public TableDataInfo list(@PathVariable String kind,AdminRecordQuery q){
  AdminRecordSql.dataset(kind);validate(q);return table(mapper.list(kind,q),mapper.count(kind,q));
 }
 /** 读取指定业务记录，不存在时返回明确的未找到错误。 */
 @GetMapping("/{kind}/{id}")
 @PreAuthorize("@ss.hasPermi('ding:' + #kind + ':list')")
 public AjaxResult detail(@PathVariable String kind,@PathVariable Long id){
  Map<String,Object> result=mapper.detail(kind,id);
  if(result==null)throw new ServiceException("记录不存在",404);
  return AjaxResult.success(result);
 }
 /** 校验执行批次存在后查询该批次已返回的源记录。 */
 @GetMapping("/execution/{id}/items")
 @PreAuthorize("@ss.hasPermi('ding:execution:list')")
 public TableDataInfo items(@PathVariable Long id,AdminRecordQuery q){
  // 约束页码、每页条数及起止日期，防止无效或过大的查询。
  validate(q);if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return table(mapper.items(id,q.getPageSize(),q.getOffset()),mapper.itemCount(id));
 }
 /** 校验批次后查询其接口调用明细，并同时要求执行和接口日志权限。 */
 @GetMapping("/execution/{id}/apis")
 @PreAuthorize("@ss.hasPermi('ding:execution:list') && @ss.hasPermi('ding:apiLog:list')")
 public TableDataInfo executionApis(@PathVariable Long id,AdminRecordQuery q){
  // 约束页码、每页条数及起止日期，防止无效或过大的查询。
  validate(q);
  if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return table(mapper.executionApis(id,q.getPageSize(),q.getOffset()),mapper.executionApiCount(id));
 }
 /** 查询同步批次的分页执行历史。 */
 @GetMapping("/execution/{id}/pages")
 @PreAuthorize("@ss.hasPermi('ding:execution:list')")
 public TableDataInfo pages(@PathVariable Long id,AdminRecordQuery q){
  // 约束页码、每页条数及起止日期，防止无效或过大的查询。
  validate(q);if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return table(mapper.pages(id,q.getPageSize(),q.getOffset()),mapper.pageCount(id));
 }
 /** 在指定批次下读取某一页的异常堆栈。 */
 @GetMapping("/execution/{id}/pages/{pageId}/stack")
 @PreAuthorize("@ss.hasPermi('ding:execution:list')")
 public AjaxResult pageStack(@PathVariable Long id,@PathVariable Long pageId){
  if(mapper.detail("execution",id)==null)throw new ServiceException("执行记录不存在",404);
  return AjaxResult.success("查询成功",mapper.pageStack(id,pageId));
 }
}
