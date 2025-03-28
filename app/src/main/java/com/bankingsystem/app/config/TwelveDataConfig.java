package com.bankingsystem.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
// Эта аннотация для привязки внешних свойств
// конфигурации к полю данного класса.
// Говорит, что поля данного класса должны быть заполнены
// значениями из конфигурационного файла
// prefix = "spring.twelvedata" означает, что Spring будет искать свойства,
// начинающиеся с spring.twelvedata, и привязывать их к полям класса.
@ConfigurationProperties(prefix = "spring.twelvedata.api")
@Getter
@Setter
// этот класс предоставляет доступ к внешним данным из .yaml файла,
// чтобы можно было пользоваться им в дальнейшем
public class TwelveDataConfig {
    private String apiKey;
    private String apiUrl;
}
