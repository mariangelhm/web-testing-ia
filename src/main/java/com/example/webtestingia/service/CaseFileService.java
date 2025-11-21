package com.example.webtestingia.service;

import com.example.webtestingia.model.QualityResult;
import com.example.webtestingia.model.TestCaseDetail;
import com.example.webtestingia.model.TestCaseSummary;
import com.example.webtestingia.model.exception.FileAccessException;
import com.example.webtestingia.model.exception.ProjectNotFoundException;
import com.example.webtestingia.quality.QualityAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio encargado de manipular archivos .feature dentro de los proyectos.
 */
@Service
public class CaseFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseFileService.class);
    private static final Pattern ESCENARIO_PATTERN = Pattern.compile("Scenario(?: Outline)?:\\s*(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG_PATTERN = Pattern.compile("^@(.+)$");

    private final Path featuresRoot = Paths.get("src/test/resources/features");
    private final QualityAnalyzer qualityAnalyzer;

    /**
     * Constructor con dependencias necesarias.
     *
     * @param qualityAnalyzer analizador de calidad inyectado.
     */
    public CaseFileService(QualityAnalyzer qualityAnalyzer) {
        this.qualityAnalyzer = qualityAnalyzer;
    }

    /**
     * Lista los casos de un proyecto de forma recursiva, retornando la calidad calculada por caso.
     *
     * @param proyecto nombre del proyecto.
     * @return lista de casos.
     */
    public List<TestCaseSummary> listarCasos(String proyecto) {
        Path projectPath = validarProyecto(proyecto);
        try {
            return Files.walk(projectPath)
                    .filter(path -> path.toString().endsWith(".feature"))
                    .map(path -> buildSummary(projectPath, path))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileAccessException("Error al recorrer los casos del proyecto", e);
        }
    }

    /**
     * Devuelve el contenido completo de un archivo .feature y su análisis de calidad.
     *
     * @param proyecto nombre del proyecto.
     * @param ruta     ruta relativa del archivo dentro del proyecto.
     * @return detalle del caso.
     */
    public TestCaseDetail leerCaso(String proyecto, String ruta) {
        Path projectPath = validarProyecto(proyecto);
        Path casePath = normalizarRuta(projectPath, ruta);
        try {
            String contenido = Files.readString(casePath, StandardCharsets.UTF_8);
            TestCaseDetail detail = new TestCaseDetail();
            detail.setRuta(projectPath.relativize(casePath).toString());
            detail.setContenido(contenido);
            detail.setEscenarios(extraerEscenarios(contenido));
            detail.setCalidad(qualityAnalyzer.analizarCaso(contenido));
            return detail;
        } catch (IOException e) {
            throw new FileAccessException("No se pudo leer el caso solicitado", e);
        }
    }

    /**
     * Crea un nuevo archivo .feature o agrega un escenario según la ruta indicada.
     *
     * @param proyecto nombre del proyecto.
     * @param ruta     ruta del archivo dentro del proyecto.
     * @param contenido contenido gherkin.
     */
    public void crearCaso(String proyecto, String ruta, String contenido) {
        Path projectPath = validarProyecto(proyecto);
        Path casePath = normalizarRuta(projectPath, ruta);
        try {
            Files.createDirectories(casePath.getParent());
            Files.writeString(casePath, contenido, StandardCharsets.UTF_8);
            LOGGER.info("Caso creado en {}", casePath);
        } catch (IOException e) {
            throw new FileAccessException("No se pudo crear el caso", e);
        }
    }

    /**
     * Actualiza un archivo .feature existente.
     *
     * @param proyecto nombre del proyecto.
     * @param ruta ruta del archivo dentro del proyecto.
     * @param contenido nuevo contenido.
     */
    public void actualizarCaso(String proyecto, String ruta, String contenido) {
        Path projectPath = validarProyecto(proyecto);
        Path casePath = normalizarRuta(projectPath, ruta);
        if (!Files.exists(casePath)) {
            throw new ProjectNotFoundException("El archivo .feature no existe: " + ruta);
        }
        try {
            Files.writeString(casePath, contenido, StandardCharsets.UTF_8);
            LOGGER.info("Caso actualizado en {}", casePath);
        } catch (IOException e) {
            throw new FileAccessException("No se pudo actualizar el caso", e);
        }
    }

    /**
     * Elimina un archivo .feature.
     *
     * @param proyecto nombre del proyecto.
     * @param ruta ruta del archivo dentro del proyecto.
     */
    public void eliminarCaso(String proyecto, String ruta) {
        Path projectPath = validarProyecto(proyecto);
        Path casePath = normalizarRuta(projectPath, ruta);
        try {
            Files.deleteIfExists(casePath);
            LOGGER.warn("Caso eliminado en {}", casePath);
        } catch (IOException e) {
            throw new FileAccessException("No se pudo eliminar el caso", e);
        }
    }

    private Path validarProyecto(String proyecto) {
        Path projectPath = featuresRoot.resolve(proyecto);
        if (!Files.exists(projectPath) || !Files.isDirectory(projectPath)) {
            throw new ProjectNotFoundException("Proyecto no encontrado: " + proyecto);
        }
        return projectPath;
    }

    private Path normalizarRuta(Path projectPath, String ruta) {
        Path casePath = projectPath.resolve(ruta).normalize();
        if (!casePath.startsWith(projectPath)) {
            throw new IllegalArgumentException("La ruta indicada es inválida o sale del proyecto");
        }
        return casePath;
    }

    private TestCaseSummary buildSummary(Path projectPath, Path casePath) {
        try {
            String contenido = Files.readString(casePath, StandardCharsets.UTF_8);
            List<String> escenarios = extraerEscenarios(contenido);
            List<String> tags = extraerTags(contenido);
            QualityResult calidad = qualityAnalyzer.analizarCaso(contenido);
            TestCaseSummary summary = new TestCaseSummary();
            summary.setRuta(projectPath.relativize(casePath).toString());
            summary.setEscenario(escenarios.isEmpty() ? "" : escenarios.get(0));
            summary.setTags(tags);
            summary.setCalidad(calidad);
            return summary;
        } catch (IOException e) {
            throw new FileAccessException("No se pudo leer el caso " + casePath, e);
        }
    }

    private List<String> extraerEscenarios(String contenido) {
        List<String> escenarios = new ArrayList<>();
        Matcher matcher = ESCENARIO_PATTERN.matcher(contenido);
        while (matcher.find()) {
            escenarios.add(matcher.group(1).trim());
        }
        return escenarios;
    }

    private List<String> extraerTags(String contenido) {
        List<String> tags = new ArrayList<>();
        String[] lineas = contenido.split("\\r?\\n");
        for (String linea : lineas) {
            Matcher matcher = TAG_PATTERN.matcher(linea.trim());
            if (matcher.find()) {
                Collections.addAll(tags, matcher.group(1).split("\\s+"));
            }
        }
        return tags;
    }
}
