# API 文档

基础地址为 `/api`。除公开接口外，请发送 `Authorization: Bearer <token>`。JSON 响应统一为：

```json
{"code":0,"message":"ok","data":{}}
```

业务失败使用适当 HTTP 状态码，同时返回 `code` 和可展示的中文 `message`。分页参数为 `page`（从 1 开始）和 `pageSize`（最大 100），分页结果含 `items`、`total`、`page`、`pageSize`。

## 认证与个人资料

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/auth/register` | 公开 | 注册普通会员；不能提交员工角色 |
| POST | `/auth/login` | 公开 | 账号密码登录，返回 Token、过期时间和用户 |
| POST | `/auth/logout` | 登录 | 撤销当前服务端会话 |
| GET/PUT | `/auth/profile` | 登录 | 查看/更新显示名、电话、邮箱和头像 URL |
| PUT | `/auth/password` | 登录 | 校验原密码、修改密码并撤销全部会话 |

注册示例：

```json
{"username":"alice","password":"Passw0rd!","displayName":"Alice","phone":"13800000000"}
```

## 目录与图片

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/catalog/public/categories` | 公开 | 启用分类 |
| GET | `/catalog/public/products` | 公开 | 在售商品分页，可传 `keyword`、`categoryId` |
| GET | `/catalog/public/products/{id}` | 公开 | 在售商品详情 |
| GET/POST | `/catalog/categories` | 管理员、库存管理员 | 分类分页/新增 |
| PUT/DELETE | `/catalog/categories/{id}` | 更新：管理员、库存管理员；删除：管理员 | 有商品的分类拒绝删除 |
| GET/POST | `/catalog/products` | 查询含收银员；新增限管理员、库存管理员 | 商品分页/新增 |
| GET/PUT | `/catalog/products/{id}` | 查询含收银员；更新限管理员、库存管理员 | 商品详情/更新上下架状态 |
| GET | `/catalog/products/barcode/{barcode}` | 员工 | 精确条码定位在售商品 |
| POST | `/catalog/products/images` | 管理员、库存管理员 | `multipart/form-data` 字段 `file`，最大 5 MB，仅图片 |

商品写入字段：`code`、`barcode`、`name`、`categoryId`、`specification`、`unit`、`purchasePrice`、`salePrice`、`imageUrl`、`status`、`lowStockThreshold`。编码和条码由数据库唯一索引兜底。

## 供应商与采购

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET/POST | `/suppliers` | 管理员、库存管理员 | 分页/新增 |
| GET | `/suppliers/enabled` | 管理员、库存管理员 | 可用供应商下拉数据 |
| PUT | `/suppliers/{id}` | 管理员、库存管理员 | 更新及启停用 |
| DELETE | `/suppliers/{id}` | 管理员 | 有采购历史时拒绝删除 |
| GET/POST | `/purchases` | 管理员、库存管理员 | 分页/创建草稿 |
| GET | `/purchases/{id}` | 管理员、库存管理员 | 详情和快照明细 |
| POST | `/purchases/{id}/confirm` | 管理员、库存管理员 | 幂等确认入库 |
| POST | `/purchases/{id}/cancel` | 管理员、库存管理员 | 取消草稿 |

采购创建示例：

```json
{"supplierId":1,"remark":"补货","items":[{"productId":1,"quantity":10,"unitPrice":1.20}]}
```

## 库存

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/inventory` | 员工 | 库存分页；`lowOnly=true` 仅预警 |
| GET | `/inventory/movements` | 员工 | 流水分页，可按商品/业务类型过滤 |
| POST | `/inventory/{productId}/movement` | 管理员、库存管理员 | 手工出入库：`direction`、`quantity`、`reason` |
| POST | `/inventory/{productId}/adjust` | 管理员、库存管理员 | 盘点到目标数量：`targetQty`、`reason` |

盘点后的当前库存不得小于预占库存；出库不足返回冲突错误且不写流水。

## 销售订单

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/orders` | 登录 | 员工看全部，会员自动限定本人 |
| GET | `/orders/{id}` | 登录 | 会员只能看本人订单 |
| POST | `/orders` | 登录 | 下单；会员为小程序渠道，员工可做 Web POS |
| POST | `/orders/{id}/cancel` | 登录 | 取消待付款；会员只能取消本人 |
| POST | `/orders/{id}/pay` | 管理员、收银员 | 模拟收款 |
| POST | `/orders/{id}/returns` | 管理员、收银员 | 幂等退货并回补库存 |

小程序下单示例：

```json
{"idempotencyKey":"client-uuid","channel":"MINIAPP","items":[{"productId":1,"quantity":2}],"autoPay":false}
```

Web POS 使用 `channel: "WEB_POS"`，可传 `autoPay: true` 与 `paymentMethod: "CASH"`；小程序会员必须使用 `channel: "MINIAPP"` 且不能自行标记收款。服务端按角色与渠道交叉校验。同一个幂等键重复提交返回原订单。退货请求包含唯一 `idempotencyKey`、`reason` 和 `items: [{"saleItemId":1,"quantity":1}]`。

## 统计与系统管理

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/stats/dashboard` | 员工 | 今日净销售额、有效订单、商品、预警 |
| GET | `/stats/sales-trend` | 员工 | `from`/`to`，最长一年 |
| GET | `/stats/hot-products` | 员工 | 日期范围和 `limit` |
| GET | `/stats/orders.csv` | 管理员、收银员 | 带 UTF-8 BOM 的订单 CSV，防公式注入 |
| GET | `/admin/users` | 管理员 | 用户分页 |
| PUT | `/admin/users/{id}/role-status` | 管理员 | 修改角色/状态并撤销现有会话 |
| GET | `/admin/roles` | 管理员 | 固定角色权限描述 |
| GET | `/admin/audits` | 管理员 | 操作审计分页 |
| GET/PUT | `/admin/config[/{key}]` | 管理员 | 基础配置查询/更新 |
