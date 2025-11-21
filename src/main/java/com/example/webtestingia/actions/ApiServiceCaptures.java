package com.example.webtestingia.actions;

import core.api.RestServiceClient;

/**
 * Se encarga de capturar valores relevantes de las respuestas HTTP utilizando el cliente provisto
 * por qa-core.
 */
public class ApiServiceCaptures {

    private final RestServiceClient restServiceClient;
    private final ScenarioValueActions scenarioValueActions;

    public ApiServiceCaptures(RestServiceClient restServiceClient, ScenarioValueActions scenarioValueActions) {
        this.restServiceClient = restServiceClient;
        this.scenarioValueActions = scenarioValueActions;
    }

    public void captureBodyValue(String jsonPath, String variable) {
        Object value = restServiceClient.extractJsonPath(jsonPath, Object.class);
        scenarioValueActions.storeValue(variable, value);
    }

    public void captureHeaderValue(String header, String variable) {
        scenarioValueActions.storeValue(variable, restServiceClient.extractHeader(header));
    }

    public void captureCookieValue(String cookie, String variable) {
        scenarioValueActions.storeValue(variable, restServiceClient.extractCookie(cookie));
    }
}
