package com.bankingsystem.app.model.limits;

import com.bankingsystem.app.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitResponse {
    private Long accountId;
    private Category category;
    private BigDecimal limit;  // need to decide whether to return in one currency or let user choose it
    private OffsetDateTime lastUpdate;
    private BigDecimal remainder;
}
