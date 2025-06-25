-- 1. Проверка количества записей в таблицах
SELECT
    'users' as table_name,
    COUNT(*) as record_count
FROM users
UNION ALL
SELECT 'products', COUNT(*) FROM products
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM order_items;

-- 2. Проверка целостности внешних ключей

-- Проверка наличия "битых" ссылок на пользователей в заказах
-- Должен вернуть пустой результат, если все user_id существуют в таблице users
SELECT o.*
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;

-- Проверка наличия "битых" ссылок на заказы в позициях заказов
-- Должен вернуть пустой результат, если все order_id существуют в таблице orders
SELECT oi.*
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.id
WHERE o.id IS NULL;

-- Проверка наличия "битых" ссылок на товары в позициях заказов
-- Должен вернуть пустой результат, если все product_id существуют в таблице products
SELECT oi.*
FROM order_items oi
LEFT JOIN products p ON oi.product_id = p.id
WHERE p.id IS NULL;

-- 3. Проверка некорректных значений

-- Проверка отрицательных цен в товарах и позициях заказов
-- Должен вернуть пустой результат, если цены корректны
SELECT * FROM products WHERE price < 0;
SELECT * FROM order_items WHERE price < 0;

-- Проверка отрицательных количеств в товарах и позициях заказов
-- Должен вернуть пустой результат, если количества корректны
SELECT * FROM products WHERE stock_quantity < 0;
SELECT * FROM order_items WHERE quantity < 0;

-- 4. Проверка заполненности обязательных полей

-- Проверка отсутствия NULL в обязательных полях
-- Должен вернуть пустой результат, если все обязательные поля заполнены
SELECT * FROM users WHERE name IS NULL OR email IS NULL;
SELECT * FROM products WHERE name IS NULL OR price IS NULL;
SELECT * FROM orders WHERE user_id IS NULL OR total IS NULL;
SELECT * FROM order_items WHERE order_id IS NULL OR product_id IS NULL OR quantity IS NULL OR price IS NULL;

-- 5. Проверка согласованности данных между таблицами

-- Сравнение общей суммы заказа (orders.total) с суммой всех его позиций (order_items)
-- Статус 'OK' - суммы совпадают, 'MISMATCH' - есть расхождение
SELECT 
    o.id, 
    o.total as order_total, 
    SUM(oi.price * oi.quantity) as calculated_total,
    CASE WHEN o.total = SUM(oi.price * oi.quantity) THEN 'OK' ELSE 'MISMATCH' END as status
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, o.total;
