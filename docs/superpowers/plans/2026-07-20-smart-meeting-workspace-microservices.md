# 智能会议室工位预约平台微服务实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 交付 Nacos、RabbitMQ 和独立数据存储支持的会议室与工位预约微服务系统。

**Architecture:** Maven 聚合工程管理 Gateway、身份、资源、预约和通知服务。服务同步调用经 OpenFeign，跨服务状态变化由本地 Outbox 发布到 RabbitMQ；每个服务仅访问自己的 MySQL schema 并通过 Flyway 管理迁移。

**Tech Stack:** Java 21, Spring Boot 4, Spring Cloud, Spring Cloud Alibaba Nacos, Spring Cloud Gateway, OpenFeign, Spring AMQP, MyBatis, MySQL 8, Redis, Flyway, Testcontainers.

## Global Constraints

- 服务模块为 `gateway`、`identity-service`、`resource-service`、`reservation-service`、`notification-service`。
- Nacos 提供服务发现和配置中心；各服务拥有独立 Data ID 与 `dev`、`test`、`prod` Namespace。
- 每个服务拥有独立数据库/schema 和 Flyway 迁移；禁止跨服务数据库、Mapper 或分布式事务。
- 事件使用 RabbitMQ Topic Exchange `claimpaws.domain.events`，消息和消费记录均以 `eventId` 幂等。
- API 统一以 `/api/v1` 为前缀，修改操作要求 JWT、`X-Request-Id`，预约创建另要求 `Idempotency-Key`。
- 新业务行为严格按 Red-Green-Refactor 完成测试。

### Task 1: 创建聚合工程和共享契约模块

**Files:**
- Modify: `pom.xml`
- Create: `claimpaws-common/pom.xml`
- Create: `claimpaws-common/src/main/java/cn/czu/claimpaws/common/api/ApiResponse.java`
- Create: `claimpaws-common/src/main/java/cn/czu/claimpaws/common/event/DomainEvent.java`
- Create: `gateway/pom.xml`
- Create: `identity-service/pom.xml`
- Create: `resource-service/pom.xml`
- Create: `reservation-service/pom.xml`
- Create: `notification-service/pom.xml`
- Test: `claimpaws-common/src/test/java/cn/czu/claimpaws/common/event/DomainEventTest.java`

**Interfaces:**
- Produces: `ApiResponse<T>`、`DomainEvent(UUID eventId, String eventType, Instant occurredAt, long aggregateId, int schemaVersion, JsonNode payload)`。

- [ ] **Step 1: 写入事件不可变性的失败测试。**

```java
@Test
void requiresEventIdentityAndType() {
    assertThatThrownBy(() -> new DomainEvent(null, "", Instant.now(), 1L, 1, objectNode))
        .isInstanceOf(IllegalArgumentException.class);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw -pl claimpaws-common test -Dtest=DomainEventTest`

Expected: FAIL，因为共享契约模块不存在。

- [ ] **Step 3: 建立父 POM、模块 POM 和最小事件契约。**

```xml
<packaging>pom</packaging>
<modules>
  <module>claimpaws-common</module><module>gateway</module><module>identity-service</module>
  <module>resource-service</module><module>reservation-service</module><module>notification-service</module>
</modules>
```

```java
public record DomainEvent(UUID eventId, String eventType, Instant occurredAt, long aggregateId, int schemaVersion, JsonNode payload) {
    public DomainEvent {
        if (eventId == null || eventType == null || eventType.isBlank() || occurredAt == null || payload == null) throw new IllegalArgumentException("event fields are required");
    }
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw -pl claimpaws-common test -Dtest=DomainEventTest`

Expected: PASS。

### Task 2: 建立 Nacos、Gateway 和服务安全基础

**Files:**
- Create: `gateway/src/main/java/cn/czu/claimpaws/gateway/GatewayApplication.java`
- Create: `gateway/src/main/resources/application.yaml`
- Create: `gateway/src/main/resources/application-dev.yaml`
- Create: `identity-service/src/main/java/cn/czu/claimpaws/identity/IdentityApplication.java`
- Create: `identity-service/src/main/java/cn/czu/claimpaws/identity/security/JwtTokenService.java`
- Create: `identity-service/src/main/resources/application.yaml`
- Test: `gateway/src/test/java/cn/czu/claimpaws/gateway/GatewayRouteIT.java`

**Interfaces:**
- Produces: Nacos 服务名 `gateway`、`identity-service`，路由 `/api/v1/auth/**` 到 `lb://identity-service`。

- [ ] **Step 1: 写入 Gateway 路由失败测试。**

```java
@Test
void routesAuthenticationRequestsToIdentityService() {
    webTestClient.get().uri("/api/v1/auth/login").exchange().expectStatus().isEqualTo(405);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw -pl gateway test -Dtest=GatewayRouteIT`

Expected: FAIL，因为 Gateway 应用和路由不存在。

- [ ] **Step 3: 配置 Nacos 导入和负载均衡路由。**

```yaml
spring:
  config:
    import: optional:nacos:${NACOS_SERVER_ADDR:127.0.0.1:8848}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
    gateway:
      server:
        webflux:
          routes:
            - id: identity
              uri: lb://identity-service
              predicates: [Path=/api/v1/auth/**]
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw -pl gateway test -Dtest=GatewayRouteIT`

Expected: PASS。

### Task 3: 交付身份和资源服务

