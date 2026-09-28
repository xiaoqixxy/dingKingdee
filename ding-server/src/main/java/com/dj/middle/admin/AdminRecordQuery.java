package com.dj.middle.admin;
import lombok.Data;
@Data
public class AdminRecordQuery {
 private int pageNum=1;
 private int pageSize=20;
 private String dingCorpId;
 private String businessNo;
 private String status;
 private String traceId;
 private String startDate;
 private String endDate;
 public int getOffset(){return (pageNum-1)*pageSize;}
}
