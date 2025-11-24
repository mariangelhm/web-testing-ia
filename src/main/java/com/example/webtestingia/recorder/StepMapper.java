package com.example.webtestingia.recorder;

import com.example.webtestingia.service.LocatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Transforma eventos del navegador en pasos Gherkin, reutilizando la lógica de resolución de locators.
 */
@Component
public class StepMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepMapper.class);
    private final LocatorService locatorService;

    public StepMapper(LocatorService locatorService) {
        this.locatorService = locatorService;
    }

    /**
     * Convierte un evento recibido en un paso Gherkin legible.
     *
     * @param proyecto proyecto asociado.
     * @param grupo    grupo activo de locators.
     * @param action   tipo de evento.
     * @param selector target o locator.
     * @param text     texto ingresado.
     * @return línea de step.
     */
    public String mapEvent(String proyecto, String grupo, String action, String selector, String text, String value) {
        String objetivo = resolverObjetivo(proyecto, grupo, selector);
        return switch (action) {
            case "click" -> "When hago clic en \"" + objetivo + "\"";
            case "input", "change" -> "When escribo \"" + (value == null ? text : value) + "\" en \"" + objetivo + "\"";
            case "navigate" -> "Given navego a \"" + (value == null || value.isBlank() ? selector : value) + "\"";
            case "submit" -> "When envío el formulario \"" + objetivo + "\"";
            default -> "When ejecuto accion \"" + action + "\" sobre \"" + objetivo + "\"";
        };
    }

    private String resolverObjetivo(String proyecto, String grupo, String selector) {
        if (selector == null) {
            return "";
        }
        String normalizado = selector.trim();
        if (normalizado.startsWith("//") || normalizado.startsWith("css=") || normalizado.startsWith("xpath=")) {
            return normalizado;
        }
        if (grupo != null && !grupo.isBlank()) {
            try {
                String resuelto = locatorService.resolveLocator(proyecto, grupo, normalizado);
                LOGGER.debug("Locator {} resuelto a {}", normalizado, resuelto);
                return normalizado;
            } catch (Exception e) {
                LOGGER.warn("No se pudo resolver locator {}, se usará literal", normalizado, e);
            }
        }
        return normalizado;
    }

    /**
     * Returns the available step templates ordered by Given, When and Then.
     */
    public List<StepTemplate> getAvailableSteps() {
        return List.of(
                new StepTemplate("GIVEN", "Given navego a \"<url>\""),
                new StepTemplate("WHEN", "When hago clic en \"<target>\""),
                new StepTemplate("WHEN", "When escribo \"<text>\" en \"<target>\""),
                new StepTemplate("WHEN", "When envío el formulario \"<target>\""),
                new StepTemplate("WHEN", "When ejecuto accion \"<action>\" sobre \"<target>\""),
                new StepTemplate("THEN", "Then debería ver el texto \"<text>\"")
        );
    }
}
