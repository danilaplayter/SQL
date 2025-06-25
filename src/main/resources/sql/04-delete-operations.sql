DELETE
FROM
    orders
WHERE
    created_at < NOW()- INTERVAL '2 years' RETURNING id,
    created_at;

UPDATE
    users
SET
    status = 'inactive',
    updated_at = NOW()
WHERE
    created_at < NOW()- INTERVAL '3 months'
    AND status = 'active' RETURNING id,
    name,
    email;

UPDATE
    products
SET
    description = 'out_of_stock'
WHERE
    stock_quantity = 0 RETURNING id,
    name;

DELETE
FROM
    orders
WHERE
    id = 2 RETURNING id,
    status,
    total AS order_total;

UPDATE
    users
SET
    status = 'deleted',
    updated_at = NOW()
WHERE
    id = 999;

CREATE
    VIEW active_users AS SELECT
        *
    FROM
        users
    WHERE
        status != 'deleted';
