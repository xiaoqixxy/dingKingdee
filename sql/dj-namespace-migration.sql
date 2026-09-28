-- Deploy with the renamed backend. Preview matches and back up matching rows first.
-- Do not update historical audit logs; these describe the code used at the time.
SELECT job_id, invoke_target FROM sys_job WHERE invoke_target LIKE 'com.ruoyi.%';
SELECT table_id, package_name, gen_path FROM gen_table
WHERE package_name = 'com.ruoyi' OR package_name LIKE 'com.ruoyi.%';

START TRANSACTION;
-- Only replace the class prefix, never method parameter values.
UPDATE sys_job SET invoke_target = CONCAT('com.dj.', SUBSTRING(invoke_target, 11))
WHERE invoke_target LIKE 'com.ruoyi.%';
UPDATE gen_table SET package_name = CONCAT('com.dj', SUBSTRING(package_name, 10))
WHERE package_name = 'com.ruoyi' OR package_name LIKE 'com.ruoyi.%';
COMMIT;

-- Review custom gen_path values manually against the new module directories.
-- This project uses the default in-memory Quartz store. If JDBC JobStore is enabled
-- in another environment, rebuild schedules during its deployment instead of doing
-- text replacements inside serialized QRTZ_* blobs.
