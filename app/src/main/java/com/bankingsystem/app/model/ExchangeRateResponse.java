package com.bankingsystem.app.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeRateResponse {
    private double rate;

    public ExchangeRateResponse(double rate) {
        this.rate = rate;
    }
}
