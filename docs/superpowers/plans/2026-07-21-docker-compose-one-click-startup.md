# Docker Compose 一键启动 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 5 个业务服务创建 Dockerfile 和 docker profile，更新 docker-compose.yml 实现一键启动全部服务。

**Architecture:** 预构建 JAR 模式（宿主机 `mvn package`），每个服务用 `eclipse-temurin:21-jre` 镜像。所有容器加入自定义网络 `claimpaws-net`，通过容器名通信。新建 `docker` Spring profile 统一管理容器化连接配置。

**Tech Stack:** Docker, docker-compose, eclipse-temurin:21-jre, Spring Boot 3.5, Spring Cloud Nacos

## Global Constraints

- 预构建 JAR 模式：Dockerfile 仅复制已编译的 JAR，不做 Maven 容器内构建
- 新建 `docker` profile（`application-docker.yaml`），不修改现有文件
- 自定义网络 `claimpaws-net`，业务服务端口不暴露到宿主机（仅 gateway:8080 对外）
- `.env` 加入 `.gitignore`，`.env.example` 提交到版本控制
- 基础设施服务保留现有健康检查，业务服务通过 `depends_on condition: service_healthy` 保证启动顺序
---

### Task 1: 环境变量与 .gitignore

**Files:**
- Create: `.env.example`
- Create: `.env`
- Modify: `.gitignore:35`

**Interface:**
- Produces: `JWT_SECRET`、`WEBHOOK_ENCRYPTION_KEY` 环境变量，供 docker-compose 和 application-docker.yaml 引用

- [ ] **Step 1: 创建 .env.example**

```bash
# claimPaws Docker Environment Variables
# Copy this file to .env and adjust values as needed.

SPRING_PROFILES_ACTIVE=docker

# MySQL
MYSQL_USER=claimpaws
MYSQL_PASSWORD=local-dev-only

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_USERNAME=claimpaws
RABBITMQ_PASSWORD=local-dev-only

# JWT (at least 32 bytes for HMAC-SHA256)
JWT_SECRET=local-dev-jwt-secret-change-me-32-bytes

# Webhook Encryption (Base64-encoded 16-byte AES key)
WEBHOOK_ENCRYPTION_KEY=MDEyMzQ1Njc4OWFiY2RlZg==

# Nacos
NACOS_SERVER_ADDR=nacos:8848
```

- [ ] **Step 2: 创建 .env（基于 .env.example 的相同内容）**

内容与 `.env.example` 完全一致。

- [ ] **Step 3: 将 .env 加入 .gitignore**

在 `.gitignore` 末尾追加一行：

```
.env
```

- [ ] **Step 4: 验证文件**

```bash
cat .env.example
cat .env
grep '\.env' .gitignore
```

- [ ] **Step 5: 提交**

```bash
git add .env.example .gitignore
git commit -m "feat: add .env.example and ignore .env for docker-compose"
```

---

### Task 2: gateway — Dockerfile 和 docker profile

**Files:**
- Create: `gateway/src/main/resources/application-docker.yaml`
- Create: `gateway/Dockerfile`

**Interface:**
- Consumes: `NACOS_SERVER_ADDR` (env var from .env)
- Produces: gateway 容器镜像，端口 8080

- [ ] **Step 1: 创建 gateway/application-docker.yaml**

```yaml
spring:
  config:
    activate:
      on-profile: docker
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
  config:
    import: optional:nacos:${NACOS_SERVER_ADDR:nacos:8848}

server:
  port: 8080
```

> 说明：gateway 无数据库、无 Redis、无 RabbitMQ。仅覆盖 Nacos 地址。

- [ ] **Step 2: 创建 gateway/Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: 验证文件存在**

```bash
cat gateway/src/main/resources/application-docker.yaml
cat gateway/Dockerfile
```

- [ ] **Step 4: 提交**

```bash
git add gateway/src/main/resources/application-docker.yaml gateway/Dockerfile
git commit -m "feat: add Dockerfile and docker profile for gateway"
```

---

### Task 3: identity-service — Dockerfile 和 docker profile

**Files:**
- Create: `identity-service/src/main/resources/application-docker.yaml`
- Create: `identity-service/Dockerfile`

