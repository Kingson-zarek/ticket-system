-- 客服工单系统建表脚本
-- MySQL 8，字符集 utf8mb4

CREATE DATABASE IF NOT EXISTS ticket_db
  DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

USE ticket_db;

-- 用户
CREATE TABLE t_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL COMMENT '登录名',
  password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后',
  nickname    VARCHAR(50)  COMMENT '显示名',
  role        VARCHAR(20)  NOT NULL DEFAULT 'aftersales'
              COMMENT 'presales 售前 / aftersales 售后',
  enabled     TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) COMMENT '用户';

-- 工单
CREATE TABLE t_ticket (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_no      VARCHAR(30)  NOT NULL COMMENT 'TK2025010001',
  title          VARCHAR(200) NOT NULL COMMENT '标题',
  content        TEXT         NOT NULL COMMENT '正文（含现象/复现步骤/影响范围）',
  status         VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
                 COMMENT 'OPEN/PROCESSING/RESOLVED/CLOSED',
  priority       VARCHAR(20)  NOT NULL DEFAULT 'NORMAL'
                 COMMENT 'LOW/NORMAL/HIGH/URGENT',
  customer_name  VARCHAR(50)  COMMENT '客户名称（售前填写）',
  created_by     BIGINT       NOT NULL COMMENT '提单人（售前）',
  assignee_id    BIGINT       COMMENT '处理人（售后）',
  resolved_at    DATETIME     COMMENT '解决时间，用于统计平均处理时长',
  deleted        TINYINT      NOT NULL DEFAULT 0,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_no (ticket_no),
  KEY idx_status (status),
  KEY idx_created_by (created_by),
  KEY idx_assignee (assignee_id),
  KEY idx_created_at (created_at)
) COMMENT '工单';

-- 评论
CREATE TABLE t_comment (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id   BIGINT NOT NULL,
  user_id     BIGINT NOT NULL,
  content     TEXT   NOT NULL,
  deleted     TINYINT NOT NULL DEFAULT 0,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_ticket (ticket_id),
  KEY idx_user (user_id)
) COMMENT '工单评论';

-- 通知，已建表未接入
CREATE TABLE t_notify (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id   BIGINT NOT NULL,
  type        VARCHAR(30)  NOT NULL
              COMMENT 'TICKET_CREATED/STATUS_CHANGED/TICKET_RETURNED',
  receiver_id BIGINT       COMMENT '接收人',
  content     VARCHAR(500) COMMENT '通知内容',
  is_read     TINYINT      NOT NULL DEFAULT 0,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_receiver (receiver_id),
  KEY idx_ticket (ticket_id)
) COMMENT '通知记录';

-- 测试账号，密码统一 123456，BCrypt 值需自行生成后替换
INSERT INTO t_user (username, password, nickname, role) VALUES
('sales01', '$2a$10$H6xsk2f26VALDX1rkuUHnupFtkk45yFMVK/n98e56Ae0k3/v6iN4W', '张售前', 'presales'),
('tech01',  '$2a$10$H6xsk2f26VALDX1rkuUHnupFtkk45yFMVK/n98e56Ae0k3/v6iN4W', '李售后', 'aftersales'),
('tech99',  '$2a$10$H6xsk2f26VALDX1rkuUHnupFtkk45yFMVK/n98e56Ae0k3/v6iN4W', '王售后', 'aftersales'),
('admin',   '$2a$10$H6xsk2f26VALDX1rkuUHnupFtkk45yFMVK/n98e56Ae0k3/v6iN4W', '管理员', 'aftersales');
