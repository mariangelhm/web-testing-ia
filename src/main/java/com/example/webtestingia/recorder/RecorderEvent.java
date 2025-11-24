package com.example.webtestingia.recorder;

import java.time.Instant;

/**
 * Represents a raw event captured from the injected recorder script.
 */
public class RecorderEvent {

    private final String action;
    private final String selector;
    private final String text;
    private final String value;
    private final Instant timestamp;

    public RecorderEvent(String action, String selector, String text, String value, Instant timestamp) {
        this.action = action;
        this.selector = selector;
        this.text = text;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public String getSelector() {
        return selector;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
