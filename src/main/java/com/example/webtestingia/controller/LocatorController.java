package com.example.webtestingia.controller;

import com.example.webtestingia.model.ApiResponse;
import com.example.webtestingia.service.LocatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para exponer los locators YAML cargados por proyecto.
 */
@RestController
@RequestMapping("/api/proyectos/{proyecto}/locators")
public class LocatorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocatorController.class);
    private final LocatorService locatorService;

    /**
     * Constructor con dependencias.
     */
    public LocatorController(LocatorService locatorService) {
        this.locatorService = locatorService;
    }

    /**
     * Lista todos los locators de un proyecto.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(@PathVariable String proyecto) {
        LOGGER.info("Listando locators de {}", proyecto);
        return ApiResponse.ok(locatorService.obtenerLocators(proyecto));
    }

    /**
     * Devuelve un grupo específico de locators.
     */
    @GetMapping("/{grupo}")
    public ResponseEntity<Map<String, Object>> grupo(@PathVariable String proyecto, @PathVariable String grupo) {
        LOGGER.info("Obteniendo grupo {} de {}", grupo, proyecto);
        return ApiResponse.ok(locatorService.obtenerGrupo(proyecto, grupo));
    }
}
