package com.example.webtestingia.steps;

import com.example.webtestingia.drivers.WebDriverFactory;
import com.example.webtestingia.service.LocatorService;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Steps genéricos que permiten utilizar locators YAML o selectores directos en los casos Gherkin.
 */
@Component
public class WebGenericSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebGenericSteps.class);
    private final LocatorService locatorService;
    private final WebDriverFactory webDriverFactory;
    private final ThreadLocal<String> grupoActual = new ThreadLocal<>();

    @Value("${web.wait.timeout:10}")
    private int waitTimeout;

    public WebGenericSteps(LocatorService locatorService, WebDriverFactory webDriverFactory) {
        this.locatorService = locatorService;
        this.webDriverFactory = webDriverFactory;
    }

    /**
     * Define el grupo de locators activo para el escenario.
     */
    @Dado("establezco el grupo \"{string}\"")
    public void establezcoElGrupo(String grupo) {
        LOGGER.info("Estableciendo grupo de locators: {}", grupo);
        grupoActual.set(grupo);
    }

    /**
     * Ejecuta un clic sobre un elemento localizado.
     */
    @Cuando("hago clic en \"{string}\"")
    public void hagoClicEn(String objetivo) {
        WebDriver driver = webDriverFactory.buildDriver();
        By selector = resolverSelector("default", objetivo);
        LOGGER.info("Haciendo clic en {}", selector);
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(waitTimeout))
                .until(ExpectedConditions.elementToBeClickable(selector));
        element.click();
    }

    /**
     * Escribe texto en un elemento localizado.
     */
    @Cuando("escribo \"{string}\" en \"{string}\"")
    public void escriboEn(String texto, String objetivo) {
        WebDriver driver = webDriverFactory.buildDriver();
        By selector = resolverSelector("default", objetivo);
        LOGGER.info("Escribiendo en {} el texto {}", selector, texto);
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(waitTimeout))
                .until(ExpectedConditions.visibilityOfElementLocated(selector));
        element.clear();
        element.sendKeys(texto);
    }

    /**
     * Valida que el texto indicado esté presente en la página.
     */
    @Entonces("debería ver el texto \"{string}\"")
    public void deberiaVerElTexto(String texto) {
        WebDriver driver = webDriverFactory.buildDriver();
        LOGGER.info("Validando presencia de texto {}", texto);
        new WebDriverWait(driver, Duration.ofSeconds(waitTimeout))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), texto));
    }

    private By resolverSelector(String proyecto, String objetivo) {
        String normalizado = objetivo.trim();
        if (normalizado.startsWith("//") || normalizado.startsWith("xpath=")) {
            return By.xpath(normalizado.replace("xpath=", ""));
        }
        if (normalizado.startsWith("css=")) {
            return By.cssSelector(normalizado.replace("css=", ""));
        }
        String grupo = grupoActual.get();
        if (grupo != null && !grupo.isBlank()) {
            String selector = locatorService.resolveLocator(proyecto, grupo, normalizado);
            if (selector.startsWith("//") || selector.startsWith("xpath=")) {
                return By.xpath(selector.replace("xpath=", ""));
            }
            if (selector.startsWith("css=")) {
                return By.cssSelector(selector.replace("css=", ""));
            }
        }
        return By.cssSelector(normalizado);
    }
}
