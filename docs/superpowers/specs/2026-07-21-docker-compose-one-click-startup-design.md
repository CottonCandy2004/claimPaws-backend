# Docker Compose 一键启动 — 设计文档

## 背景

项目目前 `docker-compose.yml` 仅启动基础设施（MySQL、Redis、RabbitMQ、Nacos），5 个业务服务需要通过 Maven 手动 `spring-boot:run`。目标是将其全部纳入 `docker compose up` 一键启动。

## 构建策略

**预构建 JAR 模式**：宿主机先 `./mvnw package -DskipTests` 打所有模块的胖 JAR，Dockerfile 仅复制 JAR 到 `eclipse-temurin:21-jre` 镜像。简单直接，无 Maven 容器化按需下载依赖的开销。

## 网络

所有服务加入自定义 bridge 网络 `claimpaws-net`。服务间通过容器名（`mysql`、`redis`、`rabbitmq`、`nacos`）通信。仅 gateway 暴露 8080 到宿主机，其余服务不暴露端口。

## Profile

每个服务新建 `application-docker.yaml`，通过 `SPRING_PROFILES_ACTIVE=docker` 激活。该 profile 中所有连接地址使用容器名而非 `127.0.0.1`。

## 容器依赖与启动顺序

基础设施服务使用 `healthcheck`，业务服务通过 `depends_on` + `condition: service_healthy` 确保依赖就绪后再启动。Flyway 迁移由各服务启动时自动执行。

## 密钥管理

`.env` 文件提供 `JWT_SECRET` 和 `WEBHOOK_ENCRYPTION_KEY`，默认使用开发占位值。`.env.example` 提交到版本控制作为模板，`.env` 加入 `.gitignore`。

## 新增文件清单

### Dockerfile（5 个）

```
gateway/Dockerfile
identity-service/Dockerfile
resource-service/Dockerfile
reservation-service/Dockerfile
notification-service/Dockerfile
```

内容模板：
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE <port>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### application-docker.yaml（5 个）

```
gateway/src/main/resources/application-docker.yaml
identity-service/src/main/resources/application-docker.yaml
resource-service/src/main/resources/application-docker.yaml
reservation-service/src/main/resources/application-docker.yaml
notification-service/src/main/resources/application-docker.yaml
```

关键差异：连接地址更换为容器名（如 `mysql:3306` 替代 `127.0.0.1:3306`）。

### 环境变量

```
.env.example  (提交到版本控制)
.env          (gitignore)
```

## 修改文件

- `docker-compose.yml` — 新增 5 个业务服务 + 网络定义，基础设施端口绑定移除 `127.0.0.1`（保持容器内通信，宿主机访问按需保留）。

## 使用方式

```bash
./mvnw package -DskipTests
docker compose up -d
docker compose logs -f gateway
docker compose down
```
