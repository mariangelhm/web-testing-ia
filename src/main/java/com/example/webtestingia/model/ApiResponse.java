package com.example.webtestingia.model;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad para normalizar las respuestas de la API.
 * Exitos: { "data": ... }
 * Errores: { "notifications": [ { message, detail, code, type } ] }
 */
public final class ApiResponse {

    private ApiResponse() {
    }

    public static <T> ResponseEntity<Map<String, Object>> ok(T payload) {
        return ResponseEntity.ok(Map.of("data", payload));
    }

    public static ResponseEntity<Map<String, Object>> notification(HttpStatus status, String message, Object detail, String type) {
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("message", message);
        notification.put("code", status.value());
        if (detail != null) {
            notification.put("detail", detail);
        }
        if (type != null) {
            notification.put("type", type);
        }
        body.put("notifications", List.of(notification));
        return ResponseEntity.status(status).body(body);
    }
}

