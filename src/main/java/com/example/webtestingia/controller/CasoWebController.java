package com.example.webtestingia.controller;

import com.example.webtestingia.model.ApiResponse;
import com.example.webtestingia.model.TestCaseDetail;
import com.example.webtestingia.model.TestCaseSummary;
import com.example.webtestingia.service.CaseFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador para operaciones sobre casos de prueba web almacenados en archivos .feature.
 */
@RestController
@RequestMapping("/api/projects/{project}/web-cases")
public class CasoWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasoWebController.class);
    private final CaseFileService caseFileService;

    /**
     * Constructor con dependencias.
     *
     * @param caseFileService servicio de casos.
     */
    public CasoWebController(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
    }

    /**
     * Lista los casos de un proyecto.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(@PathVariable("project") String proyecto) {
        LOGGER.info("Listing cases for {}", proyecto);
        return ApiResponse.ok(caseFileService.listarCasos(proyecto));
    }

    /**
     * Devuelve el contenido y análisis de un caso.
     */
    @GetMapping("/{ruta:.+}")
    public ResponseEntity<Map<String, Object>> obtener(@PathVariable("project") String proyecto, @PathVariable String ruta) {
        LOGGER.info("Reading case {} in {}", ruta, proyecto);
        return ApiResponse.ok(caseFileService.leerCaso(proyecto, ruta));
    }

    /**
     * Crea un nuevo caso .feature.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@PathVariable("project") String proyecto, @RequestBody Map<String, String> payload) {
        String ruta = payload.get("ruta");
        String contenido = payload.get("contenido");
        LOGGER.info("Creating case {} in {}", ruta, proyecto);
        caseFileService.crearCaso(proyecto, ruta, contenido);
        return ApiResponse.ok(Map.of("message", "Case created"));
    }

    /**
     * Actualiza un caso existente.
     */
    @PutMapping("/{ruta:.+}")
    public ResponseEntity<Map<String, Object>> actualizar(@PathVariable("project") String proyecto, @PathVariable String ruta, @RequestBody Map<String, String> payload) {
        String contenido = payload.get("contenido");
        LOGGER.info("Updating case {} in {}", ruta, proyecto);
        caseFileService.actualizarCaso(proyecto, ruta, contenido);
        return ApiResponse.ok(Map.of("message", "Case updated"));
    }

    /**
     * Elimina un caso de prueba.
     */
    @DeleteMapping("/{ruta:.+}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable("project") String proyecto, @PathVariable String ruta) {
        LOGGER.warn("Deleting case {} in {}", ruta, proyecto);
        caseFileService.eliminarCaso(proyecto, ruta);
        return ApiResponse.ok(Map.of("message", "Case deleted"));
    }
}
