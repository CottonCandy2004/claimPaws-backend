## Language policy

- 默认使用简体中文回答。
- 除非我明确要求英文，否则不要切换英文叙述。
- 代码、命令、报错、API 名称保持原文，不要强行翻译。
- 提问澄清时也使用中文。
- 如果有不明晰的问题，请询问用户而不是自行决断。

## Environment and toolchain
- Java 21, Maven, Spring Boot 3.5, Spring Cloud, Spring Cloud Alibaba Nacos, RabbitMQ, Redis, MyBatis, MySQL 8
- 文件读取、写入、更改相应操作使用tools工具

## 项目目标与架构

- 本项目是单组织多部门的智能会议室与工位预约后端。
- `plan`已写入`docs/superpowers/plans`，请**务必**看过相应plan后再进行相应功能的添加。
- 采用 Maven 多模块微服务。服务模块为 `gateway`、`identity-service`、`resource-service`、`reservation-service`、`notification-service` 和共享契约模块 `claimpaws-common`。
- 每个业务服务内按 `domain`、`application`、`persistence`、`infrastructure`、`web` 划分职责。不得仅按全局 Controller/Service/Mapper 分层堆放业务代码。
- 每个业务服务拥有独立 MySQL database/schema、独立 Flyway 迁移目录和独立 Nacos Data ID。严禁跨服务数据库访问、跨服务 Mapper 调用和分布式事务。
- Nacos 同时提供服务发现和配置中心，Namespace 使用 `dev`、`test`、`prod` 隔离。Gateway 不保存业务数据，只负责路由、请求 ID、跨域、限流和 JWT 透传。
- RabbitMQ 是跨服务事件总线；使用 Topic Exchange `claimpaws.domain.events`。生产方通过本地 Outbox 发布，消费方按 `eventId` 幂等处理、有限重试并进入死信队列。
- MySQL 是服务内业务事实来源，MyBatis 是唯一数据库访问层；Redis 仅用于 Token、缓存、幂等和短期分布式锁。
- 严禁使用MyBatis Plus进行生成。

## 业务边界

- 认证使用本地账号和 JWT Access Token；Refresh Token 与 Token 撤销状态存入 Redis。使用 `IdentityProvider` 接口预留 OAuth2/OIDC 企业 SSO，不直接实现外部 SSO 登录。
- API 统一使用 `/api/v1` 前缀和 `ApiResponse<T>` 响应。错误使用稳定业务码，禁止向客户端返回异常堆栈、SQL 或敏感配置。
- 所有修改类接口验证 JWT、RBAC 权限和 `X-Request-Id`；创建预约额外要求 `Idempotency-Key`。
- 资源策略定义时段粒度、预约提前期、最短/最长时长、取消截止、签到窗口、爽约规则和 0 至 2 级审批。创建预约时必须固化审批节点及审批人。
- 预约状态只能按以下路径转换：

```text
PENDING_APPROVAL -> CONFIRMED -> CHECKED_IN -> COMPLETED
PENDING_APPROVAL -> REJECTED
PENDING_APPROVAL | CONFIRMED -> CANCELLED
CONFIRMED -> NO_SHOW
```

- 预约冲突以 MySQL 事务内的重叠查询为最终防线；Redis 锁仅用于降低并发竞争。禁止只依赖 Redis 判断资源可用性。
- 预约状态变更必须在同一事务中写入状态日志和 Outbox 事件。

## 数据与迁移

- 所有 schema 修改必须在所属服务新增 `{service}/src/main/resources/db/migration/V{版本}__{说明}.sql`，不得修改已发布迁移，也不得依赖 Hibernate 自动建表。
- 业务表必须包含 `id`、创建/更新审计字段与 `deleted` 逻辑删除字段；预约状态变更额外写入不可变审计日志。
- Mapper SQL 必须参数化，禁止字符串拼接 SQL。查询必须显式过滤逻辑删除数据，并按访问路径建立索引。
- 分页接口使用确定的排序字段和边界限制；禁止无条件读取大表。

## Webhook 与安全

- Webhook 配置由 `notification.webhook` 提供默认开关、连接/读取超时和最大重试次数，并允许环境变量覆盖。
- 密钥只能以密文持久化，日志和 API 响应必须脱敏。
- 投递请求必须包含唯一事件 ID、事件类型、UTC 时间戳和 HMAC-SHA256 签名；消费端幂等由事件 ID 支持。
- 投递失败按指数退避重试，达到最大次数后标记失败；绝不回滚已提交的预约业务。

## 服务通信

- 客户端仅访问 `gateway`；业务服务仍自行校验 JWT、用户身份和权限，禁止仅信任网关透传的权限结论。
- `reservation-service` 仅通过 OpenFeign 读取 `resource-service` 的资源与策略快照，资源服务不可用时返回 `RESOURCE_SERVICE_UNAVAILABLE`，不得创建预约。
- 跨服务状态变化使用 `DomainEvent`，事件必须包含 `eventId`、`eventType`、`occurredAt`、`aggregateId`、`schemaVersion` 和载荷。禁止同步调用通知服务影响预约主事务。
- `identity-service` 是用户、角色和部门的唯一写入方；其他服务只能保存由事件构建的只读投影。

## 测试与验证

- 新功能和缺陷修复遵循 Red-Green-Refactor：先写一个失败测试并运行确认失败，再写最小实现，最后运行通过测试。
- 单元测试覆盖状态机、策略、权限、签名与纯业务规则；MySQL、Redis、RabbitMQ 使用 Testcontainers 集成测试；HTTP 契约使用 Spring MVC 或 Gateway 测试。
- 修改 Java、SQL 或 Maven 配置后至少运行关联测试；交付前运行 `./mvnw verify`。
- 常用命令：

```bash
./mvnw test
./mvnw -pl reservation-service test -Dtest=ReservationServiceIT
./mvnw -pl gateway spring-boot:run
./mvnw verify
```

## 代码规范

- 使用 Java 21 语言特性时以清晰和兼容性优先；DTO 可使用 `record`，领域实体保持显式不变量。
- Controller 只处理鉴权、请求校验和响应转换；事务边界位于 application service；Mapper 不承载业务规则。
- 不引入未经说明的框架、全局静态状态或跨领域直接访问 Mapper。跨领域协作通过 application 接口或领域事件完成。
- 配置、日志和示例不得提交真实密码、JWT 密钥、数据库连接串或 Webhook 密钥。
