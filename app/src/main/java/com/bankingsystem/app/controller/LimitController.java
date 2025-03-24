package com.bankingsystem.app.controller;

import com.bankingsystem.app.customExceptions.LimitUpdateNotAllowedException;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.repository.LimitRepository;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO
@Slf4j
@RestController
@RequestMapping("/bank/limits")
public class LimitController {
    private final LimitServiceInterface limitService;

    public LimitController(LimitServiceInterface limitService) {
        this.limitService = limitService;
    }

    @PostMapping
    public ResponseEntity<LimitEntity> createLimit(@Valid  @RequestBody LimitRequest limitRequest) {
        log.info("Create Limit Request: {}", limitRequest);
        LimitEntity limit = limitService.setLimit(limitRequest);
        // Возращаем ответ в виде ResponseEntity со
        // Статус-кодом 201 Created (для создания ресурса).
        // - Заголовком Location, указывающим URL новой транзакции.
        // - Телом ответа, содержащим созданный объект LimitEntity
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location" ,"/bank/limits/" + limit.getId())
                .body(limit);
    }

    @GetMapping
    public ResponseEntity<List<LimitResponse>> getAllLimits() {
        log.info("Get All Limits");
        List<LimitResponse> limits = limitService.getAllLimits();
        log.info("Found {} limits", limits.size());
        return ResponseEntity.ok(limits);
    }

    @GetMapping("/account")
    public ResponseEntity<List<LimitResponse>> getAllLimitsByAccountId(@RequestParam Long accountId) {
        log.info("Get All Limits By Account Id: {}", accountId);
        //проверка валидности id при некорректном бросаем exception
        if(accountId == null || accountId <= 0) {
            log.error("Account Id is not valid");
           throw new IllegalArgumentException("Account Id is not valid");
        }
        List<LimitResponse> limitsById = limitService.getLimitsByAccountId(accountId);
        log.info("Found {} Limits", limitsById.size());
        return ResponseEntity.ok(limitsById);
    }

    // FIXME: может быть проблема в этим исключением, если его могут выбросить в методах другого конроллера
    //Этот код реализует обработчик исключений Spring (@ExceptionHandler),
    // который перехватывает кастомное исключение LimitUpdateNotAllowedException
    // и преобразует его в структурированный HTTP-ответ.
    // Автоматически генерируется при выбросе этого исключения
    // в любом месте контроллера
    @ExceptionHandler(LimitUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleLimitUpdateNotAllowedException(
            LimitUpdateNotAllowedException ex){

        // Создаем тело полученной ошибки
        ErrorResponse error = ErrorResponse.create(
                ex,
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage()
        );

        // Формируем ответ в виде JSON на основе ошибки
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)  // Устанавливаем статус 429
                .body(error);                          // Добавляем тело ответа
    }
}
