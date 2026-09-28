-- Run once after the existing database-log-model migration, before deploying this version.
-- The signed records callback supplies a Kingdee source form ID, not ding_sync_form.id.
-- Keep the association NULL until a verified DingTalk form binding is available.
ALTER TABLE ding_sync_execution
  MODIFY COLUMN sync_form_id bigint NULL COMMENT '关联ding_sync_form.id；回调未提供可靠绑定时为空';
