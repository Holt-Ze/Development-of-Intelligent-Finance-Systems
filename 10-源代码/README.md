# 源代码说明

## 目录结构

```text
10-源代码/
├── backend
└── frontend
```

## 说明

- `backend`：Spring Boot 3 + MyBatis-Plus 真实后端实现，包含登录、基础信息管理、收支管理、报表统计、备份恢复接口。
- `frontend`：Vue 3 + Element Plus 前端实现，已接入后端真实接口并提供完整页面操作流程。

## 默认演示账号

- `admin / 123456`
- `finance01 / 123456`

## 运行前准备

- 确保 MySQL 中已执行 `11-数据库/财务管理系统.sql`
- 根据本机环境调整 `backend/src/main/resources/application.yml` 中数据库连接
- 备份文件默认生成到 `backend/backups/`

## 推荐启动顺序

1. 导入 `11-数据库/财务管理系统.sql`
2. 启动后端服务
3. 启动前端服务
4. 按用户手册执行演示流程
