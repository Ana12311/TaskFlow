# TaskFlow

一个基于 Spring Boot + Vue 3 的团队任务协作与项目管理平台。前后端分离单体架构，用于学习和实习项目展示。

## 功能特性

- **用户认证**：注册 / 登录 / JWT 无状态认证 / BCrypt 密码加密
- **团队管理**：创建团队、成员增删、角色管理（OWNER / ADMIN / MEMBER）
- **项目管理**：团队下建项目、成员管理、项目删除（级联删除，仅创建者）
- **任务管理**：创建 / 编辑 / 删除 / 分配负责人 / 优先级 / 状态机流转，列表分页
- **任务统计**：按状态统计任务数（缓存）
- **评论**：任务下发表 / 分页列表 / 删除本人评论
- **操作日志**：自动记录任务的关键操作（创建 / 分配 / 状态 / 优先级 / 删除）
- **通知**：分配任务、状态变更、评论、拉人入团队/项目时通知，未读红点、已读、全部已读
- **缓存**：Redis 缓存用户信息、项目任务统计

## 技术栈

**后端**

- Java 17、Spring Boot 3.3、Gradle 8.14
- MySQL 8、MyBatis-Plus 3.5（分页插件）
- Spring Security 6 + JWT（jjwt 0.12）
- Redis 7
- JUnit 5 集成测试（真 MySQL + MockMvc）

**前端**

- Vue 3（Composition API + `<script setup>`）、Vite 6
- Element Plus、Pinia、Vue Router、Axios
- Vitest 3

## 目录结构

```
├── src/main/java/com/example/taskflow/
│   ├── common/          # Result 统一响应、ResultCode 错误码、全局异常、枚举、缓存
│   │   ├── cache/       # CacheKeys、CacheService（Redis JSON 读写）
│   │   ├── enums/       # Role / TaskStatus(状态机) / TaskPriority / TaskOperationType / NotificationType
│   │   ├── exception/   # BusinessException、GlobalExceptionHandler
│   │   └── result/      # Result、ResultCode
│   ├── config/          # SecurityConfig、MybatisPlusConfig(分页)、MyMetaObjectHandler(自动填充)
│   ├── controller/      # 接口层，只接参数调 Service
│   ├── dto/             # 请求对象（@Valid 校验）
│   ├── entity/          # 数据库实体（不对外暴露）
│   ├── mapper/          # MyBatis-Plus BaseMapper，少量手写 SQL
│   ├── security/        # JWT 工具、过滤器、LoginUser、UserDetailsService
│   ├── service/         # 业务接口 + impl（权限、状态流转、日志、缓存都在这里）
│   ├── util/            # SecurityUtils（取当前登录用户）
│   └── vo/              # 响应对象（含 PageResult 通用分页）
├── src/test/            # 集成测试（BaseIntegrationTest 基类 + 各模块测试）
├── db/init.sql          # MySQL 建库建表脚本
└── frontend/            # Vue3 前端
    ├── src/api/         # Axios 接口封装（request.js 统一拦截）
    ├── src/stores/      # Pinia（auth）
    ├── src/router/      # 路由（hash 模式）
    ├── src/layout/      # MainLayout（顶栏 + 通知铃铛）
    ├── src/views/       # 登录/注册/团队/项目/任务/个人资料
    └── src/utils/       # 字典映射（状态/优先级/角色/通知类型）
```

## 快速开始（Docker，推荐）

```bash
cp .env.example .env   # 修改 .env 里的数据库密码和 JWT 密钥
docker compose up -d --build
```

启动后：

- 前端：http://localhost:8081
- 后端：http://localhost:8080/api

四个容器：`taskflow-mysql`(3307) / `taskflow-redis`(6380) / `taskflow-app`(8080) / `taskflow-frontend`(8081)。前端 nginx 将 `/api` 反代到后端容器，无需手动配置跨域。

首次启动 MySQL 自动执行 `db/init.sql` 建表。数据持久化在 `mysql-data` 卷。

