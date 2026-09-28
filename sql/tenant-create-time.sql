-- Historical missing creation dates are assigned the repair time (not original registration time).
SET SESSION time_zone = '+08:00';
UPDATE ding_tenant SET create_time = CURRENT_TIMESTAMP(3) WHERE create_time IS NULL;
ALTER TABLE ding_tenant MODIFY COLUMN create_time datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
