package com.example.webtestingia.model.exception;

/**
 * Excepción utilizada cuando el contenido de un archivo no puede ser parseado correctamente.
 */
public class ParsingException extends RuntimeException {

    /**
     * Constructor con causa original.
     *
     * @param message mensaje detallado.
     * @param cause   causa raíz.
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
