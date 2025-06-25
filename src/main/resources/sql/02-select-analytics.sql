-- 1. Получение пользователей, зарегистрированных за последний месяц
-- Сортировка по дате создания (новые сначала)
SELECT
    name,
    email,
    created_at
FROM
    users
WHERE
    created_at >= NOW()- INTERVAL '1 month'
ORDER BY
    created_at DESC;

-- 2. Нахождение самых дорогих товаров в каждой категории
-- Подзапрос находит максимальную цену для каждой категории
SELECT
    p.category,
    p.name,
    p.price
FROM
    products p
WHERE
    p.price =(
        SELECT
            MAX( price )
        FROM
            products
        WHERE
            category = p.category
    );

-- 3. Топ-5 пользователей по сумме заказов
-- Группировка по пользователю и суммирование всех его заказов
SELECT
    users.name,
    SUM( orders.total ) AS total_spent
FROM
    orders
JOIN users ON
    users.id = orders.user_id
GROUP BY
    users.id,
    users.name
ORDER BY
    total_spent DESC LIMIT 5;

-- 4. Список всех заказов с информацией о клиенте
-- Сортировка по дате заказа (новые сначала)
SELECT
    o.id AS order_id,
    u.name AS customer_name,
    o.total,
    o.status,
    o.created_at
FROM
    orders o
JOIN users u ON
    o.user_id = u.id
ORDER BY
    o.created_at DESC;

-- 5. Детализация заказов с товарами
-- Полная информация о каждом заказе: кто заказал, какие товары и в каком количестве
SELECT
    orders.id,
    users.name,
    products.name,
    order_items.quantity,
    order_items.price
FROM
    orders
JOIN users ON
    users.id = orders.user_id
JOIN order_items ON
    order_items.order_id = orders.id
JOIN products ON
    products.id = order_items.product_id;

-- 6. Статистика продаж по категориям товаров
-- Суммарное количество и сумма продаж для каждой категории
SELECT
    products.category,
    SUM( order_items.quantity ) AS total_quantity,
    SUM( order_items.quantity * order_items.price ) AS total_sum
FROM
    order_items
JOIN products ON
    products.id = order_items.product_id
GROUP BY
    products.category;

-- 7. Товары, которые никогда не заказывали
-- Находим товары, чьи id отсутствуют в таблице order_items
SELECT
    name
FROM
    products
WHERE
    id NOT IN(
        SELECT
            DISTINCT product_id
        FROM
            order_items
    );

-- 8. Пользователи с тратами выше среднего
-- CTE сначала вычисляет сумму заказов для каждого пользователя
-- Затем сравниваем со средним значением
WITH user_spending AS(
    SELECT
        user_id,
        SUM( total ) AS total
    FROM
        orders
    GROUP BY
        user_id
) SELECT
    users.name,
    user_spending.total
FROM
    user_spending
JOIN users ON
    users.id = user_spending.user_id
WHERE
    total >(
        SELECT
            AVG( total )
        FROM
            user_spending
    );

-- 9. Самые продаваемые товары в каждой категории
-- Оконная функция RANK() определяет позицию товара в своей категории по количеству продаж
-- Внешний запрос фильтрует только товары с rank = 1 (лидеры продаж)
SELECT
    category,
    name,
    sales_count
FROM
    (
        SELECT
            products.category,
            products.name,
            SUM( order_items.quantity ) AS sales_count,
            RANK() OVER(
                PARTITION BY products.category
            ORDER BY
                SUM( order_items.quantity ) DESC
            ) AS RANK
        FROM
            products
        JOIN order_items ON
            products.id = order_items.product_id
        GROUP BY
            products.category,
            products.name
    ) ranked
WHERE
    RANK = 1;