**Interface:**
- Consumes: `MYSQL_USER`、`MYSQL_PASSWORD`、`JWT_SECRET`、`NACOS_SERVER_ADDR` (env vars from .env)
- Produces: identity-service 容器镜像，端口 8081

- [ ] **Step 1: 创建 identity-service/application-docker.yaml**

```yaml
spring:
  config:
    activate:
      on-profile: docker
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
  config:
    import: optional:nacos:${NACOS_SERVER_ADDR:nacos:8848}
  datasource:
    url: jdbc:mysql://mysql:3306/claimpaws_identity?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf-8
    username: ${MYSQL_USER:claimpaws}
    password: ${MYSQL_PASSWORD:local-dev-only}
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: ${REDIS_PORT:6379}
  flyway:
    enabled: true
    locations: classpath:db/migration

jwt:
  secret: ${JWT_SECRET:local-dev-jwt-secret-change-me-32-bytes}

server:
  port: 8081
```

- [ ] **Step 2: 创建 identity-service/Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: 验证文件**

```bash
cat identity-service/src/main/resources/application-docker.yaml
cat identity-service/Dockerfile
```

- [ ] **Step 4: 提交**

```bash
git add identity-service/src/main/resources/application-docker.yaml identity-service/Dockerfile
git commit -m "feat: add Dockerfile and docker profile for identity-service"
```

---

### Task 4: resource-service — Dockerfile 和 docker profile

**Files:**
- Create: `resource-service/src/main/resources/application-docker.yaml`
- Create: `resource-service/Dockerfile`

**Interface:**
- Consumes: `MYSQL_USER`、`MYSQL_PASSWORD`、`NACOS_SERVER_ADDR` (env vars from .env)
- Produces: resource-service 容器镜像，端口 8082

- [ ] **Step 1: 创建 resource-service/application-docker.yaml**

```yaml
spring:
  config:
    activate:
      on-profile: docker
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
  config:
    import: optional:nacos:${NACOS_SERVER_ADDR:nacos:8848}
  datasource:
    url: jdbc:mysql://mysql:3306/claimpaws_resource?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf-8
    username: ${MYSQL_USER:claimpaws}
    password: ${MYSQL_PASSWORD:local-dev-only}
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8082
```

- [ ] **Step 2: 创建 resource-service/Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: 验证文件**

```bash
cat resource-service/src/main/resources/application-docker.yaml
cat resource-service/Dockerfile
```

- [ ] **Step 4: 提交**

```bash
git add resource-service/src/main/resources/application-docker.yaml resource-service/Dockerfile
git commit -m "feat: add Dockerfile and docker profile for resource-service"
```

---

### Task 5: reservation-service — Dockerfile 和 docker profile

**Files:**
- Create: `reservation-service/src/main/resources/application-docker.yaml`
- Create: `reservation-service/Dockerfile`

**Interface:**
- Consumes: `MYSQL_USER`、`MYSQL_PASSWORD`、`RABBITMQ_USERNAME`、`RABBITMQ_PASSWORD`、`NACOS_SERVER_ADDR` (env vars from .env)
- Produces: reservation-service 容器镜像，端口 8083

- [ ] **Step 1: 创建 reservation-service/application-docker.yaml**

```yaml
spring:
  config:
    activate:
      on-profile: docker
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
  config:
    import: optional:nacos:${NACOS_SERVER_ADDR:nacos:8848}
  datasource:
    url: jdbc:mysql://mysql:3306/claimpaws_reservation?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf-8
    username: ${MYSQL_USER:claimpaws}
    password: ${MYSQL_PASSWORD:local-dev-only}
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: ${REDIS_PORT:6379}
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: ${RABBITMQ_USERNAME:claimpaws}
    password: ${RABBITMQ_PASSWORD:local-dev-only}
    publisher-confirm-type: correlated
    publisher-returns: true
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8083
```

- [ ] **Step 2: 创建 reservation-service/Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: 验证文件**

```bash
cat reservation-service/src/main/resources/application-docker.yaml
cat reservation-service/Dockerfile
```

- [ ] **Step 4: 提交**

```bash
git add reservation-service/src/main/resources/application-docker.yaml reservation-service/Dockerfile
git commit -m "feat: add Dockerfile and docker profile for reservation-service"
```

