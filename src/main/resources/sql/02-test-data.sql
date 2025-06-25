-- src/main/resources/sql/02-test-data.sql
-- Тестовые данные для изучения SQL
-- Пользователи
INSERT
    INTO
        users(
            name,
            email
        )
    VALUES(
        'Алексей Петров',
        'alex@example.com'
    ),
    (
        'Мария Сидорова',
        'maria@example.com'
    ),
    (
        'Иван Козлов',
        'ivan@example.com'
    ),
    (
        'Екатерина Белова',
        'kate@example.com'
    ),
    (
        'Дмитрий Смирнов',
        'dmitry@example.com'
    );

-- Товары
INSERT
    INTO
        products(
            name,
            price,
            category,
            description
        )
    VALUES(
        'Ноутбук Dell XPS 13',
        89999.00,
        'Электроника',
        'Ультрабук для разработчиков'
    ),
    (
        'Беспроводная мышь',
        2499.00,
        'Электроника',
        'Эргономичная мышь'
    ),
    (
        'Effective Java 3rd Edition',
        3500.00,
        'Книги',
        'Книга Джошуа Блоха'
    ),
    (
        'Механическая клавиатура',
        8999.00,
        'Электроника',
        'Синие переключатели'
    ),
    (
        'Spring in Action',
        4200.00,
        'Книги',
        'Руководство по Spring'
    ),
    (
        'IntelliJ IDEA Ultimate',
        15999.00,
        'ПО',
        'Лучшая IDE для Java'
    ),
    (
        'PostgreSQL Pro',
        25000.00,
        'ПО',
        'Профессиональная СУБД'
    ),
    (
        'Clean Code',
        2800.00,
        'Книги',
        'Роберт Мартин о чистом коде'
    );

-- Заказы
INSERT
    INTO
        orders(
            user_id,
            product_id,
            quantity,
            unit_price,
            status
        )
    VALUES(
        1,
        1,
        1,
        89999.00,
        'COMPLETED'
    ),