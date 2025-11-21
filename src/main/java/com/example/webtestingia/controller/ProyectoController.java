package com.example.webtestingia.controller;

import com.example.webtestingia.model.ApiResponse;
import com.example.webtestingia.model.ProjectMetadata;
import com.example.webtestingia.service.ProjectDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para exponer operaciones sobre proyectos detectados en el sistema de archivos.
 */
@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProyectoController.class);
    private final ProjectDiscoveryService discoveryService;

    /**
     * Constructor con dependencias.
     *
     * @param discoveryService servicio de descubrimiento.
     */
    public ProyectoController(ProjectDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Lista todos los proyectos disponibles.
     *
     * @return lista de metadatos.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        LOGGER.info("Listando proyectos");
        return ApiResponse.ok(discoveryService.listarProyectos());
    }

    /**
     * Obtiene el detalle de un proyecto específico incluyendo calidad.
     *
     * @param proyecto nombre del proyecto.
     * @return metadata completa.
     */
    @GetMapping("/{proyecto}")
    public ResponseEntity<Map<String, Object>> obtener(@PathVariable String proyecto) {
        LOGGER.info("Cargando proyecto {}", proyecto);
        return ApiResponse.ok(discoveryService.obtenerProyecto(proyecto));
    }

    /**
     * Actualiza el contenido del project.json.
     *
     * @param proyecto nombre del proyecto.
     * @param metadata nuevo contenido.
     * @return metadata persistida.
     */
    @PutMapping("/{proyecto}")
    public ResponseEntity<Map<String, Object>> actualizar(@PathVariable String proyecto, @RequestBody ProjectMetadata metadata) {
        LOGGER.info("Actualizando project.json de {}", proyecto);
        return ApiResponse.ok(discoveryService.actualizarProyecto(proyecto, metadata));
    }
}