---

### Task 6: notification-service — Dockerfile 和 docker profile

**Files:**
- Create: `notification-service/src/main/resources/application-docker.yaml`
- Create: `notification-service/Dockerfile`

**Interface:**
- Consumes: `MYSQL_USER`、`MYSQL_PASSWORD`、`RABBITMQ_USERNAME`、`RABBITMQ_PASSWORD`、`WEBHOOK_ENCRYPTION_KEY`、`NACOS_SERVER_ADDR` (env vars from .env)
- Produces: notification-service 容器镜像，端口 8084

- [ ] **Step 1: 创建 notification-service/application-docker.yaml**

```yaml
spring:
  config:
    activate:
      on-profile: docker
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
  config:
    import: optional:nacos:${NACOS_SERVER_ADDR:nacos:8848}
  datasource:
    url: jdbc:mysql://mysql:3306/claimpaws_notification?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf-8
    username: ${MYSQL_USER:claimpaws}
    password: ${MYSQL_PASSWORD:local-dev-only}
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: ${RABBITMQ_USERNAME:claimpaws}
    password: ${RABBITMQ_PASSWORD:local-dev-only}
  flyway:
    enabled: true
    locations: classpath:db/migration

notification:
  webhook:
    enabled: true
    connect-timeout: 5000
    read-timeout: 10000
    max-retries: 3
    retry-base-seconds: 5
    encryption-key: ${WEBHOOK_ENCRYPTION_KEY:MDEyMzQ1Njc4OWFiY2RlZg==}

server:
  port: 8084
```

- [ ] **Step 2: 创建 notification-service/Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: 验证文件**

```bash
cat notification-service/src/main/resources/application-docker.yaml
cat notification-service/Dockerfile
```

- [ ] **Step 4: 提交**

```bash
git add notification-service/src/main/resources/application-docker.yaml notification-service/Dockerfile
git commit -m "feat: add Dockerfile and docker profile for notification-service"
```

---

### Task 7: 更新 docker-compose.yml — 网络 + 业务服务

**Files:**
- Modify: `docker-compose.yml:1-60`

**Interface:**
- Consumes: 5 个 Dockerfile 构建的镜像、`.env` 环境变量
- Produces: 完整 `docker compose up` 一键启动所有服务

- [ ] **Step 1: 读取当前 docker-compose.yml 确认基线**

```bash
cat docker-compose.yml
```

- [ ] **Step 2: 替换 docker-compose.yml 为完整内容**

