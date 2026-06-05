CREATE DATABASE IF NOT EXISTS finance_management_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finance_management_system;

DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS income_record;
DROP TABLE IF EXISTS expense_record;
DROP TABLE IF EXISTS income_category;
DROP TABLE IF EXISTS expense_category;
DROP TABLE IF EXISTS account_info;
DROP TABLE IF EXISTS dept_info;
DROP TABLE IF EXISTS backup_record;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(30) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL
);

CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

CREATE TABLE dept_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dept_code VARCHAR(30) NOT NULL UNIQUE,
  dept_name VARCHAR(100) NOT NULL,
  leader_name VARCHAR(50),
  phone VARCHAR(20),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_code VARCHAR(30) NOT NULL UNIQUE,
  account_name VARCHAR(100) NOT NULL,
  owner_unit VARCHAR(100),
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE income_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_code VARCHAR(30) NOT NULL UNIQUE,
  category_name VARCHAR(100) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE expense_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_code VARCHAR(30) NOT NULL UNIQUE,
  category_name VARCHAR(100) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE income_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_no VARCHAR(40) NOT NULL UNIQUE,
  dept_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  amount DECIMAL(12, 2) NOT NULL,
  occurred_on DATE NOT NULL,
  operator_name VARCHAR(50) NOT NULL,
  remark VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_income_dept FOREIGN KEY (dept_id) REFERENCES dept_info(id),
  CONSTRAINT fk_income_account FOREIGN KEY (account_id) REFERENCES account_info(id),
  CONSTRAINT fk_income_category FOREIGN KEY (category_id) REFERENCES income_category(id)
);

CREATE TABLE expense_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_no VARCHAR(40) NOT NULL UNIQUE,
  dept_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  amount DECIMAL(12, 2) NOT NULL,
  occurred_on DATE NOT NULL,
  operator_name VARCHAR(50) NOT NULL,
  remark VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_expense_dept FOREIGN KEY (dept_id) REFERENCES dept_info(id),
  CONSTRAINT fk_expense_account FOREIGN KEY (account_id) REFERENCES account_info(id),
  CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES expense_category(id)
);

CREATE TABLE backup_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  backup_name VARCHAR(100) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  file_size BIGINT NOT NULL DEFAULT 0,
  created_by VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  restored_at DATETIME NULL,
  remark VARCHAR(255)
);

INSERT INTO sys_role (id, role_code, role_name) VALUES
  (1, 'ADMIN', '管理员'),
  (2, 'FINANCE', '财务员');

INSERT INTO sys_user (id, username, password, real_name, phone, status) VALUES
  (1, 'admin', '123456', '系统管理员', '13800000001', 1),
  (2, 'finance01', '123456', '张会计', '13800000002', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
  (1, 1),
  (2, 2);

INSERT INTO dept_info (id, dept_code, dept_name, leader_name, phone, status) VALUES
  (1, 'D001', '学生工作处', '李老师', '0523-8000001', 1),
  (2, 'D002', '教务处', '王老师', '0523-8000002', 1),
  (3, 'D003', '信息工程学院', '周老师', '0523-8000003', 1);

INSERT INTO account_info (id, account_code, account_name, owner_unit, description, status) VALUES
  (1, 'A001', '学校基本户', '财务处', '校级统一收支账户', 1),
  (2, 'A002', '学院活动经费户', '信息工程学院', '学院活动专用账户', 1);

INSERT INTO income_category (id, category_code, category_name, status) VALUES
  (1, 'IC001', '经费拨入', 1),
  (2, 'IC002', '专项收入', 1);

INSERT INTO expense_category (id, category_code, category_name, status) VALUES
  (1, 'EC001', '办公支出', 1),
  (2, 'EC002', '活动支出', 1),
  (3, 'EC003', '设备维护', 1);

INSERT INTO income_record (record_no, dept_id, account_id, category_id, amount, occurred_on, operator_name, remark, status) VALUES
  ('IN20260601001', 3, 2, 1, 20000.00, '2026-06-01', '张会计', '6 月学院活动经费拨入', 1),
  ('IN20260603001', 1, 1, 2, 5000.00, '2026-06-03', '张会计', '专项补助到账', 1);

INSERT INTO expense_record (record_no, dept_id, account_id, category_id, amount, occurred_on, operator_name, remark, status) VALUES
  ('EX20260602001', 3, 2, 2, 3200.00, '2026-06-02', '张会计', '学生活动支出', 1),
  ('EX20260604001', 1, 1, 1, 850.00, '2026-06-04', '张会计', '办公用品采购', 1);

INSERT INTO backup_record (backup_name, file_path, file_size, created_by, remark) VALUES
  ('finance_backup_20260605.sql', '/data/backups/finance_backup_20260605.sql', 20480, '系统管理员', '初始化演示备份');
