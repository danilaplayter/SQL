-- Пользователи
INSERT
    INTO
        users(
            name,
            email
        )
    VALUES(
        'Алексей Петров',
        'alex.petrov@example.com'
    ),
    (
        'Мария Сидорова',
        'maria.sidorova@example.com'
    ),
    (
        'Иван Иванов',
        'ivan.ivanov@workmail.com'
    ),
    (
        'Екатерина Смирнова',
        'ek.smirnova@inbox.ru'
    ),
    (
        'Дмитрий Козлов',
        'dmitry.kozlov@business.org'
    ),
    (
        'Ольга Новикова',
        'olga.novikova@mail.ru'
    ),
    (
        'Сергей Волков',
        'serg_volkov@domain.com'
    ),
    (
        'Анна Кузнецова',
        'anna_kuz@example.org'
    ),
    (
        'Михаил Попов',
        'm.popov@contact.net'
    ),
    (
        'Татьяна Васильева',
        'tvasilyeva@personal.com'
    );

-- Товары
INSERT
    INTO
        products(
            name,
            price,
            category,
            stock_quantity
        )
    VALUES(
        'Ноутбук ASUS ROG',
        89999.00,
        'electronics',
        5
    ),
    (
        'Мышка Logitech MX Master',
        7500.00,
        'electronics',
        20
    ),
    (
        'Книга "Чистый код"',
        2500.00,
        'books',
        100
    ),
    (
        'Кофемашина DeLonghi',
        34999.00,
        'home',
        8
    ),
    (
        'Футболка хлопковая',
        1999.00,
        'clothing',
        50
    ),
    (
        'Беспроводные наушники Sony',
        12990.00,
        'electronics',
        15
    ),
    (
        'Сковорода с керамическим покрытием',
        4500.00,
        'home',
        30
    ),
    (
        'Вино Cabernet Sauvignon',
        2500.00,
        'food',
        40
    ),
    (
        'Беговая дорожка PRO Fit',
        89900.00,
        'sports',
        3
    ),
    (
        'Набор LEGO Technic',
        7599.00,
        'toys',
        12
    ),
    (
        'Увлажнитель воздуха Xiaomi',
        8990.00,
        'home',
        18
    ),
    (
        'Кроссовки Nike Air Max',
        14999.00,
        'clothing',
        22
    ),
    (
        'Экшн-камера GoPro Hero 11',
        37990.00,
        'electronics',
        7
    ),
    (
        'Портфель кожаный',
        9990.00,
        'accessories',
        9
    ),
    (
        'Гироскутер Smart Balance',
        23900.00,
        'sports',
        6
    );

-- Заказы
INSERT
    INTO
        orders(
            user_id,
            total,
            status
        )
    VALUES(
        1,
        97499.00,
        'completed'
    ),
    (
        2,
        15000.00,
        'processing'
    ),
    (
        5,
        1999.00,
        'pending'
    ),
    (
        8,
        124980.00,
        'shipped'
    ),
    (
        3,
        37990.00,
        'cancelled'
    );

-- Позиции заказов
INSERT
    INTO
        order_items(
            order_id,
            product_id,
            quantity,
            price
        )
    VALUES(
        1,
        1,
        1,
        89999.00
    ),
    (
        1,
        2,
        1,
        7500.00
    ),
    (
        2,
        5,
        3,
        1999.00
    ),
    (
        2,
        12,
        1,
        14999.00
    ),
    (
        3,
        5,
        1,
        1999.00
    ),
    (
        4,
        9,
        1,
        89900.00
    ),
    (
        4,
        15,
        1,
        23900.00
    ),
    (
        4,
        10,
        2,
        7599.00
    ),
    (
        5,
        13,
        1,
        37990.00
    );
