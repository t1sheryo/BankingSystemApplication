INSERT INTO accounts (id) VALUES (1), (2), (3);

INSERT INTO limits (limit_sum, expense_category, limit_datetime, limit_currency_shortname, limit_remainder, account_id)
VALUES
    (1000.00, 'SERVICE', '2025-04-04 00:00:00', 'USD', 1000.00, 1),
    (2000.00, 'PRODUCT', '2025-04-04 00:00:00', 'USD', 2000.00, 1),
    (2000.00, 'SERVICE', '2025-04-04 00:00:00', 'USD', 2000.00, 2),
    (1000.00, 'PRODUCT', '2025-04-04 00:00:00', 'USD', 1000.00, 2);
