package com.example.webtestingia.model.exception;

/**
 * Excepción lanzada cuando un proyecto no puede ser localizado en el sistema de archivos.
 */
public class ProjectNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje descriptivo.
     *
     * @param message detalle del error.
     */
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
