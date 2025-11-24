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
    private final String browserId;
    private final WebDriver webDriver;
    private final List<RecorderEvent> events = new ArrayList<>();
    private final List<String> steps = new ArrayList<>();
    private final Instant createdAt = Instant.now();
    private Instant lastPing = Instant.now();
    private String lastInjectedUrl = "";

    public RecorderSession(String sessionId, String browserId, WebDriver webDriver) {
        this.sessionId = sessionId;
        this.browserId = browserId;
        this.webDriver = webDriver;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getBrowserId() {
        return browserId;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    public List<RecorderEvent> getEvents() {
        return events;
    }

    public List<String> getSteps() {
        return steps;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastPing() {
        return lastPing;
    }

    public void touch() {
        this.lastPing = Instant.now();
    }

    public String getLastInjectedUrl() {
        return lastInjectedUrl;
    }

    public void setLastInjectedUrl(String lastInjectedUrl) {
        this.lastInjectedUrl = lastInjectedUrl;
    }
}
