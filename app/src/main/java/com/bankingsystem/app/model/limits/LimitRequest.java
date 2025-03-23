package com.bankingsystem.app.model.limits;

import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class LimitRequest {
    private Long accountId;
    private BigDecimal limit;
    private Category category;
    private final Currency limitCurrency = Currency.USD; // пока что всегда в USD
    // FIXME: можно сделать по другому на выбор пользователя например
}
