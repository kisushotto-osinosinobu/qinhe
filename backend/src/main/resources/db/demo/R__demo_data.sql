-- Development-only demo data. This location is loaded only by the dev profile.
-- All demo accounts use password: Passw0rd!
INSERT IGNORE INTO sys_user (id, username, password_hash, display_name, phone, email, role, status)
VALUES
 (1, 'admin', '$2a$10$HcyjrcGYosG7Iv7CWL5gQeQGz7JJsMuieiLMuX9ZngcDr.sy7CbBm', '系统管理员', '13800000001', 'admin@example.test', 'ADMIN', 'ACTIVE'),
 (2, 'stock', '$2a$10$HcyjrcGYosG7Iv7CWL5gQeQGz7JJsMuieiLMuX9ZngcDr.sy7CbBm', '库存管理员', '13800000002', 'stock@example.test', 'INVENTORY_MANAGER', 'ACTIVE'),
 (3, 'cashier', '$2a$10$HcyjrcGYosG7Iv7CWL5gQeQGz7JJsMuieiLMuX9ZngcDr.sy7CbBm', '收银员', '13800000003', 'cashier@example.test', 'CASHIER', 'ACTIVE'),
 (4, 'member', '$2a$10$HcyjrcGYosG7Iv7CWL5gQeQGz7JJsMuieiLMuX9ZngcDr.sy7CbBm', '演示会员', '13800000004', 'member@example.test', 'MEMBER', 'ACTIVE');

INSERT IGNORE INTO category (id, name, code, sort_order, status) VALUES
 (1, '饮料冲调', 'BEVERAGE', 10, 'ENABLED'),
 (2, '粮油副食', 'GROCERY', 20, 'ENABLED'),
 (3, '休闲零食', 'SNACK', 30, 'ENABLED'),
 (4, '日用百货', 'DAILY', 40, 'ENABLED');

INSERT IGNORE INTO product (id, code, barcode, name, category_id, specification, unit, purchase_price, sale_price, status, low_stock_threshold) VALUES
 (1, 'SP-1001', '6900000000011', '山泉水', 1, '550ml', '瓶', 1.20, 2.00, 'ON_SALE', 20),
 (2, 'SP-1002', '6900000000028', '纯牛奶', 1, '250ml×12', '箱', 38.00, 49.90, 'ON_SALE', 8),
 (3, 'SP-2001', '6900000000035', '东北大米', 2, '5kg', '袋', 32.00, 45.80, 'ON_SALE', 10),
 (4, 'SP-3001', '6900000000042', '海盐苏打饼干', 3, '400g', '袋', 8.50, 12.90, 'ON_SALE', 15),
 (5, 'SP-4001', '6900000000059', '抽取式纸巾', 4, '3层×6包', '提', 14.00, 19.90, 'ON_SALE', 12);

INSERT IGNORE INTO inventory (product_id, current_qty, reserved_qty) VALUES
 (1, 120, 0), (2, 30, 0), (3, 18, 0), (4, 45, 0), (5, 9, 0);

INSERT IGNORE INTO supplier (id, code, name, contact_name, phone, email, address, status) VALUES
 (1, 'SUP-001', '城市食品供应链有限公司', '李经理', '13900001001', 'sales@city-food.example', '示范市高新区供应链路18号', 'ENABLED'),
 (2, 'SUP-002', '绿源日用品商贸有限公司', '王女士', '13900001002', 'service@green-daily.example', '示范市商贸大道66号', 'ENABLED');

INSERT IGNORE INTO system_config (config_key, config_value, description, updated_by) VALUES
 ('store.name', '青禾生活超市', '门店名称', 1),
 ('currency', 'CNY', '结算币种', 1),
 ('payment.notice', '到店付款 / 模拟收款，不接入真实资金交易', '收款提示', 1);
