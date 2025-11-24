package com.example.webtestingia.recorder;

import com.example.webtestingia.drivers.WebDriverFactory;
import com.example.webtestingia.model.QualityResult;
import com.example.webtestingia.model.exception.BrowserException;
import com.example.webtestingia.model.exception.SessionException;
import com.example.webtestingia.quality.QualityAnalyzer;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado de convertir eventos en pasos y generar sugerencias de calidad al finalizar la grabación.
 */
@Service
public class RecorderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecorderService.class);
    private final RecorderSessionManager sessionManager;
    private final StepMapper stepMapper;
    private final QualityAnalyzer qualityAnalyzer;
    private final WebDriverFactory webDriverFactory;
    private final RecorderScriptProvider recorderScriptProvider;

    public RecorderService(RecorderSessionManager sessionManager,
                           StepMapper stepMapper,
                           QualityAnalyzer qualityAnalyzer,
                           WebDriverFactory webDriverFactory,
                           RecorderScriptProvider recorderScriptProvider) {
        this.sessionManager = sessionManager;
        this.stepMapper = stepMapper;
        this.qualityAnalyzer = qualityAnalyzer;
        this.webDriverFactory = webDriverFactory;
        this.recorderScriptProvider = recorderScriptProvider;
    }

    /**
     * Inicia un navegador visible, registra la sesión y deja el script del recorder inyectado.
     */
    public Map<String, Object> startRecording() {
        try {
            WebDriver driver = webDriverFactory.buildDriver();
            String sessionId = UUID.randomUUID().toString();
            String browserId = UUID.randomUUID().toString();
            RecorderSession session = sessionManager.createSession(sessionId, browserId, driver);
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            openAndInject(session, baseUrl);
            sessionManager.monitorLifecycle(sessionId, driver, () -> ensureInjection(session, baseUrl));
            return Map.of(
                    "sessionId", sessionId,
                    "browserId", browserId
            );
        } catch (Exception e) {
            throw new BrowserException("No se pudo iniciar el navegador para la sesión", e);
        }
    }

    /**
     * Procesa un evento y lo convierte en un paso.
     */
    public void procesarEvento(String proyecto, String grupo, Map<String, Object> payload) {
        String sessionId = stringValue(payload.get("sessionId"));
        String browserId = stringValue(payload.get("browserId"));
        String action = stringValue(payload.get("action"));
        String selector = payload.get("selector") != null ? String.valueOf(payload.get("selector")) : "";
        String text = payload.get("text") != null ? String.valueOf(payload.get("text")) : "";
        String value = payload.get("value") != null ? String.valueOf(payload.get("value")) : "";

        RecorderSession session = sessionManager.validateSession(sessionId, browserId);
        session.touch();

        RecorderEvent event = new RecorderEvent(action, selector, text, value, Instant.now());
        session.getEvents().add(event);

        String step = stepMapper.mapEvent(proyecto, grupo, action, selector, text, value);
        sessionManager.addStep(sessionId, step);
    }

    /**
     * Finaliza una sesión devolviendo los pasos y el resumen de calidad.
     */
    public Map<String, Object> finalizarSesion(String sessionId, String browserId) {
        sessionManager.validateSession(sessionId, browserId);
        RecorderSession closedSession = sessionManager.closeSession(sessionId);
        List<String> steps = closedSession.getSteps();
        String escenario = "Scenario: Recorded flow\n" + String.join("\n", steps);
        QualityResult quality = qualityAnalyzer.analizarCaso(escenario);
        double roundedScore = java.math.BigDecimal.valueOf(quality.getScore())
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
        quality.setScore(roundedScore);
        LOGGER.info("Sesión {} finalizada con {} pasos", sessionId, steps.size());

        Map<String, Object> qualityPayload = Map.of(
                "score", quality.getScore(),
                "passedRules", quality.getPassedRules(),
                "failedRules", quality.getFailedRules(),
                "suggestions", quality.getSuggestions(),
                "ruleDetails", quality.getRuleDetails()
        );

        Map<String, Object> locators = Map.of(
                "selectorsUsed", closedSession.getEvents().stream()
                        .map(RecorderEvent::getSelector)
                        .filter(selector -> selector != null && !selector.isBlank())
                        .distinct()
                        .collect(Collectors.toList())
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("steps", steps);
        response.put("quality", qualityPayload);
        response.put("suggestions", quality.getSuggestions());
        response.put("locators", locators);
        return response;
    }

    public Map<String, Object> ping(String sessionId, String browserId) {
        RecorderSession session = sessionManager.validateSession(sessionId, browserId);
        if (sessionManager.isBrowserClosed(session)) {
            throw new SessionException("El navegador ya no está disponible para la sesión " + sessionId);
        }
        session.touch();
        return Map.of(
                "status", "alive",
                "lastPing", session.getLastPing().toString()
        );
    }

    public Map<String, Object> obtenerPasos(String sessionId, String browserId) {
        RecorderSession session = sessionManager.validateSession(sessionId, browserId);
        return Map.of("steps", session.getSteps());
    }

    private void openAndInject(RecorderSession session, String baseUrl) {
        try {
            session.getWebDriver().get("about:blank");
            injectRecorderScript(session, baseUrl);
        } catch (Exception e) {
            throw new BrowserException("No se pudo preparar el navegador para la grabación", e);
        }
    }

    private void injectRecorderScript(RecorderSession session, String baseUrl) {
        WebDriver driver = session.getWebDriver();
        if (!(driver instanceof JavascriptExecutor executor)) {
            throw new BrowserException("El driver no soporta ejecución de JavaScript para inyectar el recorder", null);
        }
        String script = recorderScriptProvider.buildScript(baseUrl, session.getSessionId(), session.getBrowserId());
        executor.executeScript(script);
        session.setLastInjectedUrl(currentUrl(driver));
        LOGGER.debug("Script del recorder inyectado en sesión {}", session.getSessionId());
    }

    private void ensureInjection(RecorderSession session, String baseUrl) {
        WebDriver driver = session.getWebDriver();
        if (!(driver instanceof JavascriptExecutor executor)) {
            return;
        }
        try {
            Object injected = executor.executeScript("return window && window.__webRecorderInjected === true;");
            String current = currentUrl(driver);
            boolean urlChanged = current != null && !current.equals(session.getLastInjectedUrl());
            if (!(injected instanceof Boolean bool && bool) || urlChanged) {
                LOGGER.debug("Reinyectando script de recorder en sesión {}", session.getSessionId());
                injectRecorderScript(session, baseUrl);
            }
        } catch (Exception e) {
            LOGGER.debug("No se pudo verificar la inyección de recorder para la sesión {}", session.getSessionId(), e);
        }
    }

    private String currentUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return null;
        }
    }

    private String stringValue(Object raw) {
        return raw == null ? "" : String.valueOf(raw);
    }
}
