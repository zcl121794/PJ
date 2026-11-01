-- =============================================================
-- 农村社区管理系统 初始数据（种子数据）脚本
-- 说明：所有账号初始密码均为 123456（BCrypt 加密存储）
-- =============================================================
USE `rural_community`;

-- ---------- 1. 角色 ----------
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '管理员',       'ADMIN',        '系统管理员，负责用户、角色、权限与系统配置'),
(2, '村委工作人员', 'COMMITTEE',    '村委会工作人员，负责公告发布、信息维护、诉求受理、办事审批'),
(3, '网格员',       'GRID_WORKER',  '负责网格内信息采集、诉求走访、矛盾排查'),
(4, '村民',         'VILLAGER',     '系统服务对象，浏览公告、提交诉求、在线办事');

-- ---------- 2. 菜单权限 ----------
-- 目录/菜单/按钮，parent_id 指向父级
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
-- 公告管理（100）
(100,  0,  '公告管理',     1, '/announcement',          NULL,              NULL,                  'bell',     1),
(101,  100,'公告列表',     2, 'list',                   'announcement/list',  'announcement:list',  NULL,       1),
(10101,101,'公告新增',     3, NULL,                     NULL,                'announcement:add',   NULL,       1),
(10102,101,'公告编辑',     3, NULL,                     NULL,                'announcement:edit',  NULL,       2),
(10103,101,'公告删除',     3, NULL,                     NULL,                'announcement:remove',NULL,      3),
(10104,101,'公告审核',     3, NULL,                     NULL,                'announcement:audit', NULL,       4),
(102,  100,'公告分类',     2, 'category',               'announcement/category', 'announcement:category', NULL, 2),
-- 村民信息管理（200）
(200,  0,  '村民信息管理', 1, '/villager',              NULL,              NULL,                  'user',     2),
(201,  200,'户信息管理',   2, 'household',              'villager/household','villager:household', NULL,      1),
(20101,201,'户新增',       3, NULL,                     NULL,                'villager:household:add',   NULL, 1),
(20102,201,'户编辑',       3, NULL,                     NULL,                'villager:household:edit',  NULL, 2),
(20103,201,'户删除',       3, NULL,                     NULL,                'villager:household:remove',NULL, 3),
(202,  200,'村民档案',     2, 'profile',                'villager/profile',  'villager:list',      NULL,      2),
(20201,202,'村民新增',     3, NULL,                     NULL,                'villager:add',       NULL,       1),
(20202,202,'村民编辑',     3, NULL,                     NULL,                'villager:edit',      NULL,       2),
(20203,202,'村民删除',     3, NULL,                     NULL,                'villager:remove',    NULL,       3),
(20204,202,'导入导出',     3, NULL,                     NULL,                'villager:import',    NULL,       4),
-- 诉求反馈管理（300）
(300,  0,  '诉求反馈管理', 1, '/appeal',                NULL,              NULL,                  'chat',     3),
(301,  300,'诉求列表',     2, 'list',                   'appeal/list',      'appeal:list',        NULL,       1),
(30101,301,'诉求受理',     3, NULL,                     NULL,                'appeal:accept',      NULL,       1),
(30102,301,'诉求分派',     3, NULL,                     NULL,                'appeal:assign',      NULL,       2),
(30103,301,'诉求处理',     3, NULL,                     NULL,                'appeal:handle',      NULL,       3),
(30104,301,'诉求办结',     3, NULL,                     NULL,                'appeal:finish',      NULL,       4),
(302,  300,'诉求分类',     2, 'category',               'appeal/category',  'appeal:category',    NULL,       2),
-- 办事服务管理（400）
(400,  0,  '办事服务管理', 1, '/service',               NULL,              NULL,                  'document', 4),
(401,  400,'办事指南',     2, 'guide',                  'service/guide',    'service:guide',      NULL,       1),
(402,  400,'申办管理',     2, 'apply',                  'service/apply',    'service:apply',      NULL,       2),
(40201,402,'申办受理',     3, NULL,                     NULL,                'service:accept',     NULL,       1),
(40202,402,'申办审核',     3, NULL,                     NULL,                'service:audit',      NULL,       2),
(40203,402,'申办办结',     3, NULL,                     NULL,                'service:finish',     NULL,       3),
(403,  400,'事项管理',     2, 'item',                   'service/item',     'service:item',       NULL,       3),
-- 权限管理（500）
(500,  0,  '权限管理',     1, '/system',                NULL,              NULL,                  'setting',  5),
(501,  500,'用户管理',     2, 'user',                   'system/user',      'system:user',        NULL,       1),
(502,  500,'角色管理',     2, 'role',                   'system/role',      'system:role',        NULL,       2),
(503,  500,'菜单管理',     2, 'menu',                   'system/menu',      'system:menu',        NULL,       3),
-- 数据统计（600）
(600,  0,  '数据统计',     1, '/dashboard',             NULL,              NULL,                  'chart',    6),
(601,  600,'数据看板',     2, 'index',                  'dashboard/index',  'dashboard:view',     NULL,       1);

