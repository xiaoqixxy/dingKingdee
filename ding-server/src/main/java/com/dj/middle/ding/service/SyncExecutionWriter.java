package com.dj.middle.ding.service;

import com.dj.middle.ding.domain.SyncExecution;
import com.dj.middle.ding.mapper.SyncExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 通过独立事务幂等保存批次与分页执行结果。 */
@Service
@RequiredArgsConstructor
public class SyncExecutionWriter {
    /** 提供同步执行批次数据访问能力的映射接口。 */
    private final SyncExecutionMapper mapper;

    /** 在独立事务中建立或恢复批次，校验数据源及预期偏移后登记本页。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(SyncExecution execution) {
        SyncExecution batch;
        // 续传请求按链路恢复；首次请求按幂等请求键建立或复用批次。
        if (execution.getContinuationTrace() != null) {
            batch = mapper.byTrace(execution);
        } else {
            try { mapper.start(execution); }
            catch (org.springframework.dao.DuplicateKeyException duplicate) { /* Same initial delivery. */ }
            batch = mapper.byRequest(execution);
        }
        // 除企业隔离外再次核对数据源指纹，避免用旧游标读取另一套配置。
        if (batch == null || !java.util.Objects.equals(batch.getSourceKey(), execution.getSourceKey()))
            throw new IllegalArgumentException("同步批次不存在或数据源已变更，请重新发起同步");
        execution.setId(batch.getId());
        execution.setTraceId(batch.getTraceId());
        execution.setLogScope(batch.getLogScope());
        // 已结束批次或偏移不匹配时，只允许重放此前成功接收的页。
        boolean finished = "SUCCESS".equals(batch.getStatus()) || "SYNC_LIMIT_REACHED".equals(batch.getErrorCode());
        if ((finished || execution.getPageOffset() != batch.getExpectedOffset()) && mapper.acceptedPage(execution) == null)
            throw new IllegalArgumentException("分页位置与同步批次不一致，请重新发起同步");
        mapper.startPage(execution);
    }

    /** 锁定批次并幂等合并页统计，保存明细与接口日志，重复成功页沿用已接收的游标。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(SyncExecution execution) {
        SyncExecution batch = mapper.lock(execution);
        if (batch == null) throw new IllegalStateException("同步批次不存在");
        // 锁定批次后检查同一偏移是否已经计数，重复回调不能重复累加记录和页数。
        SyncExecution accepted = mapper.acceptedPage(execution);
        boolean success = !"FAILED".equals(execution.getStatus());
        execution.setCounted(success && accepted == null);
        // 首次成功页才能推进期望偏移并累加条数；部分失败页单独记录失败状态。
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
        // 统计使用精确加法，异常溢出不能静默回绕成错误的负数。
        batch.setDuration(Math.addExact(batch.getDuration(), execution.getDuration()));
        if (mapper.finishPage(execution) != 1) throw new IllegalStateException("分页执行状态更新失败");
        if (mapper.finish(batch) != 1) throw new IllegalStateException("同步执行状态更新失败");
        for (SyncExecution.ApiCall call : execution.getCalls()) mapper.api(execution, call);
        mapper.callback(execution);
        // 重复成功请求返回首次接受时的下一偏移和结束标志，保证重试响应稳定。
        if (success && accepted != null) {
            execution.setNextOffset(accepted.getNextOffset());
            execution.setHasMore(accepted.isHasMore());
        }
    }
}
