# 智能会议室工位预约平台微服务设计规范

## 目标

将智能会议室与工位预约平台实现为基于 Spring Cloud 的微服务系统。首期提供本地 JWT 身份、资源与策略、预约审批履约、Webhook 通知和日统计；使用 Nacos 管理服务发现与集中配置，使用 RabbitMQ 传播可靠领域事件。

## 服务边界

| 服务 | 职责 | 数据所有权 |
| --- | --- | --- |
| `gateway` | 路由、JWT 透传、限流、跨域、请求 ID | 无业务数据库 |
| `identity-service` | 本地账号、RBAC、部门树、Refresh Token、OIDC 身份绑定 | `claimpaws_identity` |
| `resource-service` | 园区、楼宇、楼层、会议室、工位、设施、预约策略 | `claimpaws_resource` |
| `reservation-service` | 可用性投影、预约、审批、签到、爽约、状态审计、日统计、Outbox | `claimpaws_reservation` |
| `notification-service` | Webhook 配置、密钥加密、投递、重试、投递审计 | `claimpaws_notification` |

- 服务以独立 Maven 模块构建和部署，每个拥有独立 Flyway 迁移目录、Nacos Data ID 和数据库/schema。
- 严禁跨服务数据库访问。跨服务读取使用 API，状态同步使用事件；不存在跨服务分布式事务。
- 每个服务按 `domain`、`application`、`persistence`、`infrastructure`、`web` 分包。

## 平台组件

- Nacos 同时提供服务注册发现和配置中心。使用 Namespace 隔离 `dev`、`test`、`prod`，每个服务使用独立 Data ID，例如 `reservation-service-dev.yaml`。
- RabbitMQ 是跨服务事件总线。使用 Topic Exchange `claimpaws.domain.events`；消费者独立队列和死信队列。
- MySQL 是每个服务的事实数据源，MyBatis 是唯一数据库访问层，Flyway 管理迁移。
- Redis 用于 Refresh Token、撤销状态、可用性缓存、幂等结果和短期预约锁，不作为业务事实来源。
- 敏感配置通过环境变量或 Nacos 加密配置注入，不得提交密码、JWT 密钥、数据库连接串或 Webhook 密钥。

## 同步调用

- 客户端只访问 `gateway`。网关透传 JWT、`X-Request-Id`、用户 ID 和权限声明；业务服务必须自行校验权限。
- `reservation-service` 在创建预约前通过 OpenFeign 调用 `resource-service` 获取资源和策略快照。资源服务不可用时返回 `RESOURCE_SERVICE_UNAVAILABLE`，且不创建预约。
- `identity-service` 是用户、角色、部门唯一写入方。其他服务可由事件维护最小只读用户投影，但不得写回身份数据。
- REST API 统一使用 `/api/v1` 前缀和 `ApiResponse<T>`；禁止向客户端返回堆栈、SQL、下游响应体或消息中间件错误。

## 预约与数据一致性

- 资源策略定义时段粒度、提前预约期、最短/最长时长、取消截止、签到窗口、爽约规则和 0 至 2 级审批。
- 预约状态只能流转：

```text
PENDING_APPROVAL -> CONFIRMED -> CHECKED_IN -> COMPLETED
PENDING_APPROVAL -> REJECTED
PENDING_APPROVAL | CONFIRMED -> CANCELLED
CONFIRMED -> NO_SHOW
```

- `reservation-service` 在本地事务中校验策略快照、重叠预约、状态日志和 Outbox。Redis 锁减少相同资源时间窗竞争；本服务 MySQL 的重叠查询和索引是最终冲突判断。
- 创建预约要求 `Idempotency-Key`。同一用户与键只返回同一个结果。
- 预约状态变更必须在本地事务中写入不可变状态日志与 Outbox；不等待通知服务处理完成。
- 资源和策略变更由 `resource-service` 发送事件，预约服务更新或失效自身可用性投影。

## RabbitMQ 事件

- Outbox 发布器以发布确认将事件发送至 `claimpaws.domain.events`。
- 路由键包括 `reservation.created`、`reservation.confirmed`、`reservation.rejected`、`reservation.cancelled`、`reservation.checked-in`、`reservation.no-show`、`resource.policy-updated`。
- 消息必须包括 `eventId`、`eventType`、`occurredAt`、`aggregateId`、`schemaVersion` 和 JSON 载荷。
- 消费端以 `eventId` 保存消费幂等记录；重复消息直接确认，不再次执行副作用。
- 消费失败经过有限重试后进入死信队列。管理员可查询死信并触发人工重投；死信不会回滚生产方已提交的业务事务。

## Webhook 与统计

- `notification-service` 消费预约事件并创建投递记录。Webhook 请求包含 `X-Webhook-Id`、`X-Webhook-Event`、`X-Webhook-Timestamp` 和 HMAC-SHA256 签名。
- 密钥以密文保存，日志和 API 响应必须脱敏。投递失败按指数退避重试，达到最大次数标记失败。
- `reservation-service` 消费自身预约事件生成资源和部门日统计，统计任务使用幂等 upsert，避免重复累计。

## 测试与运行

- 每个服务采用 Red-Green-Refactor；单元测试覆盖状态机、策略、权限、签名和事件载荷。
- MySQL、Redis、RabbitMQ 使用 Testcontainers；集成测试覆盖迁移、预约并发、Outbox 发布确认、消费幂等、死信和重试。
- 网关路由、安全和 Nacos 配置加载使用独立集成测试。
- 本地集成环境由 Docker Compose 启动 Nacos、RabbitMQ、MySQL 和 Redis；每个服务映射独立数据库/schema。

## 非目标

- 首期不支持多租户、跨组织数据访问、门禁或会议设备直连。
- 首期不引入 Kafka、分布式事务框架、共享业务数据库或跨服务 Mapper 调用。
