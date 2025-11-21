package com.example.webtestingia.recorder;

import org.openqa.selenium.WebDriver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una sesión de grabación asociada a un usuario o proyecto.
 */
public class RecorderSession {

    private final String sessionId;
    private final WebDriver webDriver;
    private final List<String> steps = new ArrayList<>();
    private final Instant createdAt = Instant.now();

    public RecorderSession(String sessionId, WebDriver webDriver) {
        this.sessionId = sessionId;
        this.webDriver = webDriver;
    }

    public String getSessionId() {
        return sessionId;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    public List<String> getSteps() {
        return steps;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
