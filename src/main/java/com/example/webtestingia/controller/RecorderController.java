package com.example.webtestingia.controller;

import com.example.webtestingia.drivers.WebDriverFactory;
import com.example.webtestingia.model.exception.BrowserException;
import com.example.webtestingia.recorder.RecorderService;
import com.example.webtestingia.recorder.RecorderSessionManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Controlador del grabador web multiusuario.
 */
@RestController
@RequestMapping("/api/recorder")
public class RecorderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecorderController.class);
    private final RecorderSessionManager sessionManager;
    private final RecorderService recorderService;
    private final WebDriverFactory webDriverFactory;

    public RecorderController(RecorderSessionManager sessionManager, RecorderService recorderService, WebDriverFactory webDriverFactory) {
        this.sessionManager = sessionManager;
        this.recorderService = recorderService;
        this.webDriverFactory = webDriverFactory;
    }

    /**
     * Inicia una sesión del grabador creando un navegador dedicado.
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> start() {
        try {
            WebDriver driver = webDriverFactory.buildDriver();
            String sessionId = UUID.randomUUID().toString();
            sessionManager.createSession(sessionId, driver);
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (Exception e) {
            throw new BrowserException("No se pudo iniciar el navegador para la sesión", e);
        }
    }

    /**
     * Registra un evento generado desde el frontend y lo mapea a pasos Gherkin.
     */
    @PostMapping("/event")
    public ResponseEntity<Map<String, String>> event(@RequestBody Map<String, Object> payload, @RequestParam(required = false) String proyecto, @RequestParam(required = false) String grupo) {
        recorderService.procesarEvento(proyecto, grupo, payload);
        return ResponseEntity.ok(Map.of("mensaje", "Evento procesado"));
    }

    /**
     * Devuelve los pasos acumulados de una sesión.
     */
    @GetMapping("/steps")
    public ResponseEntity<Map<String, Object>> steps(@RequestParam String sessionId) {
        return ResponseEntity.ok(Map.of("steps", sessionManager.getSession(sessionId).getSteps()));
    }

    /**
     * Detiene la grabación, cierra el navegador y devuelve sugerencias.
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop(@RequestParam String sessionId) {
        return ResponseEntity.ok(recorderService.finalizarSesion(sessionId));
    }
}
