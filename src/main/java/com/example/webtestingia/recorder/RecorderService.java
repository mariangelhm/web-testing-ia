package com.example.webtestingia.recorder;

import com.example.webtestingia.model.QualityResult;
import com.example.webtestingia.quality.QualityAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de convertir eventos en pasos y generar sugerencias de calidad al finalizar la grabación.
 */
@Service
public class RecorderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecorderService.class);
    private final RecorderSessionManager sessionManager;
    private final StepMapper stepMapper;
    private final QualityAnalyzer qualityAnalyzer;

    public RecorderService(RecorderSessionManager sessionManager, StepMapper stepMapper, QualityAnalyzer qualityAnalyzer) {
        this.sessionManager = sessionManager;
        this.stepMapper = stepMapper;
        this.qualityAnalyzer = qualityAnalyzer;
    }

    /**
     * Procesa un evento y lo convierte en un paso.
     */
    public void procesarEvento(String proyecto, String grupo, Map<String, Object> payload) {
        String sessionId = String.valueOf(payload.get("sessionId"));
        String action = String.valueOf(payload.get("action"));
        String selector = payload.get("selector") != null ? String.valueOf(payload.get("selector")) : "";
        String text = payload.get("text") != null ? String.valueOf(payload.get("text")) : "";
        String step = stepMapper.mapEvent(proyecto, grupo, action, selector, text);
        sessionManager.addStep(sessionId, step);
    }

    /**
     * Finaliza una sesión devolviendo los pasos y el resumen de calidad.
     */
    public Map<String, Object> finalizarSesion(String sessionId) {
        List<String> steps = sessionManager.closeSession(sessionId);
        String escenario = "Scenario: Flujo grabado\n" + String.join("\n", steps);
        QualityResult calidad = qualityAnalyzer.analizarCaso(escenario);
        LOGGER.info("Sesión {} finalizada con {} pasos", sessionId, steps.size());
        return Map.of(
                "steps", steps,
                "calidad", calidad,
                "sugerencias", calidad.getSugerencias()
        );
    }
}
