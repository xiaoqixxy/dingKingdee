package com.dj.middle.admin;
import lombok.Data;
/** 后台业务记录的分页与筛选参数。 */
@Data
public class AdminRecordQuery {
 /** 分页页码。 */
 private int pageNum=1;
 /** 每页记录数。 */
 private int pageSize=20;
 /** 钉钉企业标识。 */
 private String dingCorpId;
 /** 关联业务单号。 */
 private String businessNo;
 /** 业务状态。 */
 private String status;
 /** 同步链路编号。 */
 private String traceId;
 /** 查询开始日期。 */
 private String startDate;
 /** 查询结束日期。 */
 private String endDate;
 /** 将页码和每页条数换算为数据库偏移。 */
 public int getOffset(){return (pageNum-1)*pageSize;}
}
