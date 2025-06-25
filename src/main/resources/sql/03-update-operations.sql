-- 1. Обновление статуса конкретного заказа на 'shipped'
-- Изменяет только одну запись с указанным ID
UPDATE
    orders
SET
    status = 'shipped'
WHERE
    id = 1;

-- 2. Обновление имени пользователя и даты изменения
-- Изменяет запись по email, возвращает обновленные данные
-- RETURNING полезен для подтверждения изменений
UPDATE
    users
SET
    name = 'Алексей Петрович Петров',
    updated_at = NOW()
WHERE
    email = 'alex.petrov@example.com' RETURNING id,
    name,
    updated_at;

-- 3. Повышение цен на все товары на 10%
-- Умножает текущую цену на 1.1 для всех записей
-- Нет условия WHERE - применяется ко всей таблице
UPDATE
    products
SET
    price = price * 1.1;

-- 4. Назначение статуса 'premium' пользователям с суммой заказов > 50 000
-- Подзапрос находит пользователей, чья общая сумма заказов превышает порог
UPDATE
    users
SET
    status = 'premium'
WHERE
    id IN(
        SELECT
            user_id
        FROM
            orders
        GROUP BY
            user_id
        HAVING
            SUM( total )> 50000
    );

-- 5. Обновление количества товаров на складе после оформления заказа
-- Уменьшает stock_quantity на количество заказанных единиц
-- Для конкретного заказа (ID 123) и связанных товаров
UPDATE
    products p
SET
    stock_quantity = stock_quantity -(
        SELECT
            quantity
        FROM
            order_items oi
        WHERE
            oi.product_id = p.id
            AND oi.order_id = 123
    )
WHERE
    id IN(
        SELECT
            product_id
        FROM
            order_items
        WHERE
            order_id = 123
    );

-- 6. Автоматическое обновление статуса товара в зависимости от остатка
-- Использует CASE для установки разных статусов:
-- 'out_of_stock' - нет в наличии (0)
-- 'low_stock' - мало на складе (<5)
-- 'in_stock' - достаточно (>=5)
UPDATE
    products
SET
    status = CASE
        WHEN stock_quantity = 0 THEN 'out_of_stock'
        WHEN stock_quantity < 5 THEN 'low_stock'
        ELSE 'in_stock'
    END;