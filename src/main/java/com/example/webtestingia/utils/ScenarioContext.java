package com.example.webtestingia.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Almacena valores dinámicos durante la ejecución de un escenario.
 */
public class ScenarioContext {

    private final Map<String, Object> data = new HashMap<>();

    public void save(String key, Object value) {
        data.put(key, value);
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(data));
    }
}
