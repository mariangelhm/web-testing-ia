package com.example.webtestingia.model;

/**
 * Represents an available Gherkin step definition.
 */
public record StepDefinition(String type, String pattern, String source) {
}
