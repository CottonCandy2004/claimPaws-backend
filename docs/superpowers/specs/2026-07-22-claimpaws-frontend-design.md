# 智能会议室工位预约平台——前端设计规范

## 目标

为 claimPaws 智能会议室与工位预约平台建设完整的前端 SPA，覆盖认证、资源管理、预约审批履约和通知等全部业务模块。

前端项目位于后端仓库根目录 `front/` 下，与后端共享 Git 仓库。构建产物最终复制到 `gateway/src/main/resources/static/` 由 Spring Boot 网关作为静态资源服务。

## 技术栈

| 组件 | 选型 |
|------|------|
| 框架 | Vue 3 (Composition API) |
| 构建 | Vite |
| 语言 | TypeScript |
| 路由 | Vue Router 4 |
| 状态管理 | Pinia |
| UI 组件库 | Element Plus |
| HTTP 客户端 | Axios |
| 测试 | Vitest + @vue/test-utils |
| E2E | Playwright（可选，后期） |

## 项目结构

```
front/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── env.d.ts
├── public/
│   └── favicon.ico
└── src/
    ├── main.ts
    ├── App.vue
    ├── api/
    │   ├── request.ts              # Axios 实例 + 拦截器
    │   └── modules/
    │       ├── auth.ts             # 登录/注册/登出
    │       ├── user.ts             # 用户 CRUD
    │       ├── role.ts             # 角色管理
    │       ├── department.ts       # 部门管理
    │       ├── resource.ts         # 资源（园区/楼宇/楼层/会议室/工位/设施）
    │       ├── policy.ts           # 预约策略
    │       ├── reservation.ts      # 预约创建/查询/审批/签到/取消
    │       └── notification.ts     # Webhook 配置和投递审计
    ├── router/
    │   └── index.ts                # 路由配置 + 导航守卫
    ├── store/
    │   ├── auth.ts                 # 认证状态
    │   └── app.ts                  # 全局 UI 状态
    ├── views/
    │   ├── login/
    │   ├── dashboard/
    │   ├── resource/
    │   ├── reservation/
    │   ├── approval/
    │   ├── notification/
    │   └── system/
    ├── components/
    │   ├── AppLayout.vue           # 主布局
    │   └── StatusTag.vue           # 预约状态标签
    ├── composables/
    ├── utils/
    │   └── constants.ts            # 业务常量
    ├── types/                      # TS 类型定义
    ├── styles/
    │   └── global.scss
    └── assets/
```

分层策略：`views/` 按业务模块分目录，共享层（`api/`、`store/`、`components/`）横向切分。

## 路由设计

```text
/login                          # 登录页（无布局）
/                               # AppLayout 包裹
  /dashboard                    # 仪表盘
  /resources                    # 资源管理
    /campus                     # 园区
    /buildings                  # 楼宇
    /floors                     # 楼层
    /rooms                      # 会议室
    /workstations               # 工位
    /facilities                 # 设施
    /policies                   # 预约策略
  /reservations                 # 我的预约
  /approvals                    # 待审批
  /notifications                # 通知/Webhook 配置
  /system                       # 系统管理
    /users                      # 用户管理
    /roles                      # 角色管理
    /departments                # 部门管理
```

- 路由守卫：未认证用户统一重定向 `/login`；登录后拉取用户信息和权限，根据 RBAC 动态过滤菜单和路由。

## 认证流程

1. 登录表单提交 → `POST /api/v1/auth/login` → 后端返回 JWT Access Token + Refresh Token
2. Access Token 存入 Pinia `auth` store 和 `sessionStorage`，Axios 请求拦截器自动注入 `Authorization: Bearer <token>`
3. 响应 401 时，拦截器尝试用 Refresh Token 换新 Access Token，刷新失败跳转 `/login`
4. 每个请求自动注入 `X-Request-Id`（uuid.v4）

## 状态管理（Pinia）

| Store | 职责 |
|-------|------|
| `auth` | 当前用户信息、角色权限列表、Access/Refresh Token、登录/登出/刷新方法 |
| `app` | 侧边栏折叠状态、面包屑路径、全局 loading 状态 |

各视图内部的列表查询、表单编辑等局部状态由组件自身 `ref/reactive` 管理，不放入全局 store。

