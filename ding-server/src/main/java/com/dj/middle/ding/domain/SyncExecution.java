package com.dj.middle.ding.domain;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/** 描述一次经校验的数据源分页回调，不代表钉钉目标表已经成功写入。 */
@Data
/** 同步执行批次数据对象。 */
public class SyncExecution {
    /** 记录主键。 */
    private Long id;
    /** 企业标识。 */
    private String corpId;
    /** 同步链路编号。 */
    private String traceId;
    /** 用于识别重复请求的键。 */
    private String requestKey;
    /** 请求摘要。 */
    private String requestSummary;
    /** 业务状态。 */
    private String status = "RUNNING";
    /** 已处理记录总数。 */
    private int total;
    /** 成功返回的记录数。 */
    private int success;
    /** 跳过的记录数。 */
    private int skipped;
    /** 执行耗时毫秒数。 */
    private long duration;
    /** 统一错误编码。 */
    private String errorCode;
    /** 经过脱敏的错误信息。 */
    private String errorMessage;
    /** 响应摘要。 */
    private String responseSummary;
    /** 经过脱敏的异常堆栈。 */
    private String errorStack;
    /** 分页执行记录主键。 */
    private Long pageId;
   /** 本页起始偏移。 */
   private int pageOffset;
 /** 下一页偏移。 */
 private int nextOffset;
  /** 批次期望的下一页偏移。 */
  private int expectedOffset;
    /** 是否仍有后续页。 */
    private boolean hasMore;
   /** 本页是否首次计入批次统计。 */
   private boolean counted;
    /** 已完成页数。 */
    private int pageCount;
    /** 同步配置摘要指纹。 */
    private String sourceKey;
    /** 同步日志统计口径。 */
    private String logScope = "BATCH";
    /** 续传令牌关联的链路编号。 */
    private String continuationTrace;
    /** 本页返回的源记录标识。 */
    private final List<String> sourceIds = new ArrayList<>();
    /** 本页上游接口调用明细。 */
    private final List<ApiCall> calls = new ArrayList<>();

    /** 单次第三方接口调用的耗时、请求及响应记录。 */
    @Data
    public static class ApiCall {
        /** 名称。 */
        private String name;
        /** 处理结果。 */
        private String result;
        /** 执行耗时毫秒数。 */
        private long duration;
        /** 统一错误编码。 */
        private String errorCode;
        /** 金蝶返回的业务错误码。 */
        private String businessCode;
        /** 经过脱敏的错误信息。 */
        private String errorMessage;
        /** 经过脱敏的异常堆栈。 */
        private String errorStack;
        /** 调用发生时间。 */
        private java.util.Date occurredAt;
    }
}
