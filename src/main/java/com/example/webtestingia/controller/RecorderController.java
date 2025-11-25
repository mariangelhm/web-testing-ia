package com.example.webtestingia.controller;

import com.example.webtestingia.model.ApiResponse;
import com.example.webtestingia.recorder.RecorderService;
import com.example.webtestingia.recorder.StepMapper;
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
        recorderService.procesarEvento(proyecto, grupo, payload);
        return ApiResponse.ok(Map.of("message", "Event processed"));
    }

    /**
     * Devuelve los pasos acumulados de una sesión.
     */
    @GetMapping("/steps")
    public ResponseEntity<Map<String, Object>> steps(@RequestParam String sessionId, @RequestParam String browserId) {
        return ApiResponse.ok(recorderService.obtenerPasos(sessionId, browserId));
    }

    /**
     * Detiene la grabación, cierra el navegador y devuelve sugerencias.
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop(@RequestParam String sessionId, @RequestParam String browserId) {
        return ApiResponse.ok(recorderService.finalizarSesion(sessionId, browserId));
    }

    /**
     * Mantiene viva una sesión del recorder y valida que el navegador siga abierto.
     */
    @PostMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(@RequestParam String sessionId, @RequestParam String browserId) {
        return ApiResponse.ok(recorderService.ping(sessionId, browserId));
    }

    /**
     * Provides the catalog of available steps ordered by Given/When/Then for UI consumers.
     */
    @GetMapping("/available-steps")
    public ResponseEntity<Map<String, Object>> availableSteps() {
        return ApiResponse.ok(Map.of("steps", stepMapper.getAvailableSteps()));
    }
}
