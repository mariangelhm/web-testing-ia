package com.example.webtestingia.recorder;

import com.example.webtestingia.model.exception.SessionException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Administra sesiones concurrentes de grabación para múltiples usuarios.
 */
@Component
public class RecorderSessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecorderSessionManager.class);
    private final Map<String, RecorderSession> sesiones = new ConcurrentHashMap<>();

    /**
     * Crea una nueva sesión y la almacena en memoria.
     *
     * @param sessionId identificador de sesión.
     * @param driver    instancia de WebDriver asociada.
     */
    public void createSession(String sessionId, WebDriver driver) {
        RecorderSession session = new RecorderSession(sessionId, driver);
        sesiones.put(sessionId, session);
        LOGGER.info("Sesión de grabación {} creada", sessionId);
    }

    /**
     * Obtiene una sesión existente.
     */
    public RecorderSession getSession(String sessionId) {
        RecorderSession session = sesiones.get(sessionId);
        if (session == null) {
            throw new SessionException("Sesión no encontrada: " + sessionId);
        }
        return session;
    }

    /**
     * Agrega un paso generado a la sesión indicada.
     */
    public void addStep(String sessionId, String step) {
        RecorderSession session = getSession(sessionId);
        session.getSteps().add(step);
        LOGGER.info("Paso agregado a sesión {}: {}", sessionId, step);
    }

    /**
     * Elimina la sesión y retorna los pasos capturados.
     */
    public List<String> closeSession(String sessionId) {
        RecorderSession session = sesiones.remove(sessionId);
        if (session == null) {
            throw new SessionException("Sesión no encontrada: " + sessionId);
        }
        try {
            session.getWebDriver().quit();
        } catch (Exception e) {
            LOGGER.error("Error al cerrar navegador de la sesión {}", sessionId, e);
        }
        return session.getSteps();
    }
}
