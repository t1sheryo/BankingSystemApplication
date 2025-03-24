package com.bankingsystem.app.entity;

import com.bankingsystem.app.enums.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exchange_rates") // задает имя таблицы в бд
public class ExchangeRateEntity {
    @EmbeddedId // указывает, что первичный ключ является составным
    // в классе ExchangeRateCompositePrimaryKey сокрыты поля currencyFrom и currencyTo
    private ExchangeRateCompositePrimaryKey id;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

    @Column(name = "update_time", nullable = false)
    private OffsetDateTime updateTime;

    // Вспомогательные методы для удобства работы с полями ключа
    // Необходимо делать проверку id == null, т.к.
    // @EmbeddedId Hibernate не генерирует автоматически ключ
    // или в случае, когда обьект был только что десериализован.
    // Возможно, есть еще подобные случаи, поэтому необходимо проверять
    // значение поля id на null pointer
    public void setCurrencyFrom(Currency currencyFrom) {
        if(id == null) id = new ExchangeRateCompositePrimaryKey();
        id.setCurrencyFrom(currencyFrom);
    }

    public Currency getCurrencyFrom(){
        return id == null ? null : id.getCurrencyFrom();
    }

    public void setCurrencyTo(Currency currencyTo) {
        if(id == null) id = new ExchangeRateCompositePrimaryKey();
        id.setCurrencyFrom(currencyTo);
    }

    public Currency getCurrencyTo(){
        return id == null ? null : id.getCurrencyTo();
    }
}