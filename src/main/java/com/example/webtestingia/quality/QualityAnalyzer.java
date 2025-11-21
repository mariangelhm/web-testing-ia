package com.example.webtestingia.quality;

import com.example.webtestingia.model.QualityResult;
import com.example.webtestingia.model.QualityRule;
import com.example.webtestingia.model.exception.InvalidConfigurationException;
import com.example.webtestingia.model.exception.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ejecuta el análisis de calidad sobre escenarios Gherkin a partir de las reglas activas.
 */
@Component
public class QualityAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QualityAnalyzer.class);
    private static final String CONFIG_PATH = "config/quality-rules.yml";

    private List<QualityRule> reglasActivas;

    /**
     * Analiza un caso de prueba en formato Gherkin aplicando las reglas activas.
     *
     * @param contenido texto del caso.
     * @return resultado con puntaje y sugerencias.
     */
    public synchronized QualityResult analizarCaso(String contenido) {
        if (reglasActivas == null) {
            cargarReglas();
        }
        QualityResult result = new QualityResult();
        List<String> cumplidas = new ArrayList<>();
        List<String> falladas = new ArrayList<>();
        List<String> sugerencias = new ArrayList<>();
        double pesoTotal = reglasActivas.stream().mapToDouble(QualityRule::getPeso).sum();
        double acumulado = 0.0;

        for (QualityRule regla : reglasActivas) {
            boolean cumple = evaluarRegla(regla, contenido);
            if (cumple) {
                acumulado += regla.getPeso();
                cumplidas.add(regla.getId());
            } else {
                falladas.add(regla.getId());
                sugerencias.add("Mejorar para cumplir la regla: " + regla.getNombre());
            }
        }
        result.setPuntaje(pesoTotal == 0 ? 0 : acumulado / pesoTotal);
        result.setReglasCumplidas(cumplidas);
        result.setReglasFalladas(falladas);
        result.setSugerencias(sugerencias);
        return result;
    }

    private boolean evaluarRegla(QualityRule regla, String contenido) {
        String lower = contenido.toLowerCase();
        return switch (regla.getId()) {
            case "R1", "R4" -> lower.contains("then");
            case "R2" -> lower.contains("scenario") && contenido.lines().findFirst().orElse("").length() > 15;
            case "R3" -> contenido.lines().filter(l -> l.strip().matches("^(given|when|then|and|but).*$") ).count() <= 20;
            case "R5" -> lower.contains("given") && lower.contains("when") && lower.contains("then");
            default -> true;
        };
    }

    private void cargarReglas() {
        try (InputStream is = new ClassPathResource(CONFIG_PATH).getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(is);
            Map<String, Object> quality = (Map<String, Object>) data.get("quality");
            List<Map<String, Object>> reglas = (List<Map<String, Object>>) quality.get("reglas");
            this.reglasActivas = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            double pesoTotal = 0.0;
            for (Map<String, Object> reglaMap : reglas) {
                QualityRule rule = new QualityRule();
                rule.setId(String.valueOf(reglaMap.get("id")));
                rule.setNombre(String.valueOf(reglaMap.get("nombre")));
                rule.setDescripcion(String.valueOf(reglaMap.get("descripcion")));
                rule.setActivo(Boolean.TRUE.equals(reglaMap.get("activo")) || Boolean.parseBoolean(String.valueOf(reglaMap.get("activo"))));
                rule.setPeso(Double.parseDouble(String.valueOf(reglaMap.get("peso"))));
                if (rule.isActivo()) {
                    if (ids.contains(rule.getId())) {
                        throw new InvalidConfigurationException("ID de regla duplicado: " + rule.getId());
                    }
                    ids.add(rule.getId());
                    pesoTotal += rule.getPeso();
                    this.reglasActivas.add(rule);
                }
            }
            if (pesoTotal <= 0) {
                throw new InvalidConfigurationException("Los pesos de reglas activas deben ser mayores a cero");
            }
            LOGGER.info("{} reglas de calidad cargadas", this.reglasActivas.size());
        } catch (IOException e) {
            throw new ParsingException("No se pudo leer quality-rules.yml", e);
        }
    }
}
