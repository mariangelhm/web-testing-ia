package com.example.webtestingia.config;

import com.example.webtestingia.model.ApiResponse;
import com.example.webtestingia.model.exception.BrowserException;
import com.example.webtestingia.model.exception.FileAccessException;
import com.example.webtestingia.model.exception.InvalidConfigurationException;
import com.example.webtestingia.model.exception.ParsingException;
import com.example.webtestingia.model.exception.ProjectNotFoundException;
import com.example.webtestingia.model.exception.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para entregar respuestas claras y loguear errores.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Maneja errores de proyectos inexistentes.
     */
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ProjectNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    /**
     * Maneja problemas de lectura/escritura de archivos.
     */
    @ExceptionHandler(FileAccessException.class)
    public ResponseEntity<Map<String, Object>> handleFile(FileAccessException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }

    /**
     * Maneja errores de parsing de archivos de configuración o json.
     */
    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<Map<String, Object>> handleParsing(ParsingException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }

    /**
     * Maneja configuraciones inválidas.
     */
    @ExceptionHandler(InvalidConfigurationException.class)
    public ResponseEntity<Map<String, Object>> handleConfig(InvalidConfigurationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }

    /**
     * Maneja errores de sesión del grabador.
     */
    @ExceptionHandler(SessionException.class)
    public ResponseEntity<Map<String, Object>> handleSession(SessionException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    /**
     * Maneja errores del navegador.
     */
    @ExceptionHandler(BrowserException.class)
    public ResponseEntity<Map<String, Object>> handleBrowser(BrowserException ex) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex);
    }

    /**
     * Maneja validaciones de DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        LOGGER.error("Validación fallida", ex);
        List<Map<String, String>> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
                .collect(Collectors.toList());
        return ApiResponse.notification(HttpStatus.BAD_REQUEST, "Validación fallida", errores, "ValidationError");
    }

    /**
     * Manejo genérico para excepciones no contempladas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, Exception ex) {
        LOGGER.error("Error: {}", ex.getMessage(), ex);
        Map<String, Object> detail = new HashMap<>();
        detail.put("exception", ex.getClass().getSimpleName());
        return ApiResponse.notification(status, ex.getMessage(), detail, ex.getClass().getSimpleName());
    }
}
