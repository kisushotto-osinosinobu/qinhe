# 第三方组件、来源与许可证

本项目使用公开包管理器依赖，没有整包复制后台模板，也没有把第三方代码宣称为原创。依赖的具体解析版本由 Maven 与两个 `pnpm-lock.yaml` 锁定。

| 组件 | 版本/范围 | 来源 | 许可证 | 复用范围 |
| --- | --- | --- | --- | --- |
| Spring Boot | 3.5.16 | https://spring.io/projects/spring-boot | Apache-2.0 | 后端框架、安全、校验、Web、测试 |
| MyBatis Spring Boot Starter | 3.0.5 | https://mybatis.org/spring-boot-starter/ | Apache-2.0 | SQL 映射 |
| Flyway | Spring Boot 管理版本 | https://documentation.red-gate.com/flyway | Apache-2.0（社区组件） | 数据库迁移 |
| JJWT | 0.12.7 | https://github.com/jwtk/jjwt | Apache-2.0 | JWT 签发与解析 |
| MySQL Connector/J | Spring Boot 管理版本 | https://dev.mysql.com/downloads/connector/j/ | GPL-2.0 with FOSS exception | JDBC 驱动 |
| Vue | 3.5.42（Web）；3.4.21（uni-app 兼容） | https://github.com/vuejs/core | MIT | 双前端视图层 |
| Vite | 8.3.0（Web）；8.2.2（uni-app peer） | https://vite.dev/ | MIT | 构建与开发服务器 |
| Element Plus | 2.14.5 | https://element-plus.org/ | MIT | Web 表单、表格与反馈组件 |
| Pinia / Vue Router / Axios | 锁文件版本 | 各项目官方仓库 | MIT | 状态、路由、HTTP |
| uni-app / mp-weixin | `3.0.0-alpha-1000920260909822` | https://github.com/dcloudio/uni-app | Apache-2.0 | 编译微信小程序 |
| Vitest | 5.0.0 | https://vitest.dev/ | MIT | Web 单元测试 |

Spring Boot 3.5 与 MyBatis Starter 3.0 的兼容组合遵循 MyBatis 官方兼容表。uni-app 依赖使用其官方 `vue3` 发布标签；该标签要求 Vite 8.2.2，因此小程序与 Web 分别锁定 Vite，而不是强行共享版本。

各依赖版权和完整许可证以其发行包内 `LICENSE`/`NOTICE` 为准。本项目自身源代码可由仓库所有者在提交时选择课程要求的许可证；在未得到所有者选择前不擅自声明额外授权。
