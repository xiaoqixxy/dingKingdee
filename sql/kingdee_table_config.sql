-- ----------------------------
-- 金蝶表单配置表
-- ----------------------------
drop table if exists kingdee_table_config;
create table kingdee_table_config (
  config_id         bigint(20)      not null auto_increment    comment '主键ID',
  form_name         varchar(100)    default ''                 comment '表单名称',
  form_key          varchar(100)    default ''                 comment '表单标识',
  form_config       text            default null                comment '表单配置(JSON)',
  sort_order        int(4)          default 0                  comment '排序号',
  extend1           varchar(500)    default null               comment '扩展字段1',
  extend2           varchar(500)    default null               comment '扩展字段2',
  extend3           varchar(500)    default null               comment '扩展字段3',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime        default null               comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime        default null               comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id),
  unique key uk_form_key (form_key)
) engine=innodb auto_increment=1 comment = '金蝶表单配置表';

-- ----------------------------
-- 初始化-金蝶表单配置表数据
-- ----------------------------
insert into kingdee_table_config (form_name, form_key, form_config, sort_order, create_by, create_time, remark) values
('付款申请单', 'CN_PAYAPPLY', '{}', 1, 'admin', sysdate(), '金蝶付款申请单'),
('应付单', 'AP_Payable', '{}', 2, 'admin', sysdate(), '金蝶应付单'),
('其他应付单', 'AP_OtherPayable', '{}', 3, 'admin', sysdate(), '金蝶其他应付单'),
('收款单', 'AR_RECEIVEBILL', '{}', 4, 'admin', sysdate(), '金蝶收款单'),
('收款退款单', 'AR_REFUNDBILL', '{}', 5, 'admin', sysdate(), '金蝶收款退款单');
