# Windows PowerShell 从零运行

## 1. 安装环境

安装并确认以下命令可用：

- JDK 17：`java -version`
- Maven 3.9+：`mvn -version`
- Node.js 22+：`node -v`
- pnpm 10.17.1：`corepack enable` 后执行 `corepack prepare pnpm@10.17.1 --activate`
- Docker Desktop：`docker version` 与 `docker compose version`

若不使用 Docker，需要本机 MySQL 8.0，并自行创建数据库和账号。

## 2. 配置

```powershell
Copy-Item .env.example .env
notepad .env
```

至少修改 `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD` 和 `JWT_SECRET`。JWT 密钥建议使用密码管理器生成至少 32 个随机字符。`.env` 已被 Git 忽略。

## 3. 启动 MySQL 与后端

```powershell
Set-Location 你的仓库目录
docker compose up -d --build
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
```

首次启动会由 Flyway 建表并在 `dev` profile 初始化演示数据。得到 `status = UP` 后再启动 Web。

## 4. 启动 Web

新开一个 PowerShell：

```powershell
Set-Location 你的仓库目录\web
pnpm install --frozen-lockfile
pnpm dev
```

浏览器访问 `http://localhost:5173`，使用 `admin / Passw0rd!`。Web 开发服务器通过 Vite 代理访问 `http://localhost:8080/api`。

## 5. 构建微信小程序

```powershell
Set-Location 你的仓库目录\miniapp
pnpm install --frozen-lockfile
pnpm build:mp-weixin
```

微信开发者工具选择“导入项目”，目录为 `miniapp\dist\build\mp-weixin`。本机调试时接口为 `http://localhost:8080/api`；手机不能把 `localhost` 当作电脑，真机需改成局域网 HTTPS 调试地址或已备案的合法域名。

## 6. 不使用 Docker 的后端启动

在 MySQL 中创建 UTF-8 数据库，例如：

```sql
CREATE DATABASE supermarket CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'supermarket'@'localhost' IDENTIFIED BY '只用于本机的强密码';
GRANT ALL PRIVILEGES ON supermarket.* TO 'supermarket'@'localhost';
```

PowerShell：

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'
$env:DB_URL='jdbc:mysql://localhost:3306/supermarket?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
$env:DB_USERNAME='supermarket'
$env:DB_PASSWORD='只用于本机的强密码'
$env:JWT_SECRET='至少32位的本地随机字符串'
Set-Location backend
mvn spring-boot:run
```

## 7. 停止与清理

Web/Maven 前台进程按 `Ctrl+C`。Docker 服务：

```powershell
docker compose down
```

保留数据卷便于下次使用。`docker compose down -v` 会永久删除容器数据库数据，只有明确要重置演示库时才执行。

## 常见错误

| 现象 | 处理 |
| --- | --- |
| 8080/3306/5173 端口占用 | `Get-NetTCPConnection -LocalPort 8080` 找到占用进程，或修改端口配置 |
| 后端报数据库连接失败 | 先看 `docker compose ps`，再核对 `.env` 和健康检查 |
| Flyway checksum mismatch | 不要修改已发布版本迁移；开发库可在备份后重建，正式库应新增下一版迁移 |
| pnpm lockfile 错误 | 使用 10.17.1，运行 `pnpm install --frozen-lockfile`，不要用 npm 混装 |
| 小程序请求失败 | 开发工具勾选仅限本地调试的“不校验合法域名”；真机必须配置 HTTPS 合法域名 |
| 图片看不到 | 确认后端 `/uploads/**` 可访问，生产部署要把上传目录挂载到持久卷并使用 HTTPS 域名 |

