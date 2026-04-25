-- 钉钉多维表数据同步项目 - 计费模块SQL
-- 创建数据库表

-- 1. 产品配置表（ding_product）
CREATE TABLE `ding_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID（主键）',
  `product_name` varchar(64) NOT NULL COMMENT '产品名称（如：基础版/专业版/企业版）',
  `product_type` tinyint NOT NULL COMMENT '产品类型 1=包月 2=包年 3=按量付费',
  `product_category` varchar(32) DEFAULT NULL COMMENT '产品分类（如：基础版/专业版/企业版）',
  `is_default` tinyint DEFAULT 0 COMMENT '是否默认产品 0=否 1=是',
  `price` decimal(10,2) NOT NULL COMMENT '产品价格（单位：元）',
  `sync_form_limit` int NOT NULL DEFAULT 0 COMMENT '可绑定同步表单数量（上限）',
  `single_sync_limit` int NOT NULL DEFAULT 0 COMMENT '单次同步数据上限（条）',
  `month_sync_limit` int NOT NULL DEFAULT 0 COMMENT '每月总同步数据上限（条）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '产品状态 1=上架 2=下架',
  `sort` int DEFAULT 0 COMMENT '排序权重（数字越大，排序越靠前）',
  `remark` varchar(500) DEFAULT NULL COMMENT '产品描述（说明产品权益、限制等）',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1（可用于存储省份编码、渠道信息等）',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2（可用于存储自定义配置JSON、标记位等）',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3（可用于存储备用信息、版本号等）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-产品配置表';

-- 2. 租户企业表（ding_tenant）
CREATE TABLE `ding_tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（唯一标识）',
  `corp_name` varchar(100) NOT NULL COMMENT '企业名称',
  `industry` varchar(64) DEFAULT NULL COMMENT '企业所属行业',
  `corp_logo_url` varchar(500) DEFAULT NULL COMMENT '企业logo',
  `license_code` varchar(64) DEFAULT NULL COMMENT '序列号',
  `auth_channel` varchar(32) DEFAULT NULL COMMENT '渠道码',
  `auth_channel_type` varchar(32) DEFAULT NULL COMMENT '渠道类型',
  `is_authenticated` tinyint DEFAULT 0 COMMENT '是否认证 0=否 1=是',
  `auth_level` tinyint DEFAULT 0 COMMENT '认证等级 0=未认证 1=高级 2=中级 3=初级',
  `invite_url` varchar(500) DEFAULT NULL COMMENT '企业邀请链接',
  `contact_user` varchar(32) DEFAULT NULL COMMENT '企业联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系人电话',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '企业状态 1=正常 2=停用',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ding_corp_id` (`ding_corp_id`) COMMENT '钉钉企业ID唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-租户企业表';

-- 3. 产品订阅表（ding_subscription）
CREATE TABLE `ding_subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订阅ID（主键）',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（关联企业）',
  `product_id` bigint NOT NULL COMMENT '当前订阅的产品ID（关联ding_product）',
  `product_type` tinyint NOT NULL COMMENT '产品类型 1=包月 2=包年',
  `start_time` datetime NOT NULL COMMENT '产品生效时间',
  `end_time` datetime NOT NULL COMMENT '产品到期时间',
  `auto_renew` tinyint DEFAULT 0 COMMENT '是否自动续费 0=否 1=是',
  `used_form_count` int DEFAULT 0 COMMENT '已使用绑定表单数量（实时统计）',
  `month_used_sync` int DEFAULT 0 COMMENT '本月已同步数据总量（条）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '订阅状态 1=生效中 2=已过期 3=已取消',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ding_corp_id` (`ding_corp_id`) COMMENT '一个企业仅存在一条有效订阅'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-产品订阅表';

-- 4. 订单表（ding_order）
CREATE TABLE `ding_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID（主键）',
  `order_no` varchar(64) NOT NULL COMMENT '唯一订单号（自定义生成，用于对账）',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（关联企业）',
  `product_id` bigint NOT NULL COMMENT '购买/变更的产品ID（关联ding_product）',
  `order_type` tinyint NOT NULL COMMENT '订单类型 1=新购 2=升级 3=降级 4=续费',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额（单位：元）',
  `pay_amount` decimal(10,2) DEFAULT 0.00 COMMENT '实付金额（单位：元，扣除优惠后）',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态 0=待支付 1=已支付 2=已取消 3=退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `pay_channel` varchar(32) DEFAULT NULL COMMENT '支付渠道（如：支付宝/微信/钉钉支付）',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '第三方支付流水号（用于对账）',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注（如：产品升级补差价）',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`) COMMENT '订单号唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-订单表';

