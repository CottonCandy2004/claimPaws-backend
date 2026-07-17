# 预约生命周期实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现资源可用性查询、幂等预约、最多两级审批、签到、爽约、取消和完整状态审计。

**Architecture:** 预约模块在 MySQL 事务内执行业务校验、重叠检测、状态日志和 Outbox 写入；Redis 短期锁降低竞争，MySQL 查询作为最终冲突判断。审批和履约模块只能通过预约服务的状态转换接口改变预约。

**Tech Stack:** Spring Transaction, MyBatis, Redis, Spring MVC, Testcontainers.

## Global Constraints

- 有效预约时间段不得重叠；`CANCELLED`、`REJECTED` 和 `NO_SHOW` 不阻塞未来可用性。
- 所有预约创建请求包含 `Idempotency-Key`，相同用户和键只能返回同一个结果。
- 状态转换必须写入 `reservation_status_log` 和 Outbox。
- 用户签到遵守资源策略窗口；管理员代签到和强制取消必须写入理由。

### Task 1: 建立预约、审批、履约与审计 Schema

**Files:**
- Create: `src/main/resources/db/migration/V4__create_reservation_schema.sql`
- Create: `src/main/java/cn/czu/claimpawsbackend/reservation/domain/ReservationStatus.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/reservation/persistence/ReservationSchemaIT.java`

**Interfaces:**
- Produces: `reservation`, `reservation_approval`, `reservation_attendance`, `reservation_status_log` 表和 `ReservationStatus` 枚举。

- [ ] **Step 1: 写入失败的 schema 集成测试。**

```java
@Test
void createsReservationStatusLogTable() {
    assertThat(tableExists("reservation_status_log")).isTrue();
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=ReservationSchemaIT`

Expected: FAIL，因为迁移尚未创建预约表。

- [ ] **Step 3: 创建预约表和状态索引。**

```sql
CREATE TABLE reservation (
  id BIGINT PRIMARY KEY,
  resource_id BIGINT NOT NULL,
  requester_id BIGINT NOT NULL,
  start_at DATETIME(3) NOT NULL,
  end_at DATETIME(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  KEY idx_reservation_resource_time (resource_id, start_at, end_at, status)
);
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=ReservationSchemaIT`

Expected: PASS。

### Task 2: 实现可用性查询和策略校验

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/reservation/application/AvailabilityQueryService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/reservation/persistence/ReservationMapper.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/reservation/web/AvailabilityController.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/reservation/application/AvailabilityQueryServiceIT.java`

**Interfaces:**
- Produces: `List<AvailableResource> findAvailable(AvailabilityQuery query)`。

- [ ] **Step 1: 写入被有效预约排除的失败测试。**

```java
@Test
void excludesResourceWithOverlappingConfirmedReservation() {
    assertThat(service.findAvailable(queryFor(resourceId, start, end))).isEmpty();
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=AvailabilityQueryServiceIT`

Expected: FAIL，因为可用性查询不存在。

- [ ] **Step 3: 实现可用性查询。**

```java
public List<AvailableResource> findAvailable(AvailabilityQuery query) {
    return resourceMapper.findAvailable(query.startAt(), query.endAt(), List.of("PENDING_APPROVAL", "CONFIRMED", "CHECKED_IN"));
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=AvailabilityQueryServiceIT`

Expected: PASS。

### Task 3: 实现幂等、并发安全的预约创建

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/reservation/application/ReservationService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/reservation/infrastructure/ReservationLock.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/reservation/web/ReservationController.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/reservation/application/ReservationServiceIT.java`

**Interfaces:**
- Produces: `ReservationView create(long requesterId, String idempotencyKey, CreateReservationCommand command)`。

- [ ] **Step 1: 写入并发冲突失败测试。**

```java
@Test
void allowsOnlyOneConcurrentOverlappingReservation() throws Exception {
    List<Future<Outcome>> outcomes = submitTwoCreatesForSameResourceAndRange();
    assertThat(outcomes.stream().map(Future::get).filter(Outcome::success).count()).isEqualTo(1);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=ReservationServiceIT`

Expected: FAIL，因为预约创建服务不存在。

- [ ] **Step 3: 实现锁、事务和重叠检测。**

```java
@Transactional
public ReservationView create(long userId, String key, CreateReservationCommand command) {
    return idempotencyStore.execute(userId, key, () -> reservationLock.withLock(command.resourceId(), command.startAt(), command.endAt(), () -> {
        policyValidator.validate(command, userId);
        if (reservationMapper.existsOverlap(command.resourceId(), command.startAt(), command.endAt())) {
            throw new BusinessException(ErrorCode.RESERVATION_TIME_CONFLICT);
        }
        Reservation reservation = reservationFactory.create(command, userId);
        reservationMapper.insert(reservation);
        statusLogMapper.insert(StatusLog.created(reservation));
        return ReservationView.from(reservation);
    }));
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=ReservationServiceIT`

Expected: PASS。

### Task 4: 实现多级审批与取消

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/approval/application/ApprovalService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/approval/web/ApprovalController.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/approval/application/ApprovalServiceIT.java`

**Interfaces:**
- Produces: `void decide(long approverId, long approvalId, ApprovalDecision decision, String comment)`。

- [ ] **Step 1: 写入越级审批失败测试。**

```java
@Test
void rejectsSecondLevelDecisionBeforeFirstLevelApproved() {
    assertThatThrownBy(() -> service.decide(secondApprover, secondApprovalId, APPROVE, "ok"))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.APPROVAL_ACTION_FORBIDDEN);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=ApprovalServiceIT`

Expected: FAIL，因为审批服务不存在。

- [ ] **Step 3: 实现顺序审批。**

```java
@Transactional
public void decide(long approverId, long approvalId, ApprovalDecision decision, String comment) {
    Approval approval = approvalMapper.lockById(approvalId);
    approval.assertCanBeDecidedBy(approverId, approvalMapper.previousApproved(approval.reservationId(), approval.sequence()));
    approvalMapper.complete(approvalId, decision, comment);
    reservationStateMachine.applyApprovalResult(approval.reservationId(), decision, approvalMapper.hasPending(approval.reservationId()));
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=ApprovalServiceIT`

Expected: PASS。

### Task 5: 实现签到、爽约和管理员更正

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/attendance/application/AttendanceService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/attendance/web/AttendanceController.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/attendance/web/AdminReservationController.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/attendance/application/AttendanceServiceIT.java`

**Interfaces:**
- Produces: `void checkIn(long actorId, long reservationId, CheckInMode mode, String reason)`。

- [ ] **Step 1: 写入窗口外签到失败测试。**

```java
@Test
void rejectsUserCheckInOutsidePolicyWindow() {
    assertThatThrownBy(() -> service.checkIn(requesterId, reservationId, SELF, null))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.CHECK_IN_WINDOW_CLOSED);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=AttendanceServiceIT`

Expected: FAIL，因为签到服务不存在。

- [ ] **Step 3: 实现签到和管理员代签到。**

```java
@Transactional
public void checkIn(long actorId, long reservationId, CheckInMode mode, String reason) {
    Reservation reservation = reservationMapper.lockById(reservationId);
    attendanceAuthorizer.authorize(actorId, reservation, mode, reason);
    reservation.assertCheckInAllowed(clock.instant(), mode);
    attendanceMapper.insert(Attendance.create(reservationId, actorId, mode, reason));
    reservationStateMachine.transition(reservation, ReservationStatus.CHECKED_IN, actorId, reason);
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=AttendanceServiceIT`

Expected: PASS。
