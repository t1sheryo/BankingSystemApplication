package com.bankingsystem.app.controller;

import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.ExchangeRate;
import com.bankingsystem.app.model.Transaction;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.Errors;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.bankingsystem.app.enums.Currency;

@Slf4j
@Controller
@RequestMapping("/bank")
@SessionAttributes("transactionList")
public class TransactionController {

    @ModelAttribute
    public void addCategories(Model model) {
        List<Category> categories = Arrays.asList(Category.values());

        model.addAttribute("categories", categories);
    }

    @ModelAttribute
    public void addCurrencies(Model model) {
        List<Currency> currencies = Arrays.asList(Currency.values());

        model.addAttribute("currencies", currencies);
    }

    @ModelAttribute
    public void addExchangeRates(Model model) {
        List<ExchangeRate> exchangeRates = Arrays.asList(
                new ExchangeRate(Currency.RUB, Currency.USD, new BigDecimal("0.0104"), Timestamp.valueOf("2024-02-11 20:36:00")),
                new ExchangeRate(Currency.USD, Currency.RUB, new BigDecimal("97.8291"), Timestamp.valueOf("2024-02-11 10:36:25")),
                new ExchangeRate(Currency.RUB, Currency.EUR, new BigDecimal("0.0099"), Timestamp.valueOf("2024-02-11 05:40:27")),
                new ExchangeRate(Currency.EUR, Currency.RUB, new BigDecimal("101.0222"), Timestamp.valueOf("2025-02-11 15:30:00")),
                new ExchangeRate(Currency.USD, Currency.EUR, new BigDecimal("0.9684"), Timestamp.valueOf("2024-12-01 15:30:00")),
                new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal("1.0326"), Timestamp.valueOf("2012-02-01 17:30:50"))
        );

        model.addAttribute("exchangeRates", exchangeRates);
    }

    @ModelAttribute(name = "transactionList")
    public List<Transaction> listTransactions() {
        return new ArrayList<Transaction>();
    }

    @ModelAttribute(name = "transaction")
    public Transaction transaction(){
        return new Transaction();
    }

    @GetMapping
    public String showAddForm(){
        return "addFormPage";
    }

    @PostMapping
    public String addTransaction(@Valid @ModelAttribute Transaction transaction,
                               Errors errors,
                               @ModelAttribute List<Transaction> transactionList) {
        if(errors.hasErrors()) {
            return "addFormPage";
        }
        transactionList.add(transaction);
        log.info("Transaction added: {}" + transaction);
        return "redirect:/bank/transactions";
    }

    @GetMapping("/transactions")
    public String getTransactions(){
        return "listOfTransactionsPage";
    }

    @PostMapping("/transactions")
    public String returnToAddTransaction(){
        return "redirect:/bank";
    }

}
