package com.example.webtestingia.recorder;

/**
 * Template that describes an available Gherkin step for the recorder.
 */
public class StepTemplate {

    private String type;
    private String template;

    public StepTemplate() {
    }

    public StepTemplate(String type, String template) {
        this.type = type;
        this.template = template;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}

