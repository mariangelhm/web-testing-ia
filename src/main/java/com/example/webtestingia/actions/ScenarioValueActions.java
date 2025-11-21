package com.example.webtestingia.actions;

import com.example.webtestingia.utils.ScenarioContext;
import core.log.LoggerUtil;
import org.slf4j.Logger;

/**
 * Maneja el almacenamiento de valores generados durante la ejecución del escenario.
 */
public class ScenarioValueActions {

    private final ScenarioContext scenarioContext;
    private final Logger logger = LoggerUtil.getLogger(ScenarioValueActions.class);

    public ScenarioValueActions(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    public void storeValue(String variable, Object value) {
        scenarioContext.save(variable, value);
        logger.info("Se guardó en '{}' el valor '{}'", variable, value);
    }
}
