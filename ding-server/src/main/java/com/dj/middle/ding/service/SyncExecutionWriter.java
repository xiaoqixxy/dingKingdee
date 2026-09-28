package com.dj.middle.ding.service;

import com.dj.middle.ding.domain.SyncExecution;
import com.dj.middle.ding.mapper.SyncExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncExecutionWriter {
    private final SyncExecutionMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(SyncExecution execution) {
        SyncExecution batch;
        if (execution.getContinuationTrace() != null) {
            batch = mapper.byTrace(execution);
        } else {
            try { mapper.start(execution); }
            catch (org.springframework.dao.DuplicateKeyException duplicate) { /* Same initial delivery. */ }
            batch = mapper.byRequest(execution);
        }
        if (batch == null || !java.util.Objects.equals(batch.getSourceKey(), execution.getSourceKey()))
            throw new IllegalArgumentException("同步批次不存在或数据源已变更，请重新发起同步");
        execution.setId(batch.getId());
        execution.setTraceId(batch.getTraceId());
        execution.setLogScope(batch.getLogScope());
        boolean finished = "SUCCESS".equals(batch.getStatus()) || "SYNC_LIMIT_REACHED".equals(batch.getErrorCode());
        if ((finished || execution.getPageOffset() != batch.getExpectedOffset()) && mapper.acceptedPage(execution) == null)
            throw new IllegalArgumentException("分页位置与同步批次不一致，请重新发起同步");
        mapper.startPage(execution);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(SyncExecution execution) {
        SyncExecution batch = mapper.lock(execution);
        if (batch == null) throw new IllegalStateException("同步批次不存在");
        SyncExecution accepted = mapper.acceptedPage(execution);
        boolean success = !"FAILED".equals(execution.getStatus());
        execution.setCounted(success && accepted == null);
        if (execution.isCounted()) {
            if (execution.getPageOffset() != batch.getExpectedOffset()) throw new IllegalStateException("分页完成顺序无效");
            batch.setTotal(Math.addExact(batch.getTotal(), execution.getTotal()));
            batch.setSuccess(Math.addExact(batch.getSuccess(), execution.getSuccess()));
            batch.setSkipped(Math.addExact(batch.getSkipped(), execution.getSkipped()));
            batch.setPageCount(batch.getPageCount() + 1);
            batch.setExpectedOffset(execution.getNextOffset());
            batch.setStatus(execution.isHasMore() ? "RUNNING" : execution.getStatus());
            batch.setErrorCode(execution.getErrorCode());
            batch.setErrorMessage(execution.getErrorMessage());
            batch.setErrorStack(execution.getErrorStack());
            if (!execution.getSourceIds().isEmpty()) mapper.items(execution);
        } else if (accepted == null) {
            batch.setStatus(batch.getSuccess() > 0 ? "PARTIAL" : "FAILED");
            batch.setErrorCode(execution.getErrorCode());
            batch.setErrorMessage(execution.getErrorMessage());
            batch.setErrorStack(execution.getErrorStack());
        }
        batch.setDuration(Math.addExact(batch.getDuration(), execution.getDuration()));
        if (mapper.finishPage(execution) != 1) throw new IllegalStateException("分页执行状态更新失败");
        if (mapper.finish(batch) != 1) throw new IllegalStateException("同步执行状态更新失败");
        for (SyncExecution.ApiCall call : execution.getCalls()) mapper.api(execution, call);
        mapper.callback(execution);
        if (success && accepted != null) {
            execution.setNextOffset(accepted.getNextOffset());
            execution.setHasMore(accepted.isHasMore());
        }
    }
}
