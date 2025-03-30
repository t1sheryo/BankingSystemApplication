package com.bankingsystem.app.model.limits;

import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.enums.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
//добавил для поддержки десериализации JSON
@NoArgsConstructor
public class LimitRequest {
    @NotNull
    @Positive
    private Long accountId;
    @NotNull
    private BigDecimal limit;
    @NotNull
    private Category category;
    @NotNull
    //убрал final чтоб пользователь мог выбрать валюту
    private Currency limitCurrency;
    // можно сделать по другому на выбор пользователя например
}
