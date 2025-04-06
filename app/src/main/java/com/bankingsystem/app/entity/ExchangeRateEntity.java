package com.bankingsystem.app.entity;

import com.bankingsystem.app.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
// uniqueConstraints добавляет ограничение уникальности для столбцов
// currency_from", "currency_to","rate_date
// имя uq_currency_from_to_date на которое мы ссылаемся в базе данных
// аннотация гарантирует что в бд не будет двух записей с одинаковыми значениями currency_from", "currency_to","rate_date"
// эти столбцы будут уникальны
@Table(name = "exchange_rates", uniqueConstraints = @UniqueConstraint(name = "uq_currency_from_to_date",
        columnNames = {"currency_from", "currency_to","rate_date"})) // задает имя таблицы в бд
public class ExchangeRateEntity {
    @EmbeddedId // указывает, что первичный ключ является составным
    // в классе ExchangeRateCompositePrimaryKey сокрыты поля currencyFrom и currencyTo
    private ExchangeRateCompositePrimaryKey id;
    //добавил precision 19 и scale 6 для явного указания размера в бд
    @Column(name = "rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    // Хранит дату YY:MM:DD. Для каждого дня создаем новую запись в таблице
    @Column(name ="rate_date", nullable = false)
    private LocalDate rateDate;

    // Это поле хранит в себе точное время последнего обновления
    // в пределах одного дня. Т.е. каждый день будет происходить
    // обновления курса и его перезапись в таблице
    // и это поле будет хранить время последней такой перезаписи
    @Column(name = "update_time", nullable = false)
    private OffsetDateTime updateTime;

    //фиксирует время создания записи в бд
    //выполняется метод INSERT
    //пример: мы добавили первый курс
    @PrePersist
    public void onCreate()
    {
        updateTime = OffsetDateTime.now();
    }
    //фиксирует время обновления записи в бд
    //выполняется метод UPDATE
    //пример: обновление курса
    @PreUpdate
    public void onUpdate()
    {
        updateTime = OffsetDateTime.now();
    }

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
        id.setCurrencyTo(currencyTo);
    }

    public Currency getCurrencyTo(){
        return id == null ? null : id.getCurrencyTo();
    }
}