-- ---------- 3. 角色-菜单关联 ----------
-- 管理员：全部菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1,100),(1,101),(1,10101),(1,10102),(1,10103),(1,10104),(1,102),
(1,200),(1,201),(1,20101),(1,20102),(1,20103),(1,202),(1,20201),(1,20202),(1,20203),(1,20204),
(1,300),(1,301),(1,30101),(1,30102),(1,30103),(1,30104),(1,302),
(1,400),(1,401),(1,402),(1,40201),(1,40202),(1,40203),(1,403),
(1,500),(1,501),(1,502),(1,503),
(1,600),(1,601);

-- 村委工作人员：除权限管理外全部
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2,100),(2,101),(2,10101),(2,10102),(2,10103),(2,10104),(2,102),
(2,200),(2,201),(2,20101),(2,20102),(2,20103),(2,202),(2,20201),(2,20202),(2,20203),(2,20204),
(2,300),(2,301),(2,30101),(2,30102),(2,30103),(2,30104),(2,302),
(2,400),(2,401),(2,402),(2,40201),(2,40202),(2,40203),(2,403),
(2,600),(2,601);

-- 网格员：公告、村民信息（采集）、诉求处理、办事服务（网格初审）、数据统计
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(3,100),(3,101),(3,10101),(3,10102),
(3,200),(3,201),(3,202),(3,20201),(3,20202),
(3,300),(3,301),(3,30103),(3,30104),
(3,400),(3,401),(3,402),(3,40202),
(3,600),(3,601);

-- 村民：公告查看、诉求提交、办事指南/申办（村民端）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(4,100),(4,101),
(4,300),(4,301),
(4,400),(4,401),(4,402);

-- ---------- 4. 用户 ----------
-- 所有账号初始密码：123456
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `gender`, `village_id`, `grid_id`, `is_auth`, `status`) VALUES
(1, 'admin',        '$2a$10$M2URK9OR1invsJilAo.HxuESJSPfEjfxDuk9j7y3vSZqt73HCsU2e', '系统管理员',   '13800000001', 1, 1, NULL, 1, 1),
(2, 'committee1',   '$2a$10$M2URK9OR1invsJilAo.HxuESJSPfEjfxDuk9j7y3vSZqt73HCsU2e', '赵村委',       '13800000002', 1, 1, NULL, 1, 1),
(3, 'gridworker1',  '$2a$10$M2URK9OR1invsJilAo.HxuESJSPfEjfxDuk9j7y3vSZqt73HCsU2e', '钱网格',       '13800000003', 1, 1, 1,    1, 1),
(4, 'zhangsan',     '$2a$10$M2URK9OR1invsJilAo.HxuESJSPfEjfxDuk9j7y3vSZqt73HCsU2e', '张三',         '13800000004', 1, 1, 1,    1, 1),
(5, 'lisi',         '$2a$10$M2URK9OR1invsJilAo.HxuESJSPfEjfxDuk9j7y3vSZqt73HCsU2e', '李四',         '13800000005', 1, 1, 1,    1, 1);

-- ---------- 5. 用户-角色关联 ----------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),            -- admin -> 管理员
(2, 2),            -- committee1 -> 村委工作人员
(3, 3),            -- gridworker1 -> 网格员
(4, 4),            -- zhangsan -> 村民
(5, 4);            -- lisi -> 村民

