package com.example.webtestingia.drivers;

import com.example.webtestingia.model.exception.BrowserException;
import com.example.webtestingia.model.exception.InvalidConfigurationException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Factoría central para construir WebDriver según la configuración de navegador.
 */
@Component
public class WebDriverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverFactory.class);
    private static final String CONFIG_PATH = "config/navegador.yml";

    /**
     * Crea un nuevo WebDriver con base en la configuración YAML.
     *
     * @return instancia de WebDriver lista para usar.
     */
    public WebDriver buildDriver() {
        Map<String, Object> config = cargarConfig();
        String tipo = String.valueOf(config.getOrDefault("tipo", "chrome"));
        String modo = String.valueOf(config.getOrDefault("modo", "local"));
        boolean autoDownload = Boolean.parseBoolean(String.valueOf(config.getOrDefault("auto_download_drivers", true)));
        LOGGER.info("Iniciando WebDriver tipo {} en modo {}", tipo, modo);
        try {
            if (autoDownload) {
                configurarAutoDescarga(tipo);
            }
            return switch (modo) {
                case "grid" -> crearRemoto(tipo, String.valueOf(config.get("grid_url")));
                default -> crearLocal(tipo);
            };
        } catch (Exception e) {
            throw new BrowserException("No se pudo inicializar el navegador", e);
        }
    }

    private Map<String, Object> cargarConfig() {
        try (InputStream is = new ClassPathResource(CONFIG_PATH).getInputStream()) {
            Yaml yaml = new Yaml();
            return yaml.load(is);
        } catch (IOException e) {
            throw new InvalidConfigurationException("No se pudo leer la configuración de navegador");
        }
    }

    private void configurarAutoDescarga(String tipo) {
        switch (tipo.toLowerCase()) {
            case "firefox" -> WebDriverManager.firefoxdriver().setup();
            case "edge" -> WebDriverManager.edgedriver().setup();
            default -> WebDriverManager.chromedriver().setup();
        }
    }

    private WebDriver crearLocal(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "firefox" -> visibleWindow(new FirefoxDriver(firefoxOptions()));
            case "edge" -> visibleWindow(new EdgeDriver(edgeOptions()));
            default -> visibleWindow(new ChromeDriver(chromeOptions()));
        };
    }

    private WebDriver crearRemoto(String tipo, String gridUrl) throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setBrowserName(tipo);
        return new RemoteWebDriver(new URL(gridUrl), caps);
    }

    private ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        // Permite realizar fetch http desde páginas https (Google, etc.) para que el recorder
        // pueda enviar eventos al backend sin ser bloqueado por mixed content.
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-web-security");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        return options;
    }

    private EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-web-security");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        return options;
    }

    private FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-width=1920");
        options.addArguments("-height=1080");
        options.addPreference("dom.webdriver.enabled", false);
        options.addPreference("useAutomationExtension", false);
        // Permite contenido mixto para que los POST del recorder no sean bloqueados desde HTTPS.
        options.addPreference("security.mixed_content.block_active_content", false);
        options.addPreference("security.mixed_content.block_display_content", false);
        return options;
    }

    private WebDriver visibleWindow(WebDriver driver) {
        try {
            driver.manage().window().setPosition(new Point(0, 0));
            driver.manage().window().setSize(new Dimension(1280, 900));
        } catch (Exception e) {
            LOGGER.warn("No se pudo ajustar la ventana del navegador", e);
        }
        return driver;
    }
}
