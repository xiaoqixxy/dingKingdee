-- 订单表添加过期时间字段
ALTER TABLE `ding_order` ADD COLUMN `expire_time` datetime DEFAULT NULL COMMENT '订单过期时间（待支付订单15分钟后自动取消）' AFTER `pay_time`;
