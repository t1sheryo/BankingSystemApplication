package com.bankingsystem.app.model.limits;


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
}
