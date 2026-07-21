# ClaimPaws Backend

## 本地基础设施

启动 MySQL、Redis、RabbitMQ 与 Nacos：

```bash
docker compose up -d
docker compose ps
```

本地 Compose 仅用于开发。MySQL 使用四个独立数据库：`claimpaws_identity`、`claimpaws_resource`、`claimpaws_reservation` 和 `claimpaws_notification`；各服务首次启动时通过自己的 Flyway 迁移建表。RabbitMQ 管理界面为 `http://localhost:15672`，账号 `claimpaws`，密码是 Compose 文件中仅限本地开发的值。

服务默认连接 `127.0.0.1` 上的本地依赖。启动本地服务时必须显式启用 `local` profile；该 profile 才会注入 Compose 使用的本地 MySQL/RabbitMQ 账号 `claimpaws` 和密码 `local-dev-only`：

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw -pl identity-service spring-boot:run
SPRING_PROFILES_ACTIVE=local ./mvnw -pl reservation-service spring-boot:run
```

`local-dev-only` 仅供本地 Compose 使用，不是生产凭证。未启用 `local` profile 的服务没有凭证默认值，缺少必填变量会在启动时失败；identity-service 还要求 `JWT_SECRET`，且空值会被拒绝。

部署到非本地环境时，必须通过环境变量或 Nacos 密文配置覆盖凭证，不能依赖本地默认值：

```bash
export MYSQL_USER="..."
export MYSQL_PASSWORD="..."
export RABBITMQ_USERNAME="..."
export RABBITMQ_PASSWORD="..."
export JWT_SECRET="..."
```

`MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DB`、`RABBITMQ_HOST` 和 `RABBITMQ_PORT` 也可按部署环境覆盖。运行通知服务时需要显式提供 `WEBHOOK_ENCRYPTION_KEY`，该值必须是 Base64 编码的 AES 密钥，不能使用示例或生产密钥。

停止并移除本地数据：

```bash
docker compose down -v
```
