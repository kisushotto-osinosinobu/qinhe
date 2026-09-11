# 青禾超市信息管理系统

面向软件工程课程、项目展示与实际操作学习的完整全栈项目。Web 管理端和真实微信小程序工程共用一套 Spring Boot 业务服务与 MySQL 数据库，不维护两份演示数据。

## 已实现能力

- 账号密码登录、普通会员注册、注销、资料与密码修改；服务端 JWT + 可撤销会话，BCrypt 密码散列。
- 管理员、库存管理员、收银员、普通会员四角色；Controller 方法级鉴权与会员数据归属校验。
- 分类、商品、供应商、图片上传、采购入库、库存台账/流水/盘点/预警。
- Web 收银、小程序下单、订单查询、取消、模拟收款和员工退货。
- 商品与成交价快照；当前、预占、可售库存；事务、行锁、条件更新和业务幂等键。
- 销售仪表盘、近 7 日趋势、热销商品、订单 CSV 导出、用户/角色/配置/审计管理。
- Vue 3 Web 管理端，以及 uni-app Vue 3 编译的独立微信小程序工程。

## 目录

```text
backend/                  Spring Boot + MyBatis API、Flyway 迁移与测试
web/                      Vue 3 + TypeScript + Vite 管理端
miniapp/                  Vue 3 + uni-app 微信小程序
docs/                     API、数据库、权限状态、同步、测试与验收文档
scripts/                  Windows PowerShell 辅助脚本
.github/workflows/ci.yml  自动构建与 MySQL 集成测试
docker-compose.yml        MySQL 8.0 与后端开发环境
```

## 锁定版本

| 组件 | 版本 |
| --- | --- |
| Java / Spring Boot | 17 / 3.5.16 |
| MyBatis Starter | 3.0.5 |
| MySQL | 8.0（本机验收 8.0.46） |
| Vue / Vite / TypeScript | 3.5.42 / 8.3.0 / 5.9.3 |
| Element Plus | 2.14.5 |
| uni-app Vue 3 编译器 | 4.81，对应依赖标签 `3.0.0-alpha-1000920260909822` |
| Node.js / pnpm | 22+ / 10.17.1 |

版本选择和来源见 [第三方与许可证](docs/THIRD_PARTY.md)。

## 最快启动（Docker Desktop）

1. 安装 Docker Desktop、Node.js 22+、pnpm 10。
2. 将 `.env.example` 复制为 `.env`，修改三个密码/密钥占位值。
3. 在仓库根目录运行：

```powershell
docker compose up -d --build
Set-Location web
pnpm install --frozen-lockfile
pnpm dev
```

4. 打开 `http://localhost:5173`。后端健康检查为 `http://localhost:8080/actuator/health`。

开发演示账号只在 `dev` profile 加载，统一密码为 `Passw0rd!`：

| 账号 | 角色 |
| --- | --- |
| `admin` | 管理员 |
| `stock` | 库存管理员 |
| `cashier` | 收银员 |
| `member` | 普通会员 |

> 所有支付均为“到店付款 / 模拟收款”，不会发生真实资金交易。生产环境不要启用 `dev` profile，也不要加载演示口令。

停止服务：

```powershell
docker compose down
```

保留数据库卷；仅在明确需要清空开发数据时运行 `docker compose down -v`。

## 独立启动前端与小程序

Web：

```powershell
Set-Location web
pnpm install --frozen-lockfile
pnpm dev
```

微信小程序构建：

```powershell
Set-Location miniapp
pnpm install --frozen-lockfile
pnpm build:mp-weixin
```

将 `miniapp/dist/build/mp-weixin` 导入微信开发者工具。开发时可使用游客 AppID 做本地界面与接口调试；真机、合法域名和发布条件见 [微信小程序与同步](docs/SYNC_AND_WECHAT.md)。本仓库没有伪造微信登录，当前使用可联调的账号密码登录。

## 测试

后端完整测试需要 MySQL 8.0 空库：

```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/supermarket_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='你的本地密码'
Set-Location backend
mvn -B clean verify
```

```powershell
Set-Location web
pnpm typecheck
pnpm test
pnpm build

Set-Location ..\miniapp
pnpm typecheck
pnpm build:mp-weixin
```

本次实际执行结果与环境证据见 [测试报告](docs/TEST_REPORT.md)，人工浏览器和微信开发者工具步骤见 [手动验收](docs/MANUAL_ACCEPTANCE.md)。

## 文档索引

- [Windows 从零运行](docs/WINDOWS_SETUP.md)
- [API 文档](docs/API.md)
- [数据库说明](docs/DATABASE.md)
- [权限矩阵与业务状态](docs/PERMISSIONS_AND_STATES.md)
- [双端同步与微信发布条件](docs/SYNC_AND_WECHAT.md)
- [测试报告](docs/TEST_REPORT.md)
- [本人手动验收与截图步骤](docs/MANUAL_ACCEPTANCE.md)
- [第三方组件与许可证](docs/THIRD_PARTY.md)

## 配置安全

仓库只提交 `.env.example`。不得提交 `.env`、微信 AppSecret、JWT 生产密钥、私钥、Token、数据库数据目录、`node_modules` 或构建产物。演示数据的唯一开关是 Spring `dev` profile；生产部署不得启用该 profile。
