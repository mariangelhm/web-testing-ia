package com.example.webtestingia.controller;

import com.example.webtestingia.model.ApiResponse;
import com.example.webtestingia.recorder.RecorderService;
import com.example.webtestingia.recorder.StepMapper;
import com.example.webtestingia.model.exception.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador del grabador web multiusuario.
 */
@CrossOrigin(
        // El recorder captura eventos desde el navegador automatizado, no desde un dominio fijo;
        // permitimos cualquier origen para que los POST del script inyectado no sean bloqueados por CORS.
        allowedOrigins = "*",
        allowedHeaders = "*",
        exposedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "false"
)
@RestController
@RequestMapping("/api/recorder")
public class RecorderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecorderController.class);

    private final RecorderService recorderService;
    private final StepMapper stepMapper;

    public RecorderController(RecorderService recorderService, StepMapper stepMapper) {
        this.recorderService = recorderService;
        this.stepMapper = stepMapper;
    }

    /**
     * Inicia una sesión del grabador creando un navegador dedicado.
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start() {
        return ApiResponse.ok(recorderService.startRecording());
    }

    /**
     * Registra un evento generado desde el frontend y lo mapea a pasos Gherkin.
     */
    @PostMapping({"/event", "/event/", "/event/**"})
    public ResponseEntity<Map<String, Object>> event(@RequestBody Map<String, Object> payload,
                                                     @RequestParam(required = false) String proyecto,
                                                     @RequestParam(required = false) String grupo) {
        LOGGER.info("Llamada a /api/recorder/event recibida: proyecto={}, grupo={}, payload={}:", proyecto, grupo, payload);

        String step = recorderService.procesarEvento(proyecto, grupo, payload);

        Map<String, Object> response = Map.of(
                "message", "Event processed",
                "step", step
        );

        LOGGER.info("Respuesta /api/recorder/event: {}", response);
        return ApiResponse.ok(response);
    }

    /**
     * Devuelve los pasos acumulados de una sesión.
     */
    @GetMapping("/steps")
    public ResponseEntity<Map<String, Object>> steps(@RequestParam(required = false) String sessionId,
                                                    @RequestParam(required = false) String browserId) {
        IdBundle ids = resolveIds(sessionId, browserId, null);
        return ApiResponse.ok(recorderService.obtenerPasos(ids.sessionId, ids.browserId));
    }

    /**
     * Detiene la grabación, cierra el navegador y devuelve sugerencias.
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop(@RequestParam(required = false) String sessionId,
                                                   @RequestParam(required = false) String browserId,
                                                   @RequestBody(required = false) Map<String, Object> body) {
        IdBundle ids = resolveIds(sessionId, browserId, body);
        return ApiResponse.ok(recorderService.finalizarSesion(ids.sessionId, ids.browserId));
    }

    /**
     * Mantiene viva una sesión del recorder y valida que el navegador siga abierto.
     */
    @PostMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(@RequestParam(required = false) String sessionId,
                                                   @RequestParam(required = false) String browserId,
                                                   @RequestBody(required = false) Map<String, Object> body) {
        IdBundle ids = resolveIds(sessionId, browserId, body);
        return ApiResponse.ok(recorderService.ping(ids.sessionId, ids.browserId));
    }

    /**
     * Provides the catalog of available steps ordered by Given/When/Then for UI consumers.
     */
    @GetMapping("/available-steps")
    public ResponseEntity<Map<String, Object>> availableSteps() {
        return ApiResponse.ok(Map.of("steps", stepMapper.getAvailableSteps()));
    }

    private IdBundle resolveIds(String sessionIdParam, String browserIdParam, Map<String, Object> body) {
        String resolvedSession = firstNonBlank(sessionIdParam, body, "sessionId");
        String resolvedBrowser = firstNonBlank(browserIdParam, body, "browserId");

        if (resolvedSession == null || resolvedBrowser == null) {
            LOGGER.warn("Solicitud de recorder sin identificadores: sessionId={}, browserId={}, bodyKeys={}",
                    sessionIdParam, browserIdParam, body != null ? body.keySet() : "null");
            throw new SessionException("Faltan sessionId o browserId en la petición del recorder");
        }

        return new IdBundle(resolvedSession, resolvedBrowser);
    }

    private String firstNonBlank(String fromParam, Map<String, Object> body, String key) {
        if (fromParam != null && !fromParam.isBlank()) {
            return fromParam;
        }
        if (body != null && body.get(key) != null) {
            String fromBody = String.valueOf(body.get(key));
            if (!fromBody.isBlank()) {
                return fromBody;
            }
        }
        return null;
    }

    private record IdBundle(String sessionId, String browserId) {}
}
