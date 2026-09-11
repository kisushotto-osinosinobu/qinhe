# 数据库关系与字段说明

MySQL 8.0，默认 `utf8mb4`，金额均使用 `DECIMAL`，Java 使用 `BigDecimal`。结构迁移位于 `backend/src/main/resources/db/migration`；开发演示数据位于 `db/demo`，仅 `dev` profile 加载。

## 关系

```text
sys_user ──< auth_session
sys_user ──< purchase_order >── supplier
category ──< product ──1 inventory
product ──< purchase_item >── purchase_order
sys_user(member/cashier) ──< sale_order ──< sale_item >── product
sale_order ──< sale_return ──< sale_return_item >── sale_item
product ──< inventory_movement >── sys_user(operator)
sys_user ──< system_config
sys_user ──< audit_log（逻辑关联，允许账号历史为空）
```

## 主要表

| 表 | 作用 | 关键约束/索引 |
| --- | --- | --- |
| `sys_user` | 用户、角色、状态、Token 版本 | 用户名唯一；角色/状态 CHECK |
| `auth_session` | JWT `jti` 服务端会话与撤销时间 | `jti` 唯一；用户、过期时间索引 |
| `category` | 商品分类 | 名称、编码唯一 |
| `product` | 商品主数据和当前价格 | 编码、条码唯一；分类外键；名称/分类状态索引 |
| `supplier` | 供应商与联系人 | 编码、名称唯一 |
| `inventory` | 当前与预占数量 | 商品一对一；非负且预占不大于当前 |
| `purchase_order/item` | 采购头与商品快照 | 单号唯一；一单一商品唯一 |
| `sale_order/item` | 销售头与成交快照 | 单号、幂等键唯一；会员/状态时间索引 |
| `sale_return/item` | 退货头与退货明细 | 退货单号、幂等键唯一 |
| `inventory_movement` | 不可变库存流水 | 商品+业务类型+业务引用唯一 |
| `system_config` | 基础配置 | 配置键主键 |
| `audit_log` | 用户关键操作审计 | 用户时间、动作时间索引 |

## 快照与一致性字段

`purchase_item` 和 `sale_item` 都保存商品编码、名称、规格、单位及当时单价，销售明细另外保存条码、已退数量。商品主表后续改名或改价不会改写历史单据。

库存版本字段用于诊断并发更新；业务更新由行锁与带条件的原子 SQL 共同保证。`inventory_movement` 保存变化前后当前/预占数量、业务类型、业务单号、操作人、时间和备注，可由任一业务单追溯。

## 迁移策略

- `V1__init_schema.sql` 是结构基线，上线后不得就地修改，应新增 `V2__...sql`。
- `R__demo_data.sql` 是可重复开发数据，只在 `application-dev.yml` 的额外位置加载。
- 正式环境不启用 `dev` profile，生产配置也不应指向 `db/demo`。
- 从空库启动后，`flyway_schema_history` 应显示 V1 和 demo repeatable 迁移；CI 会在 MySQL 8.0 服务中验证这一流程。

