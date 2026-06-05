# 源代码说明

本文档用于说明 `10-源代码` 目录中的工程结构、技术实现、运行方式和演示建议，便于课程检查、代码阅读和环境复现。

## 目录结构

```text
10-源代码/
├── backend
│   ├── src/main/java/com/classdesign/finance
│   ├── src/main/resources
│   ├── backups
│   └── pom.xml
└── frontend
    ├── src
    ├── dist
    ├── package.json
    └── vite.config.js
```

## 模块说明

### backend

- 技术栈：Spring Boot 3、MyBatis-Plus、Spring Validation、MySQL 8
- 入口类：`com.classdesign.finance.FinanceManagementApplication`
- 默认端口：`8080`
- 主要职责：
  - 用户登录与角色识别
  - 用户、部门、账户、收支类别管理
  - 收入记录与支出记录管理
  - 首页仪表盘、周报表、月报表、年报表、自定义区间报表
  - 备份创建、下载与恢复

### frontend

- 技术栈：Vue 3、Vite、Element Plus、Axios、Vue Router
- 主要页面：
  - 登录页
  - 首页仪表盘
  - 用户管理
  - 部门管理
  - 账户管理
  - 收入类别管理
  - 支出类别管理
  - 收入记录
  - 支出记录
  - 报表中心
  - 备份管理
- 接口地址：默认指向 `http://localhost:8080`

## 对外接口概览

后端当前已实现以下主要接口分组：

- `/auth`：登录、退出
- `/users`：用户管理
- `/departments`：部门管理
- `/accounts`：账户管理
- `/income-categories`：收入类别管理
- `/expense-categories`：支出类别管理
- `/incomes`：收入记录管理
- `/expenses`：支出记录管理
- `/reports`：首页仪表盘、周/月/年/自定义统计
- `/backups`：备份列表、创建、下载、恢复

## 运行前准备

- 确保已经执行 `11-数据库/财务管理系统.sql`
- 检查 `backend/src/main/resources/application.yml` 中的数据库地址、用户名和密码
- 默认备份目录为 `10-源代码/backend/backups/`
- 前端首次运行需要安装依赖：`npm install`

## 启动步骤

### 后端

```bash
cd 10-源代码/backend
mvn spring-boot:run
```

### 前端

```bash
cd 10-源代码/frontend
npm install
npm run dev
```

## 默认演示账号

- 管理员：`admin / 123456`
- 财务员：`finance01 / 123456`

## 演示建议流程

1. 使用管理员账号登录，展示用户、部门、账户和类别维护。
2. 切换到财务员账号，展示收入和支出录入。
3. 进入报表中心，展示月报表与自定义区间统计。
4. 回到管理员账号，展示备份创建、下载与恢复。

## 说明

- 当前源码已具备课程设计演示所需的主流程能力。
- `dist/`、`target/` 等目录为构建产物，可用于展示当前前端打包结果和后端编译结果。
- 若后续继续扩展功能，建议优先补充接口文档、接口测试记录和操作日志设计。
