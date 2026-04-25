-- 钉钉计费模块菜单SQL
-- 在若依系统菜单管理中执行

-- 父级菜单：计费管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('计费管理', 0, 6, 'ding', NULL, 1, 0, 'M', '0', '0', '', 'money-circle', 'admin', NOW(), NULL, NULL, '钉钉计费模块父菜单');

-- 产品配置
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('产品配置', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='计费管理' LIMIT 1) m), 1, 'product', 'ding/product/index', 1, 0, 'C', '0', '0', 'ding:product:list', 'shopping', 'admin', NOW(), NULL, NULL, '产品配置菜单');

-- 产品配置按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('产品配置查询', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='产品配置' LIMIT 1) m), 1, '', '', 1, 0, 'F', '0', '0', 'ding:product:query', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('产品配置新增', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='产品配置' LIMIT 1) m), 2, '', '', 1, 0, 'F', '0', '0', 'ding:product:add', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('产品配置修改', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='产品配置' LIMIT 1) m), 3, '', '', 1, 0, 'F', '0', '0', 'ding:product:edit', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('产品配置删除', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='产品配置' LIMIT 1) m), 4, '', '', 1, 0, 'F', '0', '0', 'ding:product:remove', '#', 'admin', NOW(), NULL, NULL, '');

-- 租户企业管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('租户企业管理', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='计费管理' LIMIT 1) m), 2, 'tenant', 'ding/tenant/index', 1, 0, 'C', '0', '0', 'ding:tenant:list', 'company', 'admin', NOW(), NULL, NULL, '租户企业管理菜单');

-- 租户企业按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('租户企业查询', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='租户企业管理' LIMIT 1) m), 1, '', '', 1, 0, 'F', '0', '0', 'ding:tenant:query', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('租户企业新增', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='租户企业管理' LIMIT 1) m), 2, '', '', 1, 0, 'F', '0', '0', 'ding:tenant:add', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('租户企业修改', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='租户企业管理' LIMIT 1) m), 3, '', '', 1, 0, 'F', '0', '0', 'ding:tenant:edit', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('租户企业删除', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='租户企业管理' LIMIT 1) m), 4, '', '', 1, 0, 'F', '0', '0', 'ding:tenant:remove', '#', 'admin', NOW(), NULL, NULL, '');

-- 订单管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('订单管理', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='计费管理' LIMIT 1) m), 3, 'order', 'ding/order/index', 1, 0, 'C', '0', '0', 'ding:order:list', 'order', 'admin', NOW(), NULL, NULL, '订单管理菜单');

-- 订单按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('订单查询', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='订单管理' LIMIT 1) m), 1, '', '', 1, 0, 'F', '0', '0', 'ding:order:query', '#', 'admin', NOW(), NULL, NULL, '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('订单删除', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='订单管理' LIMIT 1) m), 2, '', '', 1, 0, 'F', '0', '0', 'ding:order:remove', '#', 'admin', NOW(), NULL, NULL, '');

-- 套餐订阅管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('套餐订阅管理', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='计费管理' LIMIT 1) m), 4, 'subscription', 'ding/subscription/index', 1, 0, 'C', '0', '0', 'ding:subscription:list', 'ticket', 'admin', NOW(), NULL, NULL, '套餐订阅管理菜单');

-- 同步表单配置
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('同步表单配置', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='计费管理' LIMIT 1) m), 5, 'syncForm', 'ding/syncForm/index', 1, 0, 'C', '0', '0', 'ding:syncForm:list', 'list', 'admin', NOW(), NULL, NULL, '同步表单配置菜单');

-- 数据同步日志
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('数据同步日志', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='计费管理' LIMIT 1) m), 6, 'syncLog', 'ding/syncLog/index', 1, 0, 'C', '0', '0', 'ding:syncLog:list', 'log', 'admin', NOW(), NULL, NULL, '数据同步日志菜单');

-- 发票申请管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) 
VALUES ('发票申请管理', (SELECT m.menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name='计费管理' LIMIT 1) m), 7, 'invoice', 'ding/invoice/index', 1, 0, 'C', '0', '0', 'ding:invoice:list', 'ticket', 'admin', NOW(), NULL, NULL, '发票申请管理菜单');