## API 层

### 统一请求封装（`api/request.ts`）

- Axios 实例 baseURL 为 `/api/v1`，开发时 Vite dev server 代理到 `http://localhost:8080`
- 请求拦截器：
  - 注入 `Authorization: Bearer <token>`
  - 注入 `X-Request-Id`（uuid）
  - 创建预约接口（POST/PUT/PATCH）自动注入 `Idempotency-Key`
- 响应拦截器：
  - 从 `ApiResponse<T>` 解包 `data` 字段返回
  - `code !== 0` 时 `ElMessage.error(message)` + 抛出业务错误
  - `code === 401` 时触发 Refresh Token 换新，失败跳登录
  - 网络异常 `ElMessage.error('网络异常，请稍后重试')`

### 模块划分

| 模块文件 | 对接后端微服务 | 示例接口 |
|----------|---------------|----------|
| `auth.ts` | identity-service | login, register, logout, refresh |
| `user.ts` | identity-service | CRUD users, assign roles |
| `role.ts` | identity-service | CRUD roles, assign permissions |
| `department.ts` | identity-service | department tree, CRUD |
| `resource.ts` | resource-service | campus/buildings/floors/rooms/workstations/facilities CRUD |
| `policy.ts` | resource-service | reservation policies CRUD |
| `reservation.ts` | reservation-service | create, list, approve, check-in, cancel |
| `notification.ts` | notification-service | webhook config, delivery audit |

## 数据流

```text
View → local state/composable → api module → Axios → Gateway(:8080) → 微服务
                 ↕                                           ↕
           Pinia store                                   ApiResponse<T>
```

- 列表页：组件维护 `searchParams`、`data`、`pagination`、`loading`，通过通用 composable 封装分页查询逻辑
- 表单提交：收集表单 → 调用 API → 展示结果或冲突提示 → 成功后跳转/刷新列表
- 审批/签到：调用预约服务对应状态流转接口，更新本地列表状态

## 错误处理

| 层面 | 策略 |
|------|------|
| Axios 响应拦截器 | `code !== 0` → ElMessage toast；可配置特定错误码跳转 |
| 网络异常 | ElMessage.error 通用提示，不暴露异常堆栈 |
| 表单校验 | Element Plus `el-form` rules 前端校验 + 后端错误逐字段回填（`validationErrors`） |
| 全局兜底 | `app.config.errorHandler` 捕获未处理异常，记录控制台但不崩溃 |

## 构建与部署

- `npm run dev` → Vite dev server（端口 5173），`/api/v1` 代理到 `http://localhost:8080`
- `npm run build` → 产出 `dist/` 目录
- 部署：将 `dist/` 内容复制到 `gateway/src/main/resources/static/`，Spring Boot 自动提供静态资源服务
- SPA fallback：Gateway 需配置将非 `/api/**` 路径 fallback 到 `index.html`

## 测试策略

| 类型 | 工具 | 范围 |
|------|------|------|
| 单元 | Vitest | composables、utils、Pinia stores |
| 组件 | Vitest + @vue/test-utils | 列表筛选、状态标签、表单校验逻辑 |
| E2E | Playwright（后期可选） | 登录→创建预约→审批完整流程 |

## 预约状态标签

预约状态枚举（与后端一致）：

```text
PENDING_APPROVAL → 待审批
CONFIRMED       → 已确认
CHECKED_IN      → 已签到
COMPLETED       → 已完成
REJECTED        → 已拒绝
CANCELLED       → 已取消
NO_SHOW         → 爽约
```

`StatusTag.vue` 根据状态展示对应 Element Plus `el-tag` 颜色：
- PENDING_APPROVAL → warning（橙色）
- CONFIRMED → primary（蓝色）
- CHECKED_IN → success（绿色）
- COMPLETED → info（灰色）
- REJECTED → danger（红色）
- CANCELLED → info（灰色）
- NO_SHOW → danger（红色）

## 非目标（首期不做）

- SSR/SEO 优化
- 多语言 i18n
- 微信/钉钉小程序
- WebSocket 实时推送
- PWA 离线支持
- 数据可视化大屏（ECharts 等）
