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
     * @param browserId identificador del navegador visible.
     * @param driver    instancia de WebDriver asociada.
     */
    public RecorderSession createSession(String sessionId, String browserId, WebDriver driver) {
        RecorderSession session = new RecorderSession(sessionId, browserId, driver);
        sesiones.put(sessionId, session);
        LOGGER.info("Sesión de grabación {} creada con browserId {}", sessionId, browserId);
        return session;
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

    public RecorderSession validateSession(String sessionId, String browserId) {
        if (sessionId == null || browserId == null) {
            throw new SessionException("Faltan sessionId o browserId");
        }
        RecorderSession session = getSession(sessionId);
        if (!browserId.equals(session.getBrowserId())) {
            throw new SessionException("La sesión no coincide con el navegador");
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
    public RecorderSession closeSession(String sessionId) {
        RecorderSession session = sesiones.remove(sessionId);
        if (session == null) {
            throw new SessionException("Sesión no encontrada: " + sessionId);
        }
        try {
            session.getWebDriver().quit();
        } catch (Exception e) {
            LOGGER.error("Error al cerrar navegador de la sesión {}", sessionId, e);
        }
        return session;
    }

    public void touch(String sessionId) {
        RecorderSession session = getSession(sessionId);
        session.touch();
    }

    public boolean isBrowserClosed(RecorderSession session) {
        return browserClosed(session.getWebDriver());
    }

    public void monitorLifecycle(String sessionId, WebDriver driver, Runnable reinjector) {
        Thread watcher = new Thread(() -> {
            while (sesiones.containsKey(sessionId)) {
                try {
                    if (browserClosed(driver)) {
                        LOGGER.info("Detectamos cierre de navegador para la sesión {}. Se liberará la sesión automáticamente.", sessionId);
                        closeSessionSafely(sessionId);
                        break;
                    }
                    if (reinjector != null) {
                        reinjector.run();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    LOGGER.debug("Error al monitorear la sesión {}", sessionId, e);
                }
            }
        }, "recorder-session-watch-" + sessionId);
        watcher.setDaemon(true);
        watcher.start();
    }


    private void monitorLifecycle(String sessionId, WebDriver driver) {
        Thread watcher = new Thread(() -> {
            while (sesiones.containsKey(sessionId)) {
                try {
                    if (browserClosed(driver)) {
                        LOGGER.info("Detectamos cierre de navegador para la sesión {}. Se liberará la sesión automáticamente.", sessionId);
                        closeSessionSafely(sessionId);
                        break;
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    LOGGER.debug("Error al monitorear la sesión {}", sessionId, e);
                }
            }
        }, "recorder-session-watch-" + sessionId);
        watcher.setDaemon(true);
        watcher.start();
    }

    private boolean browserClosed(WebDriver driver) {
        try {
            return driver == null || driver.getWindowHandles().isEmpty();
        } catch (org.openqa.selenium.NoSuchSessionException e) {
            return true;
        } catch (Exception e) {
            LOGGER.debug("No se pudo verificar el estado del navegador", e);
            return false;
        }
    }

    private void closeSessionSafely(String sessionId) {
        RecorderSession session = sesiones.remove(sessionId);
        if (session == null) {
            return;
        }
        try {
            session.getWebDriver().quit();
        } catch (Exception e) {
            LOGGER.warn("Error al cerrar navegador de la sesión {} durante limpieza automática", sessionId, e);
        }
    }
}
