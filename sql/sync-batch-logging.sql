-- Run once after sync-execution-callback.sql, before deploying the new backend.
-- Existing page logs keep their original meaning; do not guess historical batch membership.
ALTER TABLE ding_sync_execution
 ADD COLUMN source_key char(64) CHARACTER SET ascii NULL,
 ADD COLUMN log_scope varchar(16) NOT NULL DEFAULT 'PAGE',
 ADD COLUMN page_count int unsigned NOT NULL DEFAULT 0,
 ADD COLUMN next_offset int unsigned NOT NULL DEFAULT 0,
 ADD COLUMN error_stack longtext NULL;

CREATE TABLE ding_sync_execution_page (
 id bigint NOT NULL AUTO_INCREMENT,
 execution_id bigint NOT NULL,
 ding_corp_id varchar(64) NOT NULL,
 page_offset int unsigned NOT NULL,
 next_offset int unsigned NOT NULL DEFAULT 0,
 has_more tinyint NOT NULL DEFAULT 0,
 counted tinyint NOT NULL DEFAULT 0 COMMENT 'Only first successful result at each offset counts toward batch totals',
 status varchar(16) NOT NULL,
 success_count int unsigned NOT NULL DEFAULT 0,
 skipped_count int unsigned NOT NULL DEFAULT 0,
 duration_ms bigint unsigned NULL,
 error_code varchar(64) NULL,
 error_message varchar(1000) NULL,
 error_stack longtext NULL,
 request_summary json NULL,
 response_summary json NULL,
 started_at datetime(3) NOT NULL,
 finished_at datetime(3) NULL,
 PRIMARY KEY(id),
 KEY idx_page_execution(execution_id,page_offset,counted),
 KEY idx_page_corp(ding_corp_id,started_at,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Batch page delivery attempts';

ALTER TABLE ding_api_call_log ADD COLUMN page_id bigint NULL,
 ADD COLUMN error_stack longtext NULL,
 ADD KEY idx_api_page(page_id);
