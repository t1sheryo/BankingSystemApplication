package com.bankingsystem.app.entity;

import com.bankingsystem.app.enums.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable // обозначает, что этот класс будет встроен в другую сущность,
// для него не обходимо создавать таблицу в бд, не нужна аннотация @Id
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateCompositePrimaryKey implements Serializable {
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_from", nullable = false)
    private Currency currencyFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_to", nullable = false)
    private Currency currencyTo;

    // Необходимо реализовать метод equals(),
    // т.к. Hibernate использует эти методы, чтобы сравнить
    // являются ли два ключа одинаковыми при сравнении обьектов
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || this.getClass() != obj.getClass()) return false;
        ExchangeRateCompositePrimaryKey other = (ExchangeRateCompositePrimaryKey) obj;
        return (other.currencyFrom == this.currencyFrom) && (other.currencyTo == this.currencyTo);
    }

    // Необходимо реализовать метод hashCode(),
    // т.к. одинаковые обьекты должны иметь одинаковые
    // хеш-коды, в противном случае при отсутствии реализации
    // хеш-коды будут отличаться, что нарушает правила Java
    @Override
    public int hashCode() {
        return Objects.hash(currencyFrom, currencyTo);
        // Objects.hash - это утилитный метод,
        // который вычисляет хеш-код
        // на основе переданных аргументов
    }
}
