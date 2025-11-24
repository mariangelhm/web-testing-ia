package com.example.webtestingia.service;

import com.example.webtestingia.model.StepDefinition;
import com.example.webtestingia.steps.ServiceSteps;
import com.example.webtestingia.steps.WebGenericSteps;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Aggregates step definitions declared in the step classes so the UI can list them.
 */
@Service
public class StepCatalogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepCatalogService.class);
    private static final Map<String, Integer> TYPE_ORDER = Map.of(
            "GIVEN", 0,
            "WHEN", 1,
            "THEN", 2
    );

    public List<StepDefinition> listStepDefinitions() {
        List<StepDefinition> steps = new ArrayList<>();
        List<Class<?>> stepClasses = List.of(WebGenericSteps.class, ServiceSteps.class);
        for (Class<?> stepClass : stepClasses) {
            extract(stepClass).ifPresent(steps::addAll);
        }
        return steps.stream()
                .sorted(Comparator
                        .comparingInt(def -> TYPE_ORDER.getOrDefault(def.type(), Integer.MAX_VALUE))
                        .thenComparing(StepDefinition::pattern))
                .toList();
    }

    private Optional<List<StepDefinition>> extract(Class<?> stepClass) {
        try {
            List<StepDefinition> steps = new ArrayList<>();
            for (Method method : stepClass.getDeclaredMethods()) {
                Dado dado = method.getAnnotation(Dado.class);
                if (dado != null) {
                    steps.add(new StepDefinition("GIVEN", dado.value(), stepClass.getSimpleName()));
                }
                Cuando cuando = method.getAnnotation(Cuando.class);
                if (cuando != null) {
                    steps.add(new StepDefinition("WHEN", cuando.value(), stepClass.getSimpleName()));
                }
                Entonces entonces = method.getAnnotation(Entonces.class);
                if (entonces != null) {
                    steps.add(new StepDefinition("THEN", entonces.value(), stepClass.getSimpleName()));
                }
            }
            return Optional.of(steps);
        } catch (Exception e) {
            LOGGER.error("Cannot extract steps from {}", stepClass.getSimpleName(), e);
            return Optional.empty();
        }
    }
}
