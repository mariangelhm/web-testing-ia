package com.example.webtestingia.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Hooks simples para registrar inicio y fin de escenarios.
 */
@Component
public class LoggingHooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingHooks.class);

    @Before
    public void beforeScenario() {
        LOGGER.info("Iniciando escenario");
    }

    @After
    public void afterScenario() {
        LOGGER.info("Finalizando escenario");
    }
}
