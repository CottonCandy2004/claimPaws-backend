# 身份与资源管理实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 提供本地 JWT 身份、RBAC、部门、空间资源和预约策略的可管理 API。

**Architecture:** `identity` 模块负责身份提供方抽象和本地 JWT 实现，`organization` 管理部门树，`resource` 管理位置、资源和规则。所有查询经 MyBatis Mapper，所有写入由服务层控制事务。

**Tech Stack:** Spring Security, JJWT, Spring Data Redis, MyBatis, Flyway, Spring MVC, Testcontainers.

## Global Constraints

- 本地账号密码仅保存 BCrypt 哈希，不在日志、响应或异常中输出密码。
- OAuth2/OIDC 只定义提供方接口和绑定表，不实现外部回调登录。
- 管理接口必须使用细粒度权限；用户只能读取允许范围内的数据。
- 所有资源和策略修改必须失效可用性缓存。

### Task 1: 实现本地登录、Refresh Token 与身份提供方抽象

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/application/AuthenticationService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/domain/IdentityProvider.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/infrastructure/LocalIdentityProvider.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/web/AuthController.java`
- Create: `src/main/resources/db/migration/V2__create_identity_and_organization.sql`
- Test: `src/test/java/cn/czu/claimpawsbackend/identity/application/AuthenticationServiceIT.java`

**Interfaces:**
- Consumes: `JwtTokenService.issueAccessToken(Long, Set<String>)`。
- Produces: `TokenPair login(LoginCommand command)`, `TokenPair refresh(String refreshToken)` 和 `void logout(String refreshToken)`。

- [ ] **Step 1: 写入登录成功的失败集成测试。**

```java
@Test
void issuesTokensForValidLocalCredentials() {
    TokenPair pair = authenticationService.login(new LoginCommand("alice", "correct-password"));
    assertThat(pair.accessToken()).isNotBlank();
    assertThat(pair.refreshToken()).isNotBlank();
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=AuthenticationServiceIT`

Expected: FAIL，因为 `AuthenticationService` 不存在。

- [ ] **Step 3: 实现最小登录与令牌存储。**

```java
public TokenPair login(LoginCommand command) {
    User user = userMapper.findByUsername(command.username()).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
    if (!passwordEncoder.matches(command.password(), user.passwordHash())) {
        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
    String accessToken = jwtTokenService.issueAccessToken(user.id(), permissionMapper.findCodesByUserId(user.id()));
    String refreshToken = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set("auth:refresh-token:" + refreshToken, String.valueOf(user.id()), refreshTtl);
    return new TokenPair(accessToken, refreshToken);
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=AuthenticationServiceIT`

Expected: PASS。

### Task 2: 实现 RBAC 与部门树管理

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/organization/application/DepartmentService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/organization/persistence/DepartmentMapper.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/organization/web/DepartmentController.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/web/UserAdminController.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/organization/application/DepartmentServiceIT.java`

**Interfaces:**
- Produces: `DepartmentId create(CreateDepartmentCommand command)`，其中父节点必须已存在且不可形成环。

- [ ] **Step 1: 写入部门环校验的失败测试。**

```java
@Test
void rejectsMovingDepartmentBelowItsDescendant() {
    assertThatThrownBy(() -> departmentService.move(engineeringId, backendTeamId))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.DEPARTMENT_CYCLE);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=DepartmentServiceIT`

Expected: FAIL，因为部门移动服务不存在。

- [ ] **Step 3: 实现祖先链校验。**

```java
public void move(long departmentId, long parentId) {
    if (departmentMapper.isDescendant(parentId, departmentId)) {
        throw new BusinessException(ErrorCode.DEPARTMENT_CYCLE);
    }
    departmentMapper.updateParent(departmentId, parentId);
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=DepartmentServiceIT`

Expected: PASS。

### Task 3: 实现位置与资源管理

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/resource/application/ResourceService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/resource/persistence/ResourceMapper.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/resource/web/ResourceController.java`
- Create: `src/main/resources/db/migration/V3__create_resource_schema.sql`
- Test: `src/test/java/cn/czu/claimpawsbackend/resource/application/ResourceServiceIT.java`

**Interfaces:**
- Produces: `long createResource(CreateResourceCommand command)`，资源类型仅允许 `MEETING_ROOM` 或 `WORKSTATION`。

- [ ] **Step 1: 写入资源类型失败测试。**

```java
@Test
void rejectsUnknownResourceType() {
    assertThatThrownBy(() -> resourceService.create(new CreateResourceCommand("Desk 1", "PARKING", floorId)))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.RESOURCE_TYPE_INVALID);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=ResourceServiceIT`

Expected: FAIL，因为资源服务不存在。

- [ ] **Step 3: 实现资源创建。**

```java
public long create(CreateResourceCommand command) {
    ResourceType type = ResourceType.valueOf(command.type());
    resourceMapper.requireFloor(command.floorId());
    return resourceMapper.insert(Resource.create(command.name(), type, command.floorId(), command.capacity(), command.attributes()));
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=ResourceServiceIT`

Expected: PASS。

### Task 4: 实现预约策略及缓存失效

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/resource/application/ReservationPolicyService.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/resource/domain/ReservationPolicy.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/resource/web/ReservationPolicyController.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/resource/application/ReservationPolicyServiceTest.java`

**Interfaces:**
- Produces: `void validate(ReservationPolicy policy)`，审批级数必须在 0 至 2，时段粒度必须大于 0。

- [ ] **Step 1: 写入审批级数失败测试。**

```java
@Test
void rejectsPolicyWithMoreThanTwoApprovalLevels() {
    assertThatThrownBy(() -> service.create(policyWithApprovalLevels(3)))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.POLICY_APPROVAL_LEVEL_INVALID);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=ReservationPolicyServiceTest`

Expected: FAIL，因为策略校验不存在。

- [ ] **Step 3: 实现策略校验与缓存失效。**

```java
public void create(CreateReservationPolicyCommand command) {
    ReservationPolicy policy = ReservationPolicy.from(command);
    policy.validate();
    policyMapper.insert(policy);
    availabilityCache.evictByResource(policy.resourceId());
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=ReservationPolicyServiceTest`

Expected: PASS。