停止：`docker compose down`（加 `-v` 连数据卷一起删）。

## 本地开发

**后端**（需本地 MySQL + Redis，端口见环境变量）：

```bash
# 设置环境变量（PowerShell 示例）
$env:DB_HOST="localhost"; $env:DB_PORT="3306"; $env:DB_NAME="taskflow"
$env:DB_USERNAME="root"; $env:DB_PASSWORD="你的密码"
$env:REDIS_HOST="localhost"; $env:REDIS_PORT="6379"
$env:JWT_SECRET="至少32字节的随机串"
./gradlew bootRun
```

**前端**（开发服务器 5173，`/api` 自动代理到 8080）：

```bash
cd frontend
npm install
npm run dev
```

## 环境变量

| 变量 | 说明 | 默认值 |
|---|---|---|
| DB_HOST | MySQL 地址 | localhost |
| DB_PORT | MySQL 端口 | 3306 |
| DB_NAME | 数据库名 | taskflow |
| DB_USERNAME | 数据库账号 | root |
| DB_PASSWORD | 数据库密码 | 空（必须提供） |
| REDIS_HOST | Redis 地址 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |
| JWT_SECRET | JWT 密钥（≥32 字节） | 空（必须提供） |
| JWT_ACCESS_EXPIRATION | access token 过期时间（毫秒） | 1800000 |
| JWT_REFRESH_EXPIRATION | refresh token 过期时间（毫秒） | 604800000 |

> 所有密码/密钥都通过环境变量注入，`application.yml` 里不写任何真实值。

## 接口概览

| 模块 | 接口 |
|---|---|
| 认证 | POST /api/auth/register、POST /api/auth/login、POST /api/auth/refresh、POST /api/auth/logout |
| 用户 | GET /api/users/me、PUT /api/users/me |
| 团队 | POST/GET /api/teams、GET /api/teams/{id}、POST/DELETE 成员、PUT 改角色 |
| 项目 | POST/GET /api/teams/{id}/projects、GET/DELETE /api/projects/{id}、POST/DELETE 成员 |
| 任务 | POST/GET /api/projects/{id}/tasks（分页）、GET stats、GET/PUT/DELETE /api/tasks/{id}、PUT assignee/status/priority、GET logs（分页） |
| 评论 | POST/GET /api/tasks/{id}/comments（分页）、DELETE /api/comments/{id} |
| 通知 | GET /api/notifications（分页）、GET unread-count、PUT {id}/read、PUT read-all |

统一响应格式：`{ code, message, data }`。业务错误返回 HTTP 200 + 非 200 的 code，仅未带 token 返回 HTTP 401。

## 权限模型

- 团队 / 项目角色：OWNER > ADMIN > MEMBER（`Role.canManageMembers()`）
- 建项目：任意团队成员；项目成员增删：创建者 或 团队 OWNER/ADMIN
- 删除项目：仅创建者（级联删成员、任务、评论、日志、通知）
- 任务权限两档：
  - 参与级（创建者 / 负责人 / 项目 OWNER/ADMIN）：改内容、改状态
  - 管理级（创建者 / 项目 OWNER/ADMIN）：改优先级、分配负责人、删除
- 评论：项目成员可发 / 看，只能删自己

## 任务状态机

```
TODO -> IN_PROGRESS -> DONE
TODO -> CANCELLED
IN_PROGRESS -> TODO / DONE / CANCELLED
DONE -> CANCELLED
CANCELLED -> TODO
```

校验统一在 `TaskStatus.canTransitionTo()`，Service 层调用。

## 测试

**后端**（集成测试连独立库 `taskflow_test`，另有纯单元测试测状态机/权限/TokenService）：

```bash
DB_PASSWORD=你的密码 ./gradlew test
```

**前端**（Vitest，工具函数、组件渲染、API 封装）：

```bash
cd frontend && npm test
```

## 后续规划

- Swagger / OpenAPI 接口文档
- 用户搜索（当前加成员靠输入用户 ID）
- GitHub Actions CI
