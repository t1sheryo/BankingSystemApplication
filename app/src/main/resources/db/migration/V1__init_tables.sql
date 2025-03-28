CREATE TABLE limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    limit_sum DECIMAL(15,2) NOT NULL,
    expense_category ENUM('PRODUCT','SERVICE') NOT NULL,
    limit_datetime TIMESTAMP NOT NULL,
    limit_currency_shortname ENUM('USD', 'RUB', 'EUR') NOT NULL,
    limit_remainder DECIMAL NOT NULL,
    account_id BIGINT NOT NULL
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_from BIGINT NOT NULL,
    account_to BIGINT NOT NULL,
    currency_shortname ENUM('USD', 'RUB', 'EUR') NOT NULL,
    expense_category ENUM('PRODUCT', 'SERVICE') NOT NULL,
    sum DECIMAL(19,6) NOT NULL,
    datetime TIMESTAMP NOT NULL,
    limit_exceeded BOOLEAN NOT NULL DEFAULT FALSE,
    limit_id BIGINT NOT NULL,
    FOREIGN KEY (limit_id) REFERENCES limits(id)
);

CREATE TABLE  exchange_rates(
    currency_from VARCHAR(10) NOT NULL,
    currency_to VARCHAR(10) NOT NULL,
    rate DECIMAL(19,6) NOT NULL,
    rate_date DATE NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (currency_from,currency_to)
    CONSTRAINT uq_currency_from_to_date UNIQUE (currency_from,currency_to, rate_date)
);
--Нужно это закинуть в файл V2_init
INSERT INTO limits(id, limit_sum, expense_category, limit_datetime, limit_currency_shortname, limit_remainder, account_id) VALUES
(1,1000,'PRODUCT','2025-01-01 00:20:00','USD',1000,1),(1,1000,'SERVICE','2025-01-01 00:20:00','USD',1000,1);