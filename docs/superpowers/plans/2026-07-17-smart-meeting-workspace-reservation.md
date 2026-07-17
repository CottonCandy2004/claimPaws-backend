# 智能会议室工位预约平台实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 交付单组织多部门的会议室与工位预约后端，包括身份、资源、策略化预约、审批履约、Webhook 通知与统计。

**Architecture:** 使用领域模块化 Spring Boot 单体。MySQL 是预约、审批和 Outbox 的唯一事实来源，Redis 只负责令牌、缓存、幂等和短期锁；所有领域事件通过 `DomainEventPublisher` 发布并由数据库 Outbox 异步投递 Webhook。

**Tech Stack:** Java 21, Spring Boot 4, Spring MVC, Spring Security, Spring Cloud OpenFeign, MyBatis, MySQL 8, Redis 7, Flyway, JJWT, Testcontainers, JUnit 5, AssertJ.

## Global Constraints

- API 统一前缀为 `/api/v1`，响应类型为 `ApiResponse<T>`，修改操作要求 `X-Request-Id`。
- 单组织多部门，不实现租户字段或跨组织查询。
- 认证使用 JWT Access Token；Refresh Token、撤销状态与幂等结果存入 Redis。
- 预约创建要求 `Idempotency-Key`，并以 MySQL 事务校验重叠有效预约。
- 预约状态只能按 `PENDING_APPROVAL -> CONFIRMED -> CHECKED_IN -> COMPLETED`、`PENDING_APPROVAL -> REJECTED`、`PENDING_APPROVAL | CONFIRMED -> CANCELLED`、`CONFIRMED -> NO_SHOW` 流转。
- 审批链长度只能为 0、1 或 2；创建预约时必须固化节点审批人。
- Webhook 用数据库 Outbox 和定时任务投递；不引入 RabbitMQ、注册中心、网关或多服务部署。
- Flyway 管理全部 schema 变更；MyBatis 是唯一数据库访问层；业务表必须具有审计字段和逻辑删除字段。
- 所有新业务行为严格按 Red-Green-Refactor 编写测试。

## 文件与阶段

| 阶段 | 计划文件 | 产出 |
| --- | --- | --- |
| 1 | `2026-07-17-platform-foundation.md` | 配置、统一 API、迁移、异常、安全与测试容器 |
| 2 | `2026-07-17-identity-resource.md` | JWT 身份、RBAC、部门、位置、资源和策略 |
| 3 | `2026-07-17-reservation-lifecycle.md` | 可用性、幂等预约、审批、签到、爽约和审计 |
| 4 | `2026-07-17-notification-analytics.md` | Outbox Webhook、重试、统计聚合和管理查询 |

### Task 1: 执行工程基础阶段

**Files:**
- Follow: `docs/superpowers/plans/2026-07-17-platform-foundation.md`

**Interfaces:**
- Produces: `ApiResponse<T>`, `BusinessException`, `ErrorCode`, `SecurityConfig`, `BaseEntity` 与可用于 MySQL/Redis 的 Testcontainers 基类。

- [ ] **Step 1: 按阶段计划顺序完成全部 Red-Green-Refactor 步骤。**

- [ ] **Step 2: 运行基础阶段全量测试。**

Run: `./mvnw test -Dtest='*Test,*IT'`

Expected: Maven 退出码为 `0`，Flyway 测试迁移和 Spring 上下文测试均通过。

- [ ] **Step 3: 提交阶段变更。**

```bash
git add pom.xml AGENTS.md src docs/superpowers/plans/2026-07-17-platform-foundation.md
git commit -m "feat: establish platform foundation"
```

### Task 2: 执行身份与资源阶段

**Files:**
- Follow: `docs/superpowers/plans/2026-07-17-identity-resource.md`

**Interfaces:**
- Consumes: 阶段 1 的统一响应、异常、安全、迁移和测试基类。
- Produces: `AuthenticationService`, `ResourceService`, `ReservationPolicyService` 与管理 API。

- [ ] **Step 1: 按阶段计划顺序完成全部 Red-Green-Refactor 步骤。**

- [ ] **Step 2: 运行身份和资源测试。**

Run: `./mvnw test -Dtest='*Authentication*Test,*Resource*Test,*Policy*Test'`

Expected: Maven 退出码为 `0`，登录、权限与资源策略校验全部通过。

- [ ] **Step 3: 提交阶段变更。**

```bash
git add src/main src/test src/main/resources/db docs/superpowers/plans/2026-07-17-identity-resource.md
git commit -m "feat: add identity and resource management"
```

### Task 3: 执行预约生命周期阶段

**Files:**
- Follow: `docs/superpowers/plans/2026-07-17-reservation-lifecycle.md`

**Interfaces:**
- Consumes: 阶段 2 的用户、资源和策略服务。
- Produces: `ReservationService`, `ApprovalService`, `AttendanceService`, `AvailabilityQueryService` 与状态审计。

- [ ] **Step 1: 按阶段计划顺序完成全部 Red-Green-Refactor 步骤。**

- [ ] **Step 2: 运行预约并发集成测试。**

Run: `./mvnw test -Dtest='*Reservation*Test,*Approval*Test,*Attendance*Test'`

Expected: Maven 退出码为 `0`，同资源重叠预约只有一个成功请求，审批和签到状态机符合约束。

- [ ] **Step 3: 提交阶段变更。**

```bash
git add src/main src/test src/main/resources/db docs/superpowers/plans/2026-07-17-reservation-lifecycle.md
git commit -m "feat: add reservation lifecycle"
```

### Task 4: 执行通知与统计阶段

**Files:**
- Follow: `docs/superpowers/plans/2026-07-17-notification-analytics.md`

**Interfaces:**
- Consumes: 阶段 3 的领域事件与状态日志。
- Produces: `DomainEventPublisher`, `WebhookDeliveryJob`, `AnalyticsAggregationJob` 与统计 API。

- [ ] **Step 1: 按阶段计划顺序完成全部 Red-Green-Refactor 步骤。**

- [ ] **Step 2: 运行通知和统计测试。**

Run: `./mvnw test -Dtest='*Webhook*Test,*Analytics*Test'`

Expected: Maven 退出码为 `0`，签名、重试和日聚合测试全部通过。

- [ ] **Step 3: 运行完整验证。**

Run: `./mvnw verify`

Expected: Maven 退出码为 `0`，所有单元、MVC 和 Testcontainers 集成测试通过。

- [ ] **Step 4: 提交阶段变更。**

```bash
git add src/main src/test src/main/resources/db docs/superpowers/plans/2026-07-17-notification-analytics.md
git commit -m "feat: add webhook notifications and analytics"
```
