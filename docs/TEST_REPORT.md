# 测试报告

测试日期：2026-09-11（Asia/Shanghai）  
执行环境：Windows 11 x64；Java 17.0.20.1；Maven 3.9.11；Node.js 24.19；MySQL Community Server 8.0.46（官方 Windows ZIP，SHA-256 `28E9EDA019D88EFF4478D811EA2110B83F02A3966BE157FE91CC55DEF3AB0D4D`）；Web Vite 8.3.0；uni-app compiler 4.81。

状态严格使用：通过、失败、未执行、被外部条件阻塞。本报告保留首次失败及修复复验，不把失败历史抹去。

## 自动化结果

| 范围 | 命令/用例 | 预期 | 实际 | 状态 |
| --- | --- | --- | --- | --- |
| 后端编译与单测 | `mvn -B clean verify`；`JwtServiceTest` | Java 17 编译，JWT 签发/解析正确 | 1 个单测通过 | 通过 |
| 空库迁移 | 空 `supermarket_test` 执行同一命令 | Flyway 建全部表并加载 dev 数据 | V1 + repeatable demo 共 2 个迁移成功 | 通过 |
| 采购幂等 | `BusinessLifecycleIT` | 确认两次只入库一次并留流水 | 当前库存为 10 | 通过 |
| 下单/取消幂等 | `BusinessLifecycleIT` | 同键提交只预占一次；取消两次后预占为 0 | 同一订单 ID，预占 3→0 | 通过 |
| 收款与退货幂等 | `BusinessLifecycleIT` | 重复收款/同键退货不重复记账；超退拒绝 | 当前库存 10→6→8，重复值不变，超退抛业务异常 | 通过 |
| 库存不足与并发最后库存 | 手工超量出库；两线程同时购买最后 1 件 | 超量拒绝；仅 1 单成功且不超卖 | 业务异常；成功数 1，预占 1，可售 0 | 通过 |
| 分类与商品 | `CatalogSupplierIntegrationIT` | 增改查成功；负价格 400；重复编码 409；有关联商品的分类不可删 | 与预期一致 | 通过 |
| 商品图片 | `CatalogSupplierIntegrationIT` | PNG 上传后可访问；文本伪装上传拒绝 | 返回图片 URL 且 GET 200；`text/plain` 返回 400 | 通过 |
| 供应商 | `CatalogSupplierIntegrationIT` | 增改删成功；错误邮箱 400；有采购引用时 409 | 与预期一致 | 通过 |
| 登录与会话 | 错误密码、正确登录、注销后旧 Token | 401、200、注销后 401 | 与预期一致 | 通过 |
| 未登录与越权 | 未登录库存；会员库存；管理员角色接口 | 401、403、200 | 与预期一致 | 通过 |
| 订单归属 | 会员 B 访问会员 A 订单 | 403 | 返回 403 | 通过 |
| Web 类型检查 | `vue-tsc --noEmit` | 无类型错误 | 退出码 0 | 通过 |
| Web 单测 | `vitest run` | 状态展示映射正确 | 1 文件、2 用例通过 | 通过 |
| Web 生产构建 | `vite build` | 生成 `web/dist` | 1676 模块构建成功；存在大 chunk 警告但无失败 | 通过 |
| 小程序类型检查 | `vue-tsc --noEmit` | 无类型错误 | 退出码 0 | 通过 |
| 微信目标构建 | `uni build -p mp-weixin` | 生成真实微信目标目录 | `miniapp/dist/build/mp-weixin` 构建成功；仅 Sass legacy API 弃用警告 | 通过 |

最终后端 Maven 摘要：单元测试 1/1 通过；集成测试 6/6 通过；`BUILD SUCCESS`，总耗时约 9.911 秒。该轮从空库迁移，并包含增强后的业务幂等、库存不足和图片上传断言。

提交前将 Web 与小程序登录页的预填口令统一为文档所列开发口令后，再次执行 Web 类型检查、2 个单测和生产构建，以及小程序类型检查和微信目标构建，均通过；Web 仍仅有大 chunk 提示，小程序仍仅有 Sass legacy API 弃用提示。

## 首次失败与修复证据

首轮 MySQL 集成测试为 `BUILD FAILURE`：4 个集成用例中 2 个失败、2 个异常。原因是 MyBatis 对 Java Bean 应用下划线转驼峰，但默认 `MapWrapper` 保留数据库列标签，导致 `sale_price`/`product_id` 等被业务层按 `salePrice`/`productId` 读取时为空；演示账号散列也与标注口令不匹配。

修复内容：在 MyBatis 映射边界增加 Map 键驼峰包装器，并用运行时 BCrypt 生成的强演示口令散列替换种子数据；删除并重建空测试库后完整复验全部通过。

## 手动与端到端状态

| 项目 | 状态 | 说明 |
| --- | --- | --- |
| 本机后端实际启动与 HTTP 健康检查 | 通过 | Spring Boot 连接全新 `supermarket_dev`，Flyway 完成两项迁移；`/actuator/health` 返回 `UP` |
| Web 浏览器登录与关键页面 | 通过 | 实际登录管理员；目录显示 5 商品；库存页显示 5 条台账；Web POS 模拟收款 1 瓶山泉水，回执单号 `SO202609112030599305` |
| 收银到统计同步 | 通过 | 收款后商品可售 120→119；仪表盘净销售额 2.00、订单 1、趋势和热销山泉水均来自后端最新数据 |
| Web 注销和会员菜单 | 通过 | 点击退出回到登录页；会员登录后导航只显示经营概览和我的订单 |
| 分类/商品/供应商完整 GUI 增删改查 | 未执行 | 后端接口和页面已实现，按手动清单操作 |
| CSV 下载内容与统计人工算账 | 未执行 | 接口已实现，需按手动清单保留文件证据 |
| 微信开发者工具模拟器 | 未执行 | 当前环境未安装/未连接开发者工具 |
| 微信真机验证 | 被外部条件阻塞 | 缺少真实 AppID、合法 HTTPS 域名、服务器及开发者权限 |
| GitHub Actions 远端运行 | 被外部条件阻塞 | 用户尚未提供实际仓库 URL，无法推送或取得运行链接 |

## 复现命令

```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/supermarket_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='你的测试密码'
Set-Location backend
mvn -B clean verify

Set-Location ..\web
pnpm typecheck
pnpm test
pnpm build

Set-Location ..\miniapp
pnpm typecheck
pnpm build:mp-weixin
```

Maven 的机器可核查 XML/TXT 输出位于 `backend/target/surefire-reports` 与 `backend/target/failsafe-reports`，构建垃圾不提交 Git；GitHub Actions 每次运行会上传这些报告为 artifact。
