# 智能会议室工位预约平台设计规范

## 目标

构建面向单组织多部门的智能会议室与工位预约后端。首期提供账号与权限、资源管理、策略化预约、多级审批、签到与爽约、Webhook 通知和运营统计能力。

## 范围与约束

- 运行时：Java 21、Spring Boot 4、Maven、MySQL、Redis、Spring Cloud。
- 架构：模块化单体；领域模块包括 `identity`、`organization`、`resource`、`reservation`、`approval`、`attendance`、`notification` 和 `analytics`。
- 组织：一个企业实例，存在部门树；用户归属一个部门。
- 认证：本地账号签发 JWT Access Token；Refresh Token、撤销状态由 Redis 管理。通过身份提供方接口预留 OAuth2/OIDC 企业 SSO。
- 异步：首期使用 MySQL Outbox/投递表与定时任务，不引入 RabbitMQ；领域逻辑依赖 `DomainEventPublisher` 接口，后续可替换为消息队列实现。
- 持久化：MyBatis 操作 MySQL；Flyway 管理数据库迁移；禁止 ORM 自动建表。
- 缓存与协调：Redis 只能保存缓存、令牌、幂等结果和分布式锁，不作为预约与审批的事实来源。

## 领域模型

### 身份与组织

- `sys_user`、`sys_role`、`sys_permission`、`sys_user_role` 管理本地身份和 RBAC。
- `org_department` 维护部门树。
- `auth_identity_binding` 保存本地用户与未来外部 OIDC/SSO subject 的映射。
- `auth_refresh_token` 保存可审计的 Refresh Token 元数据；令牌值和撤销状态存入 Redis。

### 资源与策略

- `resource_site`、`resource_building`、`resource_floor` 和 `resource` 描述园区、楼宇、楼层及会议室/工位。
- 会议室与工位共享资源主表，资源类型差异字段和设施属性存入受校验的 JSON 配置。
- `reservation_policy` 绑定至资源，定义时段粒度、提前预约天数、最短/最长时长、取消截止时间、签到窗口、爽约处理和 0 至 2 级审批。
- 策略中的审批人可引用指定用户、指定角色或资源管理员；创建预约时解析并固化审批人，避免后续配置变化影响存量流程。

### 预约与履约

- `reservation` 保存资源、发起人、预约时间、参与人数、用途、当前状态、乐观锁版本和审计字段。
- `reservation_approval` 保存每次预约的 0 至 2 个审批节点、顺序、审批人、状态、意见和处理时间。
- `reservation_attendance` 保存用户签到、管理员代签到、状态更正及原因。
- `reservation_status_log` 保存不可变的预约状态变更记录。

预约状态只允许以下流转：

```text
PENDING_APPROVAL -> CONFIRMED -> CHECKED_IN -> COMPLETED
PENDING_APPROVAL -> REJECTED
PENDING_APPROVAL | CONFIRMED -> CANCELLED
CONFIRMED -> NO_SHOW
```

- 无审批策略时，创建预约后直接进入 `CONFIRMED`；否则进入 `PENDING_APPROVAL`，按顺序处理审批节点。
- 任何审批节点拒绝均进入 `REJECTED`。
- 用户只能在策略取消截止时间前取消；管理员可强制取消但必须填写原因。
- 用户可在签到窗口内签到，管理员可代签到或更正；超出窗口且未签到的已确认预约转为 `NO_SHOW`。

## 一致性与并发

- 创建预约时先校验策略与权限，再按 `resourceId + slot` 获取 Redis 短期锁。
- 在同一 MySQL 事务内再次检测有效预约的重叠时间段，插入预约和状态日志；数据库索引和条件查询承担最终一致性防线。
- 创建接口要求 `Idempotency-Key`；Redis 保存 `idempotency:reservation:{userId}:{key}` 及最终响应，以防止重试产生重复预约。
- 资源、预约和策略变更精准失效 `reservation:availability:*` 可用性缓存。

## 审批、通知与统计

- 审批人通过 `/api/v1/approvals` 查询待办，并处理通过或拒绝；操作必须校验当前节点、审批人身份和节点顺序。
- `notification_webhook_config` 保存端点、事件订阅、启停状态、密文密钥和超时/重试覆盖配置。
- `notification_outbox` 记录业务事务内生成的领域事件；`notification_delivery` 记录每个端点的投递尝试、响应摘要、重试时间和最终结果。
- 事务提交后由定时任务投递 Webhook。请求携带 `X-Webhook-Id`、`X-Webhook-Event`、`X-Webhook-Timestamp` 与 HMAC-SHA256 签名；重试采用指数退避，到达上限后标记失败，不回滚预约业务。
- 统计任务按日生成 `analytics_resource_daily` 与 `analytics_department_daily`，提供利用率、预约量、取消率和爽约率，避免在线聚合扫描业务表。

## API 与错误契约

- API 统一以 `/api/v1` 为前缀，统一返回 `ApiResponse<T>`。
- 身份接口：`/auth/login`、`/auth/refresh`、`/auth/logout`，以及预留的 `/auth/oidc/**`。
- 管理接口：用户、角色、部门、地点层级、资源、预约策略、Webhook 配置和统计。
- 用户接口：可用性查询、预约创建/查询/取消、签到和审批待办处理。
- 管理员接口：代签到、预约状态更正和强制取消。
- 修改类接口必须验证 JWT、RBAC 权限与 `X-Request-Id`；预约创建必须验证 `Idempotency-Key`。
- 业务错误码至少包含 `RESERVATION_TIME_CONFLICT`、`RESERVATION_POLICY_VIOLATION`、`APPROVAL_ACTION_FORBIDDEN` 和 `CHECK_IN_WINDOW_CLOSED`。
- 全局异常处理不得向客户端返回堆栈、SQL 或敏感配置。

## 质量与安全

- 单元测试覆盖策略、状态机、审批顺序、签到窗口、权限和签名。
- 使用 Testcontainers 运行 MySQL 与 Redis 的集成测试，覆盖 Flyway、事务、重叠冲突、幂等、缓存失效和并发预约。
- MVC 集成测试覆盖 JWT、请求校验、业务错误码和分页。
- 所有业务表包含创建/更新审计字段和逻辑删除字段；状态变更额外写入不可变审计日志。
- Webhook 密钥不得以明文写入日志或响应；配置项通过环境变量覆盖。

## 非目标

- 首期不引入 RabbitMQ、服务注册中心、API 网关或多服务部署。
- 首期不直接接入企业 SSO、门禁、会议设备或短信/邮件渠道。
- 首期不支持多租户；数据模型不得预设跨组织数据访问。
