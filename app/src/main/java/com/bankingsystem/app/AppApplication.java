package com.bankingsystem.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// FIXME: Асинхронность:
//    Если обновление занимает много времени, добавьте @EnableAsync и @Async:

// FIXME: Надо придумать что-то с логами,
//  потому что по всему проекту очень много логов,
//  которые наверное нужны, т.к. эти зоны тестируются тестами

@SpringBootApplication
// Включает поддержку планировщика.
// нужно для того, чтобы курсы валют
// сами обновлялись в единицу времени
@EnableScheduling
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}
