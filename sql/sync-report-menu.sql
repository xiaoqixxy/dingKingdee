-- Reuse execution-log permissions; do not grant access to additional roles.
START TRANSACTION;
INSERT INTO sys_menu (menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT '同步执行报表',p.menu_id,0,'syncReport','admin/syncReport/index','SyncReport',1,1,'C','0','0','ding:execution:list','chart','admin',NOW(),'同步运行情况与同步条数增减'
FROM sys_menu p WHERE p.path='integration' AND p.parent_id=0
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.path='syncReport' AND m.parent_id=p.menu_id);
INSERT INTO sys_role_menu (role_id,menu_id)
SELECT DISTINCT rm.role_id,target.menu_id FROM sys_role_menu rm
JOIN sys_menu source ON source.menu_id=rm.menu_id AND source.perms='ding:execution:list'
JOIN sys_menu target ON target.component='admin/syncReport/index'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu existing WHERE existing.role_id=rm.role_id AND existing.menu_id=target.menu_id);
COMMIT;
