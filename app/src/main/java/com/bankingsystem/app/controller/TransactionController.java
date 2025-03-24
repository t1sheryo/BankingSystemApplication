package com.bankingsystem.app.controller;

import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.TransactionDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.Errors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.bankingsystem.app.enums.Currency;

// FIXME: по хорошему пересмотреть вариант с добавлением
// атрибутов в sessionattributes.
// Можно это реализовать как-нибудь по другому

// FIXME: можно переписать этот класс с учетом обновлений
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
        // FIXME: тут должны быть все курсы переводов валют

        //model.addAttribute("exchangeRates", exchangeRates);
    }

    @ModelAttribute(name = "transactionList")
    public List<TransactionDTO> listTransactions() {
        return new ArrayList<TransactionDTO>();
    }

    @ModelAttribute(name = "transaction")
    public TransactionDTO transaction(){
        return new TransactionDTO();
    }

    @GetMapping
    public String showAddForm(){
        return "addFormPage";
    }

    @PostMapping
    public String addTransaction(@Valid @ModelAttribute TransactionDTO transaction,
                               Errors errors,
                               @ModelAttribute List<TransactionDTO> transactionList) {
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
    public String returnToAddTransactionPage(){ // return from transaction show form to add transaction
        return "redirect:/bank";
    }

}
