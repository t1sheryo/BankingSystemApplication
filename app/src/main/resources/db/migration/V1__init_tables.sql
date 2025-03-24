CREATE TABLE limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    limit_sum DECIMAL(15,2) NOT NULL,
    expense_datetime VARCHAR(50) NOT NULL,
    limit_datetime TIMESTAMP NOT NULL,
    limit_currency_shortname VARCHAR(3) NOT NULL,
    account_id BIGINT NOT NULL
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_from BIGINT NOT NULL,
    account_to BIGINT NOT NULL,
    currency_shortname VARCHAR(3) NOT NULL,
    sum DECIMAL(15,2) NOT NULL,
    expense_category VARCHAR(50) NOT NULL,
    datetime TIMESTAMP NOT NULL,
    limit_exceeded BOOLEAN NOT NULL DEFAULT FALSE,
    limit_id BIGINT NOT NULL,
    FOREIGN KEY (limit_id) REFERENCES limits(id)
);

CREATE TABLE  exchange_rates(
    currency_from VARCHAR(10) NOT NULL,
    currency_to VARCHAR(10) NOT NULL,
    rate DECIMAL(15,2) NOT NULL,
    update_time TIMESTAMP NOT NULL,
    PRIMARY KEY (currency_from,currency_to)
);