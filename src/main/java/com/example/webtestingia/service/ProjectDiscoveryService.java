package com.example.webtestingia.service;

import com.example.webtestingia.model.ProjectMetadata;
import com.example.webtestingia.model.QualityResult;
import com.example.webtestingia.model.TestCaseSummary;
import com.example.webtestingia.model.exception.FileAccessException;
import com.example.webtestingia.model.exception.ParsingException;
import com.example.webtestingia.model.exception.ProjectNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de descubrir proyectos en el sistema de archivos y gestionar su metadata.
 */
@Service
public class ProjectDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDiscoveryService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path featuresRoot = Paths.get("src/test/resources/features");
    private final CaseFileService caseFileService;

    /**
     * Constructor con dependencias requeridas.
     *
     * @param caseFileService servicio de casos para calcular calidad agregada.
     */
    public ProjectDiscoveryService(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
    }

    /**
     * Lista todos los proyectos detectados como carpetas directas dentro del directorio de features.
     * Si un proyecto no tiene project.json, se crea uno por defecto.
     *
     * @return lista de metadatos de proyectos.
     */
    public List<ProjectMetadata> listarProyectos() {
        if (!Files.exists(featuresRoot)) {
            try {
                Files.createDirectories(featuresRoot);
            } catch (IOException e) {
                throw new FileAccessException("No se pudo crear el directorio de features", e);
            }
        }
        try {
            return Files.list(featuresRoot)
                    .filter(Files::isDirectory)
                    .map(path -> leerOCrearMetadata(path.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileAccessException("Error al listar proyectos", e);
        }
    }

    /**
     * Obtiene el metadato de un proyecto específico y adjunta su puntaje de calidad promedio.
     *
     * @param proyecto nombre del proyecto.
     * @return metadata enriquecida.
     */
    public ProjectMetadata obtenerProyecto(String proyecto) {
        Path projectPath = featuresRoot.resolve(proyecto);
        if (!Files.exists(projectPath)) {
            throw new ProjectNotFoundException("El proyecto " + proyecto + " no existe");
        }
        ProjectMetadata metadata = leerOCrearMetadata(proyecto);
        List<TestCaseSummary> casos = caseFileService.listarCasos(proyecto);
        List<QualityResult> calidades = casos.stream()
                .map(TestCaseSummary::getCalidad)
                .filter(java.util.Objects::nonNull)
                .toList();
        double promedio = calidades.isEmpty() ? 0.0 : calidades.stream().mapToDouble(QualityResult::getScore).average().orElse(0.0);
        metadata.setCasos(casos.stream().map(TestCaseSummary::getRuta).collect(Collectors.toList()));
        LOGGER.info("Proyecto {} cargado con {} casos y calidad promedio {}", proyecto, casos.size(), promedio);
        return metadata;
    }

    /**
     * Permite actualizar el contenido de un project.json existente.
     *
     * @param proyecto nombre del proyecto.
     * @param metadata nuevo contenido.
     * @return metadata guardada.
     */
    public ProjectMetadata actualizarProyecto(String proyecto, ProjectMetadata metadata) {
        validarMetadata(metadata);
        Path jsonPath = featuresRoot.resolve(proyecto).resolve("project.json");
        if (!Files.exists(jsonPath)) {
            throw new ProjectNotFoundException("No existe el project.json para " + proyecto);
        }
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), metadata);
            LOGGER.info("project.json actualizado para {}", proyecto);
            return metadata;
        } catch (IOException e) {
            throw new FileAccessException("No se pudo escribir project.json", e);
        }
    }

    private ProjectMetadata leerOCrearMetadata(String proyecto) {
        Path projectFolder = featuresRoot.resolve(proyecto);
        Path jsonPath = projectFolder.resolve("project.json");
        if (!Files.exists(projectFolder)) {
            throw new ProjectNotFoundException("La carpeta del proyecto no existe: " + proyecto);
        }
        if (!Files.exists(jsonPath)) {
            ProjectMetadata metadata = ProjectMetadata.defaultFor(proyecto);
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), metadata);
                LOGGER.warn("Se creó project.json por defecto para {}", proyecto);
                return metadata;
            } catch (IOException e) {
                throw new FileAccessException("No se pudo crear project.json para " + proyecto, e);
            }
        }
        try {
            return objectMapper.readValue(jsonPath.toFile(), ProjectMetadata.class);
        } catch (IOException e) {
            throw new ParsingException("project.json corrupto para " + proyecto, e);
        }
    }

    private void validarMetadata(ProjectMetadata metadata) {
        List<String> faltantes = new ArrayList<>();
        if (metadata.getId() == null || metadata.getId().isBlank()) faltantes.add("id");
        if (metadata.getNombre() == null || metadata.getNombre().isBlank()) faltantes.add("nombre");
        if (metadata.getCodigoJira() == null || metadata.getCodigoJira().isBlank()) faltantes.add("codigoJira");
        if (metadata.getTipo() == null || metadata.getTipo().isBlank()) faltantes.add("tipo");
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Campos obligatorios faltantes: " + String.join(", ", faltantes));
        }
    }
}
