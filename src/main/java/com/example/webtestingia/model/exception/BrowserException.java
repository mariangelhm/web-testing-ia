package com.example.webtestingia.model.exception;

/**
 * Excepción que representa fallos al iniciar o interactuar con el navegador.
 */
public class BrowserException extends RuntimeException {

    /**
     * Constructor con detalle y causa.
     *
     * @param message mensaje descriptivo.
     * @param cause   causa raíz del error.
     */
    public BrowserException(String message, Throwable cause) {
        super(message, cause);
    }
}
