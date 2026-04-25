-- ----------------------------
-- 菜单SQL - 集成配置
-- ----------------------------

-- 添加父菜单：集成配置
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2001, '集成配置', 0, 5, 'kingdee', NULL, 1, 0, 'M', '0', '0', '', 'system', 'admin', NOW(), '', NULL, '金蝶集成模块');

-- 添加子菜单：表单配置
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2002, '表单配置', 2001, 1, 'tableConfig', 'kingdee/tableConfig/index', 1, 0, 'C', '0', '0', 'kingdee:tableConfig:list', 'table', 'admin', NOW(), '', NULL, '金蝶表单配置管理');

-- 添加按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2003, '表单配置新增', 2002, 1, '', NULL, 1, 0, 'F', '0', '0', 'kingdee:tableConfig:add', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2004, '表单配置修改', 2002, 2, '', NULL, 1, 0, 'F', '0', '0', 'kingdee:tableConfig:edit', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2005, '表单配置删除', 2002, 3, '', NULL, 1, 0, 'F', '0', '0', 'kingdee:tableConfig:remove', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2006, '表单配置导出', 2002, 4, '', NULL, 1, 0, 'F', '0', '0', 'kingdee:tableConfig:export', '#', 'admin', NOW(), '', NULL, '');
