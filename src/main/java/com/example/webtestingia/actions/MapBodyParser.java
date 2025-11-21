package com.example.webtestingia.actions;

import java.util.LinkedHashMap;
import java.util.Map;

class MapBodyParser {

    Map<String, String> parseKeyValueBody(String rawBody) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] lines = rawBody.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("[:=]", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Formato inválido en form-data: " + line);
            }
            result.put(parts[0].trim(), parts[1].trim());
        }
        return result;
    }
}
