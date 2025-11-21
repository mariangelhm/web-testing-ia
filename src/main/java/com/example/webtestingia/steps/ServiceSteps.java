package com.example.webtestingia.steps;

import com.example.webtestingia.actions.ApiServiceCaptures;
import com.example.webtestingia.actions.ScenarioValueActions;
import com.example.webtestingia.utils.ScenarioContext;
import core.api.RestServiceClient;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;

public class ServiceSteps {

    private final RestServiceClient restServiceClient;
    private final ScenarioValueActions scenarioValueActions;
    private final ApiServiceCaptures apiServiceCaptures;

    public ServiceSteps() {
        this(new RestServiceClient(), new ScenarioContext());
    }

    public ServiceSteps(RestServiceClient restServiceClient, ScenarioContext scenarioContext) {
        this.restServiceClient = restServiceClient;
        this.scenarioValueActions = new ScenarioValueActions(scenarioContext);
        this.apiServiceCaptures = new ApiServiceCaptures(restServiceClient, scenarioValueActions);
    }

    // ==============================================================================
    //                           STEP DE SERVICIO
    // ==============================================================================

    // ======================
    //     dado/given
    // ======================

    @Dado("selecciono el método HTTP {string}")
    public void seleccionoElMetodoHttp(String method) {

    }


    // ======================
    //     cuando/when
    // ======================
    @Cuando("ejecuto el servicio previo")
    public void ejecutoServicioPrevio(String nombreServicio) {
        // Aquí, como en tu proyecto original,
        // seteas el nombre del servicio en el cliente:

    }

}
