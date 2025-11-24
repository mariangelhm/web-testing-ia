package com.example.webtestingia.model;

/**
 * Represents the evaluation result of a specific quality rule.
 */
public class QualityRuleOutcome {

    private String id;
    private String description;

    public QualityRuleOutcome() {
    }

    public QualityRuleOutcome(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

