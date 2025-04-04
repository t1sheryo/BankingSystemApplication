package com.bankingsystem.app.unit.controller;

import com.bankingsystem.app.controller.LimitController;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;

@WebMvcTest(controllers = LimitController.class)
public class LimitControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LimitServiceInterface limitService;

    @TestConfiguration
    static class LimitControllerTestConfiguration {
        @Bean
        public LimitServiceInterface limitService() {
            return mock(LimitServiceInterface.class);
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(limitService);
    }

    // Тест для createLimit()
    // успешный тест

}