```yaml
networks:
  claimpaws-net:
    driver: bridge

services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: local-dev-only
      MYSQL_DATABASE: claimpaws_identity
      MYSQL_USER: claimpaws
      MYSQL_PASSWORD: local-dev-only
    ports:
      - "127.0.0.1:3306:3306"
    volumes:
      - ./docker/mysql/init-databases.sql:/docker-entrypoint-initdb.d/init-databases.sql:ro
      - mysql-data:/var/lib/mysql
    networks:
      - claimpaws-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-plocal-dev-only"]
      interval: 10s
      timeout: 5s
      retries: 10

  redis:
    image: redis:7-alpine
    ports:
      - "127.0.0.1:6379:6379"
    networks:
      - claimpaws-net
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 10

  rabbitmq:
    image: rabbitmq:3.13-management
    environment:
      RABBITMQ_DEFAULT_USER: claimpaws
      RABBITMQ_DEFAULT_PASS: local-dev-only
    ports:
      - "127.0.0.1:5672:5672"
      - "127.0.0.1:15672:15672"
    networks:
      - claimpaws-net
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 10

  nacos:
    image: nacos/nacos-server:v2.3.2
    environment:
      MODE: standalone
      PREFER_HOST_MODE: hostname
      NACOS_AUTH_ENABLE: "false"
    ports:
      - "127.0.0.1:8848:8848"
      - "127.0.0.1:9848:9848"
    networks:
      - claimpaws-net
    healthcheck:
      test: ["CMD-SHELL", "curl -fsS http://localhost:8848/nacos/actuator/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 20

  identity-service:
    build:
      context: ./identity-service
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-docker}
      NACOS_SERVER_ADDR: ${NACOS_SERVER_ADDR:-nacos:8848}
      MYSQL_USER: ${MYSQL_USER:-claimpaws}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-local-dev-only}
      REDIS_HOST: ${REDIS_HOST:-redis}
      REDIS_PORT: ${REDIS_PORT:-6379}
      JWT_SECRET: ${JWT_SECRET:-local-dev-jwt-secret-change-me-32-bytes}
    ports:
      - "127.0.0.1:8081:8081"
    networks:
      - claimpaws-net
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      nacos:
        condition: service_healthy

  resource-service:
    build:
      context: ./resource-service
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-docker}
      NACOS_SERVER_ADDR: ${NACOS_SERVER_ADDR:-nacos:8848}
      MYSQL_USER: ${MYSQL_USER:-claimpaws}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-local-dev-only}
    ports:
      - "127.0.0.1:8082:8082"
    networks:
      - claimpaws-net
    depends_on:
      mysql:
        condition: service_healthy
      nacos:
        condition: service_healthy

  reservation-service:
    build:
      context: ./reservation-service
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-docker}
      NACOS_SERVER_ADDR: ${NACOS_SERVER_ADDR:-nacos:8848}
      MYSQL_USER: ${MYSQL_USER:-claimpaws}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-local-dev-only}
      REDIS_HOST: ${REDIS_HOST:-redis}
      REDIS_PORT: ${REDIS_PORT:-6379}
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME:-claimpaws}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD:-local-dev-only}
    ports:
      - "127.0.0.1:8083:8083"
    networks:
      - claimpaws-net
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
      nacos:
        condition: service_healthy

  notification-service:
    build:
      context: ./notification-service
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-docker}
      NACOS_SERVER_ADDR: ${NACOS_SERVER_ADDR:-nacos:8848}
      MYSQL_USER: ${MYSQL_USER:-claimpaws}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-local-dev-only}
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME:-claimpaws}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD:-local-dev-only}
      WEBHOOK_ENCRYPTION_KEY: ${WEBHOOK_ENCRYPTION_KEY:-MDEyMzQ1Njc4OWFiY2RlZg==}
    ports:
      - "127.0.0.1:8084:8084"
    networks:
      - claimpaws-net
    depends_on:
      mysql:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
      nacos:
        condition: service_healthy

  gateway:
    build:
      context: ./gateway
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-docker}
      NACOS_SERVER_ADDR: ${NACOS_SERVER_ADDR:-nacos:8848}
    ports:
      - "127.0.0.1:8080:8080"
    networks:
      - claimpaws-net
    depends_on:
      nacos:
        condition: service_healthy
      identity-service:
        condition: service_started
      resource-service:
        condition: service_started
      reservation-service:
        condition: service_started
      notification-service:
        condition: service_started

volumes:
  mysql-data:
```

- [ ] **Step 3: 验证 docker-compose 语法**

```bash
docker compose config --quiet
```

- [ ] **Step 4: 提交**

```bash
git add docker-compose.yml
git commit -m "feat: add business services and network to docker-compose"
```

---

### Task 8: 端到端验证

**Files:**
- 无新建文件，验证现有构建产物

- [ ] **Step 1: 编译全部模块 JAR**

```bash
./mvnw package -DskipTests
```

- [ ] **Step 2: 构建所有镜像并启动**

```bash
docker compose up -d --build
```

- [ ] **Step 3: 等待所有服务健康检查通过（约 2-3 分钟）**

```bash
docker compose ps
```

期望输出：全部 9 个服务状态为 `Up` 且 healthy。

- [ ] **Step 4: 检查日志无严重错误**

```bash
docker compose logs gateway | tail -20
docker compose logs identity-service | tail -20
docker compose logs reservation-service | tail -20
```

- [ ] **Step 5: 测试 gateway 健康端点**

```bash
curl -s http://localhost:8080/actuator/health
```

- [ ] **Step 6: 测试 identity-service API**

```bash
curl -s http://localhost:8081/actuator/health
```

- [ ] **Step 7: 停止并清理**

```bash
docker compose down
```

- [ ] **Step 8: 提交（如有任何微调）**

```bash
git add -A
git commit -m "chore: final adjustments for docker-compose one-click startup"
```
