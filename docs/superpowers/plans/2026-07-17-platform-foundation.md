# 平台工程基础实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立可迁移、可测试、可认证的后端工程基础。

**Architecture:** 建立共享 API、异常、安全、审计与迁移基础；由 Testcontainers 提供真实 MySQL 与 Redis 集成测试环境。

**Tech Stack:** Spring MVC, Spring Security, MyBatis, Flyway, MySQL, Redis, Testcontainers.

## Global Constraints

- 使用 Java 21 与 Maven Wrapper；配置必须支持环境变量覆盖。
- 使用 Flyway 管理 MySQL schema，禁止自动建表。
- API 错误不可暴露 SQL、堆栈、密码、Token 或 Webhook 密钥。
- 新业务逻辑先写失败测试再实现。

### Task 1: 定义统一 API 与错误语义

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/common/api/ApiResponse.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/common/error/ErrorCode.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/common/error/BusinessException.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/common/web/GlobalExceptionHandler.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/common/web/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Produces: `ApiResponse.success(T data)`, `ApiResponse.failure(ErrorCode code, String message)` 和 `BusinessException(ErrorCode)`。

- [ ] **Step 1: 写入失败的异常映射 MVC 测试。**

```java
@Test
void returnsStableCodeForBusinessException() throws Exception {
    mockMvc.perform(get("/test/business-error"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("RESERVATION_POLICY_VIOLATION"));
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=GlobalExceptionHandlerTest`

Expected: FAIL，因为 `GlobalExceptionHandler` 不存在。

- [ ] **Step 3: 实现最小错误契约。**

```java
public record ApiResponse<T>(String code, String message, T data, String requestId) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("OK", "OK", data, null);
    }
}
```

```java
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handle(BusinessException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(exception.errorCode(), exception.getMessage()));
    }
}
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=GlobalExceptionHandlerTest`

Expected: PASS。

### Task 2: 建立 MySQL 迁移与审计基类

**Files:**
- Create: `src/main/resources/db/migration/V1__create_platform_foundation.sql`
- Create: `src/main/java/cn/czu/claimpawsbackend/common/persistence/BaseEntity.java`
- Create: `src/test/java/cn/czu/claimpawsbackend/support/MySqlRedisIntegrationTest.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/common/persistence/FlywayMigrationIT.java`

**Interfaces:**
- Produces: 数据库表通用列 `id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted`。

- [ ] **Step 1: 写入失败的 Flyway 集成测试。**

```java
@Test
void migratesFoundationSchema() {
    assertThat(jdbcTemplate.queryForObject("select count(*) from information_schema.tables where table_name = 'sys_user'", Integer.class)).isEqualTo(1);
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=FlywayMigrationIT`

Expected: FAIL，因为 `sys_user` 表不存在。

- [ ] **Step 3: 创建首个迁移。**

```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  department_id BIGINT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(3) NOT NULL,
  created_by BIGINT NULL,
  updated_at DATETIME(3) NOT NULL,
  updated_by BIGINT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE KEY uk_sys_user_username (username)
);
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=FlywayMigrationIT`

Expected: PASS。

### Task 3: 建立 JWT 认证安全骨架

**Files:**
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/security/SecurityConfig.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/security/JwtAuthenticationFilter.java`
- Create: `src/main/java/cn/czu/claimpawsbackend/identity/security/JwtTokenService.java`
- Test: `src/test/java/cn/czu/claimpawsbackend/identity/security/JwtAuthenticationFilterTest.java`

**Interfaces:**
- Produces: `JwtTokenService.issueAccessToken(Long userId, Set<String> permissions)` 和 Bearer Token 认证上下文。

- [ ] **Step 1: 写入受保护 API 的失败测试。**

```java
@Test
void rejectsMissingBearerToken() throws Exception {
    mockMvc.perform(get("/api/v1/users/me"))
        .andExpect(status().isUnauthorized());
}
```

- [ ] **Step 2: 运行测试确认失败。**

Run: `./mvnw test -Dtest=JwtAuthenticationFilterTest`

Expected: FAIL，因为受保护路由尚未配置。

- [ ] **Step 3: 配置无状态安全链。**

```java
http.csrf(AbstractHttpConfigurer::disable)
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(registry -> registry
        .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
        .anyRequest().authenticated())
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

- [ ] **Step 4: 运行测试确认通过。**

Run: `./mvnw test -Dtest=JwtAuthenticationFilterTest`

Expected: PASS。