-- 5. 支付日志表（ding_payment_log）
CREATE TABLE `ding_payment_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID（主键）',
  `order_no` varchar(64) NOT NULL COMMENT '关联订单号（关联ding_order）',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（关联企业）',
  `pay_channel` varchar(32) NOT NULL COMMENT '支付渠道',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '第三方支付平台流水号',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额（单位：元）',
  `status` tinyint NOT NULL COMMENT '支付状态 1=成功 2=失败',
  `raw_data` text COMMENT '第三方支付回调原始数据（用于问题排查）',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-支付日志表';

-- 6. 产品变更记录表（ding_product_change_log）
CREATE TABLE `ding_product_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID（主键）',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（关联企业）',
  `old_product_id` bigint NOT NULL COMMENT '原产品ID（变更前的产品）',
  `new_product_id` bigint NOT NULL COMMENT '新产品ID（变更后的产品）',
  `change_type` tinyint NOT NULL COMMENT '变更类型 1=升级 2=降级',
  `order_no` varchar(64) DEFAULT NULL COMMENT '关联订单号（变更对应的订单）',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作人（可记录系统/人工操作）',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-产品变更记录表';

-- 7. 同步表单配置表（ding_sync_form）
CREATE TABLE `ding_sync_form` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID（主键）',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（关联企业）',
  `ding_form_id` varchar(64) NOT NULL COMMENT '钉钉多维表ID（唯一标识）',
  `form_name` varchar(100) NOT NULL COMMENT '表单名称',
  `source_system` varchar(64) NOT NULL COMMENT '三方数据来源系统（如：CRM/ERP）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '表单状态 1=启用 2=停用',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_corp_form` (`ding_corp_id`,`ding_form_id`) COMMENT '一个企业的一个表单仅允许绑定一次'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-同步表单配置表';

-- 8. 数据同步日志表（ding_sync_log）
CREATE TABLE `ding_sync_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID（主键）',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（关联企业）',
  `ding_form_id` varchar(64) NOT NULL COMMENT '钉钉多维表ID（关联同步表单）',
  `sync_count` int NOT NULL COMMENT '本次同步数据条数',
  `status` tinyint NOT NULL COMMENT '同步状态 1=成功 2=失败 3=超量拦截',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '异常信息（同步失败/拦截时填写）',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-数据同步日志表';

-- 9. 发票申请表（ding_invoice）
CREATE TABLE `ding_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '发票ID（主键）',
  `ding_corp_id` varchar(64) NOT NULL COMMENT '钉钉企业ID（关联企业）',
  `order_no` varchar(64) NOT NULL COMMENT '关联订单号（关联ding_order）',
  `invoice_type` tinyint NOT NULL COMMENT '发票类型 1=普通发票 2=增值税专用发票',
  `invoice_title` varchar(100) NOT NULL COMMENT '发票抬头',
  `tax_no` varchar(32) DEFAULT NULL COMMENT '纳税人识别号（专票必填）',
  `amount` decimal(10,2) NOT NULL COMMENT '开票金额（单位：元）',
  `status` tinyint DEFAULT 0 COMMENT '开票状态 0=待开票 1=已开票',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(255) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉-发票申请表';

-- 初始化产品数据
INSERT INTO `ding_product` (`product_name`, `product_type`, `product_category`, `is_default`, `price`, `sync_form_limit`, `single_sync_limit`, `month_sync_limit`, `status`, `sort`, `remark`) VALUES
('基础版', 1, '基础版', 1, 0.00, 3, 1000, 10000, 1, 1, '适合小型团队使用，包含3个表单绑定，单次同步1000条，每月10000条限制'),
('专业版', 1, '专业版', 0, 299.00, 10, 5000, 50000, 1, 2, '适合中型企业使用，包含10个表单绑定，单次同步5000条，每月50000条限制'),
('企业版', 2, '企业版', 0, 2999.00, 50, 20000, 200000, 1, 3, '适合大型企业使用，包含50个表单绑定，单次同步20000条，每月200000条限制');
