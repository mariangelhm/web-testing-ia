package com.example.webtestingia.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resultado del análisis de calidad de un caso Gherkin.
 */
public class QualityResult {

    private double score;
    private List<QualityRuleOutcome> passedRules = new ArrayList<>();
    private List<QualityRuleOutcome> failedRules = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private Map<String, String> ruleDetails = new HashMap<>();

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<QualityRuleOutcome> getPassedRules() {
        return passedRules;
    }

    public void setPassedRules(List<QualityRuleOutcome> passedRules) {
        this.passedRules = passedRules;
    }

    public List<QualityRuleOutcome> getFailedRules() {
        return failedRules;
    }

    public void setFailedRules(List<QualityRuleOutcome> failedRules) {
        this.failedRules = failedRules;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Map<String, String> getRuleDetails() {
        return ruleDetails;
    }

    public void setRuleDetails(Map<String, String> ruleDetails) {
        this.ruleDetails = ruleDetails;
    }
}