**Files:**
- Create: `identity-service/src/main/resources/db/migration/V1__create_identity_schema.sql`
- Create: `identity-service/src/main/java/cn/czu/claimpaws/identity/application/AuthenticationService.java`
- Create: `resource-service/src/main/resources/db/migration/V1__create_resource_schema.sql`
- Create: `resource-service/src/main/java/cn/czu/claimpaws/resource/application/ResourceService.java`
- Create: `resource-service/src/main/java/cn/czu/claimpaws/resource/application/ReservationPolicyService.java`
- Test: `identity-service/src/test/java/cn/czu/claimpaws/identity/AuthenticationServiceIT.java`
- Test: `resource-service/src/test/java/cn/czu/claimpaws/resource/ReservationPolicyServiceIT.java`

**Interfaces:**
- Produces: `GET /internal/v1/resources/{id}/reservation-snapshot`，返回资源、策略和版本快照。

- [ ] **Step 1: 写入资源策略快照失败测试。**

```java
@Test
void returnsActivePolicySnapshotForReservableResource() {
    assertThat(resourceService.snapshot(resourceId).policy().slotMinutes()).isEqualTo(30);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw -pl resource-service test -Dtest=ReservationPolicyServiceIT`

Expected: FAIL，因为快照服务不存在。

- [ ] **Step 3: 实现身份、资源、策略及只读快照。**

```java
public ReservationSnapshot snapshot(long resourceId) {
    Resource resource = resourceMapper.requireActive(resourceId);
    return ReservationSnapshot.from(resource, policyMapper.requireActiveByResourceId(resourceId));
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw -pl identity-service,resource-service test -Dtest='*AuthenticationServiceIT,*ReservationPolicyServiceIT'`

Expected: PASS。

### Task 4: 交付预约、审批和 Outbox 发布

**Files:**
- Create: `reservation-service/src/main/resources/db/migration/V1__create_reservation_schema.sql`
- Create: `reservation-service/src/main/java/cn/czu/claimpaws/reservation/application/ReservationService.java`
- Create: `reservation-service/src/main/java/cn/czu/claimpaws/reservation/infrastructure/ResourceClient.java`
- Create: `reservation-service/src/main/java/cn/czu/claimpaws/reservation/infrastructure/OutboxPublisher.java`
- Test: `reservation-service/src/test/java/cn/czu/claimpaws/reservation/ReservationServiceIT.java`

**Interfaces:**
- Consumes: `ReservationSnapshot ResourceClient.getSnapshot(long resourceId)`。
- Produces: `ReservationView create(long userId, String idempotencyKey, CreateReservationCommand command)` 和 `reservation.created` Outbox 事件。

- [ ] **Step 1: 写入预约冲突和 Outbox 原子性失败测试。**

```java
@Test
void createsOneReservationAndOneOutboxEventInSameTransaction() {
    reservationService.create(userId, "key-1", command);
    assertThat(reservationMapper.count()).isEqualTo(1);
    assertThat(outboxMapper.countByType("reservation.created")).isEqualTo(1);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw -pl reservation-service test -Dtest=ReservationServiceIT`

Expected: FAIL，因为预约服务不存在。

- [ ] **Step 3: 在本地事务实现预约和 Outbox。**

```java
@Transactional
public ReservationView create(long userId, String key, CreateReservationCommand command) {
    ReservationSnapshot snapshot = resourceClient.getSnapshot(command.resourceId());
    return idempotency.execute(userId, key, () -> reservationLock.withLock(command, () -> {
        validator.validate(command, snapshot);
        if (reservationMapper.existsOverlap(command.resourceId(), command.startAt(), command.endAt())) throw new BusinessException(ErrorCode.RESERVATION_TIME_CONFLICT);
        Reservation reservation = reservationMapper.insert(Reservation.create(userId, command, snapshot));
        outboxMapper.insert(OutboxMessage.created(DomainEvents.reservationCreated(reservation)));
        return ReservationView.from(reservation);
    }));
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw -pl reservation-service test -Dtest=ReservationServiceIT`

Expected: PASS。

### Task 5: 交付 RabbitMQ 消费、Webhook 和死信处理

**Files:**
- Create: `reservation-service/src/main/java/cn/czu/claimpaws/reservation/job/OutboxPublishJob.java`
- Create: `notification-service/src/main/resources/db/migration/V1__create_notification_schema.sql`
- Create: `notification-service/src/main/java/cn/czu/claimpaws/notification/messaging/ReservationEventListener.java`
- Create: `notification-service/src/main/java/cn/czu/claimpaws/notification/application/WebhookDeliveryService.java`
- Test: `notification-service/src/test/java/cn/czu/claimpaws/notification/ReservationEventListenerIT.java`

**Interfaces:**
- Consumes: `claimpaws.domain.events` 上 `reservation.*` 事件。
- Produces: 以 `eventId` 幂等的 `notification_delivery` 记录和 HMAC 签名 Webhook 请求。

- [ ] **Step 1: 写入重复消息失败测试。**

```java
@Test
void ignoresDuplicateEventId() {
    listener.consume(event);
    listener.consume(event);
    assertThat(deliveryMapper.countByEventId(event.eventId())).isEqualTo(1);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw -pl notification-service test -Dtest=ReservationEventListenerIT`

Expected: FAIL，因为事件消费者不存在。

- [ ] **Step 3: 实现幂等消费和死信路由。**

```java
@RabbitListener(queues = "notification.reservation.events")
@Transactional
public void consume(DomainEvent event) {
    if (consumedEventMapper.exists(event.eventId())) return;
    consumedEventMapper.insert(event.eventId());
    webhookDeliveryService.createDeliveries(event);
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw -pl notification-service test -Dtest=ReservationEventListenerIT`

Expected: PASS。

- [ ] **Step 5: 提交实现。**

```bash
git add pom.xml claimpaws-common gateway identity-service resource-service reservation-service notification-service docs/superpowers/plans
git commit -m "feat: build reservation microservices"
```
