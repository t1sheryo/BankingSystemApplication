# FIXME : надо как изменить работу с enum чтобы она была расширяемой, не было тут явных значение enum

CREATE TABLE accounts(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_username VARCHAR(100) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    user_role ENUM('ADMIN', 'USER') NOT NULL
);

CREATE TABLE limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    limit_sum DECIMAL(15,2) NOT NULL DEFAULT 1000.00,
    expense_category ENUM('PRODUCT','SERVICE') NOT NULL,
    limit_datetime TIMESTAMP NOT NULL,
    limit_currency_shortname ENUM('USD', 'RUB', 'EUR') NOT NULL DEFAULT 'USD',
    limit_remainder DECIMAL NOT NULL,
    account_id BIGINT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    INDEX idx_account_category (account_id, expense_category)
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
    limit_datatime_at_time TIMESTAMP NOT NULL,
    limit_sum_at_time DECIMAL NOT NULL,
    limit_currency_at_time ENUM('USD','RUB', 'EUR') NOT NULL,
    FOREIGN KEY (account_from) REFERENCES accounts(id),
    FOREIGN KEY (account_to) REFERENCES accounts(id),
    FOREIGN KEY (limit_id) REFERENCES limits(id),
    INDEX idx_account_from_category (account_from, expense_category),
    INDEX idx_transaction_time (datetime)
);

CREATE TABLE  exchange_rates(
    currency_from VARCHAR(10) NOT NULL,
    currency_to VARCHAR(10) NOT NULL,
    rate DECIMAL(19,6) NOT NULL,
    rate_date DATE NOT NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (currency_from, currency_to),
    CONSTRAINT uq_currency_from_to_date UNIQUE (currency_from, currency_to, rate_date)
);