-- ---------- 6. 数据字典 ----------
-- 诉求分类
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('appeal_category', '环境整治', 'environment', 1),
('appeal_category', '邻里纠纷', 'dispute',     2),
('appeal_category', '基础设施', 'infrastructure', 3),
('appeal_category', '民生保障', 'livelihood',  4),
('appeal_category', '意见建议', 'suggestion',  5),
('appeal_category', '其他',     'other',       6);
-- 公告分类
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('announcement_category', '政策法规', 'policy',    1),
('announcement_category', '村务公开', 'affairs',   2),
('announcement_category', '通知公告', 'notice',    3),
('announcement_category', '便民信息', 'convenience', 4),
('announcement_category', '会议通知', 'meeting',   5);
-- 学历
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('education', '小学',     'primary',  1),
('education', '初中',     'junior',   2),
('education', '高中',     'senior',   3),
('education', '大专',     'college',  4),
('education', '本科及以上', 'bachelor', 5);
-- 政治面貌
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('political_status', '群众', 'mass',     1),
('political_status', '团员', 'league',   2),
('political_status', '党员', 'party',    3),
('political_status', '其他', 'other',    4);
-- 户属性
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('house_type', '一般户', 'normal',  1),
('house_type', '低保户', 'dibao',   2),
('house_type', '五保户', 'wubao',   3),
('house_type', '残疾户', 'disabled', 4);
-- 特殊群体标签
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('special_tag', '低保',     'dibao',        1),
('special_tag', '五保',     'wubao',        2),
('special_tag', '残疾',     'disabled',     3),
('special_tag', '留守老人', 'left_elderly', 4),
('special_tag', '留守儿童', 'left_children', 5),
('special_tag', '退役军人', 'veteran',      6);
-- 就业状态
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('employment_status', '务农',   'farming',   1),
('employment_status', '务工',   'worker',    2),
('employment_status', '无业',   'unemployed',3),
('employment_status', '退休',   'retired',   4),
('employment_status', '学生',   'student',   5);

-- ---------- 7. 户信息 ----------
INSERT INTO `household` (`id`, `household_no`, `house_holder_id`, `house_type`, `register_type`, `address`, `grid_id`, `contact_phone`, `member_count`) VALUES
(1, 'H2026001', 1, '一般户', 1, '一组1号',  1, '13800000004', 3),
(2, 'H2026002', 4, '低保户', 1, '一组2号',  1, '13800000005', 2),
(3, 'H2026003', 6, '一般户', 1, '二组5号',  2, '13800000006', 1);

-- ---------- 8. 村民档案 ----------
INSERT INTO `villager` (`id`, `household_id`, `name`, `id_card`, `gender`, `birth_date`, `nation`, `education`, `political_status`, `phone`, `health_status`, `employment_status`, `special_tag`, `is_householder`, `relation`, `user_id`) VALUES
(1, 1, '张三',   '110101199001011234', 1, '1990-01-01', '汉族', '高中',   '群众', '13800000004', '健康', '务农',   NULL,            1, '本人', 4),
(2, 1, '王芳',   '110101199203054567', 2, '1992-03-05', '汉族', '大专',   '群众', '13800000007', '健康', '务工',   NULL,            0, '配偶', NULL),
(3, 1, '张小明', '110101201506154321', 1, '2015-06-15', '汉族', '小学',   '群众', NULL,          '健康', '学生',   '留守儿童',      0, '子',   NULL),
(4, 2, '李四',   '110101197512304321', 1, '1975-12-30', '汉族', '初中',   '党员', '13800000005', '残疾', '无业',   '低保,残疾',     1, '本人', 5),
(5, 2, '李丽',   '110101197808087654', 2, '1978-08-08', '汉族', '初中',   '群众', '13800000008', '健康', '务农',   '低保',          0, '配偶', NULL),
(6, 3, '王五',   '110101195508156789', 2, '1955-08-15', '汉族', '小学',   '群众', '13800000006', '健康', '退休',   '留守老人',      1, '本人', NULL);

-- ---------- 9. 办事事项 ----------
INSERT INTO `service_item` (`id`, `item_name`, `category`, `condition_desc`, `material_desc`, `process_desc`, `time_limit`, `location`, `contact_phone`, `need_grid_audit`) VALUES
(1, '户籍相关证明开具', '户籍', '本村户籍村民', '身份证、户口簿', '村民申办 → 村委审核 → 出具证明', 3, '村委会服务大厅', '13800000002', 0),
(2, '低保申请',         '民政', '家庭人均收入低于低保标准', '身份证、收入证明、家庭情况说明', '村民申办 → 网格员初审 → 村委复审 → 上报', 7, '村委会服务大厅', '13800000002', 1),
(3, '宅基地审批',       '国土', '符合一户一宅政策', '身份证、户口簿、宅基地申请表', '村民申办 → 网格员初审 → 村委复审 → 上报', 10, '村委会服务大厅', '13800000002', 1),
(4, '医保/社保参保登记', '社保', '本村户籍村民', '身份证、户口簿', '村民申办 → 村委受理 → 登记办理', 3, '村委会服务大厅', '13800000002', 0),
(5, '生育登记',         '计生', '符合生育政策', '身份证、结婚证', '村民申办 → 村委受理 → 登记', 3, '村委会服务大厅', '13800000002', 0),
(6, '残疾人补贴申请',   '民政', '持有残疾证', '身份证、残疾证、银行账户', '村民申办 → 网格员初审 → 村委复审 → 上报', 7, '村委会服务大厅', '13800000002', 1);

