-- =============================================================
-- 农村社区管理系统 数据库初始化脚本
-- MySQL 8.x / utf8mb4 / InnoDB
-- =============================================================
CREATE DATABASE IF NOT EXISTS `rural_community` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `rural_community`;

-- ---------- 权限管理模块 ----------

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`        VARCHAR(50)  NOT NULL                COMMENT '登录账号',
  `password`        VARCHAR(100) NOT NULL                COMMENT '登录密码(BCrypt加密)',
  `real_name`       VARCHAR(50)  NOT NULL                COMMENT '真实姓名',
  `id_card`         VARCHAR(18)  DEFAULT NULL            COMMENT '身份证号',
  `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `gender`          TINYINT      NOT NULL DEFAULT 0      COMMENT '性别(0未知1男2女)',
  `avatar`          VARCHAR(255) DEFAULT NULL            COMMENT '头像',
  `village_id`      BIGINT       DEFAULT NULL            COMMENT '所属村ID',
  `grid_id`         BIGINT       DEFAULT NULL            COMMENT '所属网格ID',
  `is_auth`         TINYINT      NOT NULL DEFAULT 0      COMMENT '实名认证状态(0未认证1已认证)',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态(0停用1启用)',
  `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
  `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       BIGINT       DEFAULT NULL            COMMENT '更新人',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_id_card` (`id_card`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_village_id` (`village_id`),
  KEY `idx_grid_id` (`grid_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
  `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码',
  `description` VARCHAR(255) DEFAULT NULL            COMMENT '角色描述',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态(0停用1启用)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父菜单ID(0顶级)',
  `menu_name`   VARCHAR(50)  NOT NULL                COMMENT '菜单名称',
  `menu_type`   TINYINT      NOT NULL DEFAULT 1      COMMENT '菜单类型(1目录2菜单3按钮)',
  `path`        VARCHAR(200) DEFAULT NULL            COMMENT '路由地址',
  `component`   VARCHAR(200) DEFAULT NULL            COMMENT '组件路径',
  `perms`       VARCHAR(100) DEFAULT NULL            COMMENT '权限标识',
  `icon`        VARCHAR(100) DEFAULT NULL            COMMENT '图标',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  `visible`     TINYINT      NOT NULL DEFAULT 1      COMMENT '是否显示(0隐藏1显示)',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态(0停用1启用)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ---------- 村民信息管理模块 ----------

CREATE TABLE IF NOT EXISTS `household` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '户ID',
  `household_no`     VARCHAR(30)  NOT NULL                COMMENT '户号',
  `house_holder_id`  BIGINT       DEFAULT NULL            COMMENT '户主ID',
  `house_type`       VARCHAR(20)  DEFAULT '一般户'        COMMENT '户属性',
  `register_type`    TINYINT      DEFAULT 1               COMMENT '户籍类型(1农业2非农业)',
  `address`          VARCHAR(200) DEFAULT NULL            COMMENT '家庭住址',
  `grid_id`          BIGINT       DEFAULT NULL            COMMENT '所属网格ID',
  `contact_phone`    VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `member_count`     INT          NOT NULL DEFAULT 0      COMMENT '家庭人口数',
  `remark`           VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `create_by`        BIGINT       DEFAULT NULL            COMMENT '创建人',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        BIGINT       DEFAULT NULL            COMMENT '更新人',
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_household_no` (`household_no`),
  KEY `idx_house_holder_id` (`house_holder_id`),
  KEY `idx_grid_id` (`grid_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='户信息表';

CREATE TABLE IF NOT EXISTS `villager` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '村民ID',
  `household_id`      BIGINT       NOT NULL                COMMENT '户ID',
  `name`              VARCHAR(50)  NOT NULL                COMMENT '姓名',
  `id_card`           VARCHAR(18)  NOT NULL                COMMENT '身份证号',
  `gender`            TINYINT      DEFAULT 0               COMMENT '性别(0未知1男2女)',
  `birth_date`        DATE         DEFAULT NULL            COMMENT '出生日期',
  `nation`            VARCHAR(20)  DEFAULT '汉族'          COMMENT '民族',
  `education`         VARCHAR(20)  DEFAULT NULL            COMMENT '学历',
  `political_status`  VARCHAR(20)  DEFAULT '群众'          COMMENT '政治面貌',
  `phone`             VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `health_status`     VARCHAR(20)  DEFAULT '健康'          COMMENT '健康状况',
  `employment_status` VARCHAR(20)  DEFAULT NULL            COMMENT '就业状态',
  `special_tag`       VARCHAR(200) DEFAULT NULL            COMMENT '特殊群体标签(逗号分隔)',
  `is_householder`    TINYINT      NOT NULL DEFAULT 0      COMMENT '是否户主(0否1是)',
  `relation`          VARCHAR(20)  DEFAULT '本人'          COMMENT '与户主关系',
  `user_id`           BIGINT       DEFAULT NULL            COMMENT '关联账号ID',
  `remark`            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `create_by`         BIGINT       DEFAULT NULL            COMMENT '创建人',
  `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         BIGINT       DEFAULT NULL            COMMENT '更新人',
  `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`           TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_id_card` (`id_card`),
  KEY `idx_household_id` (`household_id`),
  KEY `idx_name` (`name`),
  KEY `idx_special_tag` (`special_tag`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='村民档案表';

-- ---------- 公告管理模块 ----------

CREATE TABLE IF NOT EXISTS `announcement` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title`           VARCHAR(100) NOT NULL                COMMENT '公告标题',
  `content`         TEXT         NOT NULL                COMMENT '公告正文',
  `category`        VARCHAR(30)  DEFAULT '通知公告'       COMMENT '公告分类',
  `publish_type`    TINYINT      NOT NULL DEFAULT 1      COMMENT '可见范围(1全村2网格定向)',
  `grid_id`         BIGINT       DEFAULT NULL            COMMENT '可见网格ID',
  `status`          TINYINT      NOT NULL DEFAULT 0      COMMENT '状态(0草稿1待审核2已发布3已撤回4已归档)',
  `is_top`          TINYINT      NOT NULL DEFAULT 0      COMMENT '是否置顶(0否1是)',
  `publish_by`      BIGINT       DEFAULT NULL            COMMENT '发布人',
  `audit_by`        BIGINT       DEFAULT NULL            COMMENT '审核人',
  `audit_time`      DATETIME     DEFAULT NULL            COMMENT '审核时间',
  `audit_comment`   VARCHAR(500) DEFAULT NULL            COMMENT '审核意见',
  `effective_time`  DATETIME     DEFAULT NULL            COMMENT '生效时间',
  `expire_time`     DATETIME     DEFAULT NULL            COMMENT '失效时间',
  `read_count`      INT          NOT NULL DEFAULT 0      COMMENT '阅读量',
  `attachment`      VARCHAR(500) DEFAULT NULL            COMMENT '附件路径',
  `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       BIGINT       DEFAULT NULL            COMMENT '更新人',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`),
  KEY `idx_grid_id` (`grid_id`),
  KEY `idx_effective_time` (`effective_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE IF NOT EXISTS `announcement_read` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `announcement_id` BIGINT   NOT NULL COMMENT '公告ID',
  `villager_id`     BIGINT   NOT NULL COMMENT '村民ID',
  `read_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ann_villager` (`announcement_id`,`villager_id`),
  KEY `idx_villager_id` (`villager_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录表';

-- ---------- 诉求反馈管理模块 ----------

CREATE TABLE IF NOT EXISTS `appeal` (
  `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '诉求ID',
  `appeal_no`            VARCHAR(30)  NOT NULL                COMMENT '诉求编号',
  `villager_id`          BIGINT       DEFAULT NULL            COMMENT '提交人ID',
  `category`             VARCHAR(30)  NOT NULL DEFAULT '其他' COMMENT '诉求分类',
  `title`                VARCHAR(100) NOT NULL                COMMENT '诉求标题',
  `content`              TEXT         NOT NULL                COMMENT '诉求描述',
  `location`             VARCHAR(200) DEFAULT NULL            COMMENT '发生地点',
  `is_anonymous`         TINYINT      NOT NULL DEFAULT 0      COMMENT '是否匿名(0否1是)',
  `status`               TINYINT      NOT NULL DEFAULT 0      COMMENT '状态(0待受理1已受理2处理中3已答复4已办结5已归档)',
  `handler_id`           BIGINT       DEFAULT NULL            COMMENT '受理人',
  `processor_id`         BIGINT       DEFAULT NULL            COMMENT '处理人',
  `process_result`       TEXT         DEFAULT NULL            COMMENT '办理结果',
  `submit_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `accept_time`          DATETIME     DEFAULT NULL            COMMENT '受理时间',
  `finish_time`          DATETIME     DEFAULT NULL            COMMENT '办结时间',
  `deadline_time`        DATETIME     DEFAULT NULL            COMMENT '办理时限',
  `is_overtime`          TINYINT      NOT NULL DEFAULT 0      COMMENT '是否超时(0否1是)',
  `satisfaction`         TINYINT      NOT NULL DEFAULT 0      COMMENT '满意度(0未评价1满意2基本满意3不满意)',
  `satisfaction_comment` VARCHAR(500) DEFAULT NULL            COMMENT '评价留言',
  `attachment`           VARCHAR(500) DEFAULT NULL            COMMENT '附件路径',
  `create_by`            BIGINT       DEFAULT NULL            COMMENT '创建人',
  `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`            BIGINT       DEFAULT NULL            COMMENT '更新人',
  `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`              TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appeal_no` (`appeal_no`),
  KEY `idx_villager_id` (`villager_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`),
  KEY `idx_handler_id` (`handler_id`),
  KEY `idx_processor_id` (`processor_id`),
  KEY `idx_deadline_time` (`deadline_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='诉求表';

CREATE TABLE IF NOT EXISTS `appeal_flow` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `appeal_id`       BIGINT        NOT NULL COMMENT '诉求ID',
  `operate_type`    TINYINT       NOT NULL COMMENT '操作类型(1提交2受理3分派4转办5走访6答复7办结8归档9评价10上报村委11退回12审核退回13修改结果14催办15满意16不满意17追问)',
  `operator_id`     BIGINT        NOT NULL COMMENT '操作人',
  `operate_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `operate_comment` VARCHAR(1000) DEFAULT NULL COMMENT '操作意见',
  PRIMARY KEY (`id`),
  KEY `idx_appeal_id` (`appeal_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='诉求流转记录表';

-- ---------- 办事服务管理模块 ----------

CREATE TABLE IF NOT EXISTS `service_item` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '事项ID',
  `item_name`       VARCHAR(100) NOT NULL                COMMENT '事项名称',
  `category`        VARCHAR(30)  DEFAULT '便民服务'       COMMENT '事项分类',
  `condition_desc`  TEXT         DEFAULT NULL            COMMENT '办理条件',
  `material_desc`   TEXT         DEFAULT NULL            COMMENT '所需材料',
  `process_desc`    TEXT         DEFAULT NULL            COMMENT '办理流程',
  `time_limit`      INT          NOT NULL DEFAULT 5      COMMENT '办理时限(工作日)',
  `location`        VARCHAR(200) DEFAULT NULL            COMMENT '办理地点',
  `contact_phone`   VARCHAR(20)  DEFAULT NULL            COMMENT '咨询电话',
  `need_grid_audit` TINYINT      NOT NULL DEFAULT 0      COMMENT '是否需网格初审(0否1是)',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态(0停用1启用)',
  `sort`            INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       BIGINT       DEFAULT NULL            COMMENT '更新人',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='办事事项表';

CREATE TABLE IF NOT EXISTS `service_apply` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `apply_no`           VARCHAR(30)  NOT NULL                COMMENT '申请编号',
  `item_id`            BIGINT       NOT NULL                COMMENT '事项ID',
  `villager_id`        BIGINT       NOT NULL                COMMENT '申请人ID',
  `status`             TINYINT      NOT NULL DEFAULT 0      COMMENT '状态(0待受理1已受理2办理中3待补正4已退回5已办结6已归档)',
  `apply_content`      TEXT         DEFAULT NULL            COMMENT '申请内容',
  `attachment`         VARCHAR(500) DEFAULT NULL            COMMENT '材料附件',
  `submit_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `grid_audit_by`      BIGINT       DEFAULT NULL            COMMENT '网格初审人',
  `grid_audit_time`    DATETIME     DEFAULT NULL            COMMENT '初审时间',
  `grid_audit_comment` VARCHAR(500) DEFAULT NULL            COMMENT '初审意见',
  `handle_by`          BIGINT       DEFAULT NULL            COMMENT '受理/办理人',
  `handle_time`        DATETIME     DEFAULT NULL            COMMENT '受理时间',
  `finish_time`        DATETIME     DEFAULT NULL            COMMENT '办结时间',
  `deadline_time`      DATETIME     DEFAULT NULL            COMMENT '办理时限',
  `is_overtime`        TINYINT      NOT NULL DEFAULT 0      COMMENT '是否超时(0否1是)',
  `result_content`     TEXT         DEFAULT NULL            COMMENT '办理结果',
  `create_by`          BIGINT       DEFAULT NULL            COMMENT '创建人',
  `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          BIGINT       DEFAULT NULL            COMMENT '更新人',
  `update_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`            TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_villager_id` (`villager_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deadline_time` (`deadline_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='办事申请表';

-- ---------- 通用 ----------

CREATE TABLE IF NOT EXISTS `sys_dict` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type`   VARCHAR(50)  NOT NULL                COMMENT '字典类型',
  `dict_label`  VARCHAR(50)  NOT NULL                COMMENT '标签',
  `dict_value`  VARCHAR(50)  NOT NULL                COMMENT '值',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态(0停用1启用)',
  `remark`      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除(0未删1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_value` (`dict_type`,`dict_value`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';
