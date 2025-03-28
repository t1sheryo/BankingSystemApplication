package com.bankingsystem.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// FIXME: Асинхронность:
//    Если обновление занимает много времени, добавьте @EnableAsync и @Async:

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
