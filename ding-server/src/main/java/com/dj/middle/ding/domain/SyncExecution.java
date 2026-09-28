package com.dj.middle.ding.domain;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/** One verified records callback, not an acknowledgement of DingTalk persistence. */
@Data
public class SyncExecution {
    private Long id;
    private String corpId;
    private String traceId;
    private String requestKey;
    private String requestSummary;
    private String status = "RUNNING";
    private int total;
    private int success;
    private int skipped;
    private long duration;
    private String errorCode;
    private String errorMessage;
    private String responseSummary;
    private String errorStack;
    private Long pageId;
    private int pageOffset;
    private int nextOffset;
    private int expectedOffset;
    private boolean hasMore;
    private boolean counted;
    private int pageCount;
    private String sourceKey;
    private String logScope = "BATCH";
    private String continuationTrace;
    private final List<String> sourceIds = new ArrayList<>();
    private final List<ApiCall> calls = new ArrayList<>();

    @Data
    public static class ApiCall {
        private String name;
        private String result;
        private long duration;
        private String errorCode;
        private String businessCode;
        private String errorMessage;
        private String errorStack;
        private java.util.Date occurredAt;
    }
}
