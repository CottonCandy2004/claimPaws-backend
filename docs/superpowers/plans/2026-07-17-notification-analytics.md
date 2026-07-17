# Webhook 通知与运营统计实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 通过数据库 Outbox 可靠投递 Webhook，并产生日级资源与部门运营统计。

**Architecture:** 预约状态变更在同一数据库事务内写入 Outbox。定时作业锁定待投递事件，为每个已启用端点建立投递记录并使用 OpenFeign 发送 HMAC 签名请求；失败按指数退避重试。统计作业以预约和状态日志生成幂等日聚合。

**Tech Stack:** Spring Scheduling, Spring Cloud OpenFeign, MyBatis, MySQL, Spring Crypto, Testcontainers.

## Global Constraints

- 首期不依赖 RabbitMQ；`DomainEventPublisher` 的调用者不得依赖具体投递实现。
- Webhook 密钥只能以密文持久化，响应和日志必须脱敏。
- 每个事件使用全局唯一 `eventId`，请求使用 HMAC-SHA256 和 UTC 时间戳。
- 投递失败不回滚预约业务；最大重试后保留失败记录供管理员查询和手动重投。
- 统计作业必须可重复执行，不重复累计同一天的数据。

### Task 1: 建立 Outbox、Webhook 配置与统计 Schema

**Files:**
- Create: `src/main/resources/db/migration/V5__create_notification_and_analytics_schema.sql`
- Test: `src/test/java/cn/czu/claimpawsbackend/notification/persistence/NotificationSchemaIT.java`

**Interfaces:**
- Produces: `notification_webhook_config`, `notification_outbox`, `notification_delivery`, `analytics_resource_daily`, `analytics_department_daily` 表。

- [ ] **Step 1: 写入失败的迁移测试。**

```java
@Test
void createsOutboxWithUniqueEventId() {
    assertThat(indexExists("notification_outbox", "uk_notification_outbox_event_id")).isTrue();
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=NotificationSchemaIT`

Expected: FAIL，因为 Outbox 表尚不存在。

- [ ] **Step 3: 创建通知表。**

```sql
CREATE TABLE notification_outbox (
  id BIGINT PRIMARY KEY,
  event_id CHAR(36) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id BIGINT NOT NULL,
  payload JSON NOT NULL,
  occurred_at DATETIME(3) NOT NULL,
  published_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  UNIQUE KEY uk_notification_outbox_event_id (event_id)
);
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=NotificationSchemaIT`

Expected: PASS。

### Task 2: 实现事务内领域事件发布

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/notification/domain/DomainEventPublisher.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/notification/infrastructure/DatabaseOutboxPublisher.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/notification/persistence/OutboxMapper.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/notification/infrastructure/DatabaseOutboxPublisherIT.java`

**Interfaces:**
- Produces: `void publish(DomainEvent event)`，其中 `DomainEvent` 包含 `UUID eventId()`、`String eventType()`、`long aggregateId()` 与 `Instant occurredAt()`。

- [ ] **Step 1: 写入事务回滚失败测试。**

```java
@Test
void rollsBackOutboxEventWhenReservationTransactionFails() {
    assertThatThrownBy(() -> reservationService.create(commandThatFailsAfterPublish()));
    assertThat(outboxMapper.count()).isZero();
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=DatabaseOutboxPublisherIT`

Expected: FAIL，因为 Outbox 发布器不存在。

- [ ] **Step 3: 实现 Outbox 写入。**

```java
@Component
class DatabaseOutboxPublisher implements DomainEventPublisher {
    @Override
    public void publish(DomainEvent event) {
        outboxMapper.insert(OutboxMessage.from(event));
    }
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=DatabaseOutboxPublisherIT`

Expected: PASS。

### Task 3: 实现 Webhook 签名、投递与重试

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/notification/application/WebhookDeliveryService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/notification/application/WebhookSignatureService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/notification/infrastructure/WebhookClient.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/notification/job/WebhookDeliveryJob.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/notification/application/WebhookSignatureServiceTest.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/notification/application/WebhookDeliveryServiceIT.java`

**Interfaces:**
- Produces: `String sign(String secret, String timestamp, String payload)` 和 `void deliverDueEvents()`。

- [ ] **Step 1: 写入失败的 HMAC 签名测试。**

```java
@Test
void createsExpectedSha256Signature() {
    assertThat(service.sign("secret", "2026-07-17T00:00:00Z", "{\"id\":1}"))
        .isEqualTo("3b9774c30d4e7bb1c8ac3f041e6fb7b179abdc451672bb9398a7ceda5deae1fc");
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=WebhookSignatureServiceTest`

Expected: FAIL，因为签名服务不存在。

- [ ] **Step 3: 实现签名和投递请求。**

```java
public String sign(String secret, String timestamp, String payload) {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return HexFormat.of().formatHex(mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
}
```

```java
webhookClient.post(config.url(), payload, Map.of(
    "X-Webhook-Id", event.eventId().toString(),
    "X-Webhook-Event", event.eventType(),
    "X-Webhook-Timestamp", timestamp,
    "X-Webhook-Signature", signature));
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=WebhookSignatureServiceTest,WebhookDeliveryServiceIT`

Expected: PASS，失败投递的 `next_attempt_at` 按指数退避增加。

### Task 4: 实现日统计聚合与查询

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/analytics/application/AnalyticsAggregationService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/analytics/job/AnalyticsAggregationJob.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/analytics/web/AnalyticsController.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/analytics/application/AnalyticsAggregationServiceIT.java`

**Interfaces:**
- Produces: `void aggregate(LocalDate day)` 和 `ResourceDailyMetric getResourceDailyMetric(long resourceId, LocalDate day)`。

- [ ] **Step 1: 写入幂等聚合失败测试。**

```java
@Test
void replacesInsteadOfDoubleCountingExistingDailyMetric() {
    service.aggregate(day);
    service.aggregate(day);
    assertThat(metricMapper.find(resourceId, day).reservationCount()).isEqualTo(2);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=AnalyticsAggregationServiceIT`

Expected: FAIL，因为聚合服务不存在。

- [ ] **Step 3: 实现 upsert 日聚合。**

```java
@Transactional
public void aggregate(LocalDate day) {
    analyticsMapper.upsertResourceMetrics(day);
    analyticsMapper.upsertDepartmentMetrics(day);
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=AnalyticsAggregationServiceIT`

Expected: PASS。