-- ---------- 10. 示例公告 ----------
INSERT INTO `announcement` (`id`, `title`, `content`, `category`, `publish_type`, `status`, `is_top`, `publish_by`, `audit_by`, `audit_time`, `effective_time`, `read_count`) VALUES
(1, '关于开展农村人居环境整治的通知', '各位村民：为提升村容村貌，村委会决定于本月开展人居环境集中整治行动，请各家各户做好房前屋后清理工作。', '通知公告', 1, 2, 1, 2, 2, '2026-10-20 09:00:00', '2026-10-20 09:00:00', 25),
(2, '2026年度城乡居民医保缴费开始啦', '2026年度城乡居民基本医疗保险集中缴费已开始，请广大村民按时参保缴费。', '便民信息', 1, 2, 0, 2, 2, '2026-10-15 09:00:00', '2026-10-15 09:00:00', 40),
(3, '村务公开：三季度村级财务收支情况公示', '现将本村2026年第三季度财务收支情况予以公示，欢迎广大村民监督。', '村务公开', 1, 2, 0, 2, 2, '2026-10-10 09:00:00', '2026-10-10 09:00:00', 30);

-- ---------- 11. 示例诉求 ----------
INSERT INTO `appeal` (`id`, `appeal_no`, `villager_id`, `category`, `title`, `content`, `location`, `status`, `handler_id`, `processor_id`, `submit_time`, `accept_time`, `finish_time`, `deadline_time`, `satisfaction`) VALUES
(1, 'AP20261001001', 1, '基础设施', '村口路灯损坏', '村口主干道的三盏路灯近期不亮了，晚上出行不便，希望尽快维修。', '村口主干道', 4, 2, 3, '2026-10-01 10:00:00', '2026-10-01 11:00:00', '2026-10-03 15:00:00', '2026-10-08 17:00:00', 1),
(2, 'AP20261012002', 4, '邻里纠纷', '宅基地边界争议', '与邻居因宅基地边界问题产生纠纷，希望村委协调解决。', '二组', 2, 2, 3, '2026-10-12 09:30:00', '2026-10-12 10:00:00', NULL, '2026-10-19 17:00:00', 0),
(3, 'AP20261018003', 6, '民生保障', '申请高龄补贴', '本人已年满70周岁，想了解高龄补贴申请条件和流程。', '二组5号', 0, NULL, NULL, '2026-10-18 14:00:00', NULL, NULL, '2026-10-25 17:00:00', 0);

-- ---------- 12. 诉求流转记录 ----------
INSERT INTO `appeal_flow` (`appeal_id`, `operate_type`, `operator_id`, `operate_time`, `operate_comment`) VALUES
(1, 1, 4, '2026-10-01 10:00:00', '提交诉求'),
(1, 2, 2, '2026-10-01 11:00:00', '受理，分派网格员钱网格处理'),
(1, 3, 2, '2026-10-01 11:05:00', '分派至第一网格'),
(1, 5, 3, '2026-10-02 09:00:00', '走访核实，确认为线路老化，联系电力部门维修'),
(1, 6, 3, '2026-10-03 15:00:00', '已修复完毕，恢复照明'),
(1, 7, 2, '2026-10-03 15:10:00', '办结'),
(1, 9, 4, '2026-10-04 08:00:00', '评价：满意'),
(2, 1, 4, '2026-10-12 09:30:00', '提交诉求'),
(2, 2, 2, '2026-10-12 10:00:00', '受理，分派网格员走访'),
(2, 5, 3, '2026-10-13 10:00:00', '走访双方了解情况，正在协调'),
(3, 1, 6, '2026-10-18 14:00:00', '提交诉求